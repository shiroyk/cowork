<template>
  <div>
    <Header />
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
        <el-form-item v-show="resetPass == false" label="密码" prop="password">
          <el-input
            type="password"
            v-model="loginForm.password"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item v-show="register" label="邮箱" prop="email">
          <el-input
            v-model.number="loginForm.email"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-popover v-model="resetPop" placement="bottom" width="260">
            <el-form :model="resetForm">
              <el-form-item label="验证码" label-width="120">
                <el-input autocomplete="off" v-model="resetForm.code">
                </el-input>
              </el-form-item>
              <el-form-item label="新密码" label-width="120">
                <el-input
                  type="password"
                  autocomplete="off"
                  v-model="resetForm.password"
                >
                </el-input>
              </el-form-item>
            </el-form>
            <div style="text-align: center; margin: auto">
              <el-button size="mini" type="primary" @click="resetPassword()"
                >提 交</el-button
              >
            </div>
            <el-button
              v-show="resetPass == true && register == false"
              class="button"
              type="primary"
              slot="reference"
              @click="getResetQuention()"
              >重置密码</el-button
            >
          </el-popover>
        </el-form-item>
        <el-form-item>
          <el-button
            v-show="register == false && resetPass == false"
            class="button"
            type="primary"
            @click="submitForm()"
            >登录</el-button
          >
        </el-form-item>
        <el-form-item>
          <el-button
            v-show="register == true && resetPass == false"
            class="button"
            type="primary"
            @click="goRegister()"
            >注册</el-button
          >
        </el-form-item>
        <el-form-item>
          <span style="float: right">
            <el-button
              v-show="resetPass == false && register == false"
              type="text"
              @click="
                register = true
                resetPass = false
              "
              >未注册？</el-button
            >
            <el-button
              v-show="register == false && resetPass == false"
              @click="
                resetPass = true
                register = false
              "
              type="text"
              >找回密码</el-button
            >
            <el-button
              v-show="register == true || resetPass == true"
              @click="
                register = false
                resetPass = false
              "
              type="text"
              >返回</el-button
            >
          </span>
        </el-form-item>
      </el-form>
    </el-main>
  </div>
</template>

<script>
import Header from '../components/Header'
export default {
  components: { Header },
  data() {
    return {
      register: false,
      resetPass: false,
      resetPop: false,
      loginForm: {
        username: '',
        password: '',
        email: '',
      },
      resetForm: {
        username: '',
        code: '',
        password: '',
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
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
              this.$message.success('登录成功！')
              this.$store.commit(
                'updateAccessToken',
                res.data.data.access_token
              )
              this.$store.commit(
                'updateRefreshToken',
                res.data.data.refresh_token
              )
              this.cacheUserInfo()
              this.$router.push({ path: '/document' })
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
    goRegister() {
      const params = new URLSearchParams({
        username: this.loginForm.username,
        password: this.loginForm.password,
        email: this.loginForm.email,
      })
      const loading = this.loading('注册中')
      this.$axios
        .post('/oauth/signup', params)
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
            this.register = false
          } else this.$message.error(res.data.msg)
          loading.close()
        })
        .catch(() => {
          loading.close()
          this.$message.error('注册失败，请稍后重试！')
        })
    },
    getResetQuention() {
      this.$axios
        .get('/oauth/reset?username=' + this.loginForm.username)
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success('验证码已发送至邮箱！')
          }
        })
        .catch(() => {
          this.$message.error('测试验证码: 1234')
        })
    },
    resetPassword() {
      const params = new URLSearchParams({
        username: this.loginForm.username,
        code: this.resetForm.code,
        password: this.resetForm.password,
      })
      this.$axios
        .post('/oauth/reset', params)
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
            this.resetPass = false
            this.resetPop = false
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.resetPop = false
          this.$message.error('重置密码失败，请稍后重试！')
        })
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
  created: function () {},
}
</script>

<style>
.login-form {
  width: 400px;
  margin: auto;
}
.button {
  width: 100%;
  display: block;
}
</style>