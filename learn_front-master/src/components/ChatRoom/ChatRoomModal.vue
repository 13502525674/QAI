<template>
  <div class="chat-room-modal" v-if="visible" @click.self="closeModal">
    <div class="chat-room-container">
      <div class="chat-room-header">
        <div class="header-title">
          <span class="header-icon">💬</span>
          <span>学习交流室</span>
        </div>
        <div class="header-actions">
          <button class="clear-btn" @click="clearAllMessages" title="清空聊天记录">
            🗑️ 清空记录
          </button>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
      </div>
      
      <div class="chat-room-body">
        <div class="online-users-panel">
          <div class="panel-header">
            <span>👥 在线同学</span>
            <span class="online-count">{{ onlineUsers.length }}人在线</span>
          </div>
          <div class="users-list">
            <div 
              v-for="user in onlineUsers" 
              :key="user.userId"
              class="user-item"
              :class="{ 'active': currentChatUser && currentChatUser.userId === user.userId, 'is-me': user.userId === myUserId }"
              @click="selectUser(user)"
            >
              <div class="user-avatar">{{ (user.userName || '用户').charAt(0) }}</div>
              <div class="user-info">
                <div class="user-name">{{ user.userName || '用户' }}{{ user.userId === myUserId ? ' (我)' : '' }}</div>
              </div>
              <div class="user-status"></div>
            </div>
            <div v-if="onlineUsers.length === 0" class="no-users">
              暂无在线用户
            </div>
          </div>
        </div>
        
        <div class="chat-panel">
          <div class="chat-type-tabs">
            <div 
              class="tab-item" 
              :class="{ 'active': chatType === 'group' }"
              @click="switchToGroup"
            >
              🌐 群聊
            </div>
            <div 
              v-if="currentChatUser"
              class="tab-item" 
              :class="{ 'active': chatType === 'private' }"
              @click="switchToPrivate"
            >
              💬 与 {{ currentChatUser.userName }} 私聊
            </div>
          </div>
          
          <div class="messages-container" ref="messagesContainer">
            <div 
              v-for="(msg, index) in currentMessages" 
              :key="index"
              class="message-item"
              :class="{ 'mine': msg.senderId === myUserId }"
            >
              <div class="message-avatar">{{ (msg.senderName || '用户').charAt(0) }}</div>
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">{{ msg.senderName || '用户' }}</span>
                  <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                  <button class="delete-btn" @click="deleteMessage(msg, index)" title="删除消息">×</button>
                </div>
                <div class="message-text">
                  <!-- 支持渲染图片 -->
                  <img 
                    v-if="msg.contentType === 'image'" 
                    :src="getImageUrl(msg.content)" 
                    class="message-image"
                    @click="previewImage(msg.content)"
                    @error="handleImageError"
                  >
                  <!-- 支持渲染HTML内容(表情包等) -->
                  <span v-else-if="msg.contentType === 'emoji'" class="emoji-content" v-html="msg.content"></span>
                  <!-- 普通文本 -->
                  <span v-else class="text-content">{{ msg.content }}</span>
                </div>
              </div>
            </div>
            <div v-if="currentMessages.length === 0" class="no-messages">
              暂无消息，开始聊天吧~
            </div>
          </div>
          
          <div class="input-area">
            <div class="input-toolbar">
              <button class="toolbar-btn emoji-btn" @click="toggleEmojiPicker" title="发送表情">😊</button>
              <label class="toolbar-btn image-btn" title="发送图片">
                📷
                <input type="file" accept="image/*" @change="handleImageUpload" style="display: none;">
              </label>
            </div>
            
            <!-- 表情选择器 -->
            <div v-if="showEmojiPicker" class="emoji-picker">
              <div class="emoji-picker-header">
                <span class="emoji-picker-title">选择表情</span>
                <button class="emoji-close-btn" @click="showEmojiPicker = false">×</button>
              </div>
              <div class="emoji-grid">
                <span 
                  v-for="(emoji, index) in emojis" 
                  :key="index"
                  class="emoji-item"
                  @click="insertEmoji(emoji)"
                >
                  {{ emoji }}
                </span>
              </div>
            </div>
            
            <input 
              type="text" 
              v-model="inputMessage" 
              placeholder="输入消息..." 
              @keyup.enter="sendMessage"
              class="message-input"
            >
            <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim()">
              发送
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { get, del } from '@/utils/request'

