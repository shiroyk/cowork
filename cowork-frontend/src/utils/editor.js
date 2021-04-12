import Quill from "quill"
import Delta from "quill-delta"
import QuillCursors from "quill-cursors"
import SockJS from "sockjs-client"
import Stomp from "stompjs"
import axios from "@/api"
import Sequence from "./sequence"

const Font = Quill.import("formats/font")
Font.whitelist = [
  "SimSun",
  "SimHei",
  "Microsoft-YaHei",
  "KaiTi",
  "FangSong",
  "Arial",
  "Times-New-Roman",
  "sans-serif"
]
Quill.register(Font, true)

const Size = Quill.import("attributors/style/size")
Size.whitelist = ["10px", "12px", "14px", "16px", "18px", "20px", "22px"]
Quill.register(Size, true)

Quill.register("modules/cursors", QuillCursors)

export default class CollabEditor {
  constructor(options = {}) {
    this.options = {
      ...this.defaultOptions,
      ...options
    }
    this.editor = new Quill(this.options.container, {
      debug: "error",
      modules: {
        toolbar: {
          container: this.options.toolbar,
          handlers: {
            image: this.uploadImage
          }
        },
        cursors: true
      },
      theme: "snow",
      readOnly: this.options.readOnly
    })

    this.cursors = this.editor.getModule("cursors")

    this.sequence = new Sequence({
      version: this.options.docData.version,
      uid: this.options.userInfo.id
    })
    this.editor.setContents(this.sequence.fromJson(this.options.docData.content), "silent")
    this.editor.history.clear()

    this.initStomp()
    this.editor.on("text-change", (delta, oldDelta, source) => {
      if (source === "user") {
        this.sendDelta(this.sequence.applyDelta(delta))
      }
    })
    this.editor.on("selection-change", range => {
      if (range) this.sendCursor(range)
    })
  }

  get defaultOptions() {
    return {
      container: "",
      toolbar: "",
      url: axios.defaults.baseURL + 'collab', // http://127.0.0.1:8085/collab
      token: "",
      docId: "602925bf64fadf4bad2e1a39",
      docData: {},
      readOnly: false,
      userInfo: {},
      onInitSuccess: () => { },
      onInitError: () => { },
      onUserChange: () => { },
      onReceive: crdts => {
        this.editor.updateContents(
          this.sequence.applyRemoteCrdts(crdts),
          "api"
        )
      }
    }
  }

  initStomp() {
    if (this.stompClient) return
    const socket = new SockJS(this.options.url)
    this.stompClient = Stomp.over(socket)
    this.stompClient.connect(
      {
        Authorization: this.options.token
      },
      () => {
        this.stompClient.subscribe("/topic/doc/" + this.options.docId, msg => {
          const body = JSON.parse(msg.body)
          if (Object.keys(body).includes("users")) {
            this.options.onUserChange(body.users, this.cursors)
            if (body.action === "logout") this.removeCursor(body.uid)
          } else if (Object.keys(body).includes("crdts")) {
            if (body.uid !== this.options.userInfo.id)
              this.options.onReceive(body)
          } else if (Object.keys(body).includes("index")) {
            this.updateCursor(body)
          }
        })
        this.options.onInitSuccess()
      },
      () => this.options.onInitError()
    )
  }

  createCursor(users) {
    users.forEach(user => {
      if (user.id !== this.options.userInfo.id)
        this.cursors.createCursor(
          user.id,
          user.nickname,
          "#" +
          Math.floor(Math.random() * 2 ** 24)
            .toString(16)
            .padStart(6, 0)
        )
    })
  }

  removeCursor(uid) {
    if (uid !== this.options.userInfo.id) {
      this.cursors.removeCursor(uid)
    }
  }

  sendCursor(range) {
    if (this.stompClient) {
      this.stompClient.send(
        `/app/doc/${this.options.docId}/cursor`,
        {},
        JSON.stringify(range)
      )
    }
  }

  updateCursor(cursor) {
    if (cursor.uid !== this.options.userInfo.id)
      this.cursors.moveCursor(cursor.uid, {
        index: cursor.index,
        length: cursor.length
      })
  }

  getEditor() {
    return this.editor
  }

  sendDelta(delta) {
    if (this.stompClient) {
      // console.log(JSON.stringify(delta, null, 2))
      this.stompClient.send(
        `/app/doc/${this.options.docId}`,
        {},
        JSON.stringify(delta)
      )
    }
  }

  disConnect() {
    if (this.stompClient) this.stompClient.disconnect()
  }

  uploadImage() {
    let fileInput = this.container.querySelector("input.ql-image[type=file]")
    if (fileInput == null) {
      fileInput = document.createElement("input")
      fileInput.setAttribute("type", "file")
      fileInput.setAttribute(
        "accept",
        "image/png, image/gif, image/jpeg, image/bmp, image/x-icon"
      )
      fileInput.classList.add("ql-image")
      fileInput.addEventListener("change", () => {
        const files = fileInput.files

        if (files == null && files[0] == null) {
          return
        }
        const formData = new FormData()
        formData.append("file", files[0])

        this.quill.enable(false)

        axios
          .post("/doc/image", formData)
          .then(res => {
            if (res.data.code == 200) {
              this.quill.enable(true)
              let range = this.quill.getSelection(true)
              this.quill.updateContents(
                new Delta()
                  .retain(range.index)
                  .delete(range.length)
                  .insert({
                    image: `${axios.defaults.baseURL}${res.data.msg}`
                  }),
                Quill.sources.USER
              )
              this.quill.setSelection(range.index + 1, Quill.sources.SILENT)
              fileInput.value = ""
            }
          })
          .catch(() => {
            this.quill.enable(true)
          })
      })
      this.container.appendChild(fileInput)
    }
    fileInput.click()
  }
}
