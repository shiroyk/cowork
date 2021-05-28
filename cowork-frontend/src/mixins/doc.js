import readDocFile from '../utils/docx'
const docMixin = {
  components: {
    DocTable: () => import("@/components/DocTable.vue"),
  },
  data() {
    return {
      docData: [],
      tableHeader: '',
      showElement: {
        search: true,
        create: true,
        star: true,
        remove: true,
        share: true,
        owner: false,
      },
      showPage: true,
      pageSize: 0,
      currentPage: 0,
      loadPage: () => { },
      searchDoc: () => { },
      createDoc: () => { },
      uploadDoc: () => { },
      onRowClick: () => { },
      onRowStarClick: () => { },
      onRowDeleteClick: () => { },
      onRowRecoveryClick: (docInfo) => {
        this.$axios
          .put(`/doc/trash/${docInfo.id}`)
          .then((res) => {
            if (res.data.code == 200) {
              this.$message.success(res.data.msg)
              this.loadPage()
            } else this.$message.error(res.data.msg)
          })
          .catch(() => {
            this.$message.error('操作失败，请稍后重试！')
          })
      },
    }
  },
  methods: {
    getDocData(docSizeUrl, docDataUrl) {
      this.$axios.all([
        this.$axios.get(docSizeUrl),
        this.$axios.get(docDataUrl)
      ]).then(res => {
        if (res[0].data.code == 200 && res[1].data.code == 200) {
          this.pageSize = res[0].data.data
          this.docData = res[1].data.data
        } else this.$message.error(res[0].data.msg)
      })
    },
    createNewDoc(url, title) {
      this.$axios
        .post(
          url,
          new URLSearchParams({
            title: title,
          })
        )
        .then((res) => {
          if (res.data.code === 200) {
            this.$message.success(res.data.msg)
            this.loadPage()
          } else this.$message.error(res.data.msg)
        })
        .catch(() => { })
    },
    deleteDoc(url, docInfo) {
      this.$axios
        .delete(`${url}/${docInfo.id}`)
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
            this.loadPage()
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.$message.error('操作失败，请稍后重试！')
        })
    },
    updateDocStar(id) {
      this.$axios
        .put(`/doc/${id}/star`)
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.$message.error('操作失败，请稍后重试！')
        })
    },
    searchDocs(url, title) {
      this.$axios
        .get(url + '?title=' + title)
        .then((res) => {
          if (res.data.code == 200) {
            this.docData = res.data.data
            this.showPage = false
          }
        })
        .catch(() => { })
    },
    uploadDocs(url) {
      readDocFile(
        url,
        () => {
          this.loading = this.$loading({
            lock: true,
            text: '解析并上传文件中...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.5)',
          })
        },
        (success) => {
          this.loading.close()
          this.$message.success(success)
          this.loadPage()
        },
        (err) => {
          this.loading.close()
          this.$message.error(err)
          this.loadPage()
        },
        () => {
          this.loading.close()
          this.$message.error('文档解析失败!')
        }
      )
    },
    goPage(page) {
      this.currentPage = page - 1
      this.loadPage()
    },
    prevPage() {
      if (this.currentPage > 1) this.currentPage--
      this.loadPage()
    },
    nextPage() {
      this.currentPage++
      this.loadPage()
    }
  }
}
export default docMixin