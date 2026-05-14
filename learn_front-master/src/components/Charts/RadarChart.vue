<template>
  <div class="radar-chart-container">
    <div class="chart-title" v-if="title">{{ title }}</div>
    <div ref="radarChart" class="radar-chart" :style="{ width: width, height: height }"></div>
    <div class="chart-legend" v-if="showLegend">
      <div class="legend-item" v-for="(item, index) in legendData" :key="index">
        <span class="legend-color" :style="{ backgroundColor: colors[index % colors.length] }"></span>
        <span class="legend-text">{{ item.name }}: {{ (item.value * 100).toFixed(0) }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'RadarChart',
  props: {
    studentId: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: '能力雷达图'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '300px'
    },
    showLegend: {
      type: Boolean,
      default: true
    },
    colors: {
      type: Array,
      default: () => ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
    }
  },
  data() {
    return {
      chart: null,
      radarData: null,
      legendData: []
    }
  },
  mounted() {
    this.initChart()
    if (this.studentId) {
      this.fetchData()
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  },
  watch: {
    studentId(newVal) {
      if (newVal) {
        this.fetchData()
      }
    }
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.radarChart)
      window.addEventListener('resize', this.handleResize)
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    async fetchData() {
      try {
        const res = await this.$axios.get(`/api/student/radar/${this.studentId}`)
        if (res.data.code === 200) {
          this.radarData = res.data.data
          this.updateChart()
        }
      } catch (error) {
        console.error('获取雷达图数据失败:', error)
      }
    },
    updateChart() {
      if (!this.radarData || !this.chart) return

      const indicators = this.radarData.indicators || []
      const data = this.radarData.data || []

      this.legendData = indicators.map((ind, idx) => ({
        name: ind.name,
        value: data[0]?.value?.[idx] || 0
      }))

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            let result = params.name + '<br/>'
            params.value.forEach((val, idx) => {
              result += `${indicators[idx].name}: ${(val * 100).toFixed(0)}%<br/>`
            })
            return result
          }
        },
        radar: {
          indicator: indicators,
          shape: 'polygon',
          splitNumber: 5,
          axisName: {
            color: '#333',
            fontSize: 12,
            fontWeight: 'bold'
          },
          splitLine: {
            lineStyle: {
              color: '#ddd'
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(84, 112, 198, 0.1)', 'rgba(84, 112, 198, 0.05)']
            }
          },
          axisLine: {
            lineStyle: {
              color: '#ccc'
            }
          }
        },
        series: [
          {
            name: '能力值',
            type: 'radar',
            data: data,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              width: 2,
              color: this.colors[0]
            },
            areaStyle: {
              color: this.colors[0],
              opacity: 0.3
            },
            itemStyle: {
              color: this.colors[0]
            },
            label: {
              show: true,
              formatter: (params) => {
                return (params.value * 100).toFixed(0) + '%'
              },
              color: '#333',
              fontSize: 10
            }
          }
        ]
      }

      this.chart.setOption(option)
    },
    setData(data) {
      this.radarData = data
      this.updateChart()
    }
  }
}
</script>

<style scoped>
.radar-chart-container {
  width: 100%;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 10px;
}

.radar-chart {
  margin: 0 auto;
}

.chart-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-text {
  font-size: 12px;
  color: #666;
}
</style>
