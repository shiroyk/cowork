<template>
  <div>
    <Header />
    <el-container>
      <SideBar />
      <transition name="fade">
        <router-view style="margin-left: 200px" />
      </transition>
    </el-container>
  </div>
</template>

<script>
import Header from '../components/Header'
import SideBar from '../components/SideBar'
export default {
  components: {
    Header,
    SideBar,
  },
  data() {
    return {}
  },
  methods: {},
}
</script>

<style>
@import '../styles/transition.css';
</style>