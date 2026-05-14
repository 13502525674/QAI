<template>
  <div class="text2image-container">
    <div class="star-background"></div>
    
    <div class="t2i-header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
      </button>
      <div class="header-title">
        <h2>🎨 AI画室</h2>
        <span class="studio-tag">用AI生成物理创意图片</span>
      </div>
    </div>

    <div class="t2i-content">
      <div class="input-section">
        <div class="section-header">
          <div class="header-icon">✨</div>
          <h3>描述你的物理创意</h3>
          <p class="section-desc">用文字描述你想象的物理场景，AI将为你生成精美图片</p>
        </div>
        
        <div class="prompt-input-wrapper">
          <textarea 
            v-model="prompt" 
            placeholder="例如：牛顿发现万有引力定律，苹果从树上掉落"
            rows="4"
            class="prompt-textarea"
          ></textarea>
          <div class="prompt-char-count">{{ prompt.length }}/200</div>
        </div>
        
        <div class="style-section">
          <div class="style-label">
            <span>🎭 选择风格</span>
          </div>
          <div class="style-grid">
            <div 
              v-for="s in styles" 
              :key="s.value"
              class="style-card"
              :class="{ 'selected': selectedStyle === s.value }"
              @click="selectedStyle = s.value"
            >
              <div class="style-icon">{{ s.icon }}</div>
              <div class="style-name">{{ s.label }}</div>
              <div class="style-desc">{{ s.desc }}</div>
            </div>
          </div>
        </div>

        <div class="suggestions-section">
          <div class="suggestions-label">
            <span>💡 灵感词库</span>
            <span class="suggestions-hint">点击AI生成提示词</span>
          </div>
          <div class="suggestions-list">
            <div 
              v-for="(s, index) in suggestions" 
              :key="index"
              class="suggestion-card"
              @click="generatePromptFromIdea(s, index)"
            >
              <div class="suggestion-icon">{{ s.icon }}</div>
              <div class="suggestion-text">{{ s.text }}</div>
              <div class="suggestion-arrow" v-if="generatingPromptIndex !== index">→</div>
              <div class="suggestion-loading" v-else>
                <span class="loading-spinner"></span>
              </div>
            </div>
          </div>
        </div>

        <button 
          class="generate-btn" 
          @click="generateImage"
          :disabled="!prompt.trim() || generating"
        >
          <span v-if="generating" class="btn-spinner"></span>
          <span class="btn-icon">{{ generating ? '' : '🚀' }}</span>
          <span class="btn-text">{{ generating ? '生成中...' : '开始创作' }}</span>
        </button>
      </div>

      <div class="result-section" v-if="generatedImage">
        <div class="result-header">
          <span class="result-icon">🎉</span>
          <h3>创作完成</h3>
        </div>
        
        <div class="image-preview-wrapper">
          <div class="image-frame">
            <img :src="generatedImage.imageUrl" alt="Generated Image" class="generated-image" />
            <div class="image-glow"></div>
          </div>
        </div>
        
        <div class="image-details">
          <div class="detail-item">
            <span class="detail-label">原始提示词</span>
            <span class="detail-value">{{ generatedImage.originalPrompt }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">AI增强</span>
            <span class="detail-value">{{ generatedImage.enhancedPrompt }}</span>
          </div>
        </div>
        
        <div class="image-actions">
          <div class="saved-badge">
            <span class="saved-icon">✅</span>
            <span class="saved-text">已保存到画廊</span>
          </div>
          <button 
            class="action-btn share-btn"
            @click="setPublic"
          >
            <span class="btn-icon">{{ isPublic ? '🔒' : '🌐' }}</span>
            <span class="btn-text">{{ isPublic ? '设为私密' : '设为公开' }}</span>
          </button>
        </div>
      </div>

      <div class="gallery-section">
        <div class="gallery-header">
          <div class="header-left">
            <span class="gallery-icon">🖼️</span>
            <h3>我的画廊</h3>
          </div>
          <span class="gallery-count">{{ myImages.length }}幅作品</span>
        </div>
        <div class="gallery-grid" v-if="myImages.length > 0">
          <div 
            v-for="img in myImages" 
            :key="img.id" 
            class="gallery-item"
            @click="viewImage(img)"
          >
            <img :src="getImageUrl(img)" />
            <button class="delete-btn" @click.stop="deleteMyImage(img)">
              <span class="delete-icon">×</span>
            </button>
            <div class="item-overlay">
              <div class="overlay-stats">
                <span class="stat-item">❤️ {{ img.likesCount }}</span>
              </div>
              <div class="overlay-action">查看详情</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-gallery">
          <div class="empty-icon">🎨</div>
          <div class="empty-text">还没有创作的作品</div>
          <div class="empty-hint">快来用AI创作你的第一幅物理画作吧！</div>
        </div>
      </div>

      <div class="hot-gallery">
        <div class="gallery-header">
          <div class="header-left">
            <span class="gallery-icon">🔥</span>
            <h3>热门作品</h3>
          </div>
          <span class="gallery-badge">精选</span>
        </div>
        <div class="gallery-grid" v-if="hotImages.length > 0">
          <div 
            v-for="img in hotImages" 
            :key="img.id" 
            class="gallery-item"
            @click="viewImage(img)"
          >
            <img :src="getImageUrl(img)" />
            <div class="item-overlay">
              <div class="overlay-stats">
                <span class="stat-item">❤️ {{ img.likesCount }}</span>
                <span class="stat-item">👤 {{ img.userName }}</span>
              </div>
              <div class="overlay-action">查看详情</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="image-modal" v-if="viewingImage" @click="viewingImage = null">
      <div class="modal-backdrop"></div>
      <div class="modal-content" @click.stop>
        <button class="close-btn" @click="viewingImage = null">
          <span class="close-icon">×</span>
        </button>
        
        <div class="modal-image-wrapper">
          <img :src="getImageUrl(viewingImage)" class="modal-image" />
          <div class="modal-glow"></div>
        </div>
        
        <div class="modal-info">
          <div class="modal-prompt">{{ viewingImage.prompt }}</div>
          <div class="modal-meta">
            <span class="meta-item">👤 {{ viewingImage.userName }}</span>
            <span class="meta-item">🎭 {{ viewingImage.style }}</span>
            <span class="meta-item" v-if="isOwnImage(viewingImage)">
              {{ viewingImage.isPublic ? '🌐 公开' : '🔒 私密' }}
            </span>
          </div>
        </div>
        
        <div class="modal-actions">
          <button 
            class="modal-like-btn"
            :class="{ 'liked': viewingImage.isLiked }"
            @click="toggleLike(viewingImage)"
          >
            <span class="like-icon">{{ viewingImage.isLiked ? '❤️' : '🤍' }}</span>
            <span class="like-count">{{ viewingImage.likesCount }}</span>
          </button>
          
          <button 
            v-if="isOwnImage(viewingImage)"
            class="modal-privacy-btn"
            @click="togglePrivacy(viewingImage)"
          >
            <span class="btn-icon">{{ viewingImage.isPublic ? '🔒' : '🌐' }}</span>
            <span class="btn-text">{{ viewingImage.isPublic ? '设为私密' : '设为公开' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="confirm-modal" v-if="showDeleteConfirm" @click="cancelDelete">
      <div class="confirm-backdrop"></div>
      <div class="confirm-content" @click.stop>
        <div class="confirm-icon">⚠️</div>
        <h3>确认删除</h3>
        <p>你确定要永久删除这幅物理创意画作吗？此操作无法撤销。</p>
        <div class="confirm-footer">
          <button class="cancel-btn" @click="cancelDelete">取消</button>
          <button class="confirm-btn" @click="confirmDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getFunPhysicsUserImages, getFunPhysicsHotImages, generateFunPhysicsImage, generateFunPhysicsPrompt, updateFunPhysicsImagePublic, likeFunPhysicsImage, removeFunPhysicsImage } from '@/api/api'

export default {
  name: 'FunPhysicsText2Image',
  data() {
    return {
      prompt: '',
      styles: [
        { value: 'cartoon', label: '卡通', icon: '🎨', desc: '可爱活泼的卡通风格' },
        { value: 'realistic', label: '写实', icon: '📷', desc: '真实细腻的写实风格' },
        { value: 'artistic', label: '艺术', icon: '🖼️', desc: '充满艺术感的风格' },
        { value: 'minimalist', label: '极简', icon: '✨', desc: '简洁优雅的极简风格' }
      ],
      selectedStyle: 'cartoon',
      suggestions: [
        { icon: '🍎', text: '牛顿发现万有引力定律' },
        { icon: '🧠', text: '爱因斯坦思考相对论' },
        { icon: '⚛️', text: '量子纠缠现象' },
        { icon: '🌌', text: '黑洞吞噬恒星' },
        { icon: '💡', text: '光的折射实验' },
        { icon: '⚡', text: '闪电形成过程' }
      ],
      generating: false,
      generatingPromptIndex: -1,
      generatedImage: null,
      isPublic: true,
      myImages: [],
      hotImages: [],
      viewingImage: null,
      conversationId: '',
      showDeleteConfirm: false,
      imageToDelete: null
    }
  },
  created() {
    this.conversationId = this.generateConversationId()
    this.loadMyImages()
    this.loadHotImages()
  },
  methods: {
    generateConversationId() {
      return 't2i_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    },
    loadMyImages() {
      getFunPhysicsUserImages().then(res => {
        if (res.code === 1000) {
          this.myImages = res.data || []
        }
      }).catch(() => {})
    },
    loadHotImages() {
      getFunPhysicsHotImages({ count: 8 }).then(res => {
        if (res.code === 1000) {
          this.hotImages = res.data || []
        }
      }).catch(() => {})
    },
    generatePromptFromIdea(idea, index) {
      if (this.generatingPromptIndex !== -1) return
      
      const ideaText = typeof idea === 'string' ? idea : idea.text
      
      this.generatingPromptIndex = index
      generateFunPhysicsPrompt({
        idea: ideaText,
        conversationId: this.conversationId
      }).then(res => {
        if (res.code === 1000) {
          this.prompt = res.data.prompt
          this.conversationId = res.data.conversationId || this.conversationId
        }
      }).catch(() => {
        alert('生成提示词失败，请重试')
      }).finally(() => {
        this.generatingPromptIndex = -1
      })
    },
    generateImage() {
      if (!this.prompt.trim()) return
      
      this.generating = true
      generateFunPhysicsImage({
        prompt: this.prompt,
        style: this.selectedStyle,
        conversationId: this.conversationId
      }).then(res => {
        if (res.code === 1000 || res.code === 200) {
          this.generatedImage = res.data
          this.isPublic = true // 默认公开
          this.conversationId = res.data.conversationId || this.conversationId
          this.loadMyImages()
        }
      }).catch(() => {
        alert('生成图片失败，请重试')
      }).finally(() => {
        this.generating = false
      })
    },
    setPublic() {
      if (!this.generatedImage || !this.generatedImage.id) return
      
      updateFunPhysicsImagePublic({
        imageId: this.generatedImage.id,
        isPublic: this.isPublic ? 0 : 1
      }).then(res => {
        if (res.code === 1000 || res.code === 200) {
          this.isPublic = !this.isPublic
          this.loadHotImages()
          // 同步更新 myImages 中的状态
          this.loadMyImages()
        }
      }).catch(() => {})
    },
    togglePrivacy(img) {
      const newStatus = img.isPublic ? 0 : 1
      updateFunPhysicsImagePublic({
        imageId: img.id,
        isPublic: newStatus
      }).then(res => {
        if (res.code === 1000 || res.code === 200) {
          img.isPublic = newStatus
          this.loadHotImages()
          this.loadMyImages()
        }
      }).catch(() => {
        alert('修改公开状态失败')
      })
    },
    isOwnImage(img) {
      if (!img) return false
      // 通过判断 myImages 中是否存在该 id 来确认所有权
      return this.myImages.some(item => item.id === img.id)
    },
    viewImage(img) {
      this.viewingImage = img
    },
    deleteMyImage(img) {
      this.imageToDelete = img
      this.showDeleteConfirm = true
    },
    confirmDelete() {
      if (!this.imageToDelete) return
      
      const img = this.imageToDelete
      removeFunPhysicsImage({ id: img.id }).then(res => {
        if (res.code === 1000 || res.code === 200) {
          this.loadMyImages()
          this.loadHotImages()
          if (this.generatedImage && this.generatedImage.id === img.id) {
            this.generatedImage = null
          }
          this.showDeleteConfirm = false
          this.imageToDelete = null
        }
      }).catch(() => {
        alert('删除失败，请稍后重试')
        this.showDeleteConfirm = false
        this.imageToDelete = null
      })
    },
    cancelDelete() {
      this.showDeleteConfirm = false
      this.imageToDelete = null
    },
    toggleLike(img) {
      likeFunPhysicsImage({ imageId: img.id }).then(res => {
        if (res.code === 1000) {
          img.isLiked = res.data
          img.likesCount = img.isLiked 
            ? img.likesCount + 1 
            : Math.max(0, img.likesCount - 1)
          // Immediately refresh hot images list
          this.loadHotImages()
        }
      }).catch(() => {})
    },
    goBack() {
      this.$router.push('/funphysics')
    },
    getImageUrl(img) {
      if (img.localPath) {
        // Fallback to hardcoded URL if env var is missing or not working as expected
        const baseUrl = process.env.VUE_APP_BASE_API || 'http://localhost:8080'
        return baseUrl + img.localPath
      }
      return img.imageUrl
    }
  }
}
</script>

<style scoped>
.text2image-container {
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

.t2i-header {
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

.studio-tag {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
  display: block;
}

.t2i-content {
  padding: 20px;
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
}

.input-section {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #fff;
  font-weight: 600;
}

.section-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.prompt-input-wrapper {
  position: relative;
  margin-bottom: 20px;
}

.prompt-textarea {
  width: 100%;
  padding: 16px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 15px;
  resize: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.prompt-textarea::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.prompt-textarea:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.2);
}

.prompt-char-count {
  position: absolute;
  bottom: 12px;
  right: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.style-section {
  margin-bottom: 20px;
}

.style-label {
  margin-bottom: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.style-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.style-card {
  padding: 16px 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.style-card:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateY(-2px);
}

.style-card.selected {
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.5);
}

.style-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.style-name {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
  margin-bottom: 4px;
}

.style-desc {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

.suggestions-section {
  margin-bottom: 24px;
}

.suggestions-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.suggestions-hint {
  font-size: 12px;
  color: rgba(99, 102, 241, 0.8);
}

.suggestions-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.suggestion-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.suggestion-card:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(99, 102, 241, 0.3);
}

.suggestion-icon {
  font-size: 18px;
}

.suggestion-text {
  flex: 1;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-arrow {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.suggestion-loading {
  display: flex;
  align-items: center;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(99, 102, 241, 0.3);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.generate-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.4);
}

.generate-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.btn-icon {
  font-size: 18px;
}

.result-section {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.result-icon {
  font-size: 24px;
}

.result-header h3 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.image-preview-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.image-frame {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
}

.generated-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 16px;
  display: block;
}

.image-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 16px;
  box-shadow: inset 0 0 30px rgba(99, 102, 241, 0.2);
  pointer-events: none;
}

.image-details {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.detail-item {
  margin-bottom: 12px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 4px;
}

.detail-value {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.image-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.share-btn {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.save-btn {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.3) 0%, rgba(5, 150, 105, 0.3) 100%);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.4);
}

.save-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.4) 0%, rgba(5, 150, 105, 0.4) 100%);
}

.saved-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 12px;
  color: #34d399;
  font-size: 14px;
  font-weight: 500;
}

.points-highlight {
  color: #FFD700 !important;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.share-btn:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.3);
}

.gallery-section, .hot-gallery {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
}

.gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.gallery-icon {
  font-size: 20px;
}

.gallery-header h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
  font-weight: 600;
}

.gallery-count {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.gallery-badge {
  padding: 4px 10px;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  font-size: 12px;
  color: #f87171;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.gallery-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 1;
}

.gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
}

.gallery-item:hover .item-overlay {
  opacity: 1;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.8);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  opacity: 0;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
}

