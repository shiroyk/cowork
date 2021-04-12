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
    getPageSize(url) {
      this.$axios
        .get(url)
        .then((res) => {
          if (res.data.code == 200) {
            this.pageSize = res.data.data
          } else this.$message.error(res.data.msg)
        })
        .catch(() => { })
    },
    getDocData(url) {
      this.$axios
        .get(url)
        .then((res) => {
          if (res.data.code == 200) {
            this.docData = res.data.data
          } else this.$message.error(res.data.msg)
        })
        .catch(() => { })
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