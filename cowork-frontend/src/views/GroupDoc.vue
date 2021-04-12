<template>
  <el-main>
    <DocTable
      :docData="docData"
      :tableHeader="tableHeader"
      :searchDoc="searchDoc"
      :showElement="showElement"
      :clearSearch="loadPage"
      :createDoc="createDoc"
      :onRowClick="onRowClick"
      :onRowStarClick="onRowStarClick"
      :onRowDeleteClick="onRowDeleteClick"
      :onRowRecoveryClick="onRowRecoveryClick"
    />
    <el-pagination
      style="text-align: center"
      background
      :page-size="10"
      layout="prev, pager, next"
      :total="pageSize"
      @current-change="goPage"
      @prev-click="prevPage()"
      @next-click="nextPage()"
    >
    </el-pagination>
  </el-main>
</template>

<script>
import docMixin from '../mixins/doc'

export default {
  mixins: [docMixin],
  data() {
    return {}
  },
  methods: {
    groupDoc() {
      this.docData = []
      this.showElement.remove = false
      this.getPageSize('/group/doc/count')
      this.loadPage = () => {
        this.getDocData(`/group/doc?p=${this.currentPage}`)
        this.showPage = true
      }
      this.searchDoc = (title) => {
        this.searchDocs('/group/doc/search', title)
      }
      this.createDoc = (title) => {
        this.createNewDoc('/group/doc', title)
      }
      this.onRowClick = (data) => {
        this.$router.push({ path: `/edit/${data.id}` })
      }
      this.onRowStarClick = (id) => {
        this.updateDocStar(id)
      }
      this.onRowDeleteClick = () => {}
      this.loadPage()
    },
  },
  created() {
    this.groupDoc()
  },
}
</script>

<style>
</style>