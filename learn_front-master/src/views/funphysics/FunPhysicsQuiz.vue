<template>
  <div class="quiz-container">
    <div class="star-background"></div>
    
    <div class="quiz-header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
      </button>
      <div class="header-title">
        <h2>💡 趣味问答</h2>
        <span class="intuition-tag">凭直觉选！</span>
      </div>
    </div>

    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <div class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</div>
    </div>

    <div class="quiz-content" v-if="currentQuestion && !quizComplete">
      <div class="question-card">
        <div class="question-badges">
          <div class="question-type-badge">
            {{ currentQuestion.type === 0 ? '🔬 物理直觉挑战' : '🌍 生活中的物理' }}
          </div>
          <div class="difficulty-badge" :class="'difficulty-' + currentQuestion.difficulty">
            {{ getDifficultyText(currentQuestion.difficulty) }}
          </div>
        </div>
        
        <h3 class="question-title">{{ currentQuestion.title }}</h3>
        
        <div class="question-image" v-if="currentQuestion.imageUrl">
          <img :src="currentQuestion.imageUrl" alt="题目配图" />
        </div>

        <div class="options-list">
          <div 
            v-for="(option, key) in parsedOptions" 
            :key="key"
            v-show="option && option.trim() !== ''"
            class="option-item"
            :class="{
              'selected': selectedAnswer === key,
              'correct': showResult && key === currentQuestion.correctAnswer,
              'wrong': showResult && selectedAnswer === key && key !== currentQuestion.correctAnswer
            }"
            @click="selectAnswer(key)"
          >
            <div class="option-marker">
              <span class="option-key">{{ key }}</span>
            </div>
            <span class="option-text">{{ option }}</span>
            <div class="option-indicator" v-if="selectedAnswer === key && !showResult">
              <div class="indicator-dot"></div>
            </div>
          </div>
        </div>

        <div class="result-section" v-if="showResult">
          <div class="result-card" :class="{ 'correct': isCorrect, 'wrong': !isCorrect }">
            <div class="result-icon">{{ isCorrect ? '🎉' : '🤔' }}</div>
            <div class="result-title">{{ isCorrect ? '回答正确！' : '再想想...' }}</div>
            <div class="explanation-content" v-if="!isCorrect">
              <strong>趣味解析：</strong>
              <p>{{ currentQuestion.explanation }}</p>
            </div>
            <button class="next-btn" v-if="!isCorrect" @click="nextQuestion">
              <span class="btn-icon">➡️</span>
              <span>下一题</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="quiz-complete" v-if="quizComplete">
      <div class="complete-overlay">
        <div class="complete-card">
          <div class="complete-icon">🏆</div>
          <h3>挑战完成！</h3>
          <div class="result-stats">
            <div class="stat-row">
              <span class="stat-label">正确率</span>
              <span class="stat-value">{{ accuracy }}%</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">答对题数</span>
              <span class="stat-value">{{ correctCount }} / {{ totalAnswered }}</span>
            </div>
          </div>
          <div class="complete-actions">
            <button class="restart-btn" @click="restartQuiz">
              <span class="btn-icon">🔄</span>
              <span>再来一轮</span>
            </button>
            <button class="exit-btn" @click="goBack">
              <span class="btn-icon">🚪</span>
              <span>退出</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="loading-overlay" v-if="loading">
      <div class="atom-spinner">
        <div class="electron-orbit"></div>
        <div class="nucleus"></div>
      </div>
      <div class="loading-text">加载题目中...</div>
    </div>
  </div>
</template>

<script>
import { getFunPhysicsDailyChallenge, submitFunPhysicsAnswer } from '@/api/api'

