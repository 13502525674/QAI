<template>
  <div class="moments-container">
    <div class="star-background"></div>
    
    <div class="moments-header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
      </button>
      <div class="header-title">
        <h2>🧠 物理学家朋友圈</h2>
        <span class="moments-tag">看看科学家们的动态</span>
      </div>
      <div class="refresh-btn" @click="refreshMoments">
        <span class="refresh-icon">🔄</span>
      </div>
    </div>

    <div class="moments-list" v-if="momentsList.length > 0">
      <div class="moments-card" v-for="moments in momentsList" :key="moments.id">
        <div class="moments-header-info">
          <div class="avatar-wrapper">
            <img :src="formatAvatarUrl(moments.physicistAvatar)" class="avatar" />
            <div class="avatar-glow"></div>
          </div>
          <div class="header-text">
            <div class="physicist-name">{{ moments.physicistName }}</div>
            <div class="physicist-title">{{ moments.physicistTitle }}</div>
            <div class="publish-info">
              <span class="location" v-if="moments.location">📍 {{ moments.location }}</span>
              <span class="time">{{ formatTime(moments.publishTime) }}</span>
            </div>
          </div>
        </div>

        <div class="moments-content">
          {{ moments.content }}
        </div>

        <div class="moments-images" v-if="moments.images && parseImages(moments.images).length > 0">
          <div :class="['images-grid', 'images-' + Math.min(parseImages(moments.images).length, 3)]">
            <div class="image-item" v-for="(img, index) in parseImages(moments.images).slice(0, 3)" :key="index">
              <img :src="img" @click="previewImage(img)" @error="handleImageError" />
            </div>
          </div>
        </div>

        <div class="knowledge-tags" v-if="moments.knowledgeTags">
          <span class="tag" v-for="tag in parseTags(moments.knowledgeTags)" :key="tag">
            #{{ tag }}
          </span>
        </div>

        <div class="moments-actions">
          <button class="action-btn like-btn" :class="{ 'liked': moments.isLiked }" @click="toggleLike(moments)">
            <span class="btn-icon">{{ moments.isLiked ? '❤️' : '🤍' }}</span>
            <span class="btn-count">{{ moments.likesCount || 0 }}</span>
          </button>
          <button class="action-btn comment-btn" @click="showComments(moments)">
            <span class="btn-icon">💬</span>
            <span class="btn-text">评论</span>
          </button>
          <button class="action-btn share-btn" @click="shareMoments(moments)">
            <span class="btn-icon">📤</span>
            <span class="btn-text">分享</span>
          </button>
        </div>

        <div class="comments-section" v-if="moments.showComments">
          <div class="comments-list" v-if="moments.comments.length > 0">
            <div class="comment-item" v-for="comment in moments.comments" :key="comment.id" :class="{ 'ai-reply': comment.isAiReply }">
              <img :src="formatCommentAvatar(comment)" class="comment-avatar" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-user">
                    {{ comment.userName || comment.commenterName }}
                  </span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
              </div>
            </div>
          </div>
          <div class="empty-comments" v-else>
            <span class="empty-icon">💭</span>
            <span class="empty-text">还没有评论，快来抢沙发吧！</span>
          </div>
          <div class="comment-input">
            <input 
              v-model="moments.newComment" 
              placeholder="写下你的评论..."
              @keyup.enter="submitComment(moments)"
              class="comment-field"
            />
            <button 
              class="send-btn" 
              @click="submitComment(moments)" 
              :disabled="!moments.newComment.trim()"
            >
              <span class="send-icon">📨</span>
              <span>发送</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <div class="empty-icon">🌌</div>
      <div class="empty-text">暂时没有动态</div>
      <div class="empty-hint">物理学家们正在思考宇宙奥秘...</div>
    </div>

    <div class="load-more" v-if="hasMore && momentsList.length > 0">
      <button class="load-more-btn" @click="loadMore">
        <span class="btn-icon">⬇️</span>
        <span>加载更多</span>
      </button>
    </div>
  </div>
