<template>
  <div class="puzzle-container">
    <div class="star-background"></div>
    
    <!-- 自定义Toast提示 -->
    <transition name="toast-fade">
      <div v-if="toast.show" class="custom-toast" :class="toast.type">
        <span class="toast-icon">{{ toast.icon }}</span>
        <span class="toast-message">{{ toast.message }}</span>
      </div>
    </transition>

    <!-- 提示面板 -->
    <transition name="slide-up">
      <div v-if="hintPanel.show" class="hint-panel">
        <div class="hint-content">
          <div class="hint-header">
            <span class="hint-level">💡 提示等级 {{ hintPanel.level }}/3</span>
            <button class="hint-close" @click="hintPanel.show = false">×</button>
          </div>
          <div class="hint-body">
            <p>{{ hintPanel.message }}</p>
            <div v-if="hintPanel.formula" class="hint-formula">
              {{ hintPanel.formula }}
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 玩法说明弹窗 -->
    <transition name="fade">
      <div v-if="showGuide" class="guide-overlay" @click.self="showGuide = false">
        <div class="guide-modal">
          <h3>🎮 {{ selectedType === 0 ? '公式拼图' : '概念拼图' }}玩法说明</h3>
          <div class="guide-content" v-if="selectedType === 0">
            <div class="guide-step">
              <div class="step-num">1</div>
              <p>观察<strong>拼图标题</strong>，根据提示猜出公式</p>
            </div>
            <div class="guide-step">
              <div class="step-num">2</div>
              <p>点击<strong>碎片区</strong>选择一个碎片（选中后会高亮）</p>
            </div>
            <div class="guide-step">
              <div class="step-num">3</div>
              <p>点击<strong>目标区域</strong>的空槽放置碎片</p>
            </div>
            <div class="guide-step">
              <div class="step-num">4</div>
              <p><span class="color-green">绿色边框</span>表示位置正确，<span class="color-red">红色边框</span>表示错误</p>
            </div>
            <div class="guide-tips">
              <p>💡 小技巧：不知道公式？使用提示功能获取帮助（会扣减积分）</p>
            </div>
          </div>
          <div class="guide-content" v-else>
            <div class="guide-step">
              <div class="step-num">1</div>
              <p>观察右侧的<strong>碎片预览</strong>，每个碎片都是原图的一部分</p>
            </div>
            <div class="guide-step">
              <div class="step-num">2</div>
              <p><strong>拖拽碎片</strong>到左侧画布的格子中，凭感觉还原原图</p>
            </div>
            <div class="guide-step">
              <div class="step-num">3</div>
              <p>也可以<strong>点击碎片</strong>选中，再<strong>点击画布格子</strong>放置</p>
            </div>
            <div class="guide-step">
              <div class="step-num">4</div>
              <p><span class="color-green">绿色边框</span>表示位置正确，点击已放置碎片可移除</p>
            </div>
            <div class="guide-tips">
              <p>💡 小技巧：点击"放大"按钮可以查看完整原图参考</p>
            </div>
          </div>
          <button class="guide-btn" @click="showGuide = false">我知道了</button>
        </div>
      </div>
    </transition>

    <div class="puzzle-header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
      </button>
      <div class="header-title">
        <h2>🧩 物理拼图</h2>
        <span class="level-badge" v-if="selectedDifficulty">{{ difficultyText }}</span>
      </div>
      <div class="puzzle-stats" v-if="currentPuzzle">
        <div class="stat-item">
          <span class="stat-icon">⏱️</span>
          <span class="stat-value">{{ formatTime(elapsedTime) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">💡</span>
          <span class="stat-value">剩余 {{ hintCount }} 次</span>
        </div>
      </div>
    </div>

    <!-- 选择界面 -->
    <div class="puzzle-select" v-if="!currentPuzzle">
      <div class="select-card">
        <div class="select-icon">🎯</div>
        <h3>选择难度</h3>
        <p class="select-desc">挑战不同难度的物理拼图</p>
        
        <div class="difficulty-options">
          <button 
            v-for="d in difficulties" 
            :key="d.value"
            class="difficulty-btn"
            :class="{ 'selected': selectedDifficulty === d.value }"
            @click="selectedDifficulty = d.value"
          >
            <div class="difficulty-icon">{{ d.icon }}</div>
            <div class="difficulty-label">{{ d.label }}</div>
          </button>
        </div>

        <div class="type-options" style="margin-top: 20px;">
          <h3>选择模式</h3>
          <div class="difficulty-options">
             <button 
              v-for="t in puzzleTypes" 
              :key="t.value"
              class="difficulty-btn type-btn"
              :class="{ 'selected': selectedType === t.value }"
              @click="selectedType = t.value"
            >
              <div class="difficulty-icon">{{ t.icon }}</div>
              <div class="difficulty-label">{{ t.label }}</div>
              <div class="difficulty-desc">{{ t.desc }}</div>
            </button>
          </div>
        </div>
        
        <button class="start-btn" @click="startPuzzle" :disabled="!selectedDifficulty">
          <span class="btn-icon">🚀</span>
          <span>开始挑战</span>
        </button>
      </div>
    </div>

    <!-- 游戏界面 -->
    <div class="puzzle-game" v-if="currentPuzzle && !gameComplete">
      <div class="puzzle-title-section">
        <div class="title-row">
          <h3>{{ currentPuzzle.title }}</h3>
          <button class="help-btn" @click="showGuide = true">❓ 玩法</button>
        </div>
        <p class="puzzle-desc">{{ currentPuzzle.hint || '将碎片按正确顺序排列' }}</p>
      </div>
      
      <!-- 公式拼图模式：线性排列 -->
      <div class="game-area" v-if="currentPuzzle.type === 0">
        <!-- 目标区域 -->
        <div class="formula-container">
          <div class="formula-label">🎯 目标区域 - 按顺序放置碎片</div>
          <div class="formula-slots">
            <div 
              v-for="(slot, index) in answerSlots" 
              :key="'slot-' + index"
              class="formula-slot puzzle-piece"
              :class="{ 
                'filled': slot !== null,
                'correct': slot !== null && checkSlotCorrect(index),
                'wrong': slot !== null && !checkSlotCorrect(index),
                'drag-over': dragOverIndex === index
              }"
              @click="handleSlotClick(index)"
              @dragover.prevent="dragOverIndex = index"
              @dragleave="dragOverIndex = null"
              @drop="handleDrop(index)"
            >
              <div class="piece-inner">
                <span v-if="slot !== null" class="piece-text">{{ piecesData[slot] }}</span>
                <span v-else class="slot-placeholder">{{ index + 1 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 碎片区域 -->
        <div class="pieces-container">
          <div class="pieces-label">🧩 碎片区域 - 点击或拖拽到目标位置</div>
          <div class="pieces-grid">
            <div 
              v-for="(piece, index) in shuffledPieces" 
              :key="index"
              class="piece-item puzzle-piece"
              :class="{ 
                'used': usedPieces.includes(index),
                'selected': selectedPieceIndex === index
              }"
              draggable="true"
              @click="selectPiece(index)"
              @dragstart="handleDragStart(index, $event)"
              @dragend="selectedPieceIndex = null"
            >
              <div class="piece-inner">
                <div class="piece-label">{{ piecesData[piece] }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 概念拼图模式：网格排列 -->
      <div class="game-area image-mode" v-if="currentPuzzle.type === 1">
        <div class="image-game-layout">
          <!-- 左侧：拼图画布 -->
          <div class="canvas-area">
            <div class="canvas-label">🎯 拼图画布 - 拖拽或点击放置碎片</div>
            <canvas 
              ref="puzzleCanvas" 
              :width="canvasSize" 
              :height="canvasSize"
              @click="handleCanvasClick"
              @dragover.prevent="handleDragOver"
              @drop="handleDrop"
            ></canvas>
            <div class="canvas-progress">
              进度: {{ placedCount }} / {{ totalPieces }}
            </div>
          </div>

          <!-- 右侧：碎片区 + 原图参考 -->
          <div class="right-panel">
            <!-- 碎片预览区 -->
            <div class="pieces-preview-area">
              <div class="preview-label">🧩 碎片预览 - 拖拽到左侧画布</div>
              <div class="pieces-preview-grid" :style="{ gridTemplateColumns: `repeat(${gridSize}, 1fr)` }">
                <div 
                  v-for="(piece, index) in shuffledPieces" 
                  :key="index"
                  class="piece-preview-item"
                  :class="{ 
                    'used': usedPieces.includes(index),
                    'selected': selectedPieceIndex === index
                  }"
                  draggable="true"
                  @click="selectPiece(index)"
                  @dragstart="handlePieceDragStart(index, $event)"
                  @dragend="selectedPieceIndex = null"
                >
                  <canvas 
                    :ref="'pieceCanvas' + index"
                    :width="pieceSize" 
                    :height="pieceSize"
                    class="piece-canvas"
                  ></canvas>
                  <div class="piece-number" v-if="!imageLoaded">{{ piece + 1 }}</div>
                </div>
              </div>
            </div>
            
            <!-- 原图参考 -->
            <div class="original-preview">
              <div class="preview-label">
                📷 原图参考
                <button class="peek-btn" @click="showFullImage = true">🔍 放大</button>
              </div>
              <div class="original-image" v-if="imageLoaded">
                <img :src="currentPuzzle.imageUrl" alt="原图" @click="showFullImage = true">
              </div>
              <div class="no-image-tip" v-else>
                <span>图片加载中...</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="game-actions">
        <button class="action-btn hint-btn" @click="showHint" :disabled="hintCount <= 0">
          <span class="btn-icon">💡</span>
          <span>提示 ({{ hintCount }})</span>
        </button>
        <button class="action-btn undo-btn" @click="undoLastMove" :disabled="moveHistory.length === 0">
          <span class="btn-icon">↩️</span>
          <span>撤销</span>
        </button>
        <button class="action-btn reset-btn" @click="resetPuzzle">
          <span class="btn-icon">🔄</span>
          <span>重置</span>
        </button>
      </div>
    </div>

    <!-- 图片放大查看 -->
    <transition name="fade">
      <div v-if="showFullImage" class="full-image-overlay" @click="showFullImage = false">
        <div class="full-image-container">
          <img v-if="currentPuzzle && currentPuzzle.imageUrl" :src="currentPuzzle.imageUrl" alt="原图">
          <button class="close-btn" @click="showFullImage = false">×</button>
        </div>
      </div>
    </transition>

    <!-- 完成界面 -->
    <div class="puzzle-complete" v-if="gameComplete">
      <div class="complete-overlay">
        <div class="complete-card">
          <div class="complete-icon">🎉</div>
          <h3>拼图完成！</h3>
          <p class="complete-time">用时: {{ formatTime(elapsedTime) }}</p>
          <div class="complete-quote">
            <span class="quote-mark">"</span>
            {{ getPhysicsQuote() }}
            <span class="quote-mark">"</span>
          </div>
          <button class="next-btn" @click="nextPuzzle">
            <span class="btn-icon">➡️</span>
            <span>下一关</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getFunPhysicsPuzzles, startFunPhysicsPuzzle, submitFunPhysicsPuzzle } from '@/api/api'

export default {
  name: 'FunPhysicsPuzzle',
  data() {
    return {
      difficulties: [
        { value: 1, label: '简单', icon: '⭐' },
        { value: 2, label: '中等', icon: '⭐⭐' },
        { value: 3, label: '困难', icon: '⭐⭐⭐' }
      ],
      selectedDifficulty: null,
      selectedType: 0,
      puzzleTypes: [
        { value: 0, label: '公式拼图', icon: '📐', desc: '复原物理公式' },
        { value: 1, label: '概念拼图', icon: '🖼️', desc: '复原物理原理图' }
      ],
      currentPuzzle: null,
      canvasSize: 360,
      pieceSize: 70,
      
      // 公式拼图数据
      piecesData: [],
      correctOrder: [],
      shuffledPieces: [],
      answerSlots: [],
      usedPieces: [],
      selectedPieceIndex: null,
      moveHistory: [],
      dragOverIndex: null,
      
      // 图片拼图数据
      puzzleImage: null,
      imageLoaded: false,
      gridPieces: [],
      showFullImage: false,
      
      // 游戏状态
      elapsedTime: 0,
      timer: null,
      gameComplete: false,
      hintUsed: false,
      hintCount: 3,
      recordId: null,
      startTime: null,
      showGuide: false,
      
      // Toast提示
      toast: {
        show: false,
        message: '',
        type: 'info',
        icon: '💡'
      },
      
      // 提示面板
      hintPanel: {
        show: false,
        level: 0,
        message: '',
        formula: ''
      }
    }
  },
  computed: {
    difficultyText() {
      const d = this.difficulties.find(x => x.value === this.selectedDifficulty)
      return d ? d.label : ''
    },
    gridSize() {
      return this.selectedDifficulty + 2
    },
    placedCount() {
      return this.answerSlots.filter(s => s !== null).length
    },
    totalPieces() {
      return this.piecesData.length
    }
  },
  methods: {
    // ========== Toast提示 ==========
    showToast(message, type = 'info') {
      const icons = {
        info: '💡',
        success: '✅',
        warning: '⚠️',
        error: '❌'
      }
      this.toast = {
        show: true,
        message,
        type,
        icon: icons[type] || '💡'
      }
      setTimeout(() => {
        this.toast.show = false
      }, 2500)
    },
    
    selectDifficulty(value) {
      this.selectedDifficulty = value
    },
    
    startPuzzle() {
      if (!this.selectedDifficulty) {
        this.showToast('请先选择难度', 'warning')
        return
      }
      
      // 首次进入显示玩法说明
      const key = `puzzle_guide_${this.selectedType}`
      if (!localStorage.getItem(key)) {
        this.showGuide = true
        localStorage.setItem(key, 'shown')
      }
      
      getFunPhysicsPuzzles({ count: 1, difficulty: this.selectedDifficulty, type: this.selectedType }).then(res => {
        console.log('拼图数据响应:', res)
        if (res.code === 1000 && res.data && res.data.length > 0) {
          this.currentPuzzle = res.data[0]
          this.initPuzzle()
          this.startGame()
        } else {
          this.showToast('暂无该难度的拼图，请选择其他难度', 'warning')
        }
      }).catch(err => {
        console.error('加载拼图失败:', err)
        this.showToast('加载拼图失败，请检查后端服务', 'error')
      })
    },
    
    initPuzzle() {
      this.hintUsed = false
      this.hintCount = 3
      this.gameComplete = false
      this.elapsedTime = 0
      this.selectedPieceIndex = null
      this.moveHistory = []
      this.usedPieces = []
      this.hintPanel.show = false
      this.dragOverIndex = null
      
      if (this.currentPuzzle.type === 0) {
        this.initFormulaPuzzle()
      } else {
        this.initImagePuzzle()
      }
    },
    
    initFormulaPuzzle() {
      try {
        if (this.currentPuzzle.piecesData) {
          this.piecesData = JSON.parse(this.currentPuzzle.piecesData)
        } else {
          this.piecesData = this.parseFormula(this.currentPuzzle.content || '')
        }
        
        if (this.currentPuzzle.correctOrder) {
          this.correctOrder = JSON.parse(this.currentPuzzle.correctOrder)
        } else {
          this.correctOrder = this.piecesData.map((_, i) => i)
        }
        
        console.log('碎片数据:', this.piecesData)
        console.log('正确顺序:', this.correctOrder)
        
      } catch (e) {
        console.error('解析拼图数据失败:', e)
        this.piecesData = this.parseFormula(this.currentPuzzle.content || 'E=mc²')
        this.correctOrder = this.piecesData.map((_, i) => i)
      }
      
      this.answerSlots = new Array(this.piecesData.length).fill(null)
      this.shuffledPieces = this.shuffleArray([...Array(this.piecesData.length).keys()])
    },
    
    parseFormula(formula) {
      const result = []
      let current = ''
      let i = 0
      
      while (i < formula.length) {
        const char = formula[i]
        
        if (char === '\\') {
          if (current.trim()) result.push(current.trim())
          current = ''
          
          let latexCmd = '\\'
          i++
          while (i < formula.length && /[a-zA-Z]/.test(formula[i])) {
            latexCmd += formula[i]
            i++
          }
          result.push(latexCmd)
          continue
        }
        
        if (['=', '+', '-', '·', '/', '²', '³', '₀', '₁', '₂'].includes(char)) {
          if (current.trim()) result.push(current.trim())
          result.push(char)
          current = ''
          i++
          continue
        }
        
        if (char === ' ') {
          if (current.trim()) result.push(current.trim())
          current = ''
          i++
          continue
        }
        
        current += char
        i++
      }
      
      if (current.trim()) result.push(current.trim())
      
      return result.filter(s => s.length > 0)
    },
    
    initImagePuzzle() {
      const size = this.gridSize
      const total = size * size
      
      this.piecesData = [...Array(total).keys()]
      this.correctOrder = [...Array(total).keys()]
      this.shuffledPieces = this.shuffleArray([...Array(total).keys()])
      this.answerSlots = new Array(total).fill(null)
      
      // 处理图片URL - 确保是完整URL
      let imageUrl = this.currentPuzzle.imageUrl
      if (imageUrl && !imageUrl.startsWith('http')) {
        // 拼接 baseURL
        const baseUrl = this.$store.state.HOST || 'http://localhost:8080'
        imageUrl = baseUrl + imageUrl
        this.currentPuzzle.imageUrl = imageUrl
      }
      
      console.log('拼图图片URL:', imageUrl)
      
      // 尝试加载图片
      this.imageLoaded = false
      if (imageUrl) {
        this.puzzleImage = new Image()
        this.puzzleImage.crossOrigin = "Anonymous"
        this.puzzleImage.src = imageUrl
        this.puzzleImage.onload = () => {
          console.log('图片加载成功!')
          this.imageLoaded = true
          this.$nextTick(() => {
            this.drawPuzzle()
            this.drawPieces()
          })
        }
        this.puzzleImage.onerror = (e) => {
          console.error('图片加载失败:', e)
          this.imageLoaded = false
          this.puzzleImage = null
          // 延迟执行确保DOM已渲染
          setTimeout(() => {
            this.drawPuzzle()
            this.drawNumberPieces()
          }, 100)
        }
      } else {
        // 没有图片URL，使用数字拼图模式
        setTimeout(() => {
          this.drawPuzzle()
          this.drawNumberPieces()
        }, 100)
      }
    },
    
    shuffleArray(array) {
      for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]]
      }
      return array
    },
    
    startGame() {
      this.startTime = new Date()
      
      startFunPhysicsPuzzle({ puzzleId: this.currentPuzzle.id }).then(res => {
        if (res.code === 1000 || res.code === 200) {
          this.recordId = res.data?.recordId || res.data
        }
      }).catch(() => {})
      
      this.timer = setInterval(() => {
        this.elapsedTime++
      }, 1000)
    },
    
    // ========== 公式拼图交互 ==========
    
    selectPiece(index) {
      if (this.usedPieces.includes(index)) return
      this.selectedPieceIndex = index
      this.showToast('已选中碎片，点击目标位置放置', 'info')
    },
    
    handleSlotClick(slotIndex) {
      if (this.selectedPieceIndex === null) {
        // 如果槽位有碎片，点击移除
        if (this.answerSlots[slotIndex] !== null) {
          const pieceIndex = this.answerSlots[slotIndex]
          const shuffleIndex = this.shuffledPieces.indexOf(pieceIndex)
          if (shuffleIndex !== -1) {
            this.usedPieces = this.usedPieces.filter(p => p !== shuffleIndex)
          }
          this.answerSlots[slotIndex] = null
          this.showToast('已移除碎片', 'info')
        }
        return
      }
      
      const pieceIndex = this.shuffledPieces[this.selectedPieceIndex]
      
      if (this.answerSlots[slotIndex] !== null) {
        const oldPiece = this.answerSlots[slotIndex]
        this.usedPieces = this.usedPieces.filter(p => this.shuffledPieces[p] !== oldPiece)
      }
      
      this.moveHistory.push({
        slotIndex,
        oldPiece: this.answerSlots[slotIndex],
        selectedPieceIndex: this.selectedPieceIndex
      })
      
      this.answerSlots[slotIndex] = pieceIndex
      this.usedPieces.push(this.selectedPieceIndex)
      this.selectedPieceIndex = null
      
      this.checkComplete()
    },
    
    handleDragStart(index, event) {
      this.selectedPieceIndex = index
      event.dataTransfer.effectAllowed = 'move'
      event.dataTransfer.setData('text/plain', index)
    },
    
    handleDrop(slotIndex) {
      this.dragOverIndex = null
      this.handleSlotClick(slotIndex)
    },
    
    checkSlotCorrect(index) {
      const placedPiece = this.answerSlots[index]
      return placedPiece === this.correctOrder[index]
    },
    
    undoLastMove() {
      if (this.moveHistory.length === 0) return
      
      const lastMove = this.moveHistory.pop()
      this.answerSlots[lastMove.slotIndex] = lastMove.oldPiece
      
      this.usedPieces = this.usedPieces.filter(p => p !== lastMove.selectedPieceIndex)
      
      if (lastMove.oldPiece !== null) {
        const oldPieceShuffleIndex = this.shuffledPieces.indexOf(lastMove.oldPiece)
        if (oldPieceShuffleIndex !== -1) {
          this.usedPieces.push(oldPieceShuffleIndex)
        }
      }
      
      this.showToast('已撤销上一步操作', 'info')
    },
    
    // ========== 图片拼图交互 ==========
    
    drawPuzzle() {
      const canvas = this.$refs.puzzleCanvas
      if (!canvas) return
      
      const ctx = canvas.getContext('2d')
      const size = this.gridSize
      const cellSize = this.canvasSize / size
      
      ctx.clearRect(0, 0, this.canvasSize, this.canvasSize)
      
      // 绘制背景
      ctx.fillStyle = '#1a1a2e'
      ctx.fillRect(0, 0, this.canvasSize, this.canvasSize)
      
      // 绘制网格
      ctx.strokeStyle = '#2D5BFF'
      ctx.lineWidth = 2
      
      for (let i = 0; i <= size; i++) {
        ctx.beginPath()
        ctx.moveTo(i * cellSize, 0)
        ctx.lineTo(i * cellSize, this.canvasSize)
        ctx.stroke()
        
        ctx.beginPath()
        ctx.moveTo(0, i * cellSize)
        ctx.lineTo(this.canvasSize, i * cellSize)
        ctx.stroke()
      }
      
      // 绘制已放置的碎片
      for (let i = 0; i < this.answerSlots.length; i++) {
        if (this.answerSlots[i] !== null) {
          const pieceIndex = this.answerSlots[i]
          const row = Math.floor(i / size)
          const col = i % size
          const x = col * cellSize
          const y = row * cellSize
          
          // 只有图片加载成功时才绘制图片
          if (this.imageLoaded && this.puzzleImage && this.puzzleImage.complete && this.puzzleImage.naturalWidth > 0) {
            const srcRow = Math.floor(pieceIndex / size)
            const srcCol = pieceIndex % size
            const srcX = srcCol * (this.puzzleImage.width / size)
            const srcY = srcRow * (this.puzzleImage.height / size)
            const srcW = this.puzzleImage.width / size
            const srcH = this.puzzleImage.height / size
            
            ctx.drawImage(this.puzzleImage, srcX, srcY, srcW, srcH, x, y, cellSize, cellSize)
            
            // 绘制边框
            ctx.strokeStyle = pieceIndex === i ? '#00E676' : '#2D5BFF'
            ctx.lineWidth = 3
            ctx.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2)
          } else {
            // 无图片时显示数字（数字拼图模式）
            const isCorrect = pieceIndex === i
            ctx.fillStyle = isCorrect ? 'rgba(0, 230, 118, 0.3)' : 'rgba(45, 91, 255, 0.3)'
            ctx.fillRect(x + 3, y + 3, cellSize - 6, cellSize - 6)
            
            ctx.fillStyle = '#ffffff'
            ctx.font = `bold ${cellSize * 0.4}px Arial`
            ctx.textAlign = 'center'
            ctx.textBaseline = 'middle'
            ctx.fillText(pieceIndex + 1, x + cellSize / 2, y + cellSize / 2)
            
            // 绘制边框
            ctx.strokeStyle = isCorrect ? '#00E676' : '#2D5BFF'
            ctx.lineWidth = 3
            ctx.strokeRect(x + 2, y + 2, cellSize - 4, cellSize - 4)
          }
        }
      }
    },
    
    drawPieces() {
      // 只有图片加载成功时才绘制图片碎片
      if (!this.imageLoaded || !this.puzzleImage || !this.puzzleImage.complete || this.puzzleImage.naturalWidth <= 0) {
        this.drawNumberPieces()
        return
      }
      
      const size = this.gridSize
      
      this.shuffledPieces.forEach((pieceIndex, index) => {
        const ref = this.$refs['pieceCanvas' + index]
        if (!ref || !ref[0]) return
        
        const canvas = ref[0]
        const ctx = canvas.getContext('2d')
        
        ctx.clearRect(0, 0, this.pieceSize, this.pieceSize)
        
        const srcRow = Math.floor(pieceIndex / size)
        const srcCol = pieceIndex % size
        const srcX = srcCol * (this.puzzleImage.width / size)
        const srcY = srcRow * (this.puzzleImage.height / size)
        const srcW = this.puzzleImage.width / size
        const srcH = this.puzzleImage.height / size
        
        ctx.drawImage(this.puzzleImage, srcX, srcY, srcW, srcH, 0, 0, this.pieceSize, this.pieceSize)
        
        // 绘制边框
        ctx.strokeStyle = '#2D5BFF'
        ctx.lineWidth = 2
        ctx.strokeRect(1, 1, this.pieceSize - 2, this.pieceSize - 2)
      })
    },
    
    drawNumberPieces() {
      // 数字拼图模式
      const colors = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', 
        '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F',
        '#BB8FCE', '#85C1E9', '#F8B500', '#00CED1',
        '#FF69B4', '#32CD32', '#FFD700', '#9370DB',
        '#20B2AA', '#FF6347', '#7B68EE', '#00FA9A',
        '#DC143C', '#00BFFF', '#228B22', '#FF1493',
        '#1E90FF'
      ]
      
      this.shuffledPieces.forEach((pieceIndex, index) => {
        const ref = this.$refs['pieceCanvas' + index]
        if (!ref || !ref[0]) return
        
        const canvas = ref[0]
        const ctx = canvas.getContext('2d')
        
        ctx.clearRect(0, 0, this.pieceSize, this.pieceSize)
        
        // 绘制彩色背景
        ctx.fillStyle = colors[pieceIndex % colors.length]
        ctx.fillRect(0, 0, this.pieceSize, this.pieceSize)
        
        // 绘制数字
        ctx.fillStyle = '#ffffff'
        ctx.font = `bold ${this.pieceSize * 0.5}px Arial`
        ctx.textAlign = 'center'
        ctx.textBaseline = 'middle'
        ctx.fillText(pieceIndex + 1, this.pieceSize / 2, this.pieceSize / 2)
        
        // 绘制边框
        ctx.strokeStyle = '#2D5BFF'
        ctx.lineWidth = 2
        ctx.strokeRect(1, 1, this.pieceSize - 2, this.pieceSize - 2)
      })
    },
    
    handleCanvasClick(event) {
      if (this.selectedPieceIndex === null) {
        // 点击已放置的碎片可以移除
        const canvas = this.$refs.puzzleCanvas
        const rect = canvas.getBoundingClientRect()
        const x = event.clientX - rect.left
        const y = event.clientY - rect.top
        
        const size = this.gridSize
        const cellSize = this.canvasSize / size
        const col = Math.floor(x / cellSize)
        const row = Math.floor(y / cellSize)
        const slotIndex = row * size + col
        
        if (this.answerSlots[slotIndex] !== null) {
          const pieceIndex = this.answerSlots[slotIndex]
          const shuffleIndex = this.shuffledPieces.indexOf(pieceIndex)
          if (shuffleIndex !== -1) {
            this.usedPieces = this.usedPieces.filter(p => p !== shuffleIndex)
          }
          this.answerSlots[slotIndex] = null
          this.drawPuzzle()
          this.showToast('已移除碎片', 'info')
        }
        return
      }
      
      this.placePieceAtSlot(this.selectedPieceIndex, this.getSlotFromEvent(event))
    },
    
    getSlotFromEvent(event) {
      const canvas = this.$refs.puzzleCanvas
      const rect = canvas.getBoundingClientRect()
      const x = event.clientX - rect.left
      const y = event.clientY - rect.top
      
      const size = this.gridSize
      const cellSize = this.canvasSize / size
      const col = Math.floor(x / cellSize)
      const row = Math.floor(y / cellSize)
      return row * size + col
    },
    
    placePieceAtSlot(pieceIndex, slotIndex) {
      if (this.usedPieces.includes(pieceIndex)) {
        this.showToast('该碎片已使用', 'warning')
        return
      }
      
      if (slotIndex < 0 || slotIndex >= this.answerSlots.length) return
      
      if (this.answerSlots[slotIndex] !== null) {
        // 移除已有的碎片
        const oldPiece = this.answerSlots[slotIndex]
        const oldShuffleIndex = this.shuffledPieces.indexOf(oldPiece)
        if (oldShuffleIndex !== -1) {
          this.usedPieces = this.usedPieces.filter(p => p !== oldShuffleIndex)
        }
      }
      
      const pieceValue = this.shuffledPieces[pieceIndex]
      
      this.moveHistory.push({
        slotIndex,
        oldPiece: this.answerSlots[slotIndex],
        selectedPieceIndex: pieceIndex
      })
      
      this.answerSlots[slotIndex] = pieceValue
      this.usedPieces.push(pieceIndex)
      this.selectedPieceIndex = null
      
      this.drawPuzzle()
      this.checkComplete()
    },
    
    // 拖拽功能
    handlePieceDragStart(index, event) {
      if (this.usedPieces.includes(index)) {
        event.preventDefault()
        return
      }
      this.selectedPieceIndex = index
      event.dataTransfer.setData('text/plain', index)
      event.dataTransfer.effectAllowed = 'move'
    },
    
    handleDragOver(event) {
      event.preventDefault()
      event.dataTransfer.dropEffect = 'move'
    },
    
    handleDrop(event) {
      event.preventDefault()
      const pieceIndex = parseInt(event.dataTransfer.getData('text/plain'))
      const slotIndex = this.getSlotFromEvent(event)
      this.placePieceAtSlot(pieceIndex, slotIndex)
    },
    
    // ========== 游戏逻辑 ==========
    
    checkComplete() {
      const allFilled = this.answerSlots.every(slot => slot !== null)
      if (!allFilled) return
      
      let correct = true
      for (let i = 0; i < this.answerSlots.length; i++) {
        if (this.answerSlots[i] !== this.correctOrder[i]) {
          correct = false
          break
        }
      }
      
      if (correct) {
        this.completeGame()
      }
    },
    
    completeGame() {
      clearInterval(this.timer)
      this.gameComplete = true
      
      // 确保有recordId才提交
      if (!this.recordId) {
        console.error('recordId is null, cannot submit puzzle completion')
        return
      }
      
      submitFunPhysicsPuzzle({
        id: this.recordId,
        puzzleId: this.currentPuzzle.id,
        startTime: this.startTime,
        useHint: this.hintCount < 3 ? 1 : 0,
        difficulty: this.selectedDifficulty
      }).then(res => {
        console.log('Puzzle completion response:', res)
      }).catch(err => {
        console.error('Failed to submit puzzle:', err)
      })
    },
    
    showHint() {
      if (this.hintCount <= 0) {
        this.showToast('提示次数已用完', 'warning')
        return
      }
      this.hintCount--
      this.hintUsed = true
      
      const level = 3 - this.hintCount
      
      if (this.currentPuzzle.type === 0) {
        if (level === 1) {
          this.hintPanel = {
            show: true,
            level: 1,
            message: '完整公式如下，请记住顺序：',
            formula: this.currentPuzzle.content || this.piecesData.join(' ')
          }
        } else if (level === 2) {
          for (let i = 0; i < this.answerSlots.length; i++) {
            if (this.answerSlots[i] !== this.correctOrder[i]) {
              this.hintPanel = {
                show: true,
                level: 2,
                message: `第 ${i + 1} 个位置应该放：`,
                formula: this.piecesData[this.correctOrder[i]]
              }
              break
            }
          }
        } else {
          this.autoPlaceOne()
          this.showToast('已自动放置一个正确碎片', 'success')
        }
      } else {
        if (level === 1) {
          this.hintPanel = {
            show: true,
            level: 1,
            message: this.imageLoaded ? 
              (this.currentPuzzle.hint || '请尝试将碎片放回正确位置') :
              '将碎片按数字顺序排列，从左到右、从上到下',
            formula: ''
          }
        } else if (level === 2) {
          for (let i = 0; i < this.answerSlots.length; i++) {
            if (this.answerSlots[i] !== i) {
              const row = Math.floor(i / this.gridSize) + 1
              const col = (i % this.gridSize) + 1
              this.hintPanel = {
                show: true,
                level: 2,
                message: `下一个正确位置：第 ${row} 行第 ${col} 列（应放数字 ${i + 1}）`,
                formula: ''
              }
              break
            }
          }
        } else {
          this.autoPlaceOne()
          this.showToast('已自动放置一个正确碎片', 'success')
        }
      }
    },
    
    autoPlaceOne() {
      for (let i = 0; i < this.answerSlots.length; i++) {
        if (this.answerSlots[i] !== this.correctOrder[i]) {
          const correctPiece = this.correctOrder[i]
          const shuffleIndex = this.shuffledPieces.indexOf(correctPiece)
          
          if (shuffleIndex !== -1 && !this.usedPieces.includes(shuffleIndex)) {
            if (this.answerSlots[i] !== null) {
              const wrongShuffleIndex = this.shuffledPieces.indexOf(this.answerSlots[i])
              this.usedPieces = this.usedPieces.filter(p => p !== wrongShuffleIndex)
            }
            
            this.answerSlots[i] = correctPiece
            this.usedPieces.push(shuffleIndex)
            this.selectedPieceIndex = null
            
            if (this.currentPuzzle.type === 1) {
              this.drawPuzzle()
            }
            
            this.checkComplete()
            break
          }
        }
      }
    },
    
    resetPuzzle() {
      this.answerSlots = new Array(this.piecesData.length).fill(null)
      this.usedPieces = []
      this.selectedPieceIndex = null
      this.moveHistory = []
      this.hintCount = 3
      this.hintUsed = false
      this.elapsedTime = 0
      this.hintPanel.show = false
      
      this.shuffledPieces = this.shuffleArray([...Array(this.piecesData.length).keys()])
      
      if (this.currentPuzzle.type === 1) {
        this.drawPuzzle()
        if (this.imageLoaded && this.puzzleImage) {
          this.drawPieces()
        } else {
          this.drawNumberPieces()
        }
      }
      
      this.showToast('已重置拼图', 'info')
    },
    
    nextPuzzle() {
      this.currentPuzzle = null
      this.gameComplete = false
      this.pointsEarned = 0
      this.selectedDifficulty = null
      this.selectedType = 0
      clearInterval(this.timer)
    },
    
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    
    getPhysicsQuote() {
      const quotes = [
        '简洁即真理',
        '想象力比知识更重要',
        '科学没有国界',
        '好奇心是科学之母',
        '实验是检验真理的唯一标准'
      ]
      return quotes[Math.floor(Math.random() * quotes.length)]
    },
    
    goBack() {
      clearInterval(this.timer)
      this.$router.push('/funphysics')
    }
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
}
</script>

