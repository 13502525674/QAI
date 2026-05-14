<template>
  <div class="login-container" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
    <!-- SVG 滤镜定义 -->
    <svg width="0" height="0">
      <defs>
        <filter id="glass-distortion" x="-50%" y="-50%" width="200%" height="200%">
          <feTurbulence type="fractalNoise" baseFrequency="0.01" numOctaves="1" result="turbulence" />
          <feDisplacementMap in2="turbulence" in="SourceGraphic" scale="2" xChannelSelector="R" yChannelSelector="G" />
        </filter>
      </defs>
    </svg>
    
    <!-- 动态舒适背景 -->
    <div class="animated-background">
      <div class="mesh-gradient"></div>
      <div class="grid-overlay"></div>
      <!-- 物理公式装饰 -->
      <div class="physics-decorations">
        <span class="formula f1">E = mc²</span>
        <span class="formula f2">F = ma</span>
        <span class="formula f3">λ = h/p</span>
        <span class="formula f4">∇ × E = -∂B/∂t</span>
        <span class="formula f5">S = k log W</span>
        <span class="formula f6">iħ ∂ψ/∂t = Ĥψ</span>
      </div>
    </div>
    
    <!-- 左侧内容区域 -->
    <div class="content">
      <div class="brand-image-wrapper">
        <div class="image-container-glass">
          <img class="people-img" src="../../../assets/image/people.jpg">
        </div>
        <div class="image-glow"></div>
      </div>
      <div class="welcome-text">
        <div class="tagline">智能 · 科学 · 成长</div>
        <h2 class="main-title">智学伴——物理学科智能学习伴侣平台</h2>
        <div class="divider"></div>
        <p class="description">探索物理之理，智启智慧之窗</p>
        <div class="stats-mini">
          <div class="stat-item">
            <span class="val">10w+</span>
            <span class="lab">学习资源</span>
          </div>
          <div class="stat-item">
            <span class="val">AI</span>
            <span class="lab">精准辅导</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 液态玻璃登录卡片 (样式保持原封不动) -->
    <div class="login-card" ref="tiltCard">
      <!-- 玻璃效果层 -->
      <div class="glass-effect"></div>
      <div class="glass-tint"></div>
      <div class="glass-shine"></div>
      
      <!-- 内容层 -->
      <div class="glass-content">
        <div class="login-title">
          <span>欢迎来到智学伴</span>
        </div>
        
        <div class="form-group">
          <div class="input-container">
            <i class="el-icon-user input-icon"></i>
            <input 
              v-model="username" 
              class="glass-input" 
              placeholder="请输入用户名"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            >
          </div>
        </div>
        
        <div class="form-group">
          <div class="input-container">
            <i class="el-icon-lock input-icon"></i>
            <input 
              type="password" 
              v-model="password" 
              class="glass-input" 
              placeholder="请输入密码"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            >
          </div>
        </div>
        
        <button class="glass-button" @click="login()" @mousedown="handleButtonClick" @mouseup="handleButtonRelease">
          <span>登 陆</span>
          <div class="click-gradient"></div>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
  import {login,getUser} from '../../../api/api' 
  import { setLock } from '@/utils/lock'
  export default {
    data() {
      return{
        username: '',
        password: '',
        isButtonActive: false
      }
    },
    methods: {
        login() {
            if(!this.username) {
                this.$message({
                    message: '请输入用户名',
                    type: 'warning'
                });
                return;
            }
            if(!this.password) {
                this.$message({
                    message: '请输入密码',
                    type: 'warning'
                });
                return;
            }
            var params = {
                username: this.username,
                password: this.password
            }
            login(params).then(res => {
                if(res.code == 1000) {
                    this.$message({
                        message: '登陆成功',
                        type: 'success'
                    });
                    var that = this
                    var token = res.data.token
                    this.$store.commit('user/setToken', token)
                    this.getUserInfo()
                    setLock(false)
                    setTimeout(function() {
                        that.$router.push("/index")
                    },500)
                } else {
                    this.$message.error(res.message);
                }
            })
        },
        getUserInfo() {
            getUser().then(res => {
                if(res.code == 1000) {
                    this.$store.commit('user/setUser', JSON.stringify(res.data))
                }
            })
        },
        // 3D 倾斜效果
        handleMouseMove (event) {
          const card = this.$refs.tiltCard
          if (!card) return
          
          const rect = card.getBoundingClientRect()
          const centerX = rect.left + rect.width / 2
          const centerY = rect.top + rect.height / 2
          const maxTilt = 8
          
          const rotateY = ((event.clientX - centerX) / centerX) * maxTilt
          const rotateX = -((event.clientY - centerY) / centerY) * maxTilt
          
          card.style.transform = `perspective(600px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.03)`
        },
        handleMouseLeave () {
          const card = this.$refs.tiltCard
          if (card) {
            card.style.transform = 'perspective(600px) rotateX(0deg) rotateY(0deg) scale(1)'
          }
        },
        // 输入框交互效果
        handleInputFocus (event) {
          event.target.parentElement.classList.add('focused')
        },
        handleInputBlur (event) {
          event.target.parentElement.classList.remove('focused')
        },
        // 按钮点击效果
        handleButtonClick (event) {
          this.isButtonActive = true
          const button = event.currentTarget
          button.classList.add('clicked')
          
          // 创建点击位置
          const rect = button.getBoundingClientRect()
          const x = event.clientX - rect.left
          const y = event.clientY - rect.top
          
          const gradient = button.querySelector('.click-gradient')
          gradient.style.left = x + 'px'
          gradient.style.top = y + 'px'
        },
        handleButtonRelease (event) {
          this.isButtonActive = false
          const button = event.currentTarget
          setTimeout(() => {
            button.classList.remove('clicked')
          }, 600)
        }
    },
    created() {
     
    },
    mounted() {
      
    }
 }