</template>

<script>
import { getFunPhysicsMomentsList, likeFunPhysicsMoments, getFunPhysicsMomentsComments, addFunPhysicsMomentsComment } from '@/api/api'

export default {
  name: 'FunPhysicsMoments',
  data() {
    return {
      momentsList: [],
      page: 1,
      hasMore: true,
      defaultAvatar: 'https://via.placeholder.com/50'
    }
  },
  created() {
    this.loadMoments()
  },
  methods: {
    formatAvatarUrl(url) {
      if (!url) return this.defaultAvatar
      if (url.startsWith('http')) return url
      if (url.startsWith('/images/')) return url
      return this.$store.state.HOST + url
    },
    formatCommentAvatar(comment) {
      const avatar = comment.avatar || comment.commenterAvatar
      if (!avatar) return this.defaultAvatar
      if (avatar.startsWith('http')) return avatar
      if (avatar.startsWith('/images/')) return avatar
      return this.$store.state.HOST + avatar
    },
    loadMoments() {
      console.log('加载朋友圈数据...')
      getFunPhysicsMomentsList({ count: 10 }).then(res => {
        console.log('朋友圈数据响应:', res)
        if (res.code === 1000 && res.data) {
          const list = Array.isArray(res.data) ? res.data : (res.data.records || [])
          this.momentsList = list.map(m => ({
            ...m,
            showComments: false,
            comments: [],
            newComment: ''
          }))
        }
      }).catch(err => {
        console.error('加载朋友圈失败:', err)
        alert('加载朋友圈失败，请检查后端服务是否启动')
      })
    },
    refreshMoments() {
      this.page = 1
      this.hasMore = true
      this.loadMoments()
    },
    loadMore() {
      this.page++
      getFunPhysicsMomentsList({ count: 10 }).then(res => {
        if (res.code === 1000 && res.data) {
          const list = Array.isArray(res.data) ? res.data : (res.data.records || [])
          if (list.length < 10) {
            this.hasMore = false
          }
          this.momentsList.push(...list.map(m => ({
            ...m,
            showComments: false,
            comments: [],
            newComment: ''
          })))
        }
      })
    },
    toggleLike(moments) {
      likeFunPhysicsMoments({ momentsId: moments.id }).then(res => {
        if (res.code === 1000) {
          moments.isLiked = res.data
          moments.likesCount = moments.isLiked 
            ? moments.likesCount + 1 
            : Math.max(0, moments.likesCount - 1)
        }
      }).catch(() => {})
    },
    showComments(moments) {
      moments.showComments = !moments.showComments
      if (moments.showComments && moments.comments.length === 0) {
        getFunPhysicsMomentsComments({ momentsId: moments.id }).then(res => {
          if (res.code === 1000) {
            moments.comments = res.data || []
          }
        }).catch(() => {})
      }
    },
    submitComment(moments) {
      console.log('submitComment被调用', moments.newComment)
      if (!moments.newComment.trim()) {
        console.log('评论内容为空，跳过提交')
        return
      }
      
      const commentContent = moments.newComment.trim()
      
      // 乐观更新：立即显示用户评论
      const tempCommentId = 'temp_' + Date.now()
      const tempComment = {
        id: tempCommentId,
        content: commentContent,
        userName: '我',
        commenterName: '我',
        avatar: '/images/physicists/光头强.png',
        commenterAvatar: '/images/physicists/光头强.png',
        createTime: new Date().toISOString(),
        isAiReply: 0,
        isTemp: true  // 标记为临时评论
      }
      
      // 立即添加到评论列表
      moments.comments.push(tempComment)
      console.log('乐观更新：用户评论已立即显示')
      
      // 清空输入框
      moments.newComment = ''
      
      // 发送API请求
      console.log('准备提交评论:', {
        momentsId: moments.id,
        content: commentContent
      })
      
      addFunPhysicsMomentsComment({
        momentsId: moments.id,
        content: commentContent
      }).then(res => {
        console.log('评论提交响应:', res)
        if (res.code === 1000) {
          // 用服务器返回的真实数据替换临时评论
          const index = moments.comments.findIndex(c => c.id === tempCommentId)
          if (index !== -1) {
            const realComment = res.data
            realComment.avatar = '/images/physicists/光头强.png'
            moments.comments.splice(index, 1, realComment)
            console.log('用户评论数据已更新为真实数据')
          }
          
          console.log('评论提交成功，开始获取AI回复')
          
          // 使用轮询方式快速获取AI回复
          this.pollAiReply(moments, 0)
        } else {
          console.error('评论提交失败，错误码:', res.code)
          // 移除临时评论
          const index = moments.comments.findIndex(c => c.id === tempCommentId)
          if (index !== -1) {
            moments.comments.splice(index, 1)
          }
          alert('评论失败: ' + (res.msg || '未知错误'))
          // 恢复输入框内容
          moments.newComment = commentContent
        }
      }).catch(err => {
        console.error('评论提交异常:', err)
        // 移除临时评论
        const index = moments.comments.findIndex(c => c.id === tempCommentId)
        if (index !== -1) {
          moments.comments.splice(index, 1)
        }
        alert('评论失败，请重试')
        // 恢复输入框内容
        moments.newComment = commentContent
      })
    },
    // 轮询获取AI回复
    pollAiReply(moments, retryCount) {
      const maxRetries = 5 // 最多重试5次
      const baseDelay = 800 // 初始延迟800ms
      
      if (retryCount >= maxRetries) {
        console.log('已达到最大重试次数，停止轮询')
        return
      }
      
      // 计算延迟时间：第一次800ms，之后每次增加400ms
      const delay = baseDelay + (retryCount * 400)
      
      setTimeout(() => {
        getFunPhysicsMomentsComments({ momentsId: moments.id }).then(commentsRes => {
          if (commentsRes.code === 1000) {
            const newComments = commentsRes.data || []
            const aiReply = newComments.find(c => c.isAiReply === 1 && 
              !moments.comments.some(existing => existing.id === c.id))
            
            if (aiReply) {
              console.log(`AI回复已生成（第${retryCount + 1}次尝试），追加到列表:`, aiReply)
              moments.comments.push(aiReply)
            } else {
              // AI还没回复，继续轮询
              console.log(`第${retryCount + 1}次尝试未获取到AI回复，继续轮询...`)
              this.pollAiReply(moments, retryCount + 1)
            }
          }
        }).catch(err => {
          console.error('获取AI回复失败:', err)
          // 出错也继续轮询
          this.pollAiReply(moments, retryCount + 1)
        })
      }, delay)
    },
    shareMoments(moments) {
      if (navigator.share) {
        navigator.share({
          title: `${moments.physicistName}的动态`,
          text: moments.content
        }).catch(() => {})
      } else {
        alert('已复制分享链接')
      }
    },
    parseTags(tags) {
      if (!tags) return []
      return tags.split(',').filter(t => t.trim())
    },
    parseImages(images) {
      if (!images) return []
      return images.split(',').map(img => {
        const trimmed = img.trim()
        if (trimmed) {
          if (trimmed.startsWith('http') || trimmed.startsWith('/images/')) {
            return trimmed
          }
          return this.$store.state.HOST + trimmed
        }
        return null
      }).filter(img => img)
    },
    previewImage(img) {
      window.open(img, '_blank')
    },
    handleImageError(e) {
      e.target.src = 'https://via.placeholder.com/300x200?text=Image'
    },
    formatTime(time) {
      if (!time) return ''
      if (typeof time === 'string' && time.match(/^\d+年/)) {
        return time
      }
      const date = new Date(time)
      if (isNaN(date.getTime())) {
        return time
      }
      return date.toLocaleDateString('zh-CN')
    },
    goBack() {
      this.$router.push('/funphysics')
    }
  }
}
</script>