<style scoped>
.puzzle-container {
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

/* 自定义Toast */
.custom-toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 2000;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.custom-toast.info {
  background: rgba(45, 91, 255, 0.9);
  border: 1px solid rgba(45, 91, 255, 0.5);
}

.custom-toast.success {
  background: rgba(0, 230, 118, 0.9);
  border: 1px solid rgba(0, 230, 118, 0.5);
}

.custom-toast.warning {
  background: rgba(255, 145, 0, 0.9);
  border: 1px solid rgba(255, 145, 0, 0.5);
}

.custom-toast.error {
  background: rgba(255, 82, 82, 0.9);
  border: 1px solid rgba(255, 82, 82, 0.5);
}

.toast-icon {
  font-size: 18px;
}

.toast-message {
  color: white;
  font-size: 14px;
  font-weight: 500;
}

.toast-fade-enter-active, .toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-enter-from, .toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* 提示面板 */
.hint-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1500;
  animation: popIn 0.3s ease;
}

@keyframes popIn {
  from { transform: translate(-50%, -50%) scale(0.8); opacity: 0; }
  to { transform: translate(-50%, -50%) scale(1); opacity: 1; }
}

.hint-content {
  background: linear-gradient(135deg, rgba(255, 145, 0, 0.2) 0%, rgba(255, 197, 61, 0.2) 100%);
  border: 2px solid rgba(255, 145, 0, 0.5);
  border-radius: 20px;
  padding: 24px 32px;
  min-width: 300px;
  max-width: 400px;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 40px rgba(255, 145, 0, 0.3);
}

