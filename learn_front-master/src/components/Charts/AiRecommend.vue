<template>
  <div class="ai-recommend-container">
    <div class="recommend-header">
      <h3 class="recommend-title">
        <i class="el-icon-magic-stick"></i>
        AI个性化推荐
      </h3>
      <el-button 
        type="primary" 
        size="small" 
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
      <p>点击"获取推荐"按钮，AI将根据您的学习情况提供个性化建议</p>
    </div>
    
    <div class="recommend-loading" v-if="loading">
      <i class="el-icon-loading"></i>
      <p>AI正在分析您的学习数据...</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiRecommend',
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
        this.$message.error('请先登录')
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
          this.$message.error('获取推荐失败，请稍后重试')
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
.ai-recommend-container {
  width: 100%;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  min-height: 200px;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.recommend-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommend-content {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 15px;
  backdrop-filter: blur(10px);
}

.recommend-text {
  font-size: 14px;
  line-height: 1.8;
  color: #fff;
}

.recommend-text >>> .md-heading {
  font-size: 16px;
  font-weight: bold;
  margin: 15px 0 10px;
  color: #ffd700;
}

.recommend-text >>> li {
  margin-left: 20px;
  margin-bottom: 5px;
}

.recommend-text >>> .num {
  display: inline-block;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  text-align: center;
  line-height: 20px;
  margin-right: 5px;
  font-size: 12px;
}

.recommend-placeholder {
  text-align: center;
  padding: 40px 20px;
  opacity: 0.8;
}

.recommend-placeholder i {
  font-size: 48px;
  margin-bottom: 15px;
}

.recommend-placeholder p {
  margin: 0;
  font-size: 14px;
}

.recommend-loading {
  text-align: center;
  padding: 40px 20px;
}

.recommend-loading i {
  font-size: 36px;
  margin-bottom: 15px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.recommend-loading p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}
</style>
