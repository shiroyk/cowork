<template>
  <div class="container">
    <el-row type="flex" justify="space-around" align="middle">
      <el-col :span="20">
        <img class="icon" src="../assets/logo.png" />
      </el-col>
      <el-col v-if="!accessToken" :span="2">
        <el-link class="header-link" :underline="false" href="/">主页</el-link>
      </el-col>
      <el-col v-else :span="2">
        <el-link class="header-link" :underline="false" href="/document"
          >文档</el-link
        >
      </el-col>
      <el-col v-if="accessToken" :span="2">
        <el-link :underline="false" href="/user">
          <el-avatar :src="userInfo ? userInfo.avatar : ''">
            {{ userInfo ? userInfo.username : '' }}
          </el-avatar>
        </el-link>
      </el-col>
      <el-col v-else :span="2">
        <el-link class="header-link" :underline="false" href="/login"
          >登录</el-link
        >
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState } from 'vuex'
export default {
  computed: {
    ...mapState({
      accessToken: (state) => state.accessToken,
      userInfo: (state) => state.userInfo,
    }),
  },
}
</script>

<style>
.container {
  height: 45px;
  padding: 5px 15% 0 15%;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 5px;
}
.icon {
  height: 40px;
  width: 40px;
}
.header-link {
  margin-top: -5px;
}
</style>