.hint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.hint-level {
  font-size: 16px;
  font-weight: 600;
  color: #FFC53D;
}

.hint-close {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hint-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.hint-body {
  color: white;
}

.hint-body p {
  margin: 0 0 12px 0;
  font-size: 14px;
}

.hint-formula {
  background: rgba(0, 0, 0, 0.3);
  padding: 16px 24px;
  border-radius: 12px;
  font-size: 24px;
  font-weight: bold;
  color: #FFC53D;
  text-align: center;
  letter-spacing: 2px;
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
  transform: translate(-50%, -40%);
}

/* 玩法说明弹窗 */
.guide-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  padding: 20px;
}

.guide-modal {
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.1) 0%, rgba(0, 230, 118, 0.1) 100%);
  border: 1px solid rgba(45, 91, 255, 0.3);
  border-radius: 24px;
  padding: 32px;
  max-width: 450px;
  width: 100%;
  backdrop-filter: blur(10px);
}

.guide-modal h3 {
  margin: 0 0 24px 0;
  font-size: 22px;
  color: white;
  text-align: center;
}

.guide-content {
  margin-bottom: 24px;
}

.guide-step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.step-num {
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  color: white;
  flex-shrink: 0;
}

.guide-step p {
  margin: 0;
  color: #e0e0e0;
  font-size: 14px;
  line-height: 1.6;
}

