<template>
  <div 
    class="assistant-avatar-wrapper"
  >
    <div 
      class="assistant-avatar"
      :class="{ 
        'is-dragging': isDragging, 
        'is-minimized': isMinimized,
        'is-active': isActive
      }"
      @mousedown="$emit('mousedown', $event)"
      @click="$emit('click', $event)"
      @mouseenter="onMouseEnter"
      @mouseleave="onMouseLeave"
    >
      <!-- 核心能量球 -->
      <div class="core-energy"></div>
      
      <!-- 动态轨道系统 -->
      <div class="orbit-system">
        <div class="orbit orbit-1">
          <div class="electron"></div>
        </div>
        <div class="orbit orbit-2">
          <div class="electron"></div>
        </div>
        <div class="orbit orbit-3">
          <div class="electron"></div>
        </div>
      </div>

      <!-- 粒子场效果 (CSS模拟) -->
      <div class="particle-field">
        <div v-for="n in 5" :key="n" class="particle" :style="getParticleStyle(n)"></div>
      </div>
      
      <!-- 状态指示光环 -->
      <div class="status-ring"></div>
    </div>
    
    <!-- 待机互动气泡 -->
    <transition name="bubble-pop">
      <div v-if="showIdleBubble" class="idle-bubble">
        <span class="bubble-text">快来跟我玩耍吧！</span>
        <div class="bubble-tail"></div>
      </div>
    </transition>
  </div>
</template>

<script>
import gsap from 'gsap'

export default {
  name: 'AssistantAvatar',
  props: {
    isDragging: Boolean,
    isMinimized: Boolean,
    isActive: Boolean,
    showIdleBubble: Boolean
  },
  methods: {
    getParticleStyle(n) {
      // 简单的随机粒子位置和延迟
      return {
        '--delay': `${n * 0.5}s`,
        '--angle': `${n * 72}deg`
      }
    },
    onMouseEnter() {
      this.$emit('mouseenter');
      const avatar = this.$el.querySelector('.assistant-avatar');
      // 整体放大
      gsap.to(avatar, {
        scale: 1.15,
        duration: 0.4,
        ease: 'back.out(1.7)'
      });
      // 核心能量增强
      gsap.to(avatar.querySelector('.core-energy'), {
        scale: 1.3,
        boxShadow: '0 0 25px #38BDF8, 0 0 50px rgba(56, 189, 248, 0.8)',
        duration: 0.4
      });
      // 轨道系统加速或变形 (模拟引力透镜)
      gsap.to(avatar.querySelector('.orbit-system'), {
        scale: 1.1,
        rotation: 45,
        duration: 0.8,
        ease: 'power2.out'
      });
    },
    onMouseLeave() {
      this.$emit('mouseleave');
      const avatar = this.$el.querySelector('.assistant-avatar');
      gsap.to(avatar, {
        scale: 1,
        duration: 0.4,
        ease: 'power2.out'
      });
      gsap.to(avatar.querySelector('.core-energy'), {
        scale: 1,
        boxShadow: '0 0 15px #38BDF8, 0 0 30px rgba(56, 189, 248, 0.4)',
        duration: 0.4
      });
      gsap.to(avatar.querySelector('.orbit-system'), {
        scale: 1,
        rotation: 0,
        duration: 0.8,
        ease: 'power2.out'
      });
    }
  }
}
</script>

<style scoped>
.assistant-avatar-wrapper {
  position: relative;
  display: inline-block;
}

/* 待机互动气泡 */
.idle-bubble {
  position: absolute;
  right: 70px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, rgba(255, 214, 102, 0.95), rgba(255, 180, 50, 0.95));
  padding: 10px 16px;
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(255, 180, 50, 0.4), 0 0 30px rgba(255, 214, 102, 0.3);
  white-space: nowrap;
  z-index: 100;
  animation: bubble-bounce-left 0.6s ease-out;
}

.bubble-text {
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 600;
  text-shadow: none;
}

.bubble-tail {
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-left: 10px solid rgba(255, 214, 102, 0.95);
}

@keyframes bubble-bounce-left {
  0% { opacity: 0; transform: translateY(-50%) scale(0.5) translateX(10px); }
  50% { transform: translateY(-50%) scale(1.1) translateX(-5px); }
  100% { opacity: 1; transform: translateY(-50%) scale(1) translateX(0); }
}

.bubble-pop-enter-active {
  animation: bubble-bounce-left 0.6s ease-out;
}

.bubble-pop-leave-active {
  animation: bubble-bounce-left 0.3s ease-in reverse;
}

.assistant-avatar {
  width: 60px;
  height: 60px;
  position: relative;
  cursor: pointer;
  user-select: none;
  /* 移除 CSS transition，交给 GSAP 接管，避免冲突 */
  /* transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); */
  z-index: 2001; /* 高于普通内容 */
}

/* 移除 hover 效果，由 JS 控制 */
/* .assistant-avatar:hover {
  transform: scale(1.1);
} */

.assistant-avatar.is-dragging {
  cursor: grabbing;
  transform: scale(0.95);
}

.assistant-avatar.is-active {
  /* 当聊天窗口打开时，头像可能发生变化，例如缩小或移位，这里暂时保持简单 */
}

/* 核心能量球 */
.core-energy {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 14px;
  height: 14px;
  background: #648FFF; /* 物理蓝光主题 */
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #648FFF, 0 0 40px rgba(100, 143, 255, 0.5);
  z-index: 10;
  animation: pulse-core 2.5s infinite ease-in-out;
}

/* 轨道系统 */
.orbit-system {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  border: 1px solid rgba(100, 143, 255, 0.4);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-sizing: border-box;
}

.orbit-1 { width: 100%; height: 35%; animation: spin-orbit 3.5s linear infinite; }
.orbit-2 { width: 35%; height: 100%; animation: spin-orbit 4.5s linear infinite reverse; }
.orbit-3 { width: 70%; height: 70%; animation: spin-orbit 6s linear infinite; transform: translate(-50%, -50%) rotate(45deg); }

.electron {
  position: absolute;
  top: -4px; /* 轨道线条宽度一半 + 电子半径 */
  left: 50%;
  width: 7px;
  height: 7px;
  background: #B6D7FF;
  border-radius: 50%;
  box-shadow: 0 0 8px #B6D7FF;
  transform: translateX(-50%);
}

/* 粒子场 */
.particle-field {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.particle {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 2px;
  background: rgba(56, 189, 248, 0.6);
  border-radius: 50%;
  transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-20px);
  animation: particle-float 3s infinite ease-in-out;
  animation-delay: var(--delay);
  opacity: 0;
}

/* 状态光环 */
.status-ring {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: rgba(56, 189, 248, 0.3);
  border-bottom-color: rgba(56, 189, 248, 0.3);
  animation: spin-ring 10s linear infinite;
  pointer-events: none;
}

/* 动画定义 */
@keyframes pulse-core {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.8; }
}

@keyframes spin-orbit {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes spin-ring {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes particle-float {
  0% { opacity: 0; transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-15px); }
  50% { opacity: 0.8; transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-25px); }
  100% { opacity: 0; transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-35px); }
}
</style>