</script>

<style scoped>
  .login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    overflow: hidden;
    background: #0a0e1a; /* 稍微调深一点背景色 */
    padding: 0 5%;
    font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  }

  /* --- 背景优化 --- */
  .animated-background {
    position: absolute;
    inset: 0;
    z-index: 0;
  }

  .mesh-gradient {
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(at 0% 0%, #1a2333 0px, transparent 50%),
      radial-gradient(at 50% 0%, #151540 0px, transparent 50%),
      radial-gradient(at 100% 0%, #0a0e1a 0px, transparent 50%),
      radial-gradient(at 0% 50%, #151540 0px, transparent 50%),
      radial-gradient(at 50% 50%, #0a0e1a 0px, transparent 50%),
      radial-gradient(at 100% 50%, #1a2333 0px, transparent 50%),
      radial-gradient(at 0% 100%, #0a0e1a 0px, transparent 50%),
      radial-gradient(at 50% 100%, #1a2333 0px, transparent 50%),
      radial-gradient(at 100% 100%, #151540 0px, transparent 50%);
    filter: blur(40px);
    opacity: 0.9;
  }

  .grid-overlay {
    position: absolute;
    inset: 0;
    background-image: 
      linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px), /* 增加网格亮度 */
      linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
    background-size: 50px 50px;
    mask-image: radial-gradient(circle at center, black, transparent 85%); /* 扩大遮罩范围 */
  }

  .physics-decorations {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }

  .formula {
    position: absolute;
    color: rgba(255, 255, 255, 0.2); /* 增加公式不透明度 */
    font-family: 'Times New Roman', serif;
    font-style: italic;
    user-select: none;
    animation: floatFormula 10s ease-in-out infinite;
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.1); /* 添加微弱发光 */
  }

  .f1 { top: 10%; left: 8%; font-size: 2.2rem; animation-delay: 0s; }
  .f2 { top: 55%; left: 4%; font-size: 1.8rem; animation-delay: -2s; }
  .f3 { top: 82%; left: 28%; font-size: 2rem; animation-delay: -4s; }
  .f4 { top: 8%; right: 22%; font-size: 1.6rem; animation-delay: -1s; }
  .f5 { top: 38%; right: 8%; font-size: 1.8rem; animation-delay: -3s; }
  .f6 { bottom: 12%; right: 12%; font-size: 1.7rem; animation-delay: -5s; }

  @keyframes floatFormula {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    50% { transform: translateY(-25px) rotate(2deg); }
  }

  /* --- 左侧内容优化 --- */
  .content {
    position: relative;
    z-index: 2;
    text-align: left;
    color: white;
    margin-right: 2%; /* 减小间距以腾出空间给长标题 */
    max-width: 800px; /* 增大最大宽度 */
  }

  .brand-image-wrapper {
    position: relative;
    width: fit-content;
    margin-bottom: 2.5rem;
  }

  .image-container-glass {
    position: relative;
    padding: 10px;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    border-radius: 30px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
    z-index: 2;
    overflow: hidden;
  }

  .people-img {
    width: 280px; /* 增大宽度 */
    height: 380px; /* 增大高度以显示全身/更多内容 */
    border-radius: 20px;
    object-fit: cover; /* 确保图片比例正确 */
    display: block;
    transition: transform 0.5s ease;
  }

  .image-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 120%;
    height: 120%;
    background: radial-gradient(circle, rgba(99, 102, 241, 0.25) 0%, transparent 70%);
    z-index: 1;
  }

  .tagline {
    font-size: 1rem;
    letter-spacing: 5px;
    color: #a5b4fc;
    text-transform: uppercase;
    margin-bottom: 0.8rem;
    font-weight: 600;
  }

  .main-title {
    font-size: 2.4rem; /* 稍微调整字号 */
    font-weight: 800;
    margin: 0;
    line-height: 1.2;
    white-space: nowrap; /* 强制不换行 */
    background: linear-gradient(to right, #ffffff, #c7d2fe);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 5px 15px rgba(0,0,0,0.2);
  }

  .divider {
    width: 80px;
    height: 4px;
    background: linear-gradient(to right, #6366f1, #818cf8);
    margin: 1.8rem 0;
    border-radius: 2px;
  }

  .description {
    font-size: 1.2rem;
    opacity: 0.9;
    margin-bottom: 2.5rem;
    letter-spacing: 2px;
    line-height: 1.6;
    font-weight: 300;
  }

  .stats-mini {
    display: flex;
    gap: 4rem;
  }

  .stat-item {
    display: flex;
    flex-direction: column;
  }

  .stat-item .val {
    font-size: 1.8rem;
    font-weight: 800;
    color: #fff;
    margin-bottom: 4px;
  }

  .stat-item .lab {
    font-size: 0.9rem;
    opacity: 0.6;
    text-transform: uppercase;
    letter-spacing: 1.5px;
  }

  /* --- 登录卡片 (保留原有样式) --- */
  .login-card {
    width: 400px;
    flex-shrink: 0; /* 防止卡片被压缩 */
    position: relative;
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 4px 24px 0 rgba(0,0,0,0.10), 0 1.5px 6px 0 rgba(0,0,0,0.08);
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.6);
    cursor: pointer;
    background: transparent;
    z-index: 3;
  }

  .glass-effect {
    position: absolute;
    inset: 0;
    z-index: 0;
    backdrop-filter: blur(5px);
    filter: url(#glass-distortion);
    isolation: isolate;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.1);
  }

  .glass-tint {
    position: absolute;
    inset: 0;
    z-index: 1;
    background: rgba(0, 0, 0, 0.15);
    border-radius: 24px;
  }

  .glass-shine {
    position: absolute;
    inset: 0;
    z-index: 2;
    border: 1px solid rgba(255, 255, 255, 0.13);
    border-radius: 24px;
    box-shadow: 
      inset 1px 1px 8px 0 rgba(255, 255, 255, 0.18),
      inset -1px -1px 8px 0 rgba(255, 255, 255, 0.08);
    pointer-events: none;
  }

  .glass-content {
    position: relative;
    z-index: 3;
    padding: 2rem;
    color: white;
  }

  .login-title {
    text-align: center;
    color: #fff;
    margin-bottom: 2rem;
    font-size: 1.8rem;
    font-weight: 600;
    text-shadow: 0 1px 3px rgba(0,0,0,0.2);
  }

  .form-group {
    margin-bottom: 1.5rem;
  }

  .input-container {
    position: relative;
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
  }

  .input-icon {
    position: absolute;
    left: 15px;
    color: rgba(255, 255, 255, 0.7);
    font-size: 1.2rem;
    z-index: 1;
    transition: all 0.3s ease;
  }

  .glass-input {
    width: 100%;
    padding: 12px 20px 12px 45px;
    border: none;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
    font-size: 1rem;
    backdrop-filter: blur(5px);
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .glass-input::placeholder {
    color: rgba(255, 255, 255, 0.7);
  }

  .glass-input:focus {
    outline: none;
    background: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 15px rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.3);
  }

  .input-container.focused .input-icon {
    color: #fff;
    transform: scale(1.1);
  }

  .glass-button {
    width: 100%;
    padding: 12px;
    border: none;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(5px);
    position: relative;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .glass-button:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  }

  .glass-button:active {
    transform: translateY(0);
  }

  /* 点击波纹效果 */
  .click-gradient {
    position: absolute;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, rgba(180,180,255,0.2) 40%, rgba(100,100,255,0.1) 70%, rgba(50,50,255,0) 100%);
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
    pointer-events: none;
    z-index: 4;
    width: 100px;
    height: 100px;
  }

  .glass-button.clicked .click-gradient {
    animation: gradient-ripple 0.6s ease-out;
  }

  @keyframes gradient-ripple {
    0% {
      transform: translate(-50%, -50%) scale(0);
      opacity: 1;
    }
    100% {
      transform: translate(-50%, -50%) scale(3);
      opacity: 0;
    }
  }

  /* 响应式设计 */
  @media (max-width: 1200px) {
    .main-title {
      font-size: 2rem;
      white-space: normal; /* 中等屏幕允许换行或调整字号 */
    }
    .content {
      max-width: 600px;
    }
  }

  @media (max-width: 1024px) {
    .login-container {
      flex-direction: column;
      justify-content: center;
      padding: 40px 20px;
      gap: 3rem;
    }
    
    .content {
      margin-right: 0;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      max-width: 100%;
    }

    .main-title {
      white-space: normal;
    }

    .divider {
      margin: 1.5rem auto;
    }

    .stats-mini {
      justify-content: center;
    }
    
    .login-card {
      width: 100%;
      max-width: 400px;
    }

    .people-img {
      width: 220px;
      height: 300px;
    }
  }
</style>
