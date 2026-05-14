<template>
  <div class="knowledge-graph-page">
    <headerPage></headerPage>
    
    <div class="page-container">
      <div class="page-header">
        <div class="header-title">
          <span class="title-icon">🔗</span>
          <h2>物理知识图谱</h2>
        </div>
        <p class="header-subtitle">探索物理学科知识体系，发现知识点之间的关联</p>
        <div class="header-decoration"></div>
      </div>
      
      <div class="graph-container">
        <div class="graph-toolbar">
          <el-select v-model="selectedBranch" placeholder="选择分支筛选" size="small" @change="filterByBranch" style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="力学" value="力学"></el-option>
            <el-option label="电学" value="电学"></el-option>
            <el-option label="热学" value="热学"></el-option>
            <el-option label="光学" value="光学"></el-option>
            <el-option label="运动学" value="运动学"></el-option>
          </el-select>
          
          <el-button-group style="margin-left: 15px;">
            <el-button size="small" :type="layout === 'force' ? 'primary' : 'default'" @click="changeLayout('force')">力导向布局</el-button>
            <el-button size="small" :type="layout === 'circular' ? 'primary' : 'default'" @click="changeLayout('circular')">环形布局</el-button>
          </el-button-group>
          
          <el-button size="small" icon="el-icon-refresh" @click="fetchData" style="margin-left: 15px;">刷新</el-button>
        </div>
        
        <div class="graph-main">
          <div ref="graphChart" class="graph-chart"></div>
          
          <div class="graph-legend">
            <div class="legend-title">图例说明</div>
            <div class="legend-item" v-for="branch in branchColors" :key="branch.name">
              <span class="legend-color" :style="{ backgroundColor: branch.color }"></span>
              <span class="legend-text">{{ branch.name }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="info-panel" v-if="selectedNode">
        <div class="info-header">
          <h3>{{ selectedNode.name }}</h3>
          <el-tag size="small" :color="getBranchColor(selectedNode.category)" style="color: #fff;">{{ selectedNode.category }}</el-tag>
        </div>
        <div class="info-content">
          <p><strong>层级：</strong>{{ getLevelText(selectedNode.level) }}</p>
          <p><strong>难度：</strong>{{ (selectedNode.difficulty * 100).toFixed(0) }}%</p>
          <p v-if="selectedNode.description"><strong>描述：</strong>{{ selectedNode.description }}</p>
        </div>
        <div class="info-actions">
          <el-button type="primary" size="small" @click="goToPractice(selectedNode)">开始练习</el-button>
          <el-button size="small" @click="goToVideo(selectedNode)">相关视频</el-button>
        </div>
      </div>
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"
import { getKnowledgeGraphData } from '../../api/api'

export default {
  name: 'KnowledgeGraphPage',
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      chart: null,
      graphData: null,
      selectedBranch: '',
      layout: 'force',
      selectedNode: null,
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
          this.selectedNode = params.data
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
        const res = await getKnowledgeGraphData()
        if (res.code === 1000 || res.success) {
          this.graphData = res.data
          this.updateChart()
        } else {
          this.$message.error(res.message || '获取知识图谱数据失败')
        }
      } catch (error) {
        console.error('获取知识图谱数据失败:', error)
        this.$message.error('获取知识图谱数据失败')
      }
    },
    getBranchColor(branch) {
      const found = this.branchColors.find(b => b.name === branch)
      return found ? found.color : '#999'
    },
    getLevelText(level) {
      const levels = { 1: '一级(分支)', 2: '二级(章节)', 3: '三级(知识点)' }
      return levels[level] || '未知'
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
        level: node.level,
        difficulty: node.difficulty,
        description: node.description,
        symbolSize: node.level === 1 ? 60 : (node.level === 2 ? 40 : 28),
        itemStyle: {
          color: this.getBranchColor(node.category)
        },
        label: {
          show: true,
          fontSize: node.level === 1 ? 14 : 12,
          fontWeight: node.level === 1 ? 'bold' : 'normal'
        },
        emphasis: {
          focus: 'adjacency',
          itemStyle: {
            shadowBlur: 15,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        }
      }))

      const processedLinks = links.map(link => ({
        source: link.source,
        target: link.target,
        lineStyle: {
          width: link.weight || 1.5,
          curveness: 0.1,
          color: '#aaa'
        }
      }))

      const categories = this.branchColors.map(b => ({ name: b.name }))

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            if (params.dataType === 'node') {
              return `<strong>${params.data.name}</strong><br/>
                      分支: ${params.data.category}<br/>
                      层级: ${this.getLevelText(params.data.level)}<br/>
                      难度: ${((params.data.difficulty || 0.5) * 100).toFixed(0)}%`
            } else {
              return `${params.data.source} → ${params.data.target}`
            }
          }
        },
        legend: {
          show: false
        },
        animationDuration: 1500,
        animationEasingUpdate: 'quinticInOut',
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
                width: 4
              }
            },
            force: {
              repulsion: 300,
              edgeLength: [80, 200],
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
    },
    goToPractice(node) {
      this.$router.push(`/physicsPractice?kpId=${node.id}&kpName=${node.name}&branch=${node.category}`)
    },
    goToVideo(node) {
      this.$router.push(`/task?kpId=${node.id}&kpName=${node.name}&branch=${node.category}`)
    }
  }
}
</script>

<style scoped>
.knowledge-graph-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 25px;
  padding: 25px;
  background: linear-gradient(135deg, #99CDD8 0%, #DAE8E8 25%, #FDE8D0 50%, #F5C8B7 75%, #DEF0D4 100%);
  border-radius: 16px;
  color: #555;
  box-shadow: 0 8px 24px rgba(153, 205, 216, 0.4);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -5%;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
}

.header-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 10px;
  position: relative;
  z-index: 1;
}

.title-icon {
  font-size: 36px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { 
    transform: scale(1);
    opacity: 1;
  }
  50% { 
    transform: scale(1.15);
    opacity: 0.9;
  }
}

.page-header h2 {
  font-size: 32px;
  color: #657166;
  margin: 0;
  font-weight: 700;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
  letter-spacing: 2px;
}

.header-subtitle {
  color: #7a8a7b;
  font-size: 15px;
  margin: 0 0 15px 0;
  position: relative;
  z-index: 1;
}

.header-decoration {
  height: 3px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.6) 20%,
    rgba(255, 255, 255, 0.9) 50%,
    rgba(255, 255, 255, 0.6) 80%,
    transparent 100%
  );
  border-radius: 2px;
  position: relative;
  z-index: 1;
}

.graph-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.graph-toolbar {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
}

.graph-main {
  display: flex;
  height: 600px;
}

.graph-chart {
  flex: 1;
}

.graph-legend {
  width: 150px;
  padding: 20px;
  border-left: 1px solid #eee;
  background: #fafafa;
}

.legend-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  margin-right: 8px;
}

.legend-text {
  font-size: 13px;
  color: #666;
}

.info-panel {
  margin-top: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.info-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.info-content {
  margin-bottom: 15px;
}

.info-content p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.info-actions {
  display: flex;
  gap: 10px;
}
</style>
