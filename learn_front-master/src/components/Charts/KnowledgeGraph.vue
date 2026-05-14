<template>
  <div class="knowledge-graph-container">
    <div class="graph-header">
      <h3 class="graph-title">{{ title }}</h3>
      <div class="graph-controls">
        <el-select v-model="selectedBranch" placeholder="选择分支" size="small" @change="filterByBranch">
          <el-option label="全部" value=""></el-option>
          <el-option label="力学" value="力学"></el-option>
          <el-option label="电学" value="电学"></el-option>
          <el-option label="热学" value="热学"></el-option>
          <el-option label="光学" value="光学"></el-option>
          <el-option label="运动学" value="运动学"></el-option>
        </el-select>
        <el-button-group>
          <el-button size="small" :type="layout === 'force' ? 'primary' : ''" @click="changeLayout('force')">力导向</el-button>
          <el-button size="small" :type="layout === 'circular' ? 'primary' : ''" @click="changeLayout('circular')">环形</el-button>
        </el-button-group>
      </div>
    </div>
    <div ref="graphChart" class="knowledge-graph" :style="{ height: height }"></div>
    <div class="graph-legend">
      <div class="legend-item" v-for="branch in branchColors" :key="branch.name">
        <span class="legend-color" :style="{ backgroundColor: branch.color }"></span>
        <span class="legend-text">{{ branch.name }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'KnowledgeGraph',
  props: {
    title: {
      type: String,
      default: '物理知识图谱'
    },
    height: {
      type: String,
      default: '500px'
    },
    highlightKpId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      chart: null,
      graphData: null,
      selectedBranch: '',
      layout: 'force',
      branchColors: [
        { name: '力学', color: '#5470c6' },
        { name: '电学', color: '#91cc75' },
        { name: '热学', color: '#fac858' },
        { name: '光学', color: '#ee6666' },
        { name: '运动学', color: '#73c0de' }
      ]
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
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.graphChart)
      window.addEventListener('resize', this.handleResize)
      
      this.chart.on('click', (params) => {
        if (params.dataType === 'node') {
          this.$emit('node-click', params.data)
        }
      })
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    async fetchData() {
      try {
        const res = await this.$axios.get('/knowledgeGraph/getGraphData')
        if (res.data.code === 200) {
          this.graphData = res.data.data
          this.updateChart()
        }
      } catch (error) {
        console.error('获取知识图谱数据失败:', error)
      }
    },
    getBranchColor(branch) {
      const found = this.branchColors.find(b => b.name === branch)
      return found ? found.color : '#999'
    },
    updateChart() {
      if (!this.graphData || !this.chart) return

      let nodes = this.graphData.nodes || []
      let links = this.graphData.links || []

      if (this.selectedBranch) {
        const filteredNodeIds = nodes
          .filter(n => n.category === this.selectedBranch)
          .map(n => n.id)
        nodes = nodes.filter(n => n.category === this.selectedBranch || 
          links.some(l => 
            (filteredNodeIds.includes(l.source) && l.target === n.id) ||
            (filteredNodeIds.includes(l.target) && l.source === n.id)
          ))
        const nodeIds = nodes.map(n => n.id)
        links = links.filter(l => nodeIds.includes(l.source) && nodeIds.includes(l.target))
      }

      const processedNodes = nodes.map(node => ({
        id: node.id,
        name: node.name,
        category: node.category,
        symbolSize: node.level === 1 ? 50 : (node.level === 2 ? 35 : 25),
        itemStyle: {
          color: this.getBranchColor(node.category)
        },
        label: {
          show: true,
          fontSize: node.level === 1 ? 14 : 12
        },
        emphasis: {
          focus: 'adjacency',
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        }
      }))

      const processedLinks = links.map(link => ({
        source: link.source,
        target: link.target,
        lineStyle: {
          width: link.weight || 1,
          curveness: 0.1,
          color: '#aaa'
        },
        label: {
          show: false
        }
      }))

      const categories = this.branchColors.map(b => ({ name: b.name }))

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            if (params.dataType === 'node') {
              return `${params.data.name}<br/>分支: ${params.data.category}`
            } else {
              return `${params.data.source} → ${params.data.target}`
            }
          }
        },
        legend: {
          show: false
        },
        series: [
          {
            type: 'graph',
            layout: this.layout,
            data: processedNodes,
            links: processedLinks,
            categories: categories,
            roam: true,
            draggable: true,
            label: {
              position: 'right',
              formatter: '{b}'
            },
            lineStyle: {
              color: 'source',
              curveness: 0.1
            },
            emphasis: {
              focus: 'adjacency',
              lineStyle: {
                width: 3
              }
            },
            force: {
              repulsion: 200,
              edgeLength: [50, 150],
              gravity: 0.1
            },
            circular: {
              rotateLabel: true
            }
          }
        ]
      }

      this.chart.setOption(option)
    },
    filterByBranch() {
      this.updateChart()
    },
    changeLayout(type) {
      this.layout = type
      this.updateChart()
    }
  }
}
</script>

<style scoped>
.knowledge-graph-container {
  width: 100%;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.graph-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.graph-controls {
  display: flex;
  gap: 10px;
}

.knowledge-graph {
  width: 100%;
}

.graph-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
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
  border-radius: 50%;
}

.legend-text {
  font-size: 12px;
  color: #666;
}
</style>
