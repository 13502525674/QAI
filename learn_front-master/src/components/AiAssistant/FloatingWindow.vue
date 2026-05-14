<template>
  <div class="ai-assistant-container" :style="containerStyle" ref="container">
    <!-- Floating Trigger Ball (Hexagon) -->
    <div 
      class="floating-ball-wrapper"
      v-show="!isOpen"
    >
      <div 
        class="floating-ball" 
        ref="floatingBall"
        @mousedown="startDrag"
        @click="handleBallClick" 
        @mouseenter="handleBallMouseEnter"
        :class="{ 'pulse-animation': !isOpen, 'is-dragging': isDragging, 'is-minimized': isMinimized }"
      >
        <div class="atom-icon">
          <div class="nucleus"></div>
          <div class="orbit orbit-1"></div>
          <div class="orbit orbit-2"></div>
          <div class="orbit orbit-3"></div>
        </div>
      </div>
      <!-- 待机互动气泡 -->
      <transition name="bubble-pop">
        <div v-if="showIdleBubble" class="idle-bubble">
          <span class="bubble-text">快来跟我玩耍吧！</span>
          <div class="bubble-tail"></div>
        </div>
      </transition>
    </div>

    <!-- Chat Window (Experiment Console) -->
    <transition name="expand-radial">
      <div class="chat-window" v-if="isOpen">
        <!-- Background Texture -->
        <div class="bg-texture">
          <span class="formula f1">E=mc²</span>
          <span class="formula f2">F=ma</span>
          <span class="formula f3">λ=h/p</span>
          <span class="formula f4">pV=nRT</span>
        </div>

        <!-- Header -->
        <div class="chat-header" @mousedown="startDrag">
          <div class="header-left">
            <div class="atom-icon-small">
              <div class="nucleus"></div>
              <div class="orbit orbit-1"></div>
              <div class="orbit orbit-2"></div>
            </div>
            <span class="title">物理智答</span>
          </div>
          <div class="header-actions">
            <el-tooltip content="新实验 (新对话)" placement="top">
              <i class="el-icon-refresh-right action-btn" @click.stop="startNewChat"></i>
            </el-tooltip>
            <el-tooltip content="清除数据 (清除历史)" placement="top">
              <i class="el-icon-delete action-btn" @click.stop="confirmClearHistory"></i>
            </el-tooltip>
            <i class="el-icon-close action-btn close-btn" @click.stop="toggleWindow"></i>
          </div>
        </div>

        <!-- Messages Area -->
        <div class="messages-area" ref="messagesContainer" @click="handleContentClick">
          <div v-if="messages.length === 0" class="empty-state">
            <div class="atom-icon-large">
              <div class="nucleus"></div>
              <div class="orbit orbit-1"></div>
              <div class="orbit orbit-2"></div>
              <div class="orbit orbit-3"></div>
            </div>
            <p class="welcome-text">准备好探索物理世界了吗？</p>
            <p class="sub-text">我是你的物理助教，随时为你解答。</p>
          </div>

          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            class="message-row"
            :class="msg.role"
          >
            <div class="avatar" v-if="msg.role === 'ai'">
              <div class="atom-icon-avatar">
                <div class="nucleus"></div>
                <div class="orbit orbit-1"></div>
              </div>
            </div>
            
            <div class="message-bubble">
              <div v-if="msg.role === 'ai'" class="markdown-body" v-html="formatMessage(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
              
              <!-- Concept Tooltip (Visual Demo) -->
              <div class="physics-tooltip" v-if="msg.role === 'ai' && showTooltip">
                <span class="tooltip-arrow"></span>
                <span class="tooltip-text">点击公式可放大查看</span>
              </div>
            </div>

            <div class="avatar user-avatar" v-if="msg.role === 'user'">
              <i class="el-icon-user"></i>
            </div>
          </div>

          <!-- Particle Collision Loading -->
          <div v-if="isLoading" class="message-row ai">
            <div class="avatar">
               <div class="atom-icon-avatar">
                <div class="nucleus"></div>
                <div class="orbit orbit-1"></div>
              </div>
            </div>
            <div class="message-bubble loading-bubble">
              <div class="particle-loader">
                <div class="particle p1"></div>
                <div class="particle p2"></div>
                <div class="particle p3"></div>
              </div>
              <span class="loading-text">正在计算答案维度...</span>
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <div class="input-area" :class="{ 'input-focused': isInputFocused }">
          <el-input
            type="textarea"
            :rows="1"
            placeholder="输入物理问题 (如: 牛顿定律)"
            v-model="inputMessage"
            resize="none"
            @focus="isInputFocused = true"
            @blur="isInputFocused = false"
            @keyup.enter.native="handleEnter"
            class="physics-input"
          ></el-input>
          <div class="send-btn-wrapper" @click="sendMessage">
             <div class="send-btn-inner" :class="{ 'sending': isLoading }">
                <i class="el-icon-s-promotion" v-if="!isLoading"></i>
                <i class="el-icon-loading" v-else></i>
             </div>
          </div>
        </div>
        
        <!-- Bottom Scale Decoration -->
        <div class="bottom-scale">
            <div v-for="n in 30" :key="n" class="scale-mark" :style="{ height: n % 5 === 0 ? '8px' : '4px' }"></div>
        </div>

        <!-- Formula Magnifier Overlay -->
        <transition name="fade-scale">
          <div class="formula-overlay" v-if="enlargedFormulaHtml" @click="closeEnlargedFormula">
             <div class="enlarged-content" v-html="enlargedFormulaHtml" @click.stop></div>
             <div class="close-hint">点击任意处关闭</div>
          </div>
        </transition>
      </div>
    </transition>
  </div>
