<template>
  <div class="fun-physics-container">
    <div class="star-background"></div>
    
    <div class="header">
      <div class="logo-section">
        <div class="logo-icon">🌌</div>
        <h1 class="logo-title">趣味物理新天地</h1>
      </div>
      <button class="back-home-btn" @click="goHome">
        <span class="back-icon">🏠</span>
        <span class="back-text">返回主页</span>
      </button>
    </div>

    <div class="today-recommend" v-if="recommendMoments">
      <div class="recommend-header">
        <span class="recommend-icon">🌟</span>
        <span class="recommend-title">今日推荐</span>
      </div>
      <div class="recommend-card" @click="goToMomentsDetail(recommendMoments.id)">
        <div class="recommend-avatar">
          <img :src="formatAvatarUrl(recommendMoments.physicistAvatar)" />
        </div>
        <div class="recommend-content">
          <div class="recommend-name">{{ recommendMoments.physicistName }}</div>
          <div class="recommend-text">{{ truncateText(recommendMoments.content, 50) }}</div>
          <div class="recommend-stats">
            <span>⚛️ {{ recommendMoments.likesCount || 0 }}人点赞</span>
            <span>💬 {{ recommendMoments.commentCount || 0 }}条评论</span>
            <span class="enter-hint">点击进入 →</span>
          </div>
        </div>
      </div>
    </div>

    <div class="module-grid">
      <div class="module-card puzzle" @click="goTo('puzzle')">
        <div class="card-icon">🧩</div>
        <div class="card-title">物理拼图</div>
        <div class="card-desc">拼出物理公式的完整图像</div>
        <div class="card-status" v-if="puzzleStats">{{ puzzleStats.newCount || 0 }}个新题</div>
        <div class="card-particle"></div>
      </div>

      <div class="module-card quiz" @click="goTo('quiz')">
        <div class="card-icon">💡</div>
        <div class="card-title">趣味问答</div>
        <div class="card-desc">测试你的物理直觉</div>
        <div class="card-status">每日5题</div>
        <div class="card-badge" v-if="dailyChallengeAvailable">今日挑战</div>
        <div class="card-particle"></div>
      </div>

      <div class="module-card moments" @click="goTo('moments')">
        <div class="card-icon">🧠</div>
        <div class="card-title">物理学家朋友圈</div>
        <div class="card-desc">看看科学家们的动态</div>
        <div class="card-status" v-if="momentsStats">今日更新</div>
        <div class="card-particle"></div>
      </div>

      <div class="module-card art" @click="goTo('text2image')">
        <div class="card-icon">🎨</div>
        <div class="card-title">AI画室</div>
        <div class="card-desc">用AI生成物理创意图片</div>

        <div class="card-particle"></div>
      </div>
    </div>

    <div class="hot-section">
      <div class="section-header">
        <span class="section-icon">🔥</span>
        <span class="section-title">热门动态</span>
      </div>
      <div class="hot-grid" v-if="hotList.length > 0">
        <div class="hot-card" v-for="item in hotList" :key="item.id" @click="goToDetail(item)">
          <div class="hot-image" :style="{ backgroundImage: 'url(' + getHotImageUrl(item) + ')' }"></div>
          <div class="item-overlay">
            <div class="overlay-stats">
              <span class="stat-item">❤️ {{ item.likesCount || 0 }}</span>
            </div>
            <div class="overlay-action">{{ truncateText(item.prompt, 20) }}</div>
          </div>
          <div class="hot-type-tag">🎨 AI画作</div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <div class="empty-icon">📭</div>
        <div class="empty-text">暂无热门动态，快来参与吧！</div>
      </div>
    </div>

    <div class="loading-overlay" v-if="loading">
      <div class="atom-spinner">
        <div class="electron-orbit"></div>
        <div class="nucleus"></div>
      </div>
      <div class="loading-text">加载中...</div>
    </div>
  </div>
</template>

<script>
import { getFunPhysicsRecommendMoments, getFunPhysicsPublicImages } from '@/api/api'