export default {
  name: 'ChatRoomModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      myUserId: '',
      myUserName: '',
      onlineUsers: [],
      groupMessages: [],
      privateMessages: {},
      currentChatUser: null,
      chatType: 'group',
      inputMessage: '',
      websocket: null,
      reconnectTimer: null,
      messageQueue: [], // 消息队列，用于缓存连接未建立时的消息
      showEmojiPicker: false,
      emojis: [
        '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂',
        '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛',
        '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨',
        '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔',
        '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵',
        '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕',
        '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧',
        '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞', '😓',
        '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '👍', '👎', '👏',
        '🙏', '💪', '❤️', '💔', '💯', '🎉', '🔥', '✨', '⭐', '🌟'
      ]
    }
  },
  computed: {
    currentMessages() {
      if (this.chatType === 'group') {
        return this.groupMessages
      } else if (this.currentChatUser) {
        return this.privateMessages[this.currentChatUser.userId] || []
      }
      return []
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initChat()
      } else {
        this.closeWebSocket()
      }
    }
  },
  methods: {
    initChat() {
      this.getCurrentUserInfo()
      this.connectWebSocket()
      this.loadGroupMessages()
    },
    getCurrentUserInfo() {
      const userInfoStr = window.localStorage.getItem('user_info')
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        this.myUserId = String(userInfo.id || '')
        this.myUserName = userInfo.userName || userInfo.name || '用户'
      }
    },
    connectWebSocket() {
      if (this.websocket) {
        this.websocket.close()
      }
      
      // 修复：明确指定后端WebSocket服务端口为8080
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      // 如果是开发环境，使用localhost:8080，否则使用当前host
      const isDev = window.location.port === '3001' || window.location.port === '8081' || window.location.port === '5173'
      const host = isDev ? 'localhost:8080' : window.location.host
      const wsUrl = `${protocol}//${host}/chat/${this.myUserId}`
      
      console.log('WebSocket连接地址:', wsUrl)
      
      try {
        this.websocket = new WebSocket(wsUrl)
        
        this.websocket.onopen = () => {
          console.log('WebSocket连接成功，状态:', this.websocket.readyState)
          // 发送初始化消息
          this.safeSend({
            type: 'init',
            userId: this.myUserId,
            userName: this.myUserName
          })
          // 发送队列中缓存的消息
          this.flushMessageQueue()
        }
        
        this.websocket.onmessage = (event) => {
          this.handleWebSocketMessage(event.data)
        }
        
        this.websocket.onclose = () => {
          console.log('WebSocket连接关闭')
          this.scheduleReconnect()
        }
        
        this.websocket.onerror = (error) => {
          console.error('WebSocket错误:', error)
        }
      } catch (error) {
        console.error('WebSocket连接失败:', error)
        this.scheduleReconnect()
      }
    },
    scheduleReconnect() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
      }
      this.reconnectTimer = setTimeout(() => {
        if (this.visible) {
          this.connectWebSocket()
        }
      }, 3000)
    },
    closeWebSocket() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
      }
      if (this.websocket) {
        this.websocket.close()
        this.websocket = null
      }
      // 清空消息队列
      this.messageQueue = []
    },
    // 安全发送消息：检查WebSocket状态，如果未连接则加入队列
    safeSend(message) {
      if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
        try {
          this.websocket.send(JSON.stringify(message))
          return true
        } catch (error) {
          console.error('发送消息失败:', error)
          return false
        }
      } else {
        console.warn('WebSocket未连接，消息已加入队列')
        this.messageQueue.push(message)
        return false
      }
    },
    // 发送队列中缓存的消息
    flushMessageQueue() {
      while (this.messageQueue.length > 0 && 
             this.websocket && 
             this.websocket.readyState === WebSocket.OPEN) {
        const message = this.messageQueue.shift()
        this.safeSend(message)
      }
    },
    handleWebSocketMessage(data) {
      try {
        const message = JSON.parse(data)
        
        switch (message.type) {
          case 'onlineUsers':
            this.onlineUsers = message.users || []
            console.log('在线用户列表更新:', this.onlineUsers)
            break
          case 'group':
            // 将消息添加到列表（包括自己发送的消息，因为去除了乐观更新）
              this.groupMessages.push({
              id: message.id,
              senderId: message.senderId,
              senderName: message.senderName,
              content: message.content,
              contentType: message.contentType || 'text',
              createTime: message.createTime
            })
            this.scrollToBottom()
            break
          case 'private':
            console.log('收到私聊消息:', {
              id: message.id,
              senderId: message.senderId,
              senderName: message.senderName,
              content: message.content,
              myUserId: this.myUserId
            })
            
            // 判断对方的ID，如果是自己发出的，则targetUserId为接收者，但是WebSocket目前服务端实现：
            // 如果是发给别人的私聊，接收者会收到senderId是发送者。
            // 那么自己会收到吗？ChatWebSocketHandler中：
            // if (sessions.containsKey(receiverId)) sendMessage(sessions.get(receiverId), result);
            // if (sessions.containsKey(senderId)) sendMessage(sessions.get(senderId), result);
            // 所以自己也会收到！
            // 对于发送者，聊天窗口对应的是 receiverId；对于接收者，对应的是 senderId
            let targetUserId = message.senderId === this.myUserId ? message.receiverId : message.senderId;
            
            // 确保使用Vue.set保证响应式更新
            if (!this.privateMessages[targetUserId]) {
              this.$set(this.privateMessages, targetUserId, [])
            }
            
            this.privateMessages[targetUserId].push({
              id: message.id,
              senderId: message.senderId,
              senderName: message.senderName,
              content: message.content,
              contentType: message.contentType || 'text',
              createTime: message.createTime
            })
            
            console.log('私聊消息已添加:', {
              targetUserId: targetUserId,
              messageList: JSON.parse(JSON.stringify(this.privateMessages[targetUserId]))
            })
            
            if (this.chatType === 'private' && this.currentChatUser && this.currentChatUser.userId === targetUserId) {
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            }
            break
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    },
    async loadGroupMessages() {
      try {
        console.log('加载群聊历史消息...')
        const res = await get('/chatApi/groupMessages', { limit: 100 })
        console.log('群聊历史消息API返回:', res)
        if (res && res.success && Array.isArray(res.data)) {
          // 后端已经按时间正序返回（最早在前，最新在后），直接使用
          this.groupMessages = res.data
          this.scrollToBottom()
          console.log('群聊历史消息加载完成，共', this.groupMessages.length, '条')
        } else {
          console.warn('群聊历史消息加载失败，返回数据:', res)
        }
      } catch (error) {
        console.error('加载群聊消息异常:', error)
      }
    },
    async loadPrivateMessages(userId) {
      try {
        console.log('加载私聊历史消息, userId:', userId)
        const res = await get('/chatApi/privateMessages', { userId2: userId })
        console.log('私聊历史消息API返回:', res)
        if (res && res.success && Array.isArray(res.data)) {
          this.$set(this.privateMessages, userId, res.data)
          this.$nextTick(() => {
            this.scrollToBottom()
          })
          console.log('私聊历史消息加载完成，共', res.data.length, '条')
        } else {
          console.warn('私聊历史消息加载失败，返回数据:', res)
        }
      } catch (error) {
        console.error('加载私聊消息异常:', error)
      }
    },
    selectUser(user) {
      if (user.userId === this.myUserId) return
      
      this.currentChatUser = user
      this.chatType = 'private'
      
      console.log('选择私聊对象:', {
        myUserId: this.myUserId,
        targetUserId: user.userId,
        targetUserName: user.userName
      })
      
      if (!this.privateMessages[user.userId]) {
        this.loadPrivateMessages(user.userId)
      } else {
        console.log('当前私聊消息列表:', this.privateMessages[user.userId])
      }
    },
    switchToGroup() {
      this.chatType = 'group'
      this.currentChatUser = null
      this.scrollToBottom()
    },
    switchToPrivate() {
      this.chatType = 'private'
      this.scrollToBottom()
    },
    sendMessage() {
      if (!this.inputMessage.trim()) return
      
      const now = Date.now()
      const message = {
        type: this.chatType,
        content: this.inputMessage.trim(),
        contentType: 'text',
        createTime: now
      }
      
      if (this.chatType === 'private' && this.currentChatUser) {
        message.receiverId = this.currentChatUser.userId
      }
      
      console.log('发送消息:', {
        type: this.chatType,
        myUserId: this.myUserId,
        myUserName: this.myUserName,
        currentChatUser: this.currentChatUser,
        content: message.content
      })
      
      // 不再使用完全的乐观更新，因为我们需要后端的真实ID以支持删除操作
      // 我们直接发送消息，通过WebSocket的回调来显示自己发的消息
      
      // 使用安全发送方法
      if (this.safeSend(message)) {
        this.inputMessage = ''
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const now = new Date()
      const isToday = date.toDateString() === now.toDateString()
      
      if (isToday) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else {
        return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
      }
    },
    async deleteMessage(msg, index) {
      this.$confirm('确定要删除这条消息吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'chat-confirm-dialog'
      }).then(async () => {
        try {
          // 如果消息有id，说明是数据库中的消息，需要调用后端删除
          if (msg.id) {
            const res = await del(`/chatApi/message/${msg.id}`)
            if (!res.success) {
              this.$message.error('删除失败：' + res.message)
              return
            }
          }
          
          // 从本地删除
          if (this.chatType === 'group') {
            this.groupMessages.splice(index, 1)
          } else if (this.currentChatUser) {
            const targetUserId = this.currentChatUser.userId
            if (this.privateMessages[targetUserId]) {
              this.privateMessages[targetUserId].splice(index, 1)
            }
          }
          
          this.$message.success('消息删除成功')
        } catch (error) {
          console.error('删除消息失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 取消删除
      });
    },
    async clearAllMessages() {
      this.$confirm('确定要清空当前所有聊天记录吗？此操作不可恢复！', '清空确认', {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'danger',
        customClass: 'chat-confirm-dialog'
      }).then(async () => {
        try {
          if (this.chatType === 'group') {
            const res = await del('/chatApi/groupMessages')
            if (!res.success) {
              this.$message.error('清空失败：' + res.message)
              return
            }
            this.groupMessages = []
            this.$message.success('群聊记录已清空')
          } else if (this.currentChatUser) {
            const targetUserId = this.currentChatUser.userId
            const res = await del(`/chatApi/privateMessages/${targetUserId}`)
            if (!res.success) {
              this.$message.error('清空失败：' + res.message)
              return
            }
            this.$set(this.privateMessages, targetUserId, [])
            this.$message.success('私聊记录已清空')
          }
        } catch (error) {
          console.error('清空聊天记录失败:', error)
          this.$message.error('清空失败')
        }
      }).catch(() => {
        // 取消清空
      });
    },
    closeModal() {
      this.$emit('update:visible', false)
      this.$emit('close')
    },
    toggleEmojiPicker() {
      this.showEmojiPicker = !this.showEmojiPicker
    },
    insertEmoji(emoji) {
      this.inputMessage += emoji
      // 不关闭表情选择器，允许用户连续选择多个表情
    },
    async handleImageUpload(event) {
      const file = event.target.files[0]
      if (!file) return
      
      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        this.$message.error('只能上传图片文件')
        return
      }
      
      // 验证文件大小 (最大5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('图片大小不能超过5MB')
        return
      }
      
      try {
        const formData = new FormData()
        formData.append('file', file)
        
        const token = window.localStorage.getItem('user_token')
        
        const response = await fetch(`${this.getBaseURL()}/chatApi/uploadImage`, {
          method: 'POST',
          body: formData,
          headers: {
            'x_access_token': token || ''
          }
        })
        
        const result = await response.json()
        
        if (result.success) {
          // 发送图片消息
          this.sendImageMessage(result.data.url)
        } else {
          this.$message.error(result.message || '图片上传失败')
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        this.$message.error('图片上传失败')
      }
      
      // 清空文件输入
      event.target.value = ''
    },
    sendImageMessage(imageUrl) {
      // 将相对路径转换为完整URL
      const fullUrl = imageUrl.startsWith('http') ? imageUrl : `${this.getBaseURL()}${imageUrl}`
      
      const message = {
        type: this.chatType,
        content: fullUrl,
        contentType: 'image',
        createTime: Date.now()
      }
      
      if (this.chatType === 'private' && this.currentChatUser) {
        message.receiverId = this.currentChatUser.userId
      }
      
      if (this.safeSend(message)) {
        // 消息已发送
      }
    },
    previewImage(imageUrl) {
      // 确保图片URL是完整的
      const fullUrl = imageUrl.startsWith('http') ? imageUrl : `${this.getBaseURL()}${imageUrl}`

      // 创建图片预览弹窗
      const preview = document.createElement('div')
      preview.className = 'image-preview-overlay'
      preview.innerHTML = `
        <div class="image-preview-container">
          <img src="${fullUrl}" class="preview-image" />
          <button class="close-preview-btn">×</button>
        </div>
      `

      // 关闭预览的函数
      const closePreview = () => {
        document.body.removeChild(preview)
        document.removeEventListener('keydown', handleEsc)
      }

      // ESC键关闭预览
      const handleEsc = (e) => {
        if (e.key === 'Escape' || e.keyCode === 27) {
          closePreview()
        }
      }

      // 点击关闭
      preview.addEventListener('click', (e) => {
        if (e.target === preview || e.target.classList.contains('close-preview-btn')) {
          closePreview()
        }
      })

      // 添加ESC键监听
      document.addEventListener('keydown', handleEsc)

      document.body.appendChild(preview)
    },
    getBaseURL() {
      const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
      const isDev = window.location.port === '3001' || window.location.port === '8081' || window.location.port === '5173'
      const host = isDev ? 'localhost:8080' : window.location.host
      return `${protocol}//${host}`
    },
    getImageUrl(url) {
      // 如果已经是完整URL，直接返回
      if (url && (url.startsWith('http://') || url.startsWith('https://'))) {
        return url
      }
      // 如果是相对路径，拼接完整URL
      return `${this.getBaseURL()}${url}`
    },
    handleImageError(event) {
      // 图片加载失败时的处理
      console.error('图片加载失败:', event.target.src)
      event.target.style.display = 'none'
      // 可以显示一个占位符或错误提示
      const errorMsg = document.createElement('div')
      errorMsg.className = 'image-error-msg'
      errorMsg.textContent = '图片加载失败'
      errorMsg.style.cssText = 'color: #999; font-size: 12px; padding: 8px; background: rgba(255,255,255,0.1); border-radius: 4px;'
      event.target.parentNode.appendChild(errorMsg)
    }
  },
  beforeUnmount() {
    this.closeWebSocket()
  }
}
</script>

<style>
/* 聊天室弹窗确认框全局样式 */
.chat-confirm-dialog {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.98) 0%, rgba(15, 23, 42, 0.98) 100%) !important;
  border: 1px solid rgba(79, 195, 247, 0.3) !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5) !important;
}

