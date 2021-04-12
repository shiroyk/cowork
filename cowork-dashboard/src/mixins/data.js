const TableData = {
  data() {
    return {
      tableData: [],
      showPage: true,
      pageSize: 0,
      currentPage: 0,
      loadPage: () => { },
    }
  },
  methods: {
    getTableData(url) {
      this.$axios
        .get(url)
        .then((res) => {
          if (res.data.code == 200) {
            this.tableData = res.data.data
          } else this.$message.error(res.data.msg)
        })
        .catch(() => { })
    },
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
    deleteData(url) {
      this.$axios
        .delete(url)
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
          } else this.$message.error(res.data.msg)
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
export default TableData