export default {
  name: 'FunPhysics',
  data() {
    return {
      loading: false,
      dailyChallengeAvailable: true,

      puzzleStats: null,
      momentsStats: null,
      recommendMoments: null,
      hotList: [],
      defaultAvatar: 'https://via.placeholder.com/50',
      defaultImage: 'https://via.placeholder.com/200'
    }
  },
  created() {
    this.initData()
  },
  methods: {
    formatAvatarUrl(url) {
      if (!url) return this.defaultAvatar
      if (url.startsWith('http')) return url
      if (url.startsWith('/images/')) return url
      return this.$store.state.HOST + url
    },
    getHotImageUrl(item) {
      if (item.localPath) {
        const baseUrl = this.$store.state.HOST || 'http://localhost:8080'
        return baseUrl + item.localPath
      }
      return item.imageUrl || this.defaultImage
    },
    async initData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadRecommend(),
          this.loadHotList()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    loadRecommend() {
      return getFunPhysicsRecommendMoments().then(res => {
        if ((res.code === 1000 || res.code === 200) && res.data) {
          this.recommendMoments = res.data
        }
      }).catch(() => {})
    },
    loadHotList() {
      return getFunPhysicsPublicImages({ count: 4 }).then(res => {
        if (res.code === 1000 || res.code === 200) {
          this.hotList = res.data || []
        }
      }).catch(() => {})
    },
    goTo(module) {
      console.log('点击模块:', module)
      const routes = {
        'quiz': '/funphysics/quiz',
        'moments': '/funphysics/moments',
        'puzzle': '/funphysics/puzzle',
        'text2image': '/funphysics/text2image',
        'home': '/funphysics'
      }
      if (routes[module]) {
        console.log('跳转到:', routes[module])
        this.$router.push(routes[module])
      }
    },
    goHome() {
      this.$router.push('/')
    },
    goToMomentsDetail(id) {
      this.$router.push('/funphysics/moments')
    },
    goToDetail(item) {
      if (item.type === 'image') {
        this.$router.push('/funphysics/text2image')
      } else if (item.type === 'puzzle') {
        this.$router.push('/funphysics/puzzle')
      }
    },
    truncateText(text, length) {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return date.toLocaleDateString('zh-CN')
    },
    getTypeLabel(type) {
      const labels = {
        'puzzle': '🧩 拼图',
        'image': '🎨 文生图',
        'moments': '📱 朋友圈',
        'quiz': '❓ 答题'
      }
      return labels[type] || '📌'
    }
  }
}
</script>

<style scoped>
.fun-physics-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #121212 0%, #1a1a2e 100%);
  padding: 20px;
  padding-bottom: 100px;
  position: relative;
  overflow: hidden;
}

.star-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, #eee, rgba(0,0,0,0)),
    radial-gradient(2px 2px at 40px 70px, #fff, rgba(0,0,0,0)),
    radial-gradient(2px 2px at 50px 160px, #ddd, rgba(0,0,0,0)),
    radial-gradient(2px 2px at 90px 40px, #fff, rgba(0,0,0,0)),
    radial-gradient(2px 2px at 130px 80px, #fff, rgba(0,0,0,0)),
    radial-gradient(2px 2px at 160px 120px, #ddd, rgba(0,0,0,0));
  background-repeat: repeat;
  background-size: 200px 200px;
  animation: twinkle 5s ease-in-out infinite;
  opacity: 0.1;
  z-index: 0;
  pointer-events: none;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.1; }
  50% { opacity: 0.2; }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 12px;
  position: relative;
  z-index: 1;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.logo-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.points-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #2D5BFF 0%, #FF9100 100%);
  border-radius: 20px;
  color: white;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(45, 91, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.points-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(45, 91, 255, 0.4);
}

.points-icon {
  margin-right: 6px;
  font-size: 16px;
  animation: sparkle 1.5s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.back-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.2) 0%, rgba(0, 230, 118, 0.2) 100%);
  border: 1px solid rgba(45, 91, 255, 0.3);
  border-radius: 25px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.back-home-btn:hover {
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.3) 0%, rgba(0, 230, 118, 0.3) 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(45, 91, 255, 0.3);
}

.back-icon {
  font-size: 18px;
}

.back-text {
  letter-spacing: 0.5px;
}

.today-recommend {
  margin-bottom: 32px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.recommend-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.recommend-icon {
  font-size: 20px;
  margin-right: 8px;
}

.recommend-title {
  font-size: 18px;
  font-weight: 600;
  color: #2D5BFF;
}

.recommend-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.1) 0%, rgba(0, 230, 118, 0.1) 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(45, 91, 255, 0.2);
}

.recommend-card:hover {
  transform: translateY(-2px) rotateX(2deg);
  box-shadow: 0 8px 24px rgba(45, 91, 255, 0.2);
}

.recommend-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  border: 2px solid #2D5BFF;
  box-shadow: 0 0 10px rgba(45, 91, 255, 0.3);
}

.recommend-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-content {
  flex: 1;
}

.recommend-name {
  font-weight: 600;
  color: #2D5BFF;
  margin-bottom: 4px;
}

.recommend-text {
  color: #b0b0b0;
  font-size: 14px;
  margin-bottom: 8px;
}

.recommend-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #8C8C8C;
}