<style scoped>
.moments-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 50%, #0f0f23 100%);
  position: relative;
  overflow: hidden;
}

.star-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.star-background::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, rgba(255,255,255,0.8), transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 90px 40px, rgba(255,255,255,0.9), transparent),
    radial-gradient(2px 2px at 130px 80px, rgba(255,255,255,0.5), transparent),
    radial-gradient(1px 1px at 160px 120px, rgba(255,255,255,0.7), transparent);
  background-repeat: repeat;
  background-size: 200px 150px;
  animation: twinkle 4s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.moments-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 10;
}

.back-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(-2px);
}

.back-icon {
  font-size: 18px;
}

.header-title {
  flex: 1;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.moments-tag {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
  display: block;
}

.refresh-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: rotate(180deg);
}

.refresh-icon {
  font-size: 18px;
}

.moments-list {
  padding: 20px;
  position: relative;
  z-index: 1;
  max-width: 600px;
  margin: 0 auto;
}

.moments-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.moments-card:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.12);
}

.moments-header-info {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(99, 102, 241, 0.3);
}

.avatar-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
  pointer-events: none;
}

.header-text {
  flex: 1;
}

.physicist-name {
  font-weight: 600;
  color: #fff;
  font-size: 16px;
  margin-bottom: 2px;
}