.chat-confirm-dialog .el-message-box__title {
  color: #8ECFFF !important;
}

.chat-confirm-dialog .el-message-box__content {
  color: #e2e8f0 !important;
}

.chat-confirm-dialog .el-button--default {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #e2e8f0 !important;
}

.chat-confirm-dialog .el-button--default:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.chat-confirm-dialog .el-button--primary {
  background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%) !important;
  border: none !important;
}

.chat-confirm-dialog .el-button--primary:hover {
  box-shadow: 0 4px 15px rgba(79, 195, 247, 0.4) !important;
}
</style>

<style scoped>
.chat-room-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
}

.chat-room-container {
  width: 900px;
  max-width: 95%;
  height: 600px;
  max-height: 90%;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.98) 0%, rgba(15, 23, 42, 0.98) 100%);
  border-radius: 16px;
  border: 1px solid rgba(79, 195, 247, 0.3);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(90deg, rgba(79, 195, 247, 0.2) 0%, rgba(56, 189, 248, 0.1) 100%);
  border-bottom: 1px solid rgba(79, 195, 247, 0.2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #4fc3f7;
}

.header-icon {
  font-size: 22px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}


.clear-btn {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.clear-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  transform: translateY(-1px);
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border-radius: 8px;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.4);
  transform: scale(1.1);
}

