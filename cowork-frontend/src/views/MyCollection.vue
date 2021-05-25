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
    collectDoc() {
      this.docData = []
      this.showGroupUser = false
      Object.assign(this.showElement, {
        search: false,
        create: false,
        remove: false,
        star: true,
        share: false,
      })
      this.tableHeader = ''
      this.showPage = false
      this.loadPage = () => {
        this.getDocData('/doc/star')
      }
      this.onRowStarClick = (id) => {
        this.updateDocStar(id)
        this.docData = this.docData.filter((doc) => doc.id != id)
      }
      this.searchDoc = () => {}
      this.createDoc = () => {}
      this.loadPage()
    },
  },
  created() {
    this.collectDoc()
  },
}
</script>

<style>
</style>