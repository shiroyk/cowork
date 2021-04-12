<template>
  <div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="id" width="200"> </el-table-column>
      <el-table-column prop="name" label="群族名" width="100">
      </el-table-column>
      <el-table-column prop="describe" label="简介" width="150">
      </el-table-column>
      <el-table-column label="群主" width="250">
        <template slot-scope="scope">
          <p>id: {{ scope.row.leader.id }}</p>
          <p>用户名: {{ scope.row.leader.username }}</p>
        </template>
      </el-table-column>
      <el-table-column prop="user" label="用户数" width="80"> </el-table-column>
      <el-table-column prop="doc" label="文档数" width="80"> </el-table-column>
      <el-table-column prop="memberRole" label="权限">
        <template slot-scope="scope">
          <el-tag>{{ memberRole[scope.row.memberRole] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            type="primary"
            size="mini"
            @click="
              groupInfo = scope.row
              putGroupDialog = true
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
    <el-dialog title="更新群组信息" :visible.sync="putGroupDialog">
      <el-form :model="groupInfo" label-width="80px">
        <el-form-item label="群组名称">
          <el-input v-model="groupInfo.name"></el-input>
        </el-form-item>
        <el-form-item label="群组介绍">
          <el-input v-model="groupInfo.describe"></el-input>
        </el-form-item>
        <el-form-item label="群组组长">
          <el-input v-model="groupInfo.leader_id"></el-input>
        </el-form-item>
        <el-form-item label="成员权限">
          <el-select
            v-model="groupInfo.memberRole"
            placeholder="请选择成员权限"
          >
            <el-option
              v-for="(v, k) in memberRole"
              :key="k"
              :label="v"
              :value="k"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button style="float: left" type="danger" @click="deleteGroup()"
          >删 除</el-button
        >
        <el-button @click="putGroupDialog = false">取 消</el-button>
        <el-button
          type="primary"
          @click="
            updateGroupInfo()
            putGroupDialog = false
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
      memberRole: {
        ReadWrite: '读写',
        Create: '读写、创建',
        CreateDelete: '读写、创建、删除',
      },
      putGroupDialog: false,
      groupInfo: {},
    }
  },
  methods: {
    initData() {
      this.getTableData('/admin/group')
      this.loadPage = () => {
        this.getTableData(`/admin/group?p=${this.currentPage}`)
      }
      this.getPageSize('/admin/group/count')
    },
    updateGroupInfo() {
      this.$axios
        .put(
          `/admin/group/${this.groupInfo.id}`,
          new URLSearchParams({
            name: this.groupInfo.name,
            describe: this.groupInfo.describe,
            leader: this.groupInfo.leader_id,
            memberRole: this.groupInfo.memberRole,
          })
        )
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {})
    },
    deleteGroup() {
      this.deleteData(`/admin/group/${this.groupInfo.id}`)
    },
  },
  mounted() {
    this.initData()
  },
}
</script>

<style>
</style>