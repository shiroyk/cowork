<template>
  <el-card>
    <div style="width: 100%; height: 400px" ref="lineChart"></div>
  </el-card>
</template>

<script>
import { init } from 'echarts'
import 'echarts/lib/chart/line'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/title'
export default {
  data() {
    return {
      mycharts: null,
      option: {
        color: ['#4caf50', '#bb002f', '#03a9f4'],
        title: {
          text: '数据统计',
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985',
            },
          },
        },
        toolbox: {
          feature: {
            saveAsImage: {},
          },
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: [],
          },
        ],
        yAxis: [
          {
            type: 'value',
          },
        ],
        series: [
          {
            name: '用户在线数',
            type: 'line',
            stack: '总量',
            areaStyle: {},
            data: [1, 3, 2, 1, 3, 1, 2, 3, 1],
          },
        ],
      },
    }
  },
  methods: {
    initLineChart() {
      this.mycharts = init(this.$refs.lineChart)
      this.mycharts.setOption(this.option)
    },
    resizeChart() {
      this.mycharts.resize()
    },
    generateData() {
      let data = []
      for (let index = 9; index > 0; index--) {
        const d = new Date()
        d.setDate(d.getDate() - index)
        data.push(d.toLocaleDateString())
      }
      this.option.xAxis[0].data = data
    },
  },
  mounted() {
    this.$nextTick().then(() => {
      this.generateData()
      this.initLineChart()
    })
    window.addEventListener('resize', this.resizeChart)
  },
  destroyed() {
    window.removeEventListener('resize', this.resizeChart)
  },
}
</script>

<style>
.chart-container {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style>