</template>

<script>
import { streamChat, getHistory, clearHistory } from '@/api/ai'
import MarkdownIt from 'markdown-it'
import mk from 'markdown-it-katex'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(lang, str, true).value +
               '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
});
md.use(mk);

export default {
  name: 'FloatingAssistant',
  data() {
    return {
      isOpen: false,
      inputMessage: '',
      messages: [],
      isLoading: false,
      conversationId: 'user_' + (window.localStorage.getItem('userId') || 'guest'),
      
      // Dragging state
      isDragging: false,
      dragStartX: 0,
      dragStartY: 0,
      initialRight: 40,
      initialBottom: 40,
      currentRight: 40,
      currentBottom: 40,
      
      // UI state
      isInputFocused: false,
      showTooltip: false,
      enlargedFormulaHtml: null,
      isMinimized: false,
      clickTimer: null,
      
      // Idle bubble state
      showIdleBubble: false,
      idleTimer: null,
      idleTimeout: 10000
    }
  },
  computed: {
    containerStyle() {
      return {
        right: this.currentRight + 'px',
        bottom: this.currentBottom + 'px',
        cursor: this.isDragging ? 'grabbing' : 'default'
      }
    }
  },
  mounted() {
    this.loadHistory();
    window.addEventListener('mousemove', this.handleMouseMove);
    window.addEventListener('mouseup', this.handleMouseUp);
    this.startIdleTimer();
  },
  beforeDestroy() {
    window.removeEventListener('mousemove', this.handleMouseMove);
    window.removeEventListener('mouseup', this.handleMouseUp);
    if (this.clickTimer) clearTimeout(this.clickTimer);
    this.clearIdleTimer();
  },
  methods: {
    startIdleTimer() {
      this.idleTimer = setTimeout(() => {
        if (!this.isOpen) {
          this.showIdleBubble = true;
        }
      }, this.idleTimeout);
    },
    resetIdleTimer() {
      this.showIdleBubble = false;
      this.clearIdleTimer();
      this.startIdleTimer();
    },
    clearIdleTimer() {
      if (this.idleTimer) {
        clearTimeout(this.idleTimer);
        this.idleTimer = null;
      }
    },
    startDrag(e) {
      if (this.isOpen && !e.target.closest('.chat-header')) return; // Only drag via header when open
      
      this.isDragging = true;
      this.dragStartX = e.clientX;
      this.dragStartY = e.clientY;
      this.initialRight = this.currentRight;
      this.initialBottom = this.currentBottom;
    },
    handleMouseMove(e) {
      if (!this.isDragging) return;
      
      const deltaX = this.dragStartX - e.clientX; // Dragging left increases right value
      const deltaY = this.dragStartY - e.clientY; // Dragging up increases bottom value
      
      this.currentRight = this.initialRight + deltaX;
      this.currentBottom = this.initialBottom + deltaY;
    },
    handleMouseUp() {
      if (!this.isDragging) return;
      this.isDragging = false;
      
      // Snap to edges (Simple adsorption)
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      
      // Horizontal snap
      if (this.currentRight > windowWidth / 2) {
         // Closer to left (snap to left edge with 20px margin)
         // Ball width is 60px
         this.currentRight = windowWidth - 60 - 20;
      } else {
         // Closer to right (snap to right edge with 20px margin)
         this.currentRight = 20;
      }
      
      // Vertical bounds check
      if (this.currentBottom < 20) this.currentBottom = 20;
      if (this.currentBottom > windowHeight - 80) this.currentBottom = windowHeight - 80;
    },
    handleBallClick() {
      if (this.isDragging) return; 
      this.resetIdleTimer();
      
      if (this.clickTimer) {
        clearTimeout(this.clickTimer);
        this.clickTimer = null;
        this.isMinimized = true;
      } else {
        this.clickTimer = setTimeout(() => {
          if (!this.isMinimized) {
             this.toggleWindow();
          }
          this.clickTimer = null;
        }, 250);
      }
    },
    handleBallMouseEnter() {
        if (this.isMinimized) {
            this.isMinimized = false;
        }
        this.resetIdleTimer();
    },
    toggleWindow() {
      this.isOpen = !this.isOpen;
      this.resetIdleTimer();
      if (this.isOpen) {
        this.scrollToBottom();
      }
    },
    formatMessage(content) {
      return md.render(content || '');
    },
    loadHistory() {
      getHistory(this.conversationId).then(history => {
        if (history && history.length > 0) {
          this.messages = history.map(msg => ({
            role: (msg.messageType === 'USER' || msg.messageType === 'user') ? 'user' : 'ai',
            content: msg.content
          }));
        } else {
            this.messages = []; 
        }
        this.scrollToBottom();
      }).catch(err => {
        console.error("Failed to load history", err);
      });
    },
    handleEnter(e) {
      if (!e.shiftKey) {
        e.preventDefault();
        this.sendMessage();
      }
    },
    sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return;
      
      const userMsg = this.inputMessage;
      this.messages.push({ role: 'user', content: userMsg });
      this.inputMessage = '';
      this.isLoading = true;
      this.scrollToBottom();

      this.messages.push({ role: 'ai', content: '' });
      const aiMsgIndex = this.messages.length - 1;
      
      streamChat(userMsg, this.conversationId, (chunk) => {
        this.messages[aiMsgIndex].content += chunk;
        this.scrollToBottom();
      }, (error) => {
        console.error(error);
        this.isLoading = false;
        this.messages[aiMsgIndex].content += "\n\n[能量传输中断...]";
        this.scrollToBottom();
      }, () => {
        this.isLoading = false;
        this.scrollToBottom();
      });
    },
    startNewChat() {
      this.$confirm('确定要开启新的物理实验吗？当前数据将被清除。', '新实验', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.messages = [];
          this.$message({
            type: 'success',
            message: '新实验已开启'
          });
        }).catch(() => {});
    },
    confirmClearHistory() {
      this.$confirm('此操作将永久清除云端实验数据, 是否继续?', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          clearHistory(this.conversationId).then(() => {
            this.messages = [];
            this.$message({
              type: 'success',
              message: '实验数据已清除'
            });
          });
        }).catch(() => {});
    },
    handleContentClick(e) {
      // Check if clicked element is part of a formula
      const formulaEl = e.target.closest('.katex');
      if (formulaEl) {
        this.enlargedFormulaHtml = formulaEl.outerHTML;
      }
    },
    closeEnlargedFormula() {
      this.enlargedFormulaHtml = null;
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    }
  }
}
</script>