export default {
  name: 'FunPhysicsQuiz',
  data() {
    return {
      questions: [],
      currentIndex: 0,
      currentQuestion: null,
      selectedAnswer: null,
      showResult: false,
      isCorrect: false,
      isSubmitting: false,
      correctCount: 0,
      totalAnswered: 0,
      quizComplete: false,
      loading: false
    }
  },
  computed: {
    parsedOptions() {
      if (!this.currentQuestion || !this.currentQuestion.options) return {}
      try {
        const options = typeof this.currentQuestion.options === 'string' 
          ? JSON.parse(this.currentQuestion.options) 
          : this.currentQuestion.options
        if (Array.isArray(options)) {
          const result = {}
          options.forEach(opt => {
            result[opt.label] = opt.content
          })
          return result
        }
        return options
      } catch (e) {
        console.error('解析选项失败:', e)
        return {}
      }
    },
    progressPercent() {
      if (this.questions.length === 0) return 0
      return ((this.currentIndex + 1) / this.questions.length) * 100
    },
    accuracy() {
      if (this.totalAnswered === 0) return 0
      return Math.round((this.correctCount / this.totalAnswered) * 100)
    }
  },
  created() {
    this.loadQuestions()
  },
  methods: {
    loadQuestions() {
      this.loading = true
      console.log('加载趣味问题...')
      getFunPhysicsDailyChallenge().then(res => {
        console.log('趣味问题响应:', res)
        if (res.code === 1000 && res.data) {
          this.questions = Array.isArray(res.data) ? res.data : [res.data]
          if (this.questions.length > 0) {
            this.currentQuestion = this.questions[0]
          }
        }
      }).catch(err => {
        console.error('加载题目失败:', err)
        alert('加载题目失败，请检查后端服务是否启动')
      }).finally(() => {
        this.loading = false
      })
    },
    selectAnswer(key) {
      if (this.showResult || this.isSubmitting) return
      this.isSubmitting = true
      this.selectedAnswer = key
      this.submitAnswer()
    },
    submitAnswer() {
      if (!this.selectedAnswer) return
      
      console.log('提交答案:', this.selectedAnswer, '正确答案:', this.currentQuestion.correctAnswer)
      
      submitFunPhysicsAnswer({
        questionId: this.currentQuestion.id,
        userAnswer: this.selectedAnswer
      }).then(res => {
        console.log('答题结果:', res)
        if (res.code === 1000) {
          this.showResult = true
          const isCorrect = res.data.isCorrect == 1 || res.data.isCorrect == true
          this.isCorrect = isCorrect
          this.totalAnswered++
          console.log('是否正确:', isCorrect, 'isCorrect值:', res.data.isCorrect, '类型:', typeof res.data.isCorrect)
          if (isCorrect) {
            this.correctCount++
            console.log('准备跳转到下一题，当前索引:', this.currentIndex, '题目总数:', this.questions.length)
            setTimeout(() => {
              console.log('执行跳转')
              this.nextQuestion()
            }, 500)
          }
        }
        this.isSubmitting = false
      }).catch(err => {
        console.error('答题失败:', err)
        // 即使后端出错，也要显示结果
        this.showResult = true
        this.isCorrect = this.selectedAnswer === this.currentQuestion.correctAnswer
        this.totalAnswered++
        console.log('前端判断是否正确:', this.isCorrect)
        if (this.isCorrect) {
          this.correctCount++
          setTimeout(() => {
            this.nextQuestion()
          }, 500)
        }
        this.isSubmitting = false
      })
    },
    nextQuestion() {
      console.log('nextQuestion 被调用，当前索引:', this.currentIndex, '题目总数:', this.questions.length)
      this.currentIndex++
      console.log('新索引:', this.currentIndex)
      if (this.currentIndex < this.questions.length) {
        this.currentQuestion = this.questions[this.currentIndex]
        this.selectedAnswer = null
        this.showResult = false
        this.isCorrect = false
        console.log('加载下一题:', this.currentQuestion.title)
      } else {
        this.quizComplete = true
        console.log('答题完成')
      }
    },
    restartQuiz() {
      this.currentIndex = 0
      this.correctCount = 0
      this.totalAnswered = 0
      this.quizComplete = false
      this.selectedAnswer = null
      this.showResult = false
      this.loadQuestions()
    },
    goBack() {
      this.$router.push('/funphysics')
    },
    getDifficultyText(difficulty) {
      const map = {
        1: '⭐ 简单',
        2: '⭐⭐ 中等',
        3: '⭐⭐⭐ 困难'
      }
      return map[difficulty] || '⭐ 简单'
    }
  }
}
</script>