.chat-room-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.online-users-panel {
  width: 220px;
  background: rgba(255, 255, 255, 0.03);
  border-right: 1px solid rgba(79, 195, 247, 0.15);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #94a3b8;
  border-bottom: 1px solid rgba(79, 195, 247, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.online-count {
  font-size: 12px;
  color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.users-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.user-item:hover {
  background: rgba(79, 195, 247, 0.1);
}

.user-item.active {
  background: rgba(79, 195, 247, 0.2);
  border: 1px solid rgba(79, 195, 247, 0.3);
}

.user-item.is-me {
  opacity: 0.7;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 13px;
  color: #e2e8f0;
  font-weight: 500;
}

.user-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.5);
}

.no-users {
  text-align: center;
  color: #64748b;
  font-size: 13px;
  padding: 20px;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-type-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(79, 195, 247, 0.1);
}

.tab-item {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #94a3b8;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-item:hover {
  background: rgba(79, 195, 247, 0.15);
  color: #e2e8f0;
}

.tab-item.active {
  background: linear-gradient(135deg, rgba(79, 195, 247, 0.3) 0%, rgba(56, 189, 248, 0.2) 100%);
  color: #4fc3f7;
  border: 1px solid rgba(79, 195, 247, 0.3);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 70%;
}

.message-item.mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-weight: 600;
  font-size: 12px;
  flex-shrink: 0;
}