<style scoped>
/* Core Colors */
.ai-assistant-container {
  --blue-deep: #1E3A8A;
  --blue-neon: #38BDF8;
  --silver-tech: #E2E8F0;
  --orange-warn: #F97316;
  --bg-gray: #F8FAFC;
  --text-dark: #1F2937;
  
  position: fixed;
  z-index: 2000;
  font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  user-select: none;
}

/* 1. Floating Ball Wrapper */
.floating-ball-wrapper {
  position: relative;
  display: inline-block;
}

/* 待机互动气泡 */
.idle-bubble {
  position: absolute;
  left: 70px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, rgba(255, 214, 102, 0.95), rgba(255, 180, 50, 0.95));
  padding: 10px 16px;
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(255, 180, 50, 0.4), 0 0 30px rgba(255, 214, 102, 0.3);
  white-space: nowrap;
  z-index: 100;
  animation: bubble-bounce 0.6s ease-out;
}

.bubble-text {
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 600;
  text-shadow: none;
}

.bubble-tail {
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-right: 10px solid rgba(255, 214, 102, 0.95);
}

@keyframes bubble-bounce {
  0% { opacity: 0; transform: translateY(-50%) scale(0.5) translateX(-10px); }
  50% { transform: translateY(-50%) scale(1.1) translateX(5px); }
  100% { opacity: 1; transform: translateY(-50%) scale(1) translateX(0); }
}

