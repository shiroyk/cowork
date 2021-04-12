<template>
  <el-popover
    v-if="docInfo"
    placement="bottom"
    trigger="manual"
    width="350"
    v-model="sharePop"
  >
    <div style="text-align: center; margin: 0">
      <span
        >文档分享 - <b>{{ docInfo.title || '' }}</b></span
      >
      <el-input v-model="docUrl" disabled placeholder="在线文档地址">
        <el-select
          v-if="ownerIsUser"
          slot="prepend"
          placeholder="请选择"
          v-model="permission"
        >
          <el-option
            v-for="(v, k) in urlPermission"
            :key="k"
            :label="v"
            :value="k"
          >
          </el-option>
        </el-select>
        <el-button
          slot="append"
          icon="el-icon-copy-document"
          @click="copyDocUrl"
        ></el-button>
      </el-input>
      <p>
        {{
          ownerIsUser
            ? permissionMsg[permission]
            : '群组内的其他用户可以读写文档'
        }}
      </p>
    </div>
    <div style="text-align: right; margin-top: 10px">
      <el-button size="small" type="text" @click="sharePop = false"
        >取消</el-button
      >
      <el-button type="primary" size="small" @click="updateDocUrl">{{
        docInfo.url ? '更新' : '生成'
      }}</el-button>
    </div>
    <el-button
      slot="reference"
      icon="el-icon-share"
      :size="btnSize"
      circle
      @click.stop="sharePop = !sharePop"
    ></el-button>
  </el-popover>
</template>

<script>
export default {
  props: {
    docInfo: {
      type: Object,
      default: () => ({
        url: {
          permission: 'ReadWrite',
          url: '',
          owner: {
            owner: 'user',
          },
        },
      }),
    },
    btnSize: {
      type: String,
      default: 'small',
    },
  },
  data() {
    return {
      sharePop: false,
      ownerIsUser: this.docInfo.owner.owner === 'User',
      permission: this.docInfo.url ? this.docInfo.url.permission : 'ReadWrite',
      newDocUrl: null,
      urlPermission: {
        Empty: '仅自己',
        ReadOnly: '只读',
        ReadWrite: '读写',
      },
      permissionMsg: {
        Empty: '文档仅自己具有读写权限',
        ReadOnly: '其他用户只能读取文档',
        ReadWrite: '其他用户可以读写文档',
      },
    }
  },
  methods: {
    updateDocUrl() {
      this.$axios
        .put(
          `/doc/${this.docInfo.id}/url`,
          new URLSearchParams({
            permission: this.permission,
          })
        )
        .then((res) => {
          if (res.data.code == 200) {
            this.newDocUrl = res.data.data.url.url
            this.$message.success('更新文档地址成功！')
          }
        })
        .catch(() => {})
    },
    copyDocUrl() {
      navigator.clipboard
        .writeText(this.docUrl)
        .then(() => this.$message.success('复制到剪切板成功！'))
        .catch((err) => console.log(err))
    },
  },
  computed: {
    docUrl() {
      return this.newDocUrl
        ? window.location.origin + '/doc/' + this.newDocUrl
        : this.docInfo.url
        ? window.location.origin + '/doc/' + this.docInfo.url.url
        : ''
    },
  },
  created() {},
}
</script>

<style>
</style>