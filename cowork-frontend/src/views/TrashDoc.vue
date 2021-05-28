<template>
  <el-main>
    <DocTable
      :docData="docData"
      :tableHeader="tableHeader"
      :searchDoc="searchDoc"
      :showElement="showElement"
      :loadPage="loadPage"
      :createDoc="createDoc"
      :onRowClick="onRowClick"
      :onRowStarClick="onRowStarClick"
      :onRowDeleteClick="onRowDeleteClick"
      :onRowRecoveryClick="onRowRecoveryClick"
    />
    <el-pagination
      v-show="showPage"
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
    trashDoc() {
      this.showGroupUser = false
      this.docData = []
      this.tableHeader = ''
      Object.assign(this.showElement, {
        search: true,
        create: false,
        remove: true,
        star: false,
        share: false,
      })
      this.loadPage = () => {
        this.getDocData('/doc/trash/count', `/doc/trash?p=${this.currentPage}`)
        this.showPage = true
      }
      this.searchDoc = (title) => {
        this.searchDocs('/doc/trash/search', title)
      }
      this.createDoc = () => {}
      this.onRowClick = () => {}
      this.onRowStarClick = () => {}
      this.onRowDeleteClick = (data) => {
        this.deleteDoc('/doc/trash/', data)
      }
      this.loadPage()
    },
  },
  created() {
    this.trashDoc()
  },
}
</script>

<style>
</style>