.physicist-title {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  margin-bottom: 4px;
}

.publish-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.location {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time {
  display: flex;
  align-items: center;
}

.moments-content {
  font-size: 15px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
}

.moments-images {
  margin-bottom: 16px;
}

.images-grid {
  display: grid;
  gap: 8px;
  border-radius: 12px;
  overflow: hidden;
}

.images-grid.images-1 {
  grid-template-columns: 1fr;
  max-width: 280px;
}

.images-grid.images-2 {
  grid-template-columns: repeat(2, 1fr);
}

.images-grid.images-3 {
  grid-template-columns: repeat(3, 1fr);
}

.image-item {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-item:hover img {
  transform: scale(1.05);
}

.knowledge-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag {
  padding: 4px 12px;
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 12px;
  color: #a5b4fc;
  font-size: 13px;
}

.moments-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.7);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.08);
}

.btn-icon {
  font-size: 16px;
}

.btn-count {
  font-size: 14px;
  font-weight: 500;
}

.btn-text {
  font-size: 14px;
}

.like-btn.liked {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.comments-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.comments-list {
  margin-bottom: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-item.ai-reply {
  background: rgba(99, 102, 241, 0.05);
  border-radius: 12px;
  padding: 8px;
  margin-left: -8px;
  margin-right: -8px;
}

.comment-item.ai-reply .comment-content {
  border-color: rgba(99, 102, 241, 0.2);
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.comment-content {
  flex: 1;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 12px;
  border-radius: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.comment-user {
  font-weight: 500;
  color: #a5b4fc;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-badge {
  font-size: 10px;
  padding: 2px 6px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 8px;
  color: #fff;
  font-weight: 600;
}

.comment-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

.comment-text {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  line-height: 1.5;
}

.empty-comments {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  margin-bottom: 16px;
}

.empty-comments .empty-icon {
  font-size: 18px;
}

.empty-comments .empty-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.comment-input {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.comment-field {
  flex: 1;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.comment-field::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.comment-field:focus {
  border-color: rgba(99, 102, 241, 0.5);
}

.send-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-icon {
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  position: relative;
  z-index: 1;
}

.empty-state .empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state .empty-text {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.empty-state .empty-hint {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
}

.load-more {
  text-align: center;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.load-more-btn {
  padding: 14px 32px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 auto;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.load-more-btn .btn-icon {
  font-size: 16px;
}
</style>
