<template>
  <div class="physics-assistant-container" :style="containerStyle" ref="container">
    <!-- 悬浮球 (Avatar) -->
    <transition name="avatar-fade">
      <AssistantAvatar
        v-show="!isOpen"
        :is-dragging="isDragging"
        :is-minimized="isMinimized"
        :is-active="isOpen"
        :show-idle-bubble="showIdleBubble"
        @mousedown="startDrag"
        @click="handleAvatarClick"
        @mouseenter="handleAvatarMouseEnter"
      />
    </transition>

    <!-- 聊天窗口 (Interface) -->
    <transition 
      @enter="enterAnimation" 
      @leave="leaveAnimation"
      :css="false"
    >
      <div class="chat-window-wrapper" v-if="isOpen">
        <ChatInterface
          ref="chatInterface"
          :messages="messages"
          :is-loading="isLoading"
          :show-history="showHistoryList"
          :history-sessions="historySessions"
          :user-avatar="userAvatar"
          @send-message="sendMessage"
          @close="toggleWindow"
          @new-chat="startNewChat"
          @toggle-history="handleToggleHistory"
          @load-session="handleLoadSession"
          @delete-session="handleDeleteSession"
          @speech-recognize="handleSpeechRecognize"
          @header-mousedown="startDrag"
        />
      </div>
    </transition>
  </div>
</template>

<script>
import AssistantAvatar from './AssistantAvatar.vue'
import ChatInterface from './ChatInterface.vue'
import { streamChat, getHistory, clearHistory, recognizeSpeech, synthesizeSpeechStream } from '@/api/ai'
import gsap from 'gsap'

