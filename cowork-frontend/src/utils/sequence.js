import Delta from "quill-delta"

const increaseVersion = v => {
  let nv = new Version(v.uid, v.version)
  nv.version++
  return nv
}

const compareVersion = (v1, v2) => {
  if (v1.version < v2.version) return 1
  else if (v1.version > v2.version) return -1
  else {
    if (v1.uid < v2.uid) {
      return -1
    } else if (v1.uid > v2.uid) {
      return 1
    } else {
      return 0
    }
  }
}

const continuous = (v1, v2) => {
  if (!v1 || !v2) return false
  let ver1 = v1.version.version, ver2 = v2.version.version
  return ver1 === v2.preVersion.version && ver1 + 1 === ver2
}

const compareCausal = (n1, n2) => {
  if (!n1 || !n2) return false
  let comp = compareVersion(n1.preVersion, n2.preVersion)
  return comp === 0 ? compareVersion(n1.version, n2.version) : -comp
}

const generateCRDT = (arr, from, to, key = "delete", attr) => {
  if (from > arr.length) return {}
  const CRDT = { version: arr[from] ? arr[from].version : null }
  CRDT[key] = to - from + 1
  // console.log('generateCRDT', arr[from], from, to, CRDT[key])
  if (attr) CRDT["attributes"] = attr
  return CRDT
}

class Version {
  constructor(uid, version = 0) {
    this.uid = uid
    this.version = version
  }
}

class Node {
  constructor(content, attributes, version, preVersion) {
    this.content = content
    this.attributes = attributes || null
    this.version = version
    this.preVersion = preVersion
  }

  static fromJson(json) {
    return Object.assign(new Node(), json)
  }
}

export default class Sequence {
  constructor(options = {}) {
    this.options = {
      ...this.defaultOptions,
      ...options
    }
    this.nodes = new Array()
    this.tombstone = new Map()
    this.version = new Version(this.options.uid, this.options.version.version)
  }

  get defaultOptions() {
    return { version: new Version('head'), uid: 'head' }
  }

  insertLength(s) {
    return typeof s === "string" || s instanceof String ? s.length : 1
  }

  getPreVersion(pos) {
    if (pos === 0) return new Version('head')
    let preNode = this.nodes[pos - 1]

    console.log("preNode", preNode)

    if (preNode) return preNode.version
    else if (this.nodes.length === 0 && this.tombstone.length === 0)
      return new Version('head')
    else {
      let ver = new Version('head')
      this.tombstone.forEach((v, k) => {
        if (v == pos) {
          ver = k
          return
        }
      })
      return ver
    }
  }

  findVersionPos(ver) {
    return this.nodes.findIndex(n => n.version.version === ver.version)
  }

  insertChar(pos, delta, preVersion, version = this.version) {
    let cursor = pos, preVer = preVersion, newVer = version
    let chars
    if (this.insertLength(delta.insert) > 1) chars = [...delta.insert]
    else chars = [delta.insert]
    chars.forEach(i => {
      newVer = increaseVersion(newVer)

      let newNode = new Node(i, delta.attributes, newVer, preVer)
      this.nodes.splice.call(this.nodes, cursor, 0, newNode)

      this.version.version++
      preVer = newVer
      cursor++
    })
    console.log(this.toDelta())
    return newVer
  }

  insert(pos, delta) {
    let preVersion = pos == 0 ? new Version('head') : this.getPreVersion(pos);

    console.log(preVersion)

    this.insertChar(pos, delta, preVersion)
    return { version: preVersion, insert: delta.insert, attributes: delta.attributes }
  }

  remoteInsert(last, uid, crdt, deltas) {
    let pos = this.findVersionPos(crdt.version)

    if (pos < 0 && crdt.version > 0) {
      //这里需要将找到不到的Version放入到集合，下次再进行重复操作？
      return
    }

    if (crdt.version == 0) pos = 0
    else pos++

    this.insertChar(pos, crdt, crdt.version, new Version(uid, this.version.version))

    const ops = deltas.ops, lastOp = ops[ops.length - 1]

    if ((lastOp && lastOp.insert) ||
      (lastOp && lastOp.delete) ||
      (lastOp && lastOp.retain && lastOp.attributes))
      deltas.retain(last)
    else deltas.retain(pos)

    deltas.insert(crdt.insert, crdt.attributes)
  }

