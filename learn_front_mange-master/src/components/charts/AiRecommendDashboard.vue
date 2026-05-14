<template>
  <div class="ai-recommend-wrapper">
    <div class="recommend-header">
      <span class="recommend-title">
        <i class="el-icon-magic-stick"></i>
        AI个性化推荐
      </span>
      <el-button 
        type="primary" 
        size="mini" 
        :loading="loading"
        @click="getRecommendation"
        icon="el-icon-refresh">
        {{ loading ? '分析中...' : '获取推荐' }}
      </el-button>
    </div>
    
    <div class="recommend-content" v-if="recommendation">
      <div class="recommend-text" v-html="formattedRecommendation"></div>
    </div>
    
    <div class="recommend-placeholder" v-else-if="!loading">
      <i class="el-icon-info-outline"></i>
      <p>点击获取推荐按钮</p>
    </div>
    
    <div class="recommend-loading" v-if="loading">
      <i class="el-icon-loading"></i>
      <p>AI分析中...</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiRecommendDashboard',
  props: {
    studentId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      recommendation: '',
      eventSource: null
    }
  },
  computed: {
    formattedRecommendation() {
      if (!this.recommendation) return ''
      return this.recommendation
        .replace(/\n/g, '<br>')
        .replace(/##\s*(.+)/g, '<h4 class="md-heading">$1</h4>')
        .replace(/-\s*(.+)/g, '<li>$1</li>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/(\d+)\.\s*(.+)/g, '<p><span class="num">$1.</span> $2</p>')
    }
  },
  beforeDestroy() {
    this.closeEventSource()
  },
  methods: {
    getRecommendation() {
      this.loading = true
      this.recommendation = ''
      
      const studentId = this.studentId || localStorage.getItem('userId')
      if (!studentId) {
        this.$message.error('请先选择学生')
        this.loading = false
        return
      }
      
      this.closeEventSource()
      
      const token = localStorage.getItem('token')
      const url = `${this.$axios.defaults.baseURL}/api/ai/recommend/${studentId}?conversationId=dashboard_${Date.now()}`
      
      this.eventSource = new EventSource(url, {
        headers: {
          'Authorization': token
        }
      })
      
      this.eventSource.onmessage = (event) => {
        this.recommendation += event.data
      }
      
      this.eventSource.onerror = (error) => {
        console.error('SSE Error:', error)
        this.closeEventSource()
        this.loading = false
        
        if (!this.recommendation) {
          this.$message.error('获取推荐失败')
        }
      }
      
      this.eventSource.addEventListener('complete', () => {
        this.closeEventSource()
        this.loading = false
      })
      
      setTimeout(() => {
        if (this.loading) {
          this.closeEventSource()
          this.loading = false
        }
      }, 60000)
    },
    
    closeEventSource() {
      if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
      }
    }
  }
}
</script>

<style scoped>
.ai-recommend-wrapper {
  width: 100%;
  padding: 10px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  border-radius: 8px;
  border: 1px solid rgba(0, 216, 255, 0.3);
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.recommend-title {
  font-size: 14px;
  font-weight: bold;
  color: #00d8ff;
  display: flex;
  align-items: center;
  gap: 5px;
}

.recommend-content {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  padding: 10px;
  max-height: 200px;
  overflow-y: auto;
}

.recommend-text {
  font-size: 12px;
  line-height: 1.6;
  color: #fff;
}

.recommend-text >>> .md-heading {
  font-size: 13px;
  font-weight: bold;
  margin: 10px 0 5px;
  color: #00d8ff;
}

.recommend-text >>> li {
  margin-left: 15px;
  margin-bottom: 3px;
}

.recommend-text >>> .num {
  display: inline-block;
  width: 18px;
  height: 18px;
  background: rgba(0, 216, 255, 0.2);
  border-radius: 50%;
  text-align: center;
  line-height: 18px;
  margin-right: 5px;
  font-size: 10px;
  color: #00d8ff;
}

.recommend-placeholder {
  text-align: center;
  padding: 30px 10px;
  color: #aaa;
}

.recommend-placeholder i {
  font-size: 30px;
  margin-bottom: 10px;
}

.recommend-placeholder p {
  margin: 0;
  font-size: 12px;
}

.recommend-loading {
  text-align: center;
  padding: 30px 10px;
  color: #00d8ff;
}

.recommend-loading i {
  font-size: 24px;
  margin-bottom: 10px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.recommend-loading p {
  margin: 0;
  font-size: 12px;
}
</style>