.message-item.mine .message-avatar {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sender-name {
  font-size: 12px;
  color: #94a3b8;
}

.message-time {
  font-size: 11px;
  color: #64748b;
}

.delete-btn {
  margin-left: auto;
  width: 20px;
  height: 20px;
  border: none;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 50%;
  font-size: 14px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.3);
  transform: scale(1.1);
}

.message-text {
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  border-top-left-radius: 4px;
  font-size: 14px;
  color: #e2e8f0;
  line-height: 1.5;
  word-break: break-word;
}

.message-item.mine .message-text {
  background: linear-gradient(135deg, rgba(79, 195, 247, 0.3) 0%, rgba(56, 189, 248, 0.2) 100%);
  border-radius: 12px;
  border-top-right-radius: 4px;
}

/* 消息中的表情样式 - 确保表情居中显示 */
.message-text {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.text-content,
.emoji-content {
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
  line-height: 1.5;
}

.emoji-content {
  font-size: 24px;
}

/* 确保表情与文本对齐 */
.message-text img {
  display: inline-block;
  vertical-align: middle;
  max-width: 100%;
}

.no-messages {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  padding: 40px;
}

.input-area {
  display: flex;
  gap: 10px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border-top: 1px solid rgba(79, 195, 247, 0.1);
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid rgba(79, 195, 247, 0.3);
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.08);
  color: #e2e8f0;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.message-input:focus {
  border-color: #4fc3f7;
  box-shadow: 0 0 10px rgba(79, 195, 247, 0.2);
}