  uniFormat(delta, version) {
    let pos = this.nodes.length
    let preVersion = this.getPreVersion(pos)
    const uniNode = this.nodes[pos - 1]
    if (uniNode.content === "\n") {
      if (uniNode.attributes) {
        Object.assign(uniNode.attributes, delta.attributes)
      } else {
        uniNode.attributes = delta.attributes
      }
    } else {
      this.insertChar(pos, { insert: "\n", attributes: delta.attributes }, preVersion, version
      )
    }
    return [{ version: preVersion, insert: "\n", attributes: delta.attributes }]
  }

  format(pos, delta, version = this.version) {
    let from = pos, to = pos + delta.retain || delta.format
    if (to === this.nodes.length + 1) return this.uniFormat(delta, version)

    let crdts = [], tmp = 0, cursor = 0

    console.log(from, to, this.nodes.length)
    let formatNode = this.nodes.slice(from, to)
    console.log("formatNode\n", JSON.stringify(formatNode, null, 2))

    do {
      if (cursor + 1 < formatNode.length) {
        const node = formatNode[cursor]
        const next = formatNode[cursor + 1]
        if (node.attributes) {
          Object.assign(node.attributes, delta.attributes)
        } else {
          node.attributes = delta.attributes
        }
        if (!continuous(node, next)) {
          crdts.push(generateCRDT(formatNode, tmp, cursor, "format", delta.attributes))
          tmp = cursor + 1
        }
      } else
        crdts.push(generateCRDT(formatNode, tmp, cursor, "format", delta.attributes))
      cursor++
    } while (cursor < formatNode.length)
    return crdts
  }

  remoteFormat(crdt, uid, deltas) {
    let pos = this.findVersionPos(crdt.version)

    console.log(pos, crdt)

    if (pos < 0) {
      return
    }

    this.format(pos, crdt, new Version(uid, this.version.version))
    const ops = deltas.ops,
      lastOp = ops[ops.length - 1]
    if (lastOp && lastOp.retain) {
      if (lastOp.attributes) deltas.retain(crdt.format, crdt.attributes)
    } else {
      deltas.retain(pos).retain(crdt.format, crdt.attributes)
    }
  }

  delete(pos, delta) {
    let from = pos, to = pos + delta.delete
    let crdts = [], tmp = 0, cursor = 0
    let tombstone = this.nodes.splice(from, to - from)

    do {
      let next = cursor + 1
      if (next < tombstone.length) {
        const node = tombstone[cursor]
        if (!this.tombstone.has(node.version.version)) {
          this.tombstone.set(node.version.version, cursor)
          if (!continuous(node, tombstone[next])) {
            crdts.push(generateCRDT(tombstone, tmp, cursor))
            tmp = next
          }
        }
      } else crdts.push(generateCRDT(tombstone, tmp, cursor))
      cursor++
    } while (cursor < tombstone.length)
    return crdts
  }

  remoteDelete(last, crdt, deltas) {
    let pos = this.findVersionPos(crdt.version)

    console.log("remoteDelete\n", pos, crdt)

    if (pos < 0) {
      return
    }

    this.delete(pos, crdt)

    let ops = deltas.ops, lastOp = ops[ops.length - 1]

    if (!lastOp) deltas.retain(pos)
    deltas.delete(crdt.delete)

  }

  emptyAttributes(attr) {
    if (attr === null) return true
    return Object.values(attr).every(v => v === null)
  }

  toDelta() {
    let deltas = new Delta(), preAttr = null, seq = ""
    this.nodes.forEach(node => {
      if (typeof node.content === "string") {
        if (preAttr === node.attributes ||
          (this.emptyAttributes(preAttr) && this.emptyAttributes(node.attributes)))
          seq += node.content
        else {
          if (seq.length > 0) deltas.insert(seq, preAttr)
          seq = node.content
        }
      } else {
        deltas.insert(seq, preAttr)
        seq = ""
        deltas.insert(node.content, node.attributes)
      }
      preAttr = node.attributes
    })
    deltas.insert(seq, preAttr)
    return deltas
  }

  applyRemoteCrdts(body) {
    let deltas = new Delta()
    let last = 0
    body.crdts.forEach(crdt => {
      if (crdt.insert) {
        this.remoteInsert(last, body.uid, crdt, deltas)
        last = this.insertLength(crdt.insert)
      } else if (crdt.format) {
        this.remoteFormat(crdt, body.uid, deltas)
      } else if (crdt.delete) {
        this.remoteDelete(last, crdt, deltas)
        last = crdt.delete
      }
    })

    console.log("CRDT to Delta\n", JSON.stringify(deltas, null, 2))
    console.log(this.nodes, this.version.version)

    return deltas
  }

