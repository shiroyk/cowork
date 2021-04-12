<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="6">
        <el-card class="box-card">
          <i class="el-icon-edit-outline"></i>
          在线用户: {{ statistic.collab || 0 }}
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <i class="el-icon-user"></i>
          用户总数: {{ statistic.user || 0 }}
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <i class="el-icon-menu"></i>
          群组总数: {{ statistic.group || 0 }}
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <i class="el-icon-document"></i>
          文档总数: {{ statistic.doc || 0 }}
        </el-card>
      </el-col>
    </el-row>
    <LineCharts style="margin-top: 30px" :lineChartData="lineChartData" />
  </div>
</template>

<script>
import LineCharts from '../components/LineCharts'
export default {
  components: {
    LineCharts,
  },
  data() {
    return {
      lineChartData: {},
      statistic: {},
    }
  },
  methods: {
    getStatistic() {
      this.$axios
        .get('/admin/doc/statistic')
        .then((res) => {
          if (res.data.code == 200) this.statistic = res.data.data
          else this.$message.error(res.data.msg)
        })
        .catch(() => {
          this.$message.error('加载失败！')
        })
    },
  },
  mounted() {
    this.getStatistic()
  },
}
</script>

<style>
</style>