.gallery-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: #ef4444;
  transform: scale(1.1);
  box-shadow: 0 0 10px rgba(239, 68, 68, 0.5);
}

.delete-icon {
  font-size: 16px;
  font-weight: bold;
  line-height: 1;
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
}

.empty-gallery {
  text-align: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.5);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
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

.modal-content {
  position: relative;
  background: rgba(26, 26, 46, 0.95);
  border-radius: 20px;
  padding: 24px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 删除确认弹窗样式 */
.confirm-modal {
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

.confirm-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
}

.confirm-content {
  position: relative;
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  animation: modalIn 0.3s ease-out;
}

@keyframes modalIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.confirm-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.confirm-content h3 {
  color: #fff;
  margin: 0 0 12px;
  font-size: 20px;
}

.confirm-content p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 24px;
}

.confirm-footer {
  display: flex;
  gap: 12px;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.confirm-btn {
  flex: 1;
  padding: 12px;
  background: #ef4444;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  background: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(239, 68, 68, 0.3);
}

.delete-btn {
  line-height: 1;
}

.modal-image-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.modal-image {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 16px;
  display: block;
}

.modal-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 16px;
  box-shadow: inset 0 0 30px rgba(99, 102, 241, 0.2);
  pointer-events: none;
}

.modal-info {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.modal-prompt {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 12px;
}

.modal-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.modal-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 10px;
}

.modal-privacy-btn {
  padding: 8px 16px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 20px;
  color: #6366f1;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.modal-privacy-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  transform: translateY(-2px);
}

.modal-like-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 8px 16px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.modal-like-btn:hover {
  background: rgba(239, 68, 68, 0.3);
}

.modal-like-btn.liked {
  background: rgba(239, 68, 68, 0.3);
  border-color: rgba(239, 68, 68, 0.5);
}

.like-icon {
  font-size: 18px;
}

.like-count {
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .t2i-content {
    padding: 12px;
  }
  
  .style-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .suggestions-list {
    grid-template-columns: 1fr;
  }
  
  .gallery-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .header-title h2 {
    font-size: 18px;
  }
}
</style>