export default {
  name: 'PhysicsAssistant',
  components: {
    AssistantAvatar,
    ChatInterface
  },
  data() {
    return {
      isOpen: false,
      isMinimized: false,
      
      // 拖拽状态
      isDragging: false,
      dragStartX: 0,
      dragStartY: 0,
      initialRight: 40,
      initialBottom: 40,
      currentRight: 40,
      currentBottom: 40,
      
      // 聊天数据
      messages: [],
      isLoading: false,
      conversationId: (() => {
        try {
          const info = JSON.parse(window.localStorage.getItem('user_info') || '{}');
          const userId = info.id || window.localStorage.getItem('userId') || 'guest';
          const userType = info.userType !== undefined ? info.userType : 'common';
          return `user_${userId}_role_${userType}`;
        } catch (e) {
          return 'user_guest_role_common';
        }
      })(),
      
      clickTimer: null,
      
      // 历史会话管理
      historySessions: [],
      showHistoryList: false,
      
      // 用户信息
      userAvatar: '',
      
      // 待机气泡状态
      showIdleBubble: false,
      idleTimer: null,
      idleTimeout: 10000,
      
      // 待机语音播放
      idleAudioContext: null,
      idleAudioSource: null,
      idleEventSource: null,
      idleNextStartTime: 0,
      idleAudioSources: []
    }
  },
  computed: {
    containerStyle() {
      return {
        right: this.currentRight + 'px',
        bottom: this.currentBottom + 'px',
        zIndex: 2000,
        position: 'fixed'
      }
    }
  },
  watch: {
    isOpen(val) {
      if (val) {
        this.updateUserInfo();
      }
    }
  },
  mounted() {
    this.updateUserInfo();
    this.loadHistorySessionsFromStorage();
    const restored = this.restoreLatestSession();
    if (!restored) {
      this.loadHistory();
    }
    
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
          this.playIdleSpeech();
        }
      }, this.idleTimeout);
    },
    resetIdleTimer() {
      this.showIdleBubble = false;
      this.stopIdleSpeech();
      this.clearIdleTimer();
      this.startIdleTimer();
    },
    clearIdleTimer() {
      if (this.idleTimer) {
        clearTimeout(this.idleTimer);
        this.idleTimer = null;
      }
    },
    playIdleSpeech() {
      const text = '快来跟我玩耍吧！';
      
      this.idleAudioContext = new (window.AudioContext || window.webkitAudioContext)({
        sampleRate: 16000
      });
      this.idleNextStartTime = this.idleAudioContext.currentTime;
      this.idleAudioSources = [];

      this.idleEventSource = synthesizeSpeechStream(
        text,
        (base64Chunk) => {
          this.playIdleAudioChunk(base64Chunk);
        },
        (error) => {
          console.error('待机语音播放失败', error);
          this.cleanupIdleAudio();
        },
        () => {
          this.scheduleIdleAudioEnd();
        }
      );
    },
    playIdleAudioChunk(base64Chunk) {
      if (!this.idleAudioContext) return;
      
      try {
        const binaryString = atob(base64Chunk);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }

        const int16Array = new Int16Array(bytes.buffer);
        const float32Array = new Float32Array(int16Array.length);
        for (let i = 0; i < int16Array.length; i++) {
          float32Array[i] = int16Array[i] / 32768.0;
        }

        const audioBuffer = this.idleAudioContext.createBuffer(1, float32Array.length, 16000);
        audioBuffer.getChannelData(0).set(float32Array);

        const source = this.idleAudioContext.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(this.idleAudioContext.destination);

        const currentTime = this.idleAudioContext.currentTime;
        if (this.idleNextStartTime < currentTime) {
          this.idleNextStartTime = currentTime;
        }

        source.start(this.idleNextStartTime);
        this.idleNextStartTime += audioBuffer.duration;
        
        this.idleAudioSources.push(source);
      } catch (error) {
        console.error('播放待机音频块失败', error);
      }
    },
    scheduleIdleAudioEnd() {
      const checkEnd = () => {
        if (!this.idleAudioContext) return;
        
        if (this.idleAudioContext.currentTime >= this.idleNextStartTime - 0.1) {
          this.cleanupIdleAudio();
        } else {
          setTimeout(checkEnd, 100);
        }
      };
      setTimeout(checkEnd, 100);
    },
    cleanupIdleAudio() {
      if (this.idleEventSource) {
        this.idleEventSource.close();
        this.idleEventSource = null;
      }
      if (this.idleAudioSources) {
        this.idleAudioSources.forEach(source => {
          try { source.stop(); } catch(e) {}
        });
        this.idleAudioSources = [];
      }
      if (this.idleAudioContext) {
        try { this.idleAudioContext.close(); } catch(e) {}
        this.idleAudioContext = null;
      }
      this.idleNextStartTime = 0;
    },
    stopIdleSpeech() {
      this.cleanupIdleAudio();
    },
    stopAllAudio() {
      this.stopIdleSpeech();
      if (this.$refs.chatInterface && this.$refs.chatInterface.stopAudio) {
        this.$refs.chatInterface.stopAudio();
      }
    },
    // --- GSAP 动画 ---
    enterAnimation(el, done) {
      gsap.set(el, { 
        scale: 0, 
        opacity: 0, 
        transformOrigin: 'bottom right' 
      });
      
      gsap.to(el, {
        duration: 0.5,
        scale: 1,
        opacity: 1,
        ease: 'back.out(1.2)',
        onComplete: done
      });
    },
    leaveAnimation(el, done) {
      gsap.to(el, {
        duration: 0.3,
        scale: 0,
        opacity: 0,
        transformOrigin: 'bottom right',
        ease: 'power2.in',
        onComplete: done
      });
    },

    updateUserInfo() {
      try {
        const context = this.getUserContext();
        if (!context) return;
        this.userAvatar = context.avatar || '';

        const newConvId = `user_${context.userId}_role_${context.userType}`;
        const latestSession = this.findLatestSessionForUser(context.userId, context.userType);
        if (latestSession) {
          if (this.conversationId !== latestSession.id) {
            this.conversationId = latestSession.id;
            this.messages = JSON.parse(JSON.stringify(latestSession.messages || []));
          }
          return;
        }
        if (this.conversationId !== newConvId) {
          this.conversationId = newConvId;
          this.messages = [];
          this.loadHistory();
        }
      } catch (e) {
        console.error('Error updating user info', e);
      }
    },

    // --- 交互逻辑 ---
    startDrag(e) {
      this.isDragging = true;
      this.dragStartX = e.clientX;
      this.dragStartY = e.clientY;
      this.initialRight = this.currentRight;
      this.initialBottom = this.currentBottom;
    },
    handleMouseMove(e) {
      if (!this.isDragging) return;
      const deltaX = this.dragStartX - e.clientX;
      const deltaY = this.dragStartY - e.clientY;
      this.currentRight = this.initialRight + deltaX;
      this.currentBottom = this.initialBottom + deltaY;
    },
    handleMouseUp() {
      if (!this.isDragging) return;
      this.isDragging = false;
      this.snapToEdge();
    },
    snapToEdge() {
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      
      // 边缘吸附逻辑
      if (this.currentRight > windowWidth / 2) {
         this.currentRight = windowWidth - 80; // 左侧吸附
      } else {
         this.currentRight = 20; // 右侧吸附
      }
      
      // 垂直边界检查
      if (this.currentBottom < 20) this.currentBottom = 20;
      if (this.currentBottom > windowHeight - 100) this.currentBottom = windowHeight - 100;
    },
    handleAvatarClick() {
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
    handleAvatarMouseEnter() {
      if (this.isMinimized) this.isMinimized = false;
      this.resetIdleTimer();
    },
    toggleWindow() {
      const nextOpen = !this.isOpen;
      this.isOpen = nextOpen;
      this.resetIdleTimer();
      if (nextOpen) {
        if (this.messages.length === 0) {
          const restored = this.restoreLatestSession();
          if (!restored) {
            this.loadHistory();
          }
        }
      } else {
        this.saveCurrentSession();
        this.stopAllAudio();
      }
    },

    // --- 聊天业务逻辑 ---
    loadHistorySessionsFromStorage() {
      try {
        const saved = localStorage.getItem('physics_assistant_history_v1');
        if (saved) {
          this.historySessions = JSON.parse(saved);
        }
      } catch (e) {
        console.error('Failed to load local history', e);
      }
    },
    restoreLatestSession() {
      const context = this.getUserContext();
      if (!context) return false;
      const candidates = this.historySessions.filter(s => s.userId === context.userId && s.userType === context.userType);
      if (!candidates || candidates.length === 0) {
        return false;
      }
      const latest = candidates.reduce((prev, cur) => {
        if (!prev) return cur;
        const prevTime = prev.timestamp || 0;
        const curTime = cur.timestamp || 0;
        return curTime > prevTime ? cur : prev;
      }, null);
      if (latest && Array.isArray(latest.messages)) {
        this.messages = JSON.parse(JSON.stringify(latest.messages));
        this.conversationId = latest.id;
        return true;
      }
      return false;
    },
    saveHistorySessionsToStorage() {
      try {
        localStorage.setItem('physics_assistant_history_v1', JSON.stringify(this.historySessions));
      } catch (e) {
        console.error('Failed to save local history', e);
      }
    },
    saveCurrentSession() {
      if (this.messages.length === 0) return;
      
      const context = this.getUserContext();
      const firstUserMsg = this.messages.find(m => m.role === 'user');
      const title = firstUserMsg ? firstUserMsg.content.substring(0, 15) : '未命名实验';
      const lastMsg = this.messages[this.messages.length - 1];
      const preview = lastMsg ? lastMsg.content.substring(0, 20) + '...' : '';
      
      const session = {
        id: this.conversationId,
        title: title,
        preview: preview,
        dateStr: new Date().toLocaleString(),
        messages: JSON.parse(JSON.stringify(this.messages)),
        timestamp: Date.now(),
        userId: context ? context.userId : 'guest',
        userType: context ? context.userType : 'common'
      };
      
      const index = this.historySessions.findIndex(s => s.id === session.id);
      if (index !== -1) {
        this.historySessions.splice(index, 1, session);
      } else {
        this.historySessions.unshift(session);
      }
      this.saveHistorySessionsToStorage();
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
      }).catch(err => {
        console.error("Failed to load history", err);
      });
    },
    sendMessage(content) {
      // Increment AI call counter for dashboard stats
      try {
          let count = parseInt(localStorage.getItem('ai_call_count') || '0');
          localStorage.setItem('ai_call_count', (count + 1).toString());
      } catch (e) {
          console.error('Failed to update AI call count', e);
      }

      this.messages.push({ role: 'user', content });
      this.isLoading = true;
      let aiMsgAdded = false;

      streamChat(content, this.conversationId, (chunk) => {
        if (!aiMsgAdded) {
            this.messages.push({ role: 'ai', content: '' });
            aiMsgAdded = true;
            this.isLoading = false; 
        }
        this.messages[this.messages.length - 1].content += chunk;
      }, (error) => {
        console.error(error);
        if (!aiMsgAdded) {
             this.messages.push({ role: 'ai', content: '' });
             aiMsgAdded = true;
        }
        this.isLoading = false;
        this.messages[this.messages.length - 1].content += "\n\n[连接中断，量子纠缠失败...]";
      }, () => {
        this.isLoading = false;
        // 对话结束后自动保存
        this.saveCurrentSession();
      });
    },
    startNewChat() {
      this.$confirm('确定要开启新的对话吗？当前会话将自动存档。', '新对话', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          this.saveCurrentSession();
          this.messages = [];
          const context = this.getUserContext();
          const userId = context ? context.userId : 'guest';
          const userType = context ? context.userType : 'common';
          this.conversationId = `session_${userId}_${userType}_${Date.now()}`;
          this.showHistoryList = false;
          this.$message({ type: 'success', message: '新对话已开启' });
        }).catch(() => {});
    },
    handleToggleHistory() {
      if (!this.showHistoryList) {
        this.saveCurrentSession();
      }
      this.showHistoryList = !this.showHistoryList;
    },
    handleLoadSession(session) {
      this.saveCurrentSession();
      this.messages = JSON.parse(JSON.stringify(session.messages));
      this.conversationId = session.id;
      this.showHistoryList = false;
      this.$message({ type: 'success', message: '已加载对话存档' });
    },
    handleDeleteSession(session) {
      const index = this.historySessions.findIndex(s => s.id === session.id);
      if (index === -1) return;
      
      this.historySessions.splice(index, 1);
      this.saveHistorySessionsToStorage();
      
      clearHistory(session.id).then(() => {
        this.$message({ type: 'success', message: '对话记录已删除' });
      }).catch(err => {
        console.error('Failed to clear server history', err);
      });
      
      if (this.conversationId === session.id) {
        this.messages = [];
        const context = this.getUserContext();
        const userId = context ? context.userId : 'guest';
        const userType = context ? context.userType : 'common';
        this.conversationId = `session_${userId}_${userType}_${Date.now()}`;
      }
    },
    async handleSpeechRecognize(audioBlob) {
      this.$message({ type: 'info', message: '正在识别语音...' });
      try {
        const response = await recognizeSpeech(audioBlob);
        if (response.success && response.data && response.data.text) {
          const recognizedText = response.data.text;
          if (recognizedText && recognizedText.trim()) {
            this.sendMessage(recognizedText.trim());
          } else {
            this.$message.warning('未识别到有效语音内容');
          }
        } else {
          this.$message.error(response.message || '语音识别失败');
        }
      } catch (error) {
        console.error('语音识别失败', error);
        this.$message.error('语音识别服务异常，请稍后重试');
      }
    },
    getUserContext() {
      try {
        const userInfoStr = localStorage.getItem('user_info');
        if (userInfoStr) {
          const userInfo = JSON.parse(userInfoStr);
          const userId = userInfo.id || window.localStorage.getItem('userId') || 'guest';
          const userType = userInfo.userType !== undefined ? userInfo.userType : 'common';
          return { userId, userType, avatar: userInfo.avatar || '' };
        }
      } catch (e) {
        return null;
      }
      const fallbackId = window.localStorage.getItem('userId') || 'guest';
      return { userId: fallbackId, userType: 'common', avatar: '' };
    },
    findLatestSessionForUser(userId, userType) {
      if (!this.historySessions || this.historySessions.length === 0) return null;
      const candidates = this.historySessions.filter(s => s.userId === userId && s.userType === userType);
      if (candidates.length === 0) return null;
      return candidates.reduce((prev, cur) => {
        if (!prev) return cur;
        const prevTime = prev.timestamp || 0;
        const curTime = cur.timestamp || 0;
        return curTime > prevTime ? cur : prev;
      }, null);
    }
  }
}
</script>

<style scoped>
.physics-assistant-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-window-wrapper {
  width: 480px;
  height: 700px;
  max-width: 90vw;
  max-height: 80vh;
  margin-bottom: 20px;
  transform-origin: bottom right;
}

/* 动画过渡 */
.avatar-fade-enter-active, .avatar-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.avatar-fade-enter, .avatar-fade-leave-to {
  opacity: 0;
  transform: scale(0.5);
}
</style>