.guide-step strong {
  color: #2D5BFF;
}

.guide-tips {
  background: rgba(255, 145, 0, 0.1);
  border: 1px solid rgba(255, 145, 0, 0.3);
  border-radius: 12px;
  padding: 12px 16px;
}

.guide-tips p {
  margin: 0;
  color: #FFC53D;
  font-size: 13px;
}

.color-green {
  color: #00E676;
  font-weight: 600;
}

.color-red {
  color: #FF5252;
  font-weight: 600;
}

.guide-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.guide-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(45, 91, 255, 0.3);
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.puzzle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.back-btn {
  background: rgba(45, 91, 255, 0.2);
  border: 1px solid rgba(45, 91, 255, 0.3);
  color: #2D5BFF;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(45, 91, 255, 0.3);
  transform: scale(1.1);
}

.back-icon {
  font-size: 20px;
  font-weight: bold;
}

.header-title {
  text-align: center;
  flex: 1;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.level-badge {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  border-radius: 12px;
  font-size: 12px;
  color: white;
  margin-top: 4px;
}

.puzzle-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-icon {
  font-size: 16px;
}

.stat-value {
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
}

.puzzle-select {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  position: relative;
  z-index: 1;
}

.select-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 24px;
  padding: 40px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  max-width: 500px;
  width: 100%;
  text-align: center;
}

