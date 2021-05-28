<template>
  <div>
    <Header />
    <el-container>
      <LeftSideBar />
      <transition name="fade">
        <router-view style="margin: 0 200px 0 200px" />
      </transition>
      <RightSideBar />
    </el-container>
  </div>
</template>

<script>
import Header from '../components/Header'
import LeftSideBar from '../components/LeftSideBar'
import RightSideBar from '../components/RightSideBar'
export default {
  components: {
    Header,
    LeftSideBar,
    RightSideBar,
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