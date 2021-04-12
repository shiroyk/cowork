<template>
  <div>
    <el-header
      ><div class="header">
        <div class="header-left">
          <h3 class="header-tittle">协作系统管理后台</h3>
        </div>
        <div class="header-right">
          <div class="headr-user-icon">
            <div style="padding: 3px 20px">
              <el-avatar :src="userInfo ? userInfo.avatar : ''">
                {{ userInfo ? userInfo.username : '' }}
              </el-avatar>
            </div>
          </div>
        </div>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="$router.currentRoute.path"
          class="side-bar"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          :router="true"
        >
          <el-menu-item index="/dashboard">
            <i class="el-icon-s-data"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/user">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/group">
            <i class="el-icon-menu"></i>
            <span slot="title">群组管理</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/document">
            <i class="el-icon-document"></i>
            <span slot="title">文档管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <transition name="fade">
          <router-view></router-view>
        </transition>
      </el-main>
    </el-container>
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
@import '../styles/transition.css';
.header {
  height: 50px;
  position: fixed;
  top: 0;
  width: 100%;
  left: 0;
  z-index: 999;
}
.header-left {
  width: 200px;
  height: 100%;
  float: left;
  background-color: #545c64;
  box-sizing: border-box;
  border-bottom: 1px solid #454b61;
  transition: width 0.3s;
  overflow: hidden;
}
.header-tittle {
  color: white;
  text-align: center;
  font-family: SimSun, 宋体, sans-serif, 'Microsoft YaHei', 微软雅黑;
}
.header-right {
  height: 100%;
  margin-left: 200px;
  background-color: white;
  box-shadow: 0 2px 2px rgba(0, 0, 0, 0.05), 0 1px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}
.headr-user-icon {
  float: right;
  height: 100%;
}
.side-bar {
  width: 200px;
  height: calc(100% - 50px);
  position: absolute;
  bottom: 0;
  left: 0;
  overflow: hidden;
  -webkit-transition: width 0.3s;
  transition: width 0.3s;
}
</style>