<template>
  <el-aside width="200px" class="left-side-bar">
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
      <el-submenu index="/document/group">
        <template slot="title">
          <i class="el-icon-menu"></i>
          <span>群组文档</span>
        </template>
        <el-menu-item
          v-for="g in groups"
          :key="g.id"
          :index="'/document/group/' + g.id"
        >
          {{ g.name }}
        </el-menu-item>
      </el-submenu>
      <el-menu-item index="/document/trash">
        <i class="el-icon-delete"></i>
        <span slot="title">回收站</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script>
export default {
  data() {
    return {
      groups: [],
    }
  },
  methods: {
    menuSelect(key) {
      if (!key.includes('group')) this.$store.commit('setCurrentGroup', null)
    },
    getAllGroup() {
      this.$axios.get('/group').then((res) => {
        if (res.data.code == 200) {
          this.groups = res.data.data
        } else this.$message.error(res.data.msg)
      })
    },
  },
  created() {
    this.getAllGroup()
  },
}
</script>

<style>
@import '../styles/transition.css';
.left-side-bar {
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
</style>