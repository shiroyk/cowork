<template>
  <el-aside width="200px" class="side-bar">
    <el-menu
      :default-active="$router.currentRoute.path"
      :router="true"
      @select="menuSelect"
    >
      <el-menu-item index="/document">
        <i class="el-icon-document"></i>
        <span slot="title">个人文档</span>
      </el-menu-item>
      <el-menu-item index="/document/collection">
        <i class="el-icon-collection-tag"></i>
        <span slot="title">我的收藏</span>
      </el-menu-item>
      <el-menu-item index="/document/group">
        <i class="el-icon-menu"></i>
        <span slot="title">群组文档</span>
      </el-menu-item>
      <el-menu-item index="/document/trash">
        <i class="el-icon-delete"></i>
        <span slot="title">回收站</span>
      </el-menu-item>
    </el-menu>
    <transition name="fade">
      <div v-show="showGroupUser" class="info-box">
        <p>
          <i
            style="margin: 0 8px 0 3px; font-size: 18px; color: #909399"
            class="el-icon-user"
          ></i
          >群组成员
        </p>
        <el-popover
          style="text-align: center"
          v-for="(user, i) in groupUser"
          :key="i"
          placement="bottom"
          width="200"
          trigger="click"
        >
          <div style="text-align: center; margin: 0">
            {{ user.username }}<br />
            {{ user.nickname }}<br />
            {{ user.email }}<br />
          </div>
          <el-button type="text" slot="reference">
            <el-avatar
              style="margin-right: 10px"
              :src="user ? user.avatar : ''"
            >
              {{ user ? user.username : '' }}
            </el-avatar>
          </el-button>
        </el-popover>
        <el-button type="text" @click="groupUserDialog = !groupUserDialog">
          <el-avatar icon="el-icon-more"></el-avatar>
        </el-button>
        <GroupTable :groupUserDialog.sync="groupUserDialog" />
      </div>
    </transition>
  </el-aside>
</template>

<script>
import GroupTable from '../components/GroupTable'
export default {
  components: {
    GroupTable,
  },
  data() {
    return {
      groupUser: [],
      groupInfo: null,
      showGroupUser: false,
      groupUserDialog: false,
    }
  },
  methods: {
    menuSelect(key) {
      if (key === '/document/group') {
        if (this.$router.currentRoute.path !== '/document/group')
          this.getGroupUser()
      } else {
        this.showGroupUser = false
      }
    },
    getGroupUser() {
      this.$axios
        .get('/group/user?p=0&s=9')
        .then((res) => {
          if (res.data.code == 200) {
            this.groupUser = res.data.data
            this.showGroupUser = true
          } else {
            this.showGroupUser = false
            this.$message.error(res.data.msg)
          }
        })
        .catch(() => {})
    },
  },
  created() {
    if (this.$router.currentRoute.path === '/document/group')
      this.getGroupUser()
  },
}
</script>

<style>
@import '../styles/transition.css';
.side-bar {
  width: 200px;
  height: calc(100% - 60px);
  position: absolute;
  bottom: 0;
  left: 0;
  overflow: hidden;
  -webkit-transition: width 0.3s;
  transition: width 0.3s;
  border-right: solid 1px #e6e6e6;
}
.el-menu {
  border: none;
}
.info-box {
  font-size: 14px;
  color: #303133;
  padding: 0 20px;
  transition: border-color 0.3s, background-color 0.3s, color 0.3s;
  box-sizing: border-box;
}
</style>