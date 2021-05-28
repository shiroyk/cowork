<template>
  <div class="editor-container">
    <div class="editor-header">
      <div class="editor-header-left">
        <el-link :underline="false" @click="$router.go(-1)">返回</el-link>
        <el-divider direction="vertical"></el-divider>
        <el-tooltip :content="editorTitle" placement="bottom">
          <span class="doc-title">{{ editorTitle }}</span>
        </el-tooltip>
      </div>
      <div class="editor-header-right" v-if="!loading && hasPermission">
        <el-popover
          style="text-align: center"
          v-for="(user, i) in coUsers"
          :key="i"
          placement="bottom"
          width="200"
          trigger="click"
        >
          <div style="text-align: center; margin: 0">
            {{ user.username }}<br />
            {{ user.nickname }}<br />
            {{ user.email }}<br />
          </div>
          <el-button type="text" slot="reference">
            <el-avatar
              style="vertical-align: middle"
              size="small"
              :src="user.avatar"
            >
              <i class="el-icon-user"></i>
            </el-avatar>
          </el-button>
        </el-popover>
        <span v-show="putDoc">
          <el-divider direction="vertical"></el-divider>
          <DocUrlPop :docInfo="docInfo" />
          <el-divider direction="vertical"></el-divider>
          <el-popover
            v-show="!loading && hasPermission"
            placement="top"
            width="160"
            v-model="newNamePop"
          >
            <el-input v-model="newDocName" placeholder="新文档名称"></el-input>
            <div style="text-align: right; margin-top: 10px">
              <el-button size="small" type="text" @click="newNamePop = false"
                >取消</el-button
              >
              <el-button
                type="primary"
                size="small"
                @click="
                  updateDocTitle()
                  newNamePop = false
                "
                >更新</el-button
              >
            </div>
            <el-button
              slot="reference"
              icon="el-icon-edit"
              size="small"
              circle
            ></el-button>
          </el-popover>
        </span>
        <el-divider direction="vertical"></el-divider>
        <el-button
          size="small"
          circle
          icon="el-icon-download"
          @click="saveDoc()"
        >
        </el-button>
      </div>
    </div>
    <el-alert
      v-show="!loading && !hasPermission"
      style="width: 50%; margin: 10px auto"
      :title="noPermissionTitle"
      type="error"
      show-icon
      :closable="false"
      center
    >
    </el-alert>
    <div v-if="hasPermission">
      <div :class="{ 'editor-toolbar-fixed': fixedTool }">
        <div class="editor-toolbar" ref="toolbar">
          <select class="ql-size">
            <option value="10px">10</option>
            <option value="12px">12</option>
            <option value="14px" selected>14</option>
            <option value="16px">16</option>
            <option value="18px">18</option>
            <option value="20px">20</option>
            <option value="22px">22</option>
          </select>
          <select class="ql-font">
            <option value="SimSun"></option>
            <option value="SimHei"></option>
            <option value="KaiTi"></option>
            <option value="Microsoft-YaHei" selected></option>
            <option value="FangSong"></option>
            <option value="Arial"></option>
            <option value="Times-New-Roman"></option>
            <option value="sans-serif"></option>
          </select>
          <button class="ql-bold"></button>
          <button class="ql-italic"></button>
          <button class="ql-underline"></button>
          <button class="ql-strike"></button>
          <select class="ql-color"></select>
          <select class="ql-background"></select>

          <button class="ql-blockquote"></button>
          <button class="ql-code-block"></button>
          <button class="ql-script" value="sub"></button>
          <button class="ql-script" value="super"></button>
          <button class="ql-list" value="ordered"></button>
          <button class="ql-list" value="bullet"></button>
          <button class="ql-indent" value="-1"></button>
          <button class="ql-indent" value="+1"></button>
          <button class="ql-align" value="justify"></button>
          <button class="ql-align" value="center"></button>
          <button class="ql-align" value="right"></button>
          <button class="ql-link"></button>
          <button class="ql-image"></button>
          <select class="ql-header">
            <option selected></option>
            <option :value="i" v-for="i in 6" :key="i"></option>
          </select>
          <button class="ql-clean"></button>
        </div>
      </div>
      <div class="editor-body">
        <div style="border: none" ref="container"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import Editor from '../utils/editor'
import DocUrlPop from '../components/DocUrlPop'

