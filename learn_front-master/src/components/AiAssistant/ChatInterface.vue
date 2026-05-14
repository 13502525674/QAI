<template>
  <div class="chat-interface">
    <!-- 背景纹理 -->
    <div class="bg-grid"></div>
    <div class="bg-formulas">
      <span class="bg-formula f1" style="top: 10%; left: 5%">E=mc²</span>
      <span class="bg-formula f2" style="top: 20%; right: 10%">F=ma</span>
      <span class="bg-formula f3" style="bottom: 15%; left: 8%">iℏ∂ψ/∂t=Ĥψ</span>
    </div>

    <!-- 顶部导航栏 -->
    <div class="chat-header" @mousedown="$emit('header-mousedown', $event)">
      <div class="header-title">
        <i class="el-icon-cpu"></i>
        <span>物理高级助教——布莱克</span>
      </div>
      <div class="header-controls">
        <el-tooltip content="新对话" placement="top">
          <i class="el-icon-refresh action-icon" @click="$emit('new-chat')"></i>
        </el-tooltip>
        <el-tooltip content="历史对话" placement="top">
          <i class="el-icon-time action-icon" @click="$emit('toggle-history')"></i>
        </el-tooltip>
        <i class="el-icon-close close-icon" @click="$emit('close')"></i>
      </div>
    </div>

    <!-- 消息区域 -->
    <div class="messages-container" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="hologram-effect">
          <div class="scanner-line"></div>
          <i class="el-icon-s-opportunity" style="font-size: 48px; color: #38BDF8;"></i>
        </div>
        <p>初始化物理探索程序...</p>
        <p class="sub-hint">您可以询问任何物理概念、公式或现象</p>
      </div>

      <transition-group name="message-fade">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-row"
          :class="[msg.role, { 'qa-pair-end': isQaPairEnd(index) }]"
        >
          <div v-if="msg.role === 'ai'" class="avatar ai-avatar">
            <div class="ai-orbit"></div>
            <div class="ai-core"></div>
            <div class="ai-wave"></div>
          </div>

          <div class="message-bubble-wrapper">
            <div class="message-bubble">
              <div v-if="msg.role === 'ai'" class="markdown-body" v-html="formatMessage(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
              <div class="bubble-corner"></div>
            </div>
            <div v-if="msg.role === 'ai'" class="message-actions">
              <el-tooltip :content="msg.copied ? '已复制!' : '复制回答'" placement="top">
                <button class="copy-btn" :class="{ 'copied': msg.copied }" @click="copyMessage(msg)">
                  <i :class="msg.copied ? 'el-icon-check' : 'el-icon-document-copy'"></i>
                </button>
              </el-tooltip>
              <el-tooltip :content="msg.playing ? '停止播放' : '朗读回答'" placement="top">
                <button class="speak-btn" :class="{ 'playing': msg.playing }" @click="speakMessage(msg)">
                  <i :class="msg.playing ? 'el-icon-video-pause' : 'el-icon-headset'"></i>
                </button>
              </el-tooltip>
            </div>
          </div>

          <div v-if="msg.role === 'user'" class="avatar user-avatar">
            <div class="default-user-avatar">
              <span class="user-initial">me</span>
            </div>
          </div>
        </div>
      </transition-group>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="message-row ai">
        <div class="avatar ai-avatar">
          <div class="ai-orbit"></div>
          <div class="ai-core"></div>
          <div class="ai-wave"></div>
        </div>
        <div class="message-bubble loading-bubble">
          <div class="quantum-loader">
            <span></span><span></span><span></span>
          </div>
          <span class="thinking-text">头脑风暴ing</span>
        </div>
      </div>

    </div>

    <!-- 输入区域 -->
    <div class="input-section" :class="{ 'is-focused': isInputFocused }">
      <div class="input-wrapper">
        <textarea
          v-model="localInput"
          placeholder="在此输入物理问题..."
          rows="1"
          @focus="isInputFocused = true"
          @blur="isInputFocused = false"
          @keydown.enter.prevent="handleEnter"
          @input="autoResize"
          ref="textarea"
          class="custom-textarea"
        ></textarea>
      </div>
      <div class="action-buttons">
        <button 
          class="voice-btn" 
          :class="{ 'is-recording': isRecording }"
          @click="toggleRecording"
          :disabled="isLoading"
        >
          <div v-if="isRecording" class="recording-animation">
            <span class="wave w1"></span>
            <span class="wave w2"></span>
            <span class="wave w3"></span>
            <span class="wave w4"></span>
            <span class="wave w5"></span>
          </div>
          <i class="el-icon-microphone" v-else></i>
        </button>
        <button 
          class="send-btn" 
          :class="{ 'is-loading': isLoading }"
          @click="handleSend"
          :disabled="isLoading || !localInput.trim()"
        >
          <i class="el-icon-loading" v-if="isLoading"></i>
          <i class="el-icon-s-promotion" v-else></i>
        </button>
      </div>
    </div>

    <!-- 历史记录覆盖层 -->
    <transition name="fade">
      <div v-if="showHistory" class="history-overlay">
        <div class="history-header-title">
          <span><i class="el-icon-files"></i> 对话档案库</span>
        </div>
        <div class="history-list">
          <div 
            v-for="session in historySessions" 
            :key="session.id" 
            class="history-item"
          >
            <div class="history-info" @click="$emit('load-session', session)">
              <span class="history-title">{{ session.title || '未命名对话' }}</span>
              <span class="history-preview">{{ session.preview }}</span>
            </div>
            <div class="history-actions">
              <span class="history-date">{{ session.dateStr }}</span>
              <el-tooltip content="删除此记录" placement="top">
                <i class="el-icon-delete delete-btn" @click.stop="handleDeleteSession(session)"></i>
              </el-tooltip>
            </div>
          </div>
          <div v-if="historySessions.length === 0" class="empty-history">
            <i class="el-icon-folder-opened"></i>
            <p>暂无对话存档</p>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it'
