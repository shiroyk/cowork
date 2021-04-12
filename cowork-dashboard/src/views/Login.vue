<template>
  <div>
    <div>
      <h1 class="header">协作系统管理后台</h1>
    </div>
    <el-main>
      <el-form
        :model="loginForm"
        status-icon
        :rules="rules"
        label-width="100px"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            type="text"
            v-model="loginForm.username"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            type="password"
            v-model="loginForm.password"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button class="button" type="primary" @click="submitForm()"
            >登录</el-button
          >
        </el-form-item>
      </el-form>
    </el-main>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      },
    }
  },
  methods: {
    loading(text) {
      return this.$loading({
        lock: true,
        text: text,
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)',
      })
    },
    parseJwt(token) {
      let base64Url = token.split('.')[1]
      let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      let jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
          })
          .join('')
      )

      return JSON.parse(jsonPayload)
    },
    submitForm() {
      if (!this.loginForm.username) {
        this.$message.error('请输入用户名！')
        return
      } else if (!this.loginForm.password) {
        this.$message.error('请输入密码！')
        return
      } else {
        const params = new URLSearchParams({
          grant_type: 'password',
          client_id: 'cowork',
          client_secret: 'cowork',
          username: this.loginForm.username,
          password: this.loginForm.password,
        })
        const loading = this.loading('登录中')
        this.$axios
          .post('/oauth/token', params, {
            auth: {
              username: 'cowork',
              password: 'cowork',
            },
          })
          .then((res) => {
            if (res.data.code === 200) {
              const jwt = res.data.data.access_token
              const jwtPayload = this.parseJwt(jwt)
              if (jwtPayload && jwtPayload.authorities[0] !== 'Admin')
                this.$message.error('无权访问！')
              else {
                this.cacheUserInfo()
                this.$store.commit('updateAccessToken', jwt)
                this.$store.commit(
                  'updateRefreshToken',
                  res.data.data.refresh_token
                )
                this.$router.push({ path: '/dashboard' })
              }
            } else if (res.data.code === 400) {
              this.$message.error('输入的用户名或密码错误！')
            }
            loading.close()
          })
          .catch(() => {
            loading.close()
          })
      }
    },
    cacheUserInfo() {
      this.$axios
        .get('/user')
        .then((res) => {
          if (res.data.code == 200) {
            const info = res.data.data
            this.$store.commit('updateUserInfo', {
              id: info.id,
              username: info.username,
              nickname: info.nickname,
              avatar: info.avatar,
            })
          }
        })
        .catch(() => {})
    },
  },
}
</script>

<style>
.login-form {
  width: 400px;
  margin: auto;
}
.header {
  text-align: center;
  font-family: SimSun, 宋体, sans-serif, 'Microsoft YaHei', 微软雅黑;
}
.button {
  width: 100%;
  display: block;
}
</style>