.bubble-pop-enter-active {
  animation: bubble-bounce 0.6s ease-out;
}

.bubble-pop-leave-active {
  animation: bubble-bounce 0.3s ease-in reverse;
}

/* 1. Floating Ball (Hexagon) */
.floating-ball {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--blue-deep) 0%, #2c5282 100%);
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275), box-shadow 0.3s;
  box-shadow: 0 4px 15px rgba(30, 58, 138, 0.4);
  position: relative;
}

.floating-ball.is-minimized {
  background: transparent;
  box-shadow: none;
  clip-path: none;
}
.floating-ball.is-minimized .atom-icon {
  transform: scale(0.8);
}

.floating-ball:hover {
  transform: scale(1.1);
  box-shadow: 0 0 15px var(--blue-neon);
}

.floating-ball::after {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.2), transparent);
    z-index: 1;
}

/* Atom Icon */
.atom-icon {
  width: 40px;
  height: 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nucleus {
  width: 8px;
  height: 8px;
  background: var(--blue-neon);
  border-radius: 50%;
  box-shadow: 0 0 5px var(--blue-neon);
  z-index: 2;
}

.orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 34px;
  height: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  transform-origin: center;
}

.orbit-1 { transform: translate(-50%, -50%) rotate(0deg); animation: spin1 4s linear infinite; }
.orbit-2 { transform: translate(-50%, -50%) rotate(60deg); animation: spin2 4s linear infinite; }
.orbit-3 { transform: translate(-50%, -50%) rotate(120deg); animation: spin3 4s linear infinite; }

@keyframes spin1 { 100% { transform: translate(-50%, -50%) rotate(360deg); } }
@keyframes spin2 { 100% { transform: translate(-50%, -50%) rotate(420deg); } }
@keyframes spin3 { 100% { transform: translate(-50%, -50%) rotate(480deg); } }

.pulse-animation {
  animation: float-pulse 3s infinite ease-in-out;
}

@keyframes float-pulse {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 2. Chat Window */
.chat-window {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 380px;
  height: 600px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--silver-tech);
}

/* Background Texture */
.bg-texture {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none;
  overflow: hidden;
  opacity: 0.05;
  z-index: 0;
}
.formula {
  position: absolute;
  font-family: 'Times New Roman', serif;
  font-style: italic;
  font-weight: bold;
  font-size: 24px;
  color: var(--text-dark);
}