import mk from 'markdown-it-katex'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import gsap from 'gsap'
import { synthesizeSpeechStream } from '@/api/ai'

const md = new MarkdownIt({
  html: true,
  breaks: true,
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
  name: 'ChatInterface',
  props: {
    messages: {
      type: Array,
      default: () => []
    },
    isLoading: Boolean,
    showHistory: Boolean,
    historySessions: {
      type: Array,
      default: () => []
    },
    userAvatar: String
  },
  data() {
    return {
      localInput: '',
      isInputFocused: false,
      isRecording: false,
      mediaRecorder: null,
      audioChunks: [],
      recordingTimer: null,
      recordingDuration: 0,
      audioContext: null,
      audioStream: null,
      audioWorkletNode: null,
      pcmData: [],
      ttsAudioContext: null,
      ttsAudioSource: null,
      currentPlayingMsg: null,
      currentEventSource: null,
      ttsNextStartTime: 0,
      ttsAudioSources: []
    }
  },
  watch: {
    messages() {
      this.scrollToBottom();
    }
  },
  mounted() {
    this.initBackgroundAnimations();
  },
  beforeDestroy() {
    this.stopAudio();
  },
  methods: {
    initBackgroundAnimations() {
      // 浮动公式动画 (模拟失重感)
      gsap.utils.toArray('.bg-formula').forEach((el) => {
        gsap.to(el, {
          y: 'random(-30, 30)',
          x: 'random(-20, 20)',
          rotation: 'random(-15, 15)',
          duration: 'random(4, 8)',
          repeat: -1,
          yoyo: true,
          ease: 'sine.inOut',
          delay: 'random(0, 2)'
        });
      });
      
      // 网格呼吸效果
      gsap.to('.bg-grid', {
        opacity: 0.3, // 微调透明度变化
        duration: 5,
        repeat: -1,
        yoyo: true,
        ease: 'sine.inOut'
      });
    },
    formatMessage(content) {
      if (!content) return '';
      let processed = content.replace(/\r\n/g, '\n');
      processed = processed
        .replace(/([^\n])\s*(\d{1,2})[、.]\s*(?=\S)/g, '$1\n$2. ')
        .replace(/([^\n])\s*-\s+(?=\S)/g, '$1\n- ')
        .replace(/([^\n])(\d+)[、.]\s*(结论|分析|公式|步骤|总结|要点|说明|注意|结果)/g, '$1\n$2. $3');
      processed = processed
        .replace(/—####/g, '\n#### ')
        .replace(/####(\d)/g, '#### $1')
        .replace(/^\s*(\d+)[、.]\s*(.+)$/gm, (_, num, text) => this.formatNumberedHeading(num, text))
        .replace(/^\s*([一二三四五六七八九十]+)[、.]\s*(.+)$/gm, (_, cn, text) => {
          const num = this.convertCnNumber(cn);
          return num ? this.formatNumberedHeading(num, text) : `${cn}、${text}`;
        })
        .replace(/^\s*(结论|分析|公式|步骤|总结|要点|说明|注意|结果)[:：]\s*/gm, '#### $1\n');
      return md.render(processed);
    },
    formatNumberedHeading(num, text) {
      const raw = (text || '').trim();
      if (!raw) return `### ${num}.`;
      const match = raw.match(/^(.+?)[:：]\s*(.+)$/);
      if (match) {
        const heading = match[1].trim();
        const body = match[2].trim();
        return `### ${num}. ${heading}\n\n${body}`;
      }
      return `### ${num}. ${raw}\n`;
    },
    convertCnNumber(input) {
      const map = {
        一: 1, 二: 2, 三: 3, 四: 4, 五: 5,
        六: 6, 七: 7, 八: 8, 九: 9, 十: 10
      };
      if (!input) return null;
      if (input === '十') return 10;
      if (input.length === 1) return map[input] || null;
      if (input.length === 2 && input[0] === '十') {
        const unit = map[input[1]];
        return unit ? 10 + unit : null;
      }
      if (input.length === 2 && input[1] === '十') {
        const tens = map[input[0]];
        return tens ? tens * 10 : null;
      }
      if (input.length === 3 && input[1] === '十') {
        const tens = map[input[0]];
        const unit = map[input[2]];
        return tens && unit ? tens * 10 + unit : null;
      }
      return null;
    },
    handleSend() {
      if (!this.localInput.trim() || this.isLoading) return;
      this.$emit('send-message', this.localInput);
      this.localInput = '';
      this.$nextTick(() => {
        this.autoResize();
      });
    },
    autoResize() {
      const textarea = this.$refs.textarea;
      if (textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
      }
    },
    handleEnter(e) {
      if (!e.shiftKey) {
        this.handleSend();
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    },
    handleDeleteSession(session) {
      this.$confirm('确定要删除此对话记录吗？删除后无法恢复。', '删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('delete-session', session);
      }).catch(() => {});
    },
    copyMessage(msg) {
      const text = msg.content;
      navigator.clipboard.writeText(text).then(() => {
        this.$set(msg, 'copied', true);
        setTimeout(() => {
          this.$set(msg, 'copied', false);
        }, 2000);
      }).catch(() => {
        this.$message.error('复制失败');
      });
    },
    speakMessage(msg) {
      if (msg.playing) {
        this.stopAudio();
        this.$set(msg, 'playing', false);
        this.currentPlayingMsg = null;
        return;
      }

      this.stopAudio();
      
      this.messages.forEach(m => {
        if (m.playing) {
          this.$set(m, 'playing', false);
        }
      });

      this.$set(msg, 'playing', true);
      this.currentPlayingMsg = msg;

      const plainText = msg.content.replace(/[#*`_\[\]()]/g, '').replace(/\n+/g, ' ');
      
      this.audioChunks = [];
      this.initStreamAudioContext();

      this.currentEventSource = synthesizeSpeechStream(
        plainText,
        (base64Chunk) => {
          if (this.currentPlayingMsg !== msg) return;
          this.playAudioChunk(base64Chunk);
        },
        (error) => {
          console.error('流式语音合成失败', error);
          this.$message.error('语音合成服务异常');
          this.$set(msg, 'playing', false);
          this.currentPlayingMsg = null;
          this.cleanupStreamAudio();
        },
        () => {
          if (this.currentPlayingMsg === msg) {
            this.scheduleStreamEnd(msg);
          }
        }
      );
    },
    initStreamAudioContext() {
      if (this.ttsAudioContext) {
        try { this.ttsAudioContext.close(); } catch(e) {}
      }
      this.ttsAudioContext = new (window.AudioContext || window.webkitAudioContext)({
        sampleRate: 16000
      });
      this.ttsNextStartTime = this.ttsAudioContext.currentTime;
      this.ttsAudioSources = [];
    },
    playAudioChunk(base64Chunk) {
      if (!this.ttsAudioContext) return;
      
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

        const audioBuffer = this.ttsAudioContext.createBuffer(1, float32Array.length, 16000);
        audioBuffer.getChannelData(0).set(float32Array);

        const source = this.ttsAudioContext.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(this.ttsAudioContext.destination);

        const currentTime = this.ttsAudioContext.currentTime;
        if (this.ttsNextStartTime < currentTime) {
          this.ttsNextStartTime = currentTime;
        }

        source.start(this.ttsNextStartTime);
        this.ttsNextStartTime += audioBuffer.duration;
        
        this.ttsAudioSources.push(source);
      } catch (error) {
        console.error('播放音频块失败', error);
      }
    },
    scheduleStreamEnd(msg) {
      const checkEnd = () => {
        if (!this.ttsAudioContext || this.currentPlayingMsg !== msg) return;
        
        if (this.ttsAudioContext.currentTime >= this.ttsNextStartTime - 0.1) {
          this.$set(msg, 'playing', false);
          this.currentPlayingMsg = null;
          this.cleanupStreamAudio();
        } else {
          setTimeout(checkEnd, 100);
        }
      };
      setTimeout(checkEnd, 100);
    },
    cleanupStreamAudio() {
      if (this.currentEventSource) {
        this.currentEventSource.close();
        this.currentEventSource = null;
      }
      if (this.ttsAudioSources) {
        this.ttsAudioSources.forEach(source => {
          try { source.stop(); } catch(e) {}
        });
        this.ttsAudioSources = [];
      }
      if (this.ttsAudioContext) {
        try { this.ttsAudioContext.close(); } catch(e) {}
        this.ttsAudioContext = null;
      }
      this.ttsNextStartTime = 0;
    },
    stopAudio() {
      this.cleanupStreamAudio();
      if (this.currentPlayingMsg) {
        this.$set(this.currentPlayingMsg, 'playing', false);
        this.currentPlayingMsg = null;
      }
    },
    isQaPairEnd(index) {
      if (index === this.messages.length - 1) return true;
      const currentMsg = this.messages[index];
      const nextMsg = this.messages[index + 1];
      return currentMsg.role === 'ai' && nextMsg && nextMsg.role === 'user';
    },
    async toggleRecording() {
      if (this.isRecording) {
        this.stopRecording();
      } else {
        await this.startRecording();
      }
    },
    async startRecording() {
      try {
        this.audioStream = await navigator.mediaDevices.getUserMedia({ 
          audio: {
            sampleRate: 16000,
            channelCount: 1,
            echoCancellation: true,
            noiseSuppression: true
          } 
        });
        
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)({
          sampleRate: 16000
        });
        
        const source = this.audioContext.createMediaStreamSource(this.audioStream);
        const bufferSize = 4096;
        const scriptProcessor = this.audioContext.createScriptProcessor(bufferSize, 1, 1);
        
        this.pcmData = [];
        
        scriptProcessor.onaudioprocess = (e) => {
          const inputData = e.inputBuffer.getChannelData(0);
          const pcm16 = this.float32To16BitPCM(inputData);
          this.pcmData.push(pcm16);
        };
        
        source.connect(scriptProcessor);
        scriptProcessor.connect(this.audioContext.destination);
        
        this.scriptProcessor = scriptProcessor;
        this.audioSource = source;
        
        this.isRecording = true;
        this.recordingDuration = 0;
        
        this.recordingTimer = setInterval(() => {
          this.recordingDuration++;
          if (this.recordingDuration >= 60) {
            this.stopRecording();
          }
        }, 1000);
        
      } catch (error) {
        console.error('无法访问麦克风:', error);
        this.$message.error('无法访问麦克风，请检查权限设置');
      }
    },
    float32To16BitPCM(float32Array) {
      const buffer = new ArrayBuffer(float32Array.length * 2);
      const view = new DataView(buffer);
      for (let i = 0; i < float32Array.length; i++) {
        let s = Math.max(-1, Math.min(1, float32Array[i]));
        view.setInt16(i * 2, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
      }
      return buffer;
    },
    stopRecording() {
      if (this.audioContext && this.isRecording) {
        this.scriptProcessor.disconnect();
        this.audioSource.disconnect();
        this.audioContext.close();
        
        if (this.audioStream) {
          this.audioStream.getTracks().forEach(track => track.stop());
        }
        
        this.isRecording = false;
        
        if (this.recordingTimer) {
          clearInterval(this.recordingTimer);
          this.recordingTimer = null;
        }
        
        const totalLength = this.pcmData.reduce((acc, buf) => acc + buf.byteLength, 0);
        const pcmBuffer = new Uint8Array(totalLength);
        let offset = 0;
        for (const chunk of this.pcmData) {
          pcmBuffer.set(new Uint8Array(chunk), offset);
          offset += chunk.byteLength;
        }
        
        const wavBlob = this.encodeWAV(pcmBuffer.buffer, 16000, 1, 16);
        this.sendAudioForRecognition(wavBlob);
      }
    },
    encodeWAV(samples, sampleRate, numChannels, bitsPerSample) {
      const buffer = new ArrayBuffer(44 + samples.byteLength);
      const view = new DataView(buffer);
      
      const writeString = (view, offset, string) => {
        for (let i = 0; i < string.length; i++) {
          view.setUint8(offset + i, string.charCodeAt(i));
        }
      };
      
      writeString(view, 0, 'RIFF');
      view.setUint32(4, 36 + samples.byteLength, true);
      writeString(view, 8, 'WAVE');
      writeString(view, 12, 'fmt ');
      view.setUint32(16, 16, true);
      view.setUint16(20, 1, true);
      view.setUint16(22, numChannels, true);
      view.setUint32(24, sampleRate, true);
      view.setUint32(28, sampleRate * numChannels * bitsPerSample / 8, true);
      view.setUint16(32, numChannels * bitsPerSample / 8, true);
      view.setUint16(34, bitsPerSample, true);
      writeString(view, 36, 'data');
      view.setUint32(40, samples.byteLength, true);
      
      const pcmData = new Uint8Array(samples);
      const outputData = new Uint8Array(buffer, 44);
      outputData.set(pcmData);
      
      return new Blob([buffer], { type: 'audio/wav' });
    },
    async sendAudioForRecognition(audioBlob) {
      this.$emit('speech-recognize', audioBlob);
    }
  }
}
</script>

<style scoped>
/* Variables */
.chat-interface {
  --bg-color: rgba(10, 15, 30, 0.95); /* 深邃夜空蓝黑 */
  --border-color: rgba(100, 143, 255, 0.3); /* 更深邃的蓝光边框 */
  --accent-color: #648fff; /* 物理蓝光主题色 */
  --text-primary: #E6F0FF; /* 更亮的文本色 */
  --text-secondary: #A3BFF0; /* 柔和的辅助文字 */
  --bubble-ai: rgba(25, 35, 65, 0.85); /* 更柔和的AI气泡 */
  --bubble-user: rgba(100, 143, 255, 0.25); /* 主题色用户气泡 */
  
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: var(--bg-color);
  color: var(--text-primary);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.6), 0 0 0 1px var(--border-color);
  backdrop-filter: blur(12px);
  position: relative;
}

/* 背景纹理 */
.bg-grid {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: 
    linear-gradient(rgba(100, 143, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(100, 143, 255, 0.06) 1px, transparent 1px);
  background-size: 45px 45px;
  pointer-events: none;
  z-index: 0;
  opacity: 0.4;
}

.bg-formulas {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.bg-formula {
  position: absolute;
  font-family: 'Georgia', 'Times New Roman', serif;
  font-style: italic;
  color: rgba(182, 215, 255, 0.08);
  font-size: 26px;
  user-select: none;
  font-weight: bold;
}

/* Header */
.chat-header {
  padding: 18px 24px;
  background: linear-gradient(90deg, rgba(20, 30, 50, 0.85), rgba(25, 40, 70, 0.85));
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
  cursor: grab;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
.chat-header:active {
  cursor: grabbing;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #8ECFFF;
  font-size: 16px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.header-controls {
  display: flex;
  gap: 18px;
  align-items: center;
}

.action-icon {
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 18px;
  padding: 4px;
  border-radius: 6px;
}
.action-icon:hover { 
  color: #B6D7FF; 
  background: rgba(100, 143, 255, 0.15);
  transform: scale(1.1);
}

.close-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  padding: 4px;
  border-radius: 6px;
}
.close-icon:hover { 
  transform: rotate(90deg) scale(1.1); 
  color: #FF9EB3; 
  background: rgba(255, 158, 179, 0.15);
}

/* Messages */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  z-index: 1;
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) transparent;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}
.messages-container::-webkit-scrollbar-thumb {
  background-color: rgba(56, 189, 248, 0.2);
  border-radius: 3px;
}

.empty-state {
  margin: auto;
  text-align: center;
  color: var(--text-secondary);
}
.hologram-effect {
  position: relative;
  margin-bottom: 20px;
}
.scanner-line {
  position: absolute;
  top: 0; left: 50%; width: 100px; height: 2px;
  background: var(--accent-color);
  transform: translateX(-50%);
  animation: scan 2s infinite ease-in-out;
  box-shadow: 0 0 10px var(--accent-color);
  opacity: 0.5;
}
@keyframes scan {
  0%, 100% { top: 0; opacity: 0; }
  50% { top: 100%; opacity: 1; }
}

/* Message Rows */
.message-row {
  display: flex;
  gap: 12px;
  max-width: 90%;
}
.message-row.user {
  align-self: flex-end;
  margin-left: auto;
  justify-content: flex-end;
}
.message-row.qa-pair-end {
  margin-bottom: 16px;
}
.message-bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.message-actions {
  display: flex;
  justify-content: flex-start;
  opacity: 0;
  transition: opacity 0.2s ease;
}
.message-row:hover .message-actions {
  opacity: 1;
}
.copy-btn {
  background: rgba(100, 143, 255, 0.1);
  border: 1px solid rgba(100, 143, 255, 0.3);
  color: var(--text-secondary);
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}
.copy-btn:hover {
  background: rgba(100, 143, 255, 0.2);
  color: var(--text-primary);
  border-color: var(--accent-color);
}
.copy-btn.copied {
  background: rgba(52, 211, 153, 0.2);
  border-color: rgba(52, 211, 153, 0.5);
  color: #34D399;
}
.speak-btn {
  background: rgba(100, 143, 255, 0.1);
  border: 1px solid rgba(100, 143, 255, 0.3);
  color: var(--text-secondary);
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}
.speak-btn:hover {
  background: rgba(100, 143, 255, 0.2);
  color: var(--text-primary);
  border-color: var(--accent-color);
}
.speak-btn.playing {
  background: rgba(255, 107, 157, 0.2);
  border-color: rgba(255, 107, 157, 0.5);
  color: #FF6B9D;
  animation: pulse-speak 1.5s infinite;
}
@keyframes pulse-speak {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 107, 157, 0.4); }
  50% { box-shadow: 0 0 0 6px rgba(255, 107, 157, 0); }
}

.avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-avatar {
  background: radial-gradient(circle at 35% 30%, rgba(255, 214, 102, 0.25), rgba(15, 23, 42, 0.9) 65%);
  border: 1px solid rgba(255, 214, 102, 0.35);
  position: relative;
  overflow: hidden;
}
.ai-orbit {
  position: absolute;
  width: 22px; height: 22px;
  border: 1px solid rgba(255, 214, 102, 0.5);
  border-radius: 50%;
  animation: spin 4s linear infinite;
}
.ai-core {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: rgba(255, 214, 102, 0.9);
  box-shadow: 0 0 6px rgba(255, 214, 102, 0.8);
}
.ai-wave {
  position: absolute;
  width: 30px; height: 30px;
  border-radius: 50%;
  border: 1px dashed rgba(255, 214, 102, 0.35);
  animation: spin 6s linear infinite reverse;
}
@keyframes spin { to { transform: rotate(360deg); } }

.user-avatar {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.5);
  color: #E2E8F0;
  overflow: hidden;
}
.default-user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #648fff, #8ECFFF);
  border-radius: 50%;
  color: white;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(100, 143, 255, 0.3);
}