.enter-hint {
  color: #00E676;
  font-weight: 500;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.module-card {
  position: relative;
  padding: 24px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.module-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #2D5BFF 0%, #00E676 100%);
}

.module-card.puzzle::before {
  background: linear-gradient(90deg, #2D5BFF 0%, #5B8FF9 100%);
}

.module-card.quiz::before {
  background: linear-gradient(90deg, #FF9100 0%, #FFC53D 100%);
}

.module-card.moments::before {
  background: linear-gradient(90deg, #FF7AA2 0%, #FFA940 100%);
}

.module-card.art::before {
  background: linear-gradient(90deg, #00E676 0%, #5BD974 100%);
}

.module-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 32px rgba(45, 91, 255, 0.2);
}

.module-card:hover .card-particle {
  opacity: 1;
  transform: scale(1);
}

.card-icon {
  font-size: 36px;
  margin-bottom: 12px;
  text-align: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8px;
  text-align: center;
}

.card-desc {
  font-size: 12px;
  color: #b0b0b0;
  margin-bottom: 12px;
  text-align: center;
}

.card-status {
  font-size: 11px;
  color: #00E676;
  background: rgba(0, 230, 118, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
  display: inline-block;
  text-align: center;
  width: 100%;
}

.card-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 10px;
  color: #FF9100;
  background: rgba(255, 145, 0, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
}

.card-particle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(45, 91, 255, 0.2) 0%, transparent 70%);
  opacity: 0;
  transition: all 0.3s ease;
  pointer-events: none;
}

.hot-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 20px;
  margin-right: 8px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2D5BFF;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.hot-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 1;
  transition: all 0.3s ease;
}

.hot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 91, 255, 0.15);
}

.hot-card:hover .item-overlay {
  opacity: 1;
}

.hot-image {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: absolute;
  top: 0;
  left: 0;
}

.hot-type-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  z-index: 2;
}

.item-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.overlay-stats {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
}

.stat-item {
  font-size: 11px;
  color: #fff;
}

.overlay-action {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #8C8C8C;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.progress-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.progress-card {
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.1) 0%, rgba(0, 230, 118, 0.1) 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(45, 91, 255, 0.2);
}

.progress-points {
  font-size: 16px;
  color: #2D5BFF;
  margin-bottom: 16px;
  text-align: center;
}

.highlight {
  color: #FF9100;
  font-weight: 600;
  font-size: 18px;
  text-shadow: 0 0 10px rgba(255, 145, 0, 0.3);
}

.badge-wall {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0.4;
  transition: all 0.3s ease;
}

.badge-item.earned {
  opacity: 1;
  transform: scale(1.1);
}

.badge-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.badge-name {
  font-size: 11px;
  color: #b0b0b0;
}

.view-all-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 14px;
}

.view-all-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 91, 255, 0.3);
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  background: rgba(18, 18, 18, 0.95);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  z-index: 1000;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #8C8C8C;
}

.nav-item.active {
  color: #2D5BFF;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-label {
  font-size: 11px;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(18, 18, 18, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.atom-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
}

.electron-orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 60px;
  height: 60px;
  border: 2px solid rgba(45, 91, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: orbit 1s linear infinite;
}

.electron-orbit::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #2D5BFF;
  border-radius: 50%;
  transform: translateX(-50%);
  box-shadow: 0 0 10px #2D5BFF;
}

@keyframes orbit {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

.nucleus {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 12px;
  height: 12px;
  background: #FF9100;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #FF9100;
  animation: pulse 1s ease-in-out infinite;
}

.loading-text {
  color: #2D5BFF;
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .fun-physics-container {
    padding: 12px;
  }
  
  .module-grid {
    grid-template-columns: 1fr;
  }
  
  .hot-grid {
    grid-template-columns: 1fr;
  }
  
  .logo-title {
    font-size: 20px;
  }
  
  .recommend-card {
    flex-direction: column;
    text-align: center;
  }
  
  .recommend-avatar {
    margin-right: 0;
    margin-bottom: 12px;
  }
}

/* 排行榜弹窗样式 */
.ranking-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
}

.ranking-modal .modal-content {
  position: relative;
  width: 90%;
  max-width: 400px;
  max-height: 70vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 20px;
  border: 1px solid rgba(45, 91, 255, 0.3);
  overflow: hidden;
}

.ranking-modal .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.ranking-modal .modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: 18px;
}

.ranking-modal .close-btn {
  background: none;
  border: none;
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.ranking-modal .close-btn:hover {
  opacity: 1;
}

.ranking-list {
  padding: 15px;
  max-height: 50vh;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.rank-badge {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-right: 12px;
}

.rank-badge.rank-1, .rank-badge.rank-2, .rank-badge.rank-3 {
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 14px;
  margin-right: 10px;
}

.user-details {
  flex: 1;
}

.user-level {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.user-points {
  color: #8C8C8C;
  font-size: 12px;
}

.level-badge {
  padding: 4px 10px;
  background: rgba(45, 91, 255, 0.2);
  border: 1px solid rgba(45, 91, 255, 0.3);
  border-radius: 12px;
  color: #2D5BFF;
  font-size: 12px;
  font-weight: 500;
}

.empty-ranking {
  text-align: center;
  padding: 40px;
  color: #8C8C8C;
}
</style>