/* Header */
.chat-header {
  height: 60px;
  background: linear-gradient(90deg, var(--blue-deep), #2c5282);
  padding: 0 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
  z-index: 10;
  cursor: grab;
  border-bottom: 2px solid var(--silver-tech);
}
.chat-header:active {
  cursor: grabbing;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.atom-icon-small {
  width: 24px;
  height: 24px;
  position: relative;
}
.atom-icon-small .orbit { border-color: rgba(255,255,255,0.6); }

.title {
  font-weight: 600;
  font-size: 16px;
  letter-spacing: 1px;
}

.header-actions i {
  color: var(--silver-tech);
  margin-left: 12px;
  cursor: pointer;
  transition: color 0.2s;
}
.header-actions i:hover {
  color: var(--blue-neon);
}

/* Messages Area */
.messages-area {
  flex: 1;
  background: rgba(248, 250, 252, 0.9);
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
  position: relative;
  z-index: 1;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  text-align: center;
}

.atom-icon-large {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  position: relative;
}
.atom-icon-large .orbit { border-color: #cbd5e1; }
.atom-icon-large .nucleus { background: var(--blue-deep); }

.welcome-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--blue-deep);
  margin-bottom: 8px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  animation: slide-up 0.3s ease-out;
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.atom-icon-avatar {
  width: 30px;
  height: 30px;
  position: relative;
}
.atom-icon-avatar .orbit { border-color: var(--blue-deep); }
.atom-icon-avatar .nucleus { background: var(--blue-deep); }

.user-avatar i {
  font-size: 24px;
  color: var(--blue-deep);
}

.message-bubble {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 2px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-dark);
  max-width: 85%;
  border: 1px solid var(--silver-tech);
  position: relative;
}

.message-row.user .message-bubble {
  background: var(--blue-deep);
  color: white;
  border-radius: 12px;
  border-top-right-radius: 2px;
  border: none;
}

/* Particle Loader */
.particle-loader {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  gap: 5px;
}

.particle {
  width: 6px;
  height: 6px;
  background: var(--blue-neon);
  border-radius: 50%;
  animation: particle-collision 1s infinite ease-in-out;
}
.p1 { animation-delay: 0s; }
.p2 { animation-delay: 0.2s; }
.p3 { animation-delay: 0.4s; }

@keyframes particle-collision {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.5); opacity: 1; box-shadow: 0 0 5px var(--blue-neon); }
}

.loading-text {
  font-size: 12px;
  color: #64748b;
  margin-left: 8px;
}
.loading-bubble {
  display: flex;
  align-items: center;
}

/* Input Area */
.input-area {
  padding: 15px;
  background: white;
  border-top: 1px solid var(--silver-tech);
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 10;
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.physics-input >>> .el-textarea__inner {
  border: 1px solid var(--silver-tech);
  border-radius: 4px;
  padding: 8px;
  font-family: inherit;
  transition: all 0.3s;
  background-image: linear-gradient(white 98%, transparent 2%);
  background-size: 100% 20px;
  caret-color: var(--blue-neon);
}

.physics-input >>> .el-textarea__inner:focus {
  border-color: var(--blue-neon);
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.1);
}

.send-btn-wrapper {
  width: 40px;
  height: 40px;
  cursor: pointer;
}

.send-btn-inner {
  width: 100%;
  height: 100%;
  background: var(--blue-deep);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s;
}

.send-btn-inner:hover {
  background: var(--blue-neon);
  box-shadow: 0 0 10px var(--blue-neon);
  transform: scale(1.05);
}

/* Bottom Scale */
.bottom-scale {
  height: 12px;
  background: #f1f5f9;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 10px;
  border-top: 1px solid #e2e8f0;
}
.scale-mark {
  width: 1px;
  background: #cbd5e1;
}

/* Transitions */
.expand-radial-enter-active, .expand-radial-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform-origin: bottom right;
}
.expand-radial-enter, .expand-radial-leave-to {
  transform: scale(0);
  opacity: 0;
  border-radius: 50%;
}

/* Tooltip */
.physics-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(30, 58, 138, 0.9);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 8px;
  white-space: nowrap;
  pointer-events: none;
  animation: slide-up 0.3s;
  z-index: 100;
}
.tooltip-arrow {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 5px;
  border-style: solid;
  border-color: rgba(30, 58, 138, 0.9) transparent transparent transparent;
}

/* Markdown overrides */
.markdown-body >>> p { margin-bottom: 8px; }
.markdown-body >>> pre { background: #0f172a; border-radius: 6px; padding: 10px; color: #e2e8f0; }
.markdown-body >>> code { color: #d63384; background: rgba(0,0,0,0.05); padding: 2px 4px; border-radius: 3px; }

/* Formula Overlay */
.formula-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: zoom-out;
}

.enlarged-content {
  font-size: 2em;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  border: 1px solid var(--silver-tech);
  max-width: 90%;
  overflow: auto;
}

.close-hint {
  margin-top: 20px;
  color: #94a3b8;
  font-size: 14px;
}

.fade-scale-enter-active, .fade-scale-leave-active {
  transition: all 0.3s ease;
}
.fade-scale-enter, .fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