export default {
  props: {
    docId: {
      type: String,
    },
  },
  components: {
    DocUrlPop,
  },
  data: function () {
    return {
      loading: true,
      newNamePop: false,
      newDocName: '',
      editor: null,
      docInfo: null,
      coUsers: [],
      fixedTool: false,
      editorTitle: '',
      hasPermission: false,
      putDoc: false,
      noPermissionTitle: '没有访问该文档的权限',
    }
  },
  methods: {
    getDocInfo() {
      const loading = this.$loading({
        lock: true,
        text: '加载中',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)',
      })
      if (this.docId) {
        this.$axios
          .get(`/doc/${this.docId}`)
          .then((res) => {
            switch (res.data.code) {
              case 200:
                this.docInfo = res.data.data
                console.log(this.docInfo)
                if (!this.docInfo.delete) {
                  this.hasPermission = true
                  this.putDoc = this.docInfo.putDoc
                  this.editorTitle = this.docInfo.title || '在线文档编辑'
                  this.$nextTick(() => {
                    this.getDocContent()
                  })
                } else {
                  this.noPermissionTitle = '文档在回收站中，无法进行编辑！'
                }
                break
              case 401:
                this.$message.error('请先登录！')
                this.$router.push('/login')
                break
              case 400:
                this.noPermissionTitle = res.data.msg
                break
              default:
                this.noPermissionTitle = '加载文档失败，请稍后重试！'
            }
            this.loading = false
          })
          .catch(() => {
            this.noPermissionTitle = '加载文档失败，请稍后重试！'
            this.loading = false
          })
      }
      loading.close()
    },
    getDocContent() {
      this.$axios
        .get(`/doc/${this.docId}/content`)
        .then((res) => {
          if (res.data.code == 200) {
            if (res.data.data.readOnly)
              this.editorTitle = `${this.docInfo.title} (只读)`
            this.initEditor(res.data.data)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    initEditor(docData) {
      const userInfo = this.parseJwt(this.accessToken)
      this.editor = new Editor({
        container: this.$refs.container,
        toolbar: this.$refs.toolbar,
        token: this.accessToken,
        docId: this.docInfo.id,
        docData: docData,
        userInfo: userInfo,
        onInitError: () => {
          this.hasPermission = false
          this.noPermissionTitle = '连接协作服务失败，请稍后重试！'
        },
        onUserChange: (users) => {
          this.getUserInfo(users)
        },
      })
      window.addEventListener('scroll', this.fixedToolBar)
    },
    fixedToolBar() {
      let scrollTop =
        document.documentElement.scrollTop || document.body.scrollTop
      if (scrollTop > 50) {
        this.fixedTool = true
      } else {
        this.fixedTool = false
      }
    },
    updateDocTitle() {
      this.$axios
        .put(
          `/doc/${this.docInfo.id}`,
          new URLSearchParams({
            title: this.newDocName,
          })
        )
        .then((res) => {
          if (res.data.code == 200) {
            this.editorTitle = this.newDocName
            this.newDocName = ''
            this.$message.success(res.data.msg)
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {})
    },
    getUserInfo(users) {
      this.$axios
        .post('/user/list', users)
        .then((res) => {
          if (res.data.code == 200) {
            this.coUsers = res.data.data
            this.editor.createCursor(this.coUsers)
          }
        })
        .catch(() => {})
    },
    parseJwt(token) {
      let base64Url = token.split('.')[1]
      let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      let jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
          })
          .join('')
      )
      return JSON.parse(jsonPayload)
    },
    saveDoc() {
      const loading = this.$loading({
        lock: true,
        text: '文档生成中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)',
      })
      this.editor.saveDoc(
        this.docInfo.title,
        () => {
          loading.close()
        },
        () => {
          loading.close()
          this.$message.error('文档转换失败，请稍后重试！')
        }
      )
    },
  },
  created: function () {},
  mounted() {
    this.getDocInfo()
  },
  computed: {
    ...mapState({
      accessToken: (state) => state.accessToken,
    }),
  },
  beforeDestroy() {
    if (this.editor) this.editor.disConnect()
  },
}
</script>

<style scoped>
@import '../assets/styles/quill.snow.css';
@import '../assets/styles/quill-font.css';
@import '../assets/styles/quill-font-size.css';
.editor-header {
  height: 50px;
  line-height: 50px;
  padding: 0 10%;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 5px;
}
.editor-header-left {
  display: flex;
  align-items: center;
  width: 50%;
  float: left;
  height: 100%;
}
.editor-header-right {
  width: 50%;
  text-align: right;
  overflow: hidden;
  height: 100%;
}
.editor-toolbar {
  display: flex;
  justify-content: center;
  margin: 0 5%;
  border: none !important;
}
.editor-toolbar-fixed {
  background-color: white;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.2);
}
.editor-body {
  min-height: 297mm;
  width: 210mm;
  margin: 10px auto;
  flex: 1;
  overflow-x: hidden;
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.2);
}
.doc-title {
  margin: 0;
  width: 80%;
  font-size: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