.select-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.select-card h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #ffffff;
}

.select-desc {
  color: #b0b0b0;
  font-size: 14px;
  margin-bottom: 32px;
}

.difficulty-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 32px;
}

.difficulty-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.difficulty-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.difficulty-btn.selected {
  border-color: #2D5BFF;
  background: rgba(45, 91, 255, 0.1);
  box-shadow: 0 0 20px rgba(45, 91, 255, 0.3);
}

.type-btn {
  padding: 20px 8px;
}

.difficulty-icon {
  font-size: 24px;
}

.difficulty-label {
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
}

.difficulty-desc {
  font-size: 11px;
  color: #888;
}

.difficulty-points {
  font-size: 12px;
  color: #00E676;
}

.start-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 91, 255, 0.3);
}

.start-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 18px;
}

.puzzle-game {
  position: relative;
  z-index: 1;
}

.puzzle-title-section {
  text-align: center;
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.title-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.title-row h3 {
  margin: 0;
  font-size: 20px;
  color: #ffffff;
}

.help-btn {
  background: rgba(45, 91, 255, 0.2);
  border: 1px solid rgba(45, 91, 255, 0.3);
  color: #2D5BFF;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.help-btn:hover {
  background: rgba(45, 91, 255, 0.3);
}

.puzzle-desc {
  margin: 8px 0 0 0;
  color: #b0b0b0;
  font-size: 14px;
}

.game-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

/* 公式拼图样式 */
.formula-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.formula-label {
  text-align: center;
  color: #2D5BFF;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 16px;
}

.formula-slots {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

/* 拼图块样式 - 添加凹凸效果 */
.puzzle-piece {
  position: relative;
}

.puzzle-piece::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  pointer-events: none;
}

.formula-slot {
  min-width: 70px;
  height: 70px;
  background: rgba(45, 91, 255, 0.1);
  border: 2px dashed rgba(45, 91, 255, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.formula-slot:hover {
  border-color: #2D5BFF;
  background: rgba(45, 91, 255, 0.2);
  transform: scale(1.05);
}

.formula-slot.filled {
  border-style: solid;
  background: rgba(45, 91, 255, 0.15);
  border-color: #2D5BFF;
  box-shadow: 0 4px 15px rgba(45, 91, 255, 0.3);
}

.formula-slot.correct {
  background: rgba(0, 230, 118, 0.15);
  border-color: #00E676;
  box-shadow: 0 4px 15px rgba(0, 230, 118, 0.3);
}

.formula-slot.wrong {
  background: rgba(255, 82, 82, 0.15);
  border-color: #FF5252;
  box-shadow: 0 4px 15px rgba(255, 82, 82, 0.3);
}

.formula-slot.drag-over {
  border-color: #FFC53D;
  background: rgba(255, 197, 61, 0.2);
  transform: scale(1.1);
}

.piece-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.piece-text {
  font-size: 20px;
  font-weight: bold;
  color: #ffffff;
}

.slot-placeholder {
  color: rgba(255, 255, 255, 0.3);
  font-size: 16px;
}

/* 碎片区域样式 */
.pieces-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.pieces-label {
  text-align: center;
  color: #2D5BFF;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 16px;
}

.pieces-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.piece-item {
  min-width: 70px;
  height: 70px;
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.2) 0%, rgba(45, 91, 255, 0.1) 100%);
  border: 2px solid rgba(45, 91, 255, 0.4);
  border-radius: 12px;
  cursor: grab;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.piece-item::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 70%
  );
  transform: rotate(45deg) translateY(100%);
  transition: transform 0.6s ease;
}

