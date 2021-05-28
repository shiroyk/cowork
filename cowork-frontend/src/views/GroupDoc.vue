<template>
  <el-main>
    <DocTable
      :docData="docData"
      :tableHeader="tableHeader"
      :searchDoc="searchDoc"
      :showElement="showElement"
      :loadPage="loadPage"
      :createDoc="createDoc"
      :uploadDoc="uploadDoc"
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
  props: {
    gid: {
      type: String,
    },
  },
  data() {
    return {}
  },
  methods: {
    groupDoc() {
      this.docData = []
      this.showElement.remove = true
      this.loadPage = () => {
        this.$store.commit('setCurrentGroup', this.gid)
        this.getDocData(
          `/group/${this.gid}/doc/count`,
          `/group/${this.gid}/doc?p=${this.currentPage}`
        )
        this.showPage = true
      }
      this.searchDoc = (title) => {
        this.searchDocs(`/group/${this.gid}/doc/search`, title)
      }
      this.createDoc = (title) => {
        this.createNewDoc(`/group/${this.gid}/doc`, title)
      }
      this.uploadDoc = () => {
        this.uploadDocs(`/group/${this.gid}/doc/upload`)
      }
      this.onRowClick = (data) => {
        this.$router.push({ path: `/doc/${data.id}` })
      }
      this.onRowStarClick = (id) => {
        this.updateDocStar(id)
      }
      this.onRowDeleteClick = (data) => {
        this.deleteDoc(`/group/${this.gid}/doc/`, data)
      }
      this.loadPage()
    },
  },
  created() {
    this.groupDoc()
  },
  watch: {
    gid(val, oldVal) {
      if (val && val != oldVal) this.loadPage()
    },
  },
}
</script>

<style>
</style>