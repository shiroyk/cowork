<template>
  <div>
    <Header />
    <div class="user-info">
      <el-card v-loading="loading" class="user-card">
        <p>
          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :http-request="avatarToBase64"
            :before-upload="beforeAvatarUpload"
          >
            <el-avatar
              class="avator"
              v-if="userInfo.avatar"
              :src="userInfo.avatar"
              :size="60"
            >
              {{ userInfo.username }}
            </el-avatar>
            <i v-else class="el-icon-plus avatar"></i>
          </el-upload>
        </p>
        <el-tag>{{ userInfo.role }}</el-tag>
        <div class="user-detail">
          <h5>用户名: {{ userInfo.username }}</h5>
          <h5>昵称: {{ userInfo.nickname }}</h5>
          <h5>邮箱: {{ userInfo.email }}</h5>
        </div>
        <el-button
          icon="el-icon-edit"
          circle
          @click="editUserInfo = !editUserInfo"
        ></el-button>
        <el-button
          icon="el-icon-switch-button"
          circle
          @click="logout"
        ></el-button>
      </el-card>
      <el-dialog title="更新资料" :visible.sync="editUserInfo">
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
          <el-button @click="editUserInfo = false">取 消</el-button>
          <el-button
            type="primary"
            @click="
              updateUserInfo()
              editUserInfo = false
            "
            >确 定</el-button
          >
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import Header from '../components/Header'
export default {
  components: { Header },
  data() {
    return {
      loading: false,
      editUserInfo: false,
      userInfo: {},
      imageUrl: '',
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
    getUserInfo() {
      this.loading = true
      this.$axios
        .get('/user')
        .then((res) => {
          if (res.data.code == 200) this.userInfo = res.data.data
          else if (res.data.code == 401) {
            this.$router.push('/')
            this.$message.error(res.data.msg)
          }
        })
        .catch(() => {
          this.$message.error('加载失败！')
        })
      this.loading = false
    },
    updateUserInfo() {
      this.$axios
        .put(
          '/user',
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
            this.updateCacheUserInfo()
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.$message.error('更新失败，请稍后重试！')
        })
    },
    avatarToBase64(param) {
      let reader = new FileReader()
      reader.readAsDataURL(param.file)
      reader.onload = (e) => {
        this.uploadAvatar(e.target.result)
      }
    },
    uploadAvatar(base64) {
      this.$axios
        .post(
          '/user/avatar',
          new URLSearchParams({
            avatar: base64,
          })
        )
        .then((res) => {
          if (res.data.code == 200) {
            this.userInfo.avatar = base64
            this.$message.success('更新头像成功！')
            this.updateCacheUserInfo()
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.$message.error('更新头像失败，请稍后重试！')
        })
    },
    beforeAvatarUpload(file) {
      const isLt128 = file.size / 1024 / 1024 < 128
      if (!isLt128) {
        this.$message.error('上传头像图片大小不能超过 128KB!')
      }
      return isLt128
    },
    updateCacheUserInfo() {
      this.$store.commit('updateUserInfo', {
        username: this.userInfo.username,
        nickname: this.userInfo.nickname,
        avatar: this.userInfo.avatar,
      })
    },
    logout() {
      this.$message.success('退出成功！')
      this.$store.commit('removeUserInfo')
      this.$store.commit('removeAccessToken')
      this.$store.commit('removeRefreshToken')
      this.$router.push('/')
    },
  },
  mounted() {
    this.getUserInfo()
  },
}
</script>

<style>
.user-info {
  margin: 20px 0;
}
.user-card {
  margin: auto;
  width: 80%;
  text-align: center;
}
.user-detail {
  width: 20%;
  margin: auto;
  text-align: start;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avator {
  margin: 5px 5px 0 5px;
  text-align: center;
}
</style>