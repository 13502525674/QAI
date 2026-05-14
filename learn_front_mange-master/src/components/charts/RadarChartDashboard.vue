<template>
  <div class="radar-chart-wrapper">
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
import { get } from '@/utils/request'

export default {
  name: 'RadarChartDashboard',
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
      default: '180px'
    },
    showLegend: {
      type: Boolean,
      default: true
    },
    colors: {
      type: Array,
      default: () => ['#00d8ff', '#f8b500', '#00ff99', '#ff3d00', '#a800ff']
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
    this.fetchData()
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.radarChart)
      window.addEventListener('resize', this.handleResize)
      this.showDefaultChart()
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    showDefaultChart() {
      const defaultIndicators = [
        { name: '力学', max: 1 },
        { name: '电学', max: 1 },
        { name: '热学', max: 1 },
        { name: '光学', max: 1 },
        { name: '运动学', max: 1 }
      ]
      const defaultValues = [0, 0, 0, 0, 0]
      
      this.legendData = defaultIndicators.map((ind, idx) => ({
        name: ind.name,
        value: defaultValues[idx]
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: '#00d8ff',
          textStyle: { color: '#fff' }
        },
        radar: {
          indicator: defaultIndicators,
          shape: 'polygon',
          splitNumber: 5,
          center: ['50%', '65%'],
          radius: '55%',
          axisName: {
            color: '#00d8ff',
            fontSize: 11,
            fontWeight: 'bold'
          },
          splitLine: {
            lineStyle: { color: 'rgba(0, 216, 255, 0.3)' }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(0, 216, 255, 0.1)', 'rgba(0, 216, 255, 0.05)']
            }
          },
          axisLine: {
            lineStyle: { color: 'rgba(0, 216, 255, 0.5)' }
          }
        },
        series: [{
          name: '能力值',
          type: 'radar',
          data: [{ value: defaultValues, name: '能力值' }],
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { width: 2, color: this.colors[0] },
          areaStyle: { color: this.colors[0], opacity: 0.4 },
          itemStyle: { color: this.colors[0] }
        }]
      }
      this.chart.setOption(option)
    },
    async fetchData() {
      try {
        console.log('Fetching overall average radar data')
        const res = await get('/api/student/radar/overall')
        console.log('Radar API response:', res)
        if (res.success || res.code === 1000 || res.code === 200) {
          this.radarData = res.data
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

      if (indicators.length === 0) {
        this.showDefaultChart()
        return
      }

      this.legendData = indicators.map((ind, idx) => ({
        name: ind.name,
        value: data[0]?.value?.[idx] || 0
      }))

      const option = {
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: '#00d8ff',
          textStyle: {
            color: '#fff'
          },
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
          center: ['50%', '65%'],
          radius: '55%',
          axisName: {
            color: '#00d8ff',
            fontSize: 11,
            fontWeight: 'bold'
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(0, 216, 255, 0.3)'
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(0, 216, 255, 0.1)', 'rgba(0, 216, 255, 0.05)']
            }
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(0, 216, 255, 0.5)'
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
              opacity: 0.4
            },
            itemStyle: {
              color: this.colors[0]
            },
            label: {
              show: true,
              formatter: (params) => {
                return (params.value * 100).toFixed(0) + '%'
              },
              color: '#00d8ff',
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
.radar-chart-wrapper {
  width: 100%;
  padding: 0;
}

.chart-title {
  font-size: 14px;
  font-weight: bold;
  color: #00d8ff;
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
  gap: 10px;
  margin-top: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-color {
  width: 10px;
  height: 10px;
  border-radius: 2px;
}

.legend-text {
  font-size: 11px;
  color: #aaa;
}
</style>