  applyDelta(deltas) {
    let pos = 0, crdts = []
    deltas.forEach(delta => {
      let retain = delta.retain && !delta.attributes
      if (retain) {
        pos += delta.retain
      } else if (delta.insert) {
        console.log(pos, delta)
        crdts.push(this.insert(pos, delta))
        pos += this.insertLength(delta.insert)
      } else if (delta.retain) {
        crdts.push(...this.format(pos, delta))
        pos += delta.retain
      } else if (delta.delete) {
        crdts.push(...this.delete(pos, delta))
      }
    })

    console.log("Original delta\n", JSON.stringify(deltas, null, 2))
    console.log(this.nodes, this.version.version)

    return { crdts: crdts }
  }

  fromJson(json) {
    if (json.length == 0) return []
    /**
     * 将乱序的字符按照因果顺序进行排序
     */

    let causal = [], tmp = 0, cursor = 0

    // 先按照version从小到大排序
    json.sort((a, b) => compareVersion(a, b))

    // 复制version连续的节点
    do {
      let next = cursor + 1
      if (next < json.length) {
        const node = json[cursor]
        const nextNode = json[next]
        if (!continuous(node, nextNode)) {
          causal.push(json.slice(tmp, next))
          tmp = next
        }
      } else causal.push(json.slice(tmp, next))
      cursor++
    } while (cursor < json.length)

    // 按照preVersion从小到大排序，如果preVersion相同，version大的在前
    causal.sort((a, b) => compareCausal(a[0], b[0]))

    let range = [], duplicate = []

    // 插入节点preVersion在version连续区间内的节点
    causal.reduce((result, cur, i, arr) => {
      const curVR = cur.map(n => n.version.version) // 现在节点的所有version
      const head = cur[0] // 即将插入节点的头部
      const headVersion = head.preVersion.version // 即将插入节点的头部preVersion
      const rangePos = range.findIndex(n => (headVersion >= n[0] &&
        headVersion <= n[n.length - 1]) || n.includes(headVersion))

      if (rangePos >= 0) {
        const vr = range[rangePos] // version区间
        const fixNodes = arr[rangePos] // 被插入的节点数组

        console.log(`${rangePos} [${curVR}] \n ${arrPreVersion(cur)} \n => \n ${arrPreVersion(fixNodes)}`)

        let pos = fixNodes.length

        /**
         *                寻找插入位置
         * 插入节点的preVersion    被插入的节点的version
         *        [3,8]       =>   [1,2,3,5,6,7]
         * 插入节点的version       被插入的节点的preVersion
         *        [8,9]      =>    [0,1,3,3,5,6]
         *                  插入后
         *  preVersion  [0,1,2,3,8,3,5,6]
         *   version   [1,2,3,8,9,5,6,7]
         **/

        for (let index = 0; index < fixNodes.length; index++) {
          const node = fixNodes[index]
          const next = fixNodes[index + 1]

          if (!next) break // 插入位置在fixNode尾部

          /**
           * 节点的version与即将插入节点head的preVersion相同
           * 且下个节点的version小于head的version, 则可以插入
           **/
          if (compareVersion(node.version, head.preVersion) === 0 &&
            compareVersion(next.version, head.version) > 0) {
            pos = index
            break
          } else if (compareVersion(next.preVersion, head.preVersion) === 0) {
            /**
             * 如果插入位置的下一节点的preVersion与即将插入节点head的preVersion相同
             * 查找下一个不连续点, 也就是最小的version连续区间尾部
             **/
            for (let p = index + 1; p < fixNodes.length; p++) {
              const a = fixNodes[p]
              const b = fixNodes[p + 1]
              // 插入点在连续区间后
              if (b && !continuous(a, b) &&
                compareVersion(b.preVersion, head.preVersion) !== 0) {
                pos = p
                break
              }
            }
            break
          }
        }

        fixNodes.splice.call(fixNodes, pos + 1, 0, ...cur)

        console.log(`${pos} ${arrPreVersion(fixNodes)}`)

        // 更新version数组, 以便后续的使用
        if (vr.length > 1) vr.splice.call(vr, 1, 0, ...curVR)
        else vr.push(...curVR)

        duplicate.push(i)
      }

      range.push(curVR)
      return result
    }, causal)

    console.log(causal, range, duplicate)
    // 删除已经插入的节点
    causal = causal.filter((v, i) => duplicate.indexOf(i) === -1)

    this.nodes = this.nodes.concat(...causal)
    console.log(this.nodes)
    return this.toDelta()
  }
}

const arrPreVersion = arr => {
  return arr.map(n => `[${n.preVersion.version} ${n.content} ${n.version.version}]\n`).join(" ")
}