<style scoped>
.quiz-container {
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
    radial-gradient(2px 2px at 50px 160px, #ddd, rgba(0,0,0,0));
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

.quiz-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.intuition-tag {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #FF9100 0%, #FFC53D 100%);
  border-radius: 12px;
  font-size: 12px;
  color: white;
  margin-top: 4px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.score-display {
  display: flex;
  gap: 12px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.score-icon {
  font-size: 16px;
}

.score-value {
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
}

.progress-bar {
  position: relative;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2D5BFF 0%, #00E676 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-text {
  position: absolute;
  top: -24px;
  right: 0;
  font-size: 12px;
  color: #b0b0b0;
}

.quiz-content {
  position: relative;
  z-index: 1;
}

.question-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.question-badges {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.question-type-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(45, 91, 255, 0.1);
  border: 1px solid rgba(45, 91, 255, 0.3);
  border-radius: 20px;
  font-size: 13px;
  color: #2D5BFF;
}

.difficulty-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.difficulty-badge.difficulty-1 {
  background: rgba(76, 175, 80, 0.1);
  border: 1px solid rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.difficulty-badge.difficulty-2 {
  background: rgba(255, 152, 0, 0.1);
  border: 1px solid rgba(255, 152, 0, 0.3);
  color: #FF9800;
}

.difficulty-badge.difficulty-3 {
  background: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.3);
  color: #F44336;
}

.question-title {
  font-size: 20px;
  color: #ffffff;
  margin-bottom: 24px;
  line-height: 1.6;
}

.question-image {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.05);
}

.question-image img {
  width: 100%;
  height: auto;
  display: block;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.option-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(4px);
}

.option-item.selected {
  border-color: #2D5BFF;
  background: rgba(45, 91, 255, 0.1);
  box-shadow: 0 0 20px rgba(45, 91, 255, 0.2);
}

.option-item.correct {
  border-color: #00E676;
  background: rgba(0, 230, 118, 0.1);
  animation: successPulse 0.5s ease;
}

@keyframes successPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

.option-item.wrong {
  border-color: #FF4D4F;
  background: rgba(255, 77, 79, 0.1);
  animation: shake 0.5s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.option-marker {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2D5BFF 0%, #5B8FF9 100%);
  border-radius: 50%;
  flex-shrink: 0;
}

.option-key {
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.option-text {
  flex: 1;
  font-size: 16px;
  color: #ffffff;
  line-height: 1.5;
}

.option-indicator {
  width: 12px;
  height: 12px;
  position: relative;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  background: #00E676;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.result-section {
  margin-top: 24px;
}

.result-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.result-card.correct {
  border-color: #00E676;
  background: rgba(0, 230, 118, 0.05);
}

.result-card.wrong {
  border-color: #FF4D4F;
  background: rgba(255, 77, 79, 0.05);
}

.result-icon {
  font-size: 48px;
  text-align: center;
  margin-bottom: 12px;
}

.result-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 16px;
  color: #ffffff;
}

.explanation-content {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.explanation-content strong {
  color: #2D5BFF;
  display: block;
  margin-bottom: 8px;
}

.explanation-content p {
  margin: 0;
  color: #b0b0b0;
  line-height: 1.6;
  font-size: 14px;
}

.next-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  border: none;
  border-radius: 8px;
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

.btn-icon {
  font-size: 18px;
}

.quiz-complete {
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

.complete-overlay {
  padding: 20px;
}

.complete-card {
  background: linear-gradient(135deg, rgba(45, 91, 255, 0.1) 0%, rgba(0, 230, 118, 0.1) 100%);
  border-radius: 24px;
  padding: 50px 60px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(45, 91, 255, 0.2);
  text-align: center;
  max-width: 600px;
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

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.complete-card h3 {
  margin: 0 0 24px 0;
  font-size: 24px;
  color: #ffffff;
}

.result-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.stat-label {
  color: #b0b0b0;
  font-size: 14px;
}

.stat-value {
  color: #2D5BFF;
  font-size: 18px;
  font-weight: bold;
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

.points-desc {
  color: #b0b0b0;
  font-size: 14px;
  margin: 0;
}

.restart-btn {
  width: 100%;
  padding: 12px 20px;
  background: linear-gradient(135deg, #2D5BFF 0%, #00E676 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.restart-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 91, 255, 0.3);
}

.complete-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.exit-btn {
  flex: 1;
  padding: 12px 20px;
  background: linear-gradient(135deg, #FF9800 0%, #F44336 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.exit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 152, 0, 0.3);
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
  .quiz-container {
    padding: 12px;
  }
  
  .question-title {
    font-size: 18px;
  }
  
  .option-text {
    font-size: 14px;
  }
}
</style>