.message-input::placeholder {
  color: #64748b;
}

.send-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(79, 195, 247, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.messages-container::-webkit-scrollbar,
.users-list::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track,
.users-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb,
.users-list::-webkit-scrollbar-thumb {
  background: rgba(79, 195, 247, 0.3);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover,
.users-list::-webkit-scrollbar-thumb:hover {
  background: rgba(79, 195, 247, 0.5);
}

/* 输入区域工具栏 */
.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(79, 195, 247, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s;
}

.toolbar-btn:hover {
  background: rgba(79, 195, 247, 0.2);
  border-color: rgba(79, 195, 247, 0.5);
  transform: translateY(-1px);
}

/* 表情选择器 */
.emoji-picker {
  position: absolute;
  bottom: 200px;
  left: 50%;
  transform: translateX(-50%);
  width: 320px;
  max-width: calc(100% - 32px);
  background: rgba(30, 41, 59, 0.98);
  border: 1px solid rgba(79, 195, 247, 0.3);
  border-radius: 12px;
  padding: 12px;
  max-height: 280px;
  overflow-y: auto;
  z-index: 10;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.emoji-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(79, 195, 247, 0.2);
}

.emoji-picker-title {
  font-size: 14px;
  color: #4fc3f7;
  font-weight: 600;
}

.emoji-close-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.emoji-close-btn:hover {
  background: rgba(239, 68, 68, 0.4);
  transform: scale(1.1);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.emoji-item:hover {
  background: rgba(79, 195, 247, 0.2);
  transform: scale(1.15);
}

/* 消息中的图片样式 */
.message-image {
  max-width: 250px;
  max-height: 200px;
  width: auto;
  height: auto;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  object-fit: contain;
  display: block;
  margin: 4px 0;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(79, 195, 247, 0.2);
}

.message-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(79, 195, 247, 0.4);
  border: 2px solid rgba(79, 195, 247, 0.6);
}

/* 图片错误提示样式 */
.image-error-msg {
  color: #999;
  font-size: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px dashed rgba(255, 77, 79, 0.3);
  display: inline-block;
}

/* 图片预览弹窗 */
.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10001;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.image-preview-container {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  animation: zoomIn 0.3s ease;
}

@keyframes zoomIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.preview-image {
  max-width: 100%;
  max-height: 90vh;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.close-preview-btn {
  position: absolute;
  top: -50px;
  right: 0;
  width: 44px;
  height: 44px;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-radius: 50%;
  font-size: 32px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.close-preview-btn:hover {
  background: rgba(255, 77, 79, 0.8);
  transform: rotate(90deg) scale(1.1);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .emoji-picker {
    width: 280px;
    max-width: calc(100% - 32px);
  }

  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }

  .emoji-item {
    width: 30px;
    height: 30px;
    font-size: 18px;
  }

  .message-image {
    max-width: 180px;
    max-height: 150px;
  }

  .input-toolbar {
    gap: 6px;
  }

  .toolbar-btn {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
}
</style>
