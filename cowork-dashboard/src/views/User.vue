<template>
  <div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="id" width="200"> </el-table-column>
      <el-table-column label="头像" width="70">
        <template slot-scope="scope">
          <el-avatar :src="scope.row.avatar || ''">
            {{ scope.row.username || '' }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="100">
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" width="100">
      </el-table-column>
      <el-table-column prop="email" label="邮箱地址" width="150">
      </el-table-column>
      <el-table-column prop="group" label="群组"> </el-table-column>
      <el-table-column label="权限">
        <template slot-scope="scope">
          <el-tag>{{ userPermission[scope.row.role] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            type="primary"
            size="mini"
            @click="
              userInfo = scope.row
              putUserDialog = true
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
    <el-dialog title="更新用户" :visible.sync="putUserDialog">
      <el-form label-width="80px" :model="userInfo">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="userInfo.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userInfo.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="用户权限">
          <el-select v-model="userInfo.role" placeholder="用户权限">
            <el-option
              v-for="(v, k) in userPermission"
              :key="k"
              :label="v"
              :value="k"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <el-form
        :model="passForm"
        status-icon
        :rules="passRole"
        label-width="80px"
        ref="passForm"
      >
        <el-form-item label="密码" prop="pass">
          <el-input
            type="password"
            v-model="passForm.pass"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPass">
          <el-input
            type="password"
            v-model="passForm.checkPass"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button style="float: left" type="danger" @click="deleteUser()"
          >删 除</el-button
        >
        <el-button @click="putUserDialog = false">取 消</el-button>
        <el-button
          type="primary"
          @click="
            updateUserInfo()
            putUserDialog = false
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
      userPermission: {
        Admin: '管理员',
        Normal: '普通用户',
      },
      putUserDialog: false,
      userInfo: {},
      passForm: {
        pass: '',
        checkPass: '',
      },
      passRole: {
        pass: [
          {
            validator: (rule, value, callback) => {
              if (value === '') {
                callback(new Error('请输入密码'))
              } else {
                if (this.passForm.checkPass !== '') {
                  this.$refs.passForm.validateField('checkPass')
                }
                callback()
              }
            },
            trigger: 'blur',
          },
        ],
        checkPass: [
          {
            validator: (rule, value, callback) => {
              if (value === '') {
                callback(new Error('请再次输入密码'))
              } else if (value !== this.passForm.pass) {
                callback(new Error('两次输入密码不一致!'))
              } else {
                callback()
              }
            },
            trigger: 'blur',
          },
        ],
      },
    }
  },
  methods: {
    initData() {
      this.getTableData('/admin/user')
      this.loadPage = () => {
        this.getTableData(`/admin/user?p=${this.currentPage}`)
      }
      this.getPageSize('/admin/user/count')
    },
    updateUserInfo() {
      this.$axios
        .put(
          `/admin/user/${this.userInfo.id}`,
          new URLSearchParams({
            username: this.userInfo.username,
            nickname: this.userInfo.nickname,
            email: this.userInfo.email,
            password: this.passForm.pass,
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
    },
    deleteUser() {
      this.deleteData(`/admin/user/${this.userInfo.id}`)
    },
  },
  mounted() {
    this.initData()
  },
}
</script>

<style>
</style>