<template>
  <div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="id" width="200"> </el-table-column>
      <el-table-column prop="title" label="文档名" width="130">
      </el-table-column>
      <el-table-column label="文档链接" width="200">
        <template slot-scope="scope">
          <p>{{ scope.row.url ? scope.row.url.url : '无' }}</p>
          <el-tag v-if="scope.row.url">{{
            urlPermission[scope.row.url.permission]
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所有者" width="220">
        <template slot-scope="scope">
          <p>{{ scope.row.owner ? scope.row.owner.id : '无' }}</p>
          <el-tag v-if="scope.row.owner">
            {{ ownerName[scope.row.owner.owner] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态">
        <template slot-scope="scope">
          <el-tag>
            {{ scope.row.delete ? '回收站' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间"> </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            type="primary"
            size="mini"
            @click="
              docInfo = scope.row
              putDocDialog = true
            "
            >编辑</el-button
          >
        </template>
      </el-table-column>
    </el-table>
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
    <el-dialog title="更新文档信息" :visible.sync="putDocDialog">
      <el-form label-width="100px" :model="docInfo">
        <el-form-item label="文档名">
          <el-input v-model="docInfo.title" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="文档所有者">
          <el-input v-model="docOwner" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="文档链接">
          <el-input v-model="docUrl" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="回收站">
          <el-checkbox v-model="docInfo.delete"></el-checkbox>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button style="float: left" type="danger" @click="deleteDoc()"
          >删 除</el-button
        >
        <el-button @click="putDocDialog = false">取 消</el-button>
        <el-button
          type="primary"
          @click="
            updateDocInfo()
            putDocDialog = false
          "
          >更 新</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import TableData from '../mixins/data'
export default {
  mixins: [TableData],
  data() {
    return {
      ownerName: {
        User: '用户',
        Group: '群组',
      },
      urlPermission: {
        Empty: '仅自己',
        ReadOnly: '只读',
        ReadWrite: '读写',
      },
      putDocDialog: false,
      docInfo: {},
      docOwner: '',
      docUrl: '',
    }
  },
  methods: {
    initData() {
      this.getTableData('/admin/doc')
      this.loadPage = () => {
        this.getTableData(`/admin/doc?p=${this.currentPage}`)
      }
      this.getPageSize('/admin/doc/count')
    },
    updateUserInfo() {
      this.$axios
        .put(
          `/admin/doc/${this.docInfo.id}`,
          new URLSearchParams({
            title: this.docInfo.title,
            owner: this.docOwner,
            url: this.docUrl,
            delete: this.docInfo.delete,
          })
        )
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.$message.error('更新失败，请稍后重试！')
        })
      this.docOwner = ''
      this.docUrl = ''
    },
    deleteDoc() {
      this.deleteData(`/admin/doc/${this.docInfo.id}`)
    },
  },
  mounted() {
    this.initData()
  },
}
</script>

<style>
</style>