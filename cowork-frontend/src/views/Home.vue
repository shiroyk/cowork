<template>
  <div>
    <Header />
    <el-main>
      <el-card shadow="hover" class="doc-desc">
        <div class="desc-left">
          <h2>协同编辑</h2>
          <p>多人在线实时协作编辑文档</p>
          <p>丰富的文本格式化</p>
          <p>实时自动保存，随时随地进行协作编辑</p>
        </div>
        <img
          class="desc-right"
          :src="require('../assets/main-p1.png')"
          alt=""
          style="width: 60%; display: block"
        />
      </el-card>
      <el-card style="margin-top: 20px" shadow="hover" class="doc-desc">
        <div class="desc-left">
          <h2>群组文档</h2>
          <p>群组用户共享、编辑更加便捷</p>
          <p>生产文档链接，邀请他人参与协作</p>
          <p>管理群组成员对文档的操作权限</p>
        </div>
        <img
          class="desc-right"
          :src="require('../assets/main-p2.png')"
          alt=""
          style="width: 60%; display: block"
        />
      </el-card>
    </el-main>
  </div>
</template>

<script>
import Header from '../components/Header'
export default {
  components: { Header },
}
</script>

<style>
.doc-desc {
  width: 80%;
  height: 500px;
  margin: auto;
  padding: 0 20px;
  border: none !important;
  font-size: 18px;
  font-family: SimSun, 宋体, sans-serif, 'Microsoft YaHei', 微软雅黑;
}
.desc-left {
  width: 40%;
  float: left;
  height: 100%;
}
.desc-right {
  width: 60%;
  text-align: right;
  overflow: hidden;
  height: 100%;
}
</style>