.piece-item:hover:not(.used)::after {
  transform: rotate(45deg) translateY(-100%);
}

.piece-item:hover:not(.used) {
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.3) 0%, rgba(45, 91, 255, 0.2) 100%);
  transform: scale(1.08) rotate(2deg);
  box-shadow: 0 8px 25px rgba(45, 91, 255, 0.4);
}

.piece-item.selected {
  border-color: #00E676;
  background: linear-gradient(135deg, rgba(0, 230, 118, 0.2) 0%, rgba(0, 230, 118, 0.1) 100%);
  box-shadow: 0 0 20px rgba(0, 230, 118, 0.4);
  transform: scale(1.1);
}

.piece-item.used {
  opacity: 0.3;
  cursor: not-allowed;
  background: rgba(100, 100, 100, 0.1);
  border-color: rgba(100, 100, 100, 0.2);
  transform: none;
}

.piece-label {
  font-size: 20px;
  font-weight: bold;
  color: #ffffff;
  z-index: 1;
}

/* 图片拼图布局 */
.image-game-layout {
  display: flex;
  gap: 24px;
  justify-content: center;
  align-items: flex-start;
  flex-wrap: wrap;
}

.canvas-area {
  flex: 0 0 auto;
  text-align: center;
}

.canvas-label {
  color: #2D5BFF;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

canvas {
  display: block;
  margin: 0 auto;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 0 30px rgba(45, 91, 255, 0.3);
}

.canvas-progress {
  margin-top: 12px;
  padding: 8px 16px;
  background: rgba(45, 91, 255, 0.1);
  border-radius: 8px;
  color: #2D5BFF;
  font-size: 14px;
  display: inline-block;
}

/* 右侧面板 */
.right-panel {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pieces-preview-area {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.preview-label {
  color: #00E676;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.peek-btn {
  background: rgba(45, 91, 255, 0.2);
  border: 1px solid rgba(45, 91, 255, 0.3);
  color: #2D5BFF;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.peek-btn:hover {
  background: rgba(45, 91, 255, 0.3);
}

.pieces-preview-grid {
  display: grid;
  gap: 8px;
}

.piece-preview-item {
  aspect-ratio: 1;
  background: rgba(45, 91, 255, 0.1);
  border: 2px solid rgba(45, 91, 255, 0.3);
  border-radius: 8px;
  cursor: grab;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.piece-preview-item:hover:not(.used) {
  transform: scale(1.05);
  border-color: #2D5BFF;
  box-shadow: 0 4px 15px rgba(45, 91, 255, 0.4);
}

.piece-preview-item.selected {
  border-color: #00E676;
  box-shadow: 0 0 15px rgba(0, 230, 118, 0.5);
}

.piece-preview-item.used {
  opacity: 0.3;
  cursor: not-allowed;
}

.piece-preview-item .piece-canvas {
  width: 100%;
  height: 100%;
  border-radius: 6px;
}

.piece-number {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  pointer-events: none;
}

/* 原图参考 */
.original-preview {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.original-image {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(0, 230, 118, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.original-image:hover {
  transform: scale(1.02);
  box-shadow: 0 0 20px rgba(0, 230, 118, 0.3);
}

.original-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image-tip {
  width: 200px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  color: #888;
  font-size: 13px;
}

/* 图片碎片样式 */
.image-pieces {
  justify-content: center;
}

.image-piece {
  width: 70px;
  height: 70px;
  min-width: auto;
  padding: 0;
  overflow: hidden;
  background: rgba(45, 91, 255, 0.1);
}

.piece-canvas {
  width: 100%;
  height: 100%;
  border-radius: 10px;
}

/* 全图查看 */
.full-image-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 3000;
  padding: 40px;
}

.full-image-container {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.full-image-container img {
  max-width: 100%;
  max-height: 80vh;
  border-radius: 12px;
  box-shadow: 0 0 50px rgba(45, 91, 255, 0.5);
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 操作按钮 */
.game-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.hint-btn {
  background: linear-gradient(135deg, #FF9100 0%, #FFC53D 100%);
  color: white;
}

.hint-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 145, 0, 0.3);
}

.undo-btn {
  background: rgba(100, 200, 255, 0.2);
  color: #64C8FF;
  border: 1px solid rgba(100, 200, 255, 0.3);
}

.undo-btn:hover:not(:disabled) {
  background: rgba(100, 200, 255, 0.3);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

/* 完成界面 */
.puzzle-complete {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(18, 18, 18, 0.95);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.complete-overlay {
  padding: 20px;
}

.complete-card {
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.1) 0%, rgba(0, 230, 118, 0.1) 100%);
  border-radius: 24px;
  padding: 40px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(45, 91, 255, 0.2);
  text-align: center;
  max-width: 400px;
  animation: scaleIn 0.5s ease;
}

@keyframes scaleIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.complete-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: bounce 1s ease-in-out infinite;
}

.complete-card h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #ffffff;
}

.complete-time {
  color: #b0b0b0;
  font-size: 14px;
  margin-bottom: 24px;
}

.points-section {
  margin-bottom: 24px;
}

.points-earned {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.hint-note {
  color: #FF9100;
  font-size: 12px;
  margin: 0;
}

.complete-quote {
  color: #b0b0b0;
  font-style: italic;
  font-size: 14px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border-left: 3px solid #2D5BFF;
}

.quote-mark {
  color: #2D5BFF;
  font-size: 20px;
}

.next-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.next-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 91, 255, 0.3);
}

@media (max-width: 768px) {
  .puzzle-container {
    padding: 12px;
  }
  
  .select-card {
    padding: 24px;
  }
  
  .difficulty-options {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .formula-slot, .piece-item {
    min-width: 55px;
    height: 55px;
    font-size: 16px;
  }
  
  .piece-text, .piece-label {
    font-size: 16px;
  }
  
  .image-game-layout {
    flex-direction: column;
    align-items: center;
  }
  
  .canvas-area canvas {
    width: 300px !important;
    height: 300px !important;
  }
}
</style>