.user-initial {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 16px;
  position: relative;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  letter-spacing: 0.1px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.loading-bubble {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 140px;
  padding: 18px 22px;
}

.thinking-text {
  font-size: 13px;
  color: var(--text-secondary);
  animation: pulse 1.5s infinite;
  font-style: italic;
  font-weight: 500;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.message-row.ai .message-bubble {
  background: var(--bubble-ai);
  border: 1px solid var(--border-color);
  border-top-left-radius: 4px;
  box-shadow: inset 0 0 15px rgba(0, 0, 0, 0.1), 0 4px 12px rgba(0, 0, 0, 0.1);
}

.message-row.user .message-bubble {
  background: var(--bubble-user);
  border: 1px solid rgba(100, 143, 255, 0.4);
  border-top-right-radius: 4px;
  color: #FFFFFF;
  box-shadow: inset 0 0 10px rgba(100, 143, 255, 0.1), 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Input Section */
.input-section {
  padding: 18px;
  background: rgba(10, 15, 30, 0.85);
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  z-index: 10;
  transition: all 0.3s;
}

.input-section.is-focused {
  background: rgba(10, 15, 30, 0.95);
  box-shadow: 0 -8px 25px rgba(100, 143, 255, 0.1);
}

.input-wrapper {
  flex: 1;
  position: relative;
  background: rgba(20, 30, 50, 0.4);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.2);
}

.input-wrapper:focus-within {
  border-color: #8ECFFF;
  box-shadow: 
    inset 0 0 15px rgba(100, 143, 255, 0.2),
    0 0 15px rgba(100, 143, 255, 0.2);
}

.custom-textarea {
  width: 100%;
  background: transparent;
  border: none;
  color: #E6F0FF;
  padding: 14px;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  resize: none;
  outline: none;
  min-height: 45px;
  max-height: 150px;
  font-size: 14px;
  line-height: 1.6;
  overflow-y: auto;
  box-sizing: border-box;
}

.action-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.voice-btn {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: rgba(20, 30, 50, 0.6);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 18px;
}

.voice-btn:hover:not(:disabled) {
  border-color: #FF6B9D;
  color: #FF6B9D;
  background: rgba(255, 107, 157, 0.1);
  transform: scale(1.05);
}

.voice-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.voice-btn.is-recording {
  background: linear-gradient(135deg, #FF6B9D, #FF8E53);
  border-color: transparent;
  color: white;
  animation: pulse-record 1.5s infinite;
}

@keyframes pulse-record {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 107, 157, 0.4); }
  50% { box-shadow: 0 0 0 10px rgba(255, 107, 157, 0); }
}

.recording-animation {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  height: 20px;
}

.recording-animation .wave {
  width: 3px;
  height: 8px;
  background: white;
  border-radius: 2px;
  animation: wave-animation 0.8s ease-in-out infinite;
}

.recording-animation .w1 { animation-delay: 0s; }
.recording-animation .w2 { animation-delay: 0.1s; }
.recording-animation .w3 { animation-delay: 0.2s; }
.recording-animation .w4 { animation-delay: 0.3s; }
.recording-animation .w5 { animation-delay: 0.4s; }

@keyframes wave-animation {
  0%, 100% { height: 8px; }
  50% { height: 18px; }
}

.send-btn {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #648fff, #8ECFFF);
  color: #FFFFFF;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(100, 143, 255, 0.3);
  font-size: 18px;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(100, 143, 255, 0.5);
  background: linear-gradient(135deg, #8ECFFF, #648fff);
}

.send-btn:disabled {
  background: #334155;
  color: #64748B;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.send-btn:disabled {
  background: #334155;
  color: #64748B;
  cursor: not-allowed;
}

/* Loader */
.quantum-loader span {
  display: inline-block;
  width: 6px; height: 6px;
  background: var(--accent-color);
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 0.6s infinite alternate;
}
.quantum-loader span:nth-child(2) { animation-delay: 0.2s; }
.quantum-loader span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce { to { transform: translateY(-6px); } }

/* Markdown Styles - 物理主题优化 */
.markdown-body >>> h1, 
.markdown-body >>> h2, 
.markdown-body >>> h3, 
.markdown-body >>> h4 { 
  color: #8ECFFF; /* 物理蓝光高亮色 */
  margin-top: 1em; 
  margin-bottom: 0.5em; 
  font-weight: 700;
  line-height: 1.3;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}
.markdown-body >>> h1 { font-size: 1.5em; border-bottom: 2px solid rgba(100, 143, 255, 0.3); padding-bottom: 0.3em; }
.markdown-body >>> h2 { font-size: 1.3em; }
.markdown-body >>> h3 { font-size: 1.1em; }
.markdown-body >>> h4 { font-size: 1.0em; color: #B6D7FF; }

.markdown-body >>> p { 
  margin-bottom: 1em; 
  line-height: 1.8; 
  text-align: justify;
  hyphens: auto;
}
.markdown-body >>> h3 + p,
.markdown-body >>> h3 + ul,
.markdown-body >>> h3 + ol {
  margin-top: 0.6em;
}
.markdown-body >>> ul,
.markdown-body >>> ol {
  margin-top: 0.6em;
  margin-bottom: 1em;
}

.markdown-body >>> ul, .markdown-body >>> ol { 
  padding-left: 1.8em; 
  margin-bottom: 1em; 
}
.markdown-body >>> li { 
  margin-bottom: 0.6em; 
  position: relative;
}
.markdown-body >>> li::marker {
  color: var(--accent-color);
}

.markdown-body >>> pre { 
  background: #0F172A; 
  border-radius: 10px; 
  padding: 16px; 
  overflow-x: auto; 
  border: 1px solid rgba(100, 143, 255, 0.2);
  margin: 1.2em 0;
  box-shadow: inset 0 0 15px rgba(0, 0, 0, 0.3);
}

.markdown-body >>> code { 
  color: #F8BBD9; 
  background: rgba(255, 255, 255, 0.08); 
  padding: 4px 8px; 
  border-radius: 6px; 
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace; 
  font-size: 0.95em;
  border: 1px solid rgba(100, 143, 255, 0.15);
}
.markdown-body >>> pre code {
  color: #E6F0FF;
  background: transparent;
  padding: 0;
  font-size: 0.95em;
  border: none;
}

.markdown-body >>> blockquote {
  border-left: 4px solid #648FFF;
  padding: 12px 20px;
  color: #C6D9F1;
  margin: 1.2em 0;
  background: rgba(100, 143, 255, 0.08);
  border-radius: 0 8px 8px 0;
  position: relative;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
.markdown-body >>> blockquote::before {
  content: '"';
  font-size: 3em;
  position: absolute;
  top: -0.3em;
  left: 10px;
  color: rgba(100, 143, 255, 0.2);
  font-family: Georgia, serif;
}

.markdown-body >>> a { 
  color: #8ECFFF; 
  text-decoration: none; 
  border-bottom: 1px solid rgba(142, 207, 255, 0.4);
  transition: all 0.2s ease;
}
.markdown-body >>> a:hover { 
  color: #B6D7FF; 
  border-bottom: 1px solid var(--accent-color);
  opacity: 1;
}

.markdown-body >>> table {
  width: 100%;
  border-collapse: collapse;
  margin: 1.2em 0;
  background: rgba(25, 35, 65, 0.5);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
.markdown-body >>> th, .markdown-body >>> td {
  border: 1px solid rgba(100, 143, 255, 0.2);
  padding: 10px 12px;
  text-align: left;
}
.markdown-body >>> th { 
  background: rgba(100, 143, 255, 0.2); 
  color: #B6D7FF; 
  font-weight: 600;
}
.markdown-body >>> tr:nth-child(even) {
  background: rgba(25, 35, 65, 0.3);
}

/* KaTeX 公式适配 - 物理公式优化 */
.markdown-body >>> .katex { 
  font-size: 1.15em; 
  color: #B6D7FF;
}
.markdown-body >>> .katex-display { 
  margin: 1.5em 0; 
  overflow-x: auto; 
  overflow-y: hidden;
  padding: 12px 0;
  text-align: center;
}
.markdown-body >>> .katex .base { 
  color: #E6F0FF; 
}
/* 特殊数学符号高亮 */
.markdown-body >>> .katex .mathnormal,
.markdown-body >>> .katex .mathit {
  color: #8ECFFF;
}
.markdown-body >>> .katex-display { 
  margin: 1em 0; 
  overflow-x: auto; 
  overflow-y: hidden;
  padding: 8px 0;
}
.markdown-body >>> .katex .base { color: #E2E8F0; } /* 确保公式颜色 */

/* History Overlay */
.history-overlay {
  position: absolute;
  top: 55px; /* Header height */
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.98);
  z-index: 20;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(20px);
}

.history-header-title {
  padding: 15px 20px;
  color: var(--accent-color);
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
  background: rgba(15, 23, 42, 0.95);
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.history-item {
  padding: 15px;
  margin-bottom: 10px;
  background: rgba(56, 189, 248, 0.05);
  border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
}

.history-item:hover {
  background: rgba(56, 189, 248, 0.1);
  border-color: var(--accent-color);
  transform: translateX(5px);
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  overflow: hidden;
  cursor: pointer;
}

.history-title {
  color: #F1F5F9;
  font-weight: 500;
}

.history-preview {
  color: #94A3B8;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.history-date {
  color: #64748B;
  font-size: 12px;
  white-space: nowrap;
}

.delete-btn {
  color: #64748B;
  font-size: 14px;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  color: #FF6B6B;
  background: rgba(255, 107, 107, 0.15);
  transform: scale(1.1);
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748B;
  gap: 10px;
  font-size: 16px;
}
.empty-history i { font-size: 40px; opacity: 0.5; }

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style>
