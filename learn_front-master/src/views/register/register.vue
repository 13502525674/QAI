<template>
  <div class="student-login-container" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
    <!-- SVG 滤镜定义 -->
    <svg width="0" height="0">
      <defs>
        <filter id="glass-distortion-student" x="-50%" y="-50%" width="200%" height="200%">
          <feTurbulence type="fractalNoise" baseFrequency="0.01" numOctaves="1" result="turbulence" />
          <feDisplacementMap in2="turbulence" in="SourceGraphic" scale="2" xChannelSelector="R" yChannelSelector="G" />
        </filter>
      </defs>
    </svg>
    
    <!-- 蓝色科技风格背景 -->
    <div class="animated-background-student"></div>
    
    <!-- 顶部导航 -->
    <div class="login-top">
      <div class="logo">
        <img style="width:100px;padding-left:100px" src="../../assets/image/logo.png">
      </div>
      <div class="btns">
        <div style="padding-right:100px;display:flex">
          <div class="toReg glass-btn" @click="toLogin">
            <div>登陆</div>
          </div>
          <div class="toRegTeacher glass-btn" @click="toTeacherRegister">
            <div>教师入驻</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="login-center">
      <!-- 左侧内容区 -->
      <div class="login-content">
        <div style="padding-left: 100px;">
          <div class="title">
            <div>STUDY FROM</div>
            <div>HOME WITH EXPERT</div>
          </div>
          <div class="subtitle">智学伴，一个在家就能学习物理的陪伴平台</div>
          <div class="inspiration-text">
            <div>| 我们都得经历一段努力闭嘴不抱怨的时光，</div>
            <div>| 才能熠熠生辉，才能去更酷的地方，成为更酷的人。</div>
          </div>
          <div class="inspiration-text">
            <div>| 最好的生活状态：一个人时，安静而丰盛</div>
            <div>| 两个人是，温暖而踏实。</div>
          </div>
          <div class="image-container">
            <img style="width:20%" src="../../assets/image/einsten-thumb.svg">
            <img style="width:25%;padding-right:30px" src="../../assets/image/login-jiantou.png">
          </div>
        </div>
      </div>
      
      <!-- 液态玻璃注册表单 -->
      <div class="login-form-glass" ref="tiltCard">
        <!-- 玻璃效果层 -->
        <div class="glass-effect-student"></div>
        <div class="glass-tint-student"></div>
        <div class="glass-shine-student"></div>
        
        <div class="form">
          <div class="login-title-glass">智学伴</div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-user input-icon"></i>
              <input 
                v-model="userInfo.userName" 
                class="glass-input" 
                placeholder="请输入用户名"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-user input-icon"></i>
              <input 
                v-model="userInfo.loginAccount" 
                class="glass-input" 
                placeholder="请输入用户账号"
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
                v-model="userInfo.password" 
                class="glass-input" 
                placeholder="请输入用户密码"
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
                v-model="userInfo.passwordNew" 
                class="glass-input" 
                placeholder="请输入确认密码"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <button class="glass-button-student" @click="toRegister()" @mousedown="handleButtonClick" @mouseup="handleButtonRelease">
            <span>注 册</span>
            <div class="click-gradient"></div>
          </button>
        </div>
      </div>
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {register} from '../../api/api' 
  import bottomPage from "../../components/bottom/login-bottom"
  export default {
	data() {
	  return{
		userInfo: {
			userName: "",
			password: "",
			passwordNew: "",
			loginAccount: "",
			userType: 2
		},
		isButtonActive: false,
		rules: {
          userName: [
            { required: true, message: '请输入名', trigger: 'blur' },
          ],
          password: [
            { required: true, message: '请输入用户密码', trigger: 'blur' }
		  ],
		  passwordNew: [
            { required: true, message: '请输入确认密码', trigger: 'blur' }
		  ],
		  loginAccount: [
            { required: true, message: '请输入用户账号', trigger: 'blur' }
		  ],
        }
	  }
	},
	components: {
        bottomPage
	},
	methods: {
		toLogin() {
			this.$router.push("/login")
		},
		toTeacherRegister() {
			this.$router.push("/teacherRegister")
		},
		toRegister() {
			if(!this.userInfo.userName) {
				this.$message({
					message: '请输入用户名',
					type: 'warning'
				});
				return;
			}
			if(!this.userInfo.loginAccount) {
				this.$message({
					message: '请输入用户账号',
					type: 'warning'
				});
				return;
			}
			if(!this.userInfo.password) {
				this.$message({
					message: '请输入密码',
					type: 'warning'
				});
				return;
			}
			if(!this.userInfo.passwordNew) {
				this.$message({
					message: '请输入确认密码',
					type: 'warning'
				});
				return;
			}
			if (this.userInfo.password != this.userInfo.passwordNew) {
				this.$message({
					message: '两次输入的密码不一致！',
					type: 'warning'
				});
				return;
			}
			register(this.userInfo).then(res => {
				if(res.code == 1000) {
					this.$message({
						message: '注册成功',
						type: 'success'
					});
					var that = this
					setTimeout(function() {
						that.$router.push("/login")
					},500)
				} else {
					this.$message.error(res.message);
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
  .student-login-container {
    min-height: 100vh;
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
    font-family: 'Microsoft YaHei', sans-serif;
  }

  /* 蓝色科技风格动画背景 */
  .animated-background-student {
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
      radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
    animation: backgroundShift 8s ease-in-out infinite;
    z-index: 0;
  }

  @keyframes backgroundShift {
    0%, 100% { transform: scale(1) rotate(0deg); }
    50% { transform: scale(1.1) rotate(180deg); }
  }

  /* 顶部导航 */
  .login-top {
    width: 100%;
    height: 100px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    position: relative;
    z-index: 10;
  }

  .logo {
    width: 55%;
    text-align: center;
    height: 100%;
    display: flex;
    align-items: center;
  }

  .btns {
    width: 45%;
    display: flex;
    height: 100%;
    align-items: center;
    justify-content: flex-end;
  }

  .glass-btn {
    width: 130px;
    height: 40px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    font-family: 'Microsoft YaHei', sans-serif;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    color: white;
    transition: all 0.3s ease;
    border-radius: 8px;
  }

  .glass-btn:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }

  /* 主要内容区域 */
  .login-center {
    width: 100%;
    height: calc(100vh - 100px);
    display: flex;
    flex-direction: row;
    position: relative;
    z-index: 5;
  }

  /* 左侧内容区 */
  .login-content {
    width: 55%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding-left: 100px;
  }

  .title {
    font-size: 55px;
    font-weight: bold;
    color: white;
    line-height: 1.2;
    margin-bottom: 20px;
  }

  .subtitle {
    color: rgba(255, 255, 255, 0.9);
    font-size: 18px;
    margin-bottom: 30px;
    font-family: 'Microsoft YaHei', sans-serif;
  }

  .inspiration-text {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    margin-bottom: 15px;
    font-family: 'Microsoft YaHei', sans-serif;
  }

  .image-container {
    margin-top: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  /* 液态玻璃表单 */
  .login-form-glass {
    width: 400px;
    top: 150px;
    position: relative;
    transform: translateY(-50%);
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 4px 24px 0 rgba(0,0,0,0.15), 0 1.5px 6px 0 rgba(0,0,0,0.1);
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.6);
    cursor: pointer;
    background: transparent;
    z-index: 3;
    margin-right: 5%;
    height: 58%;
  }

  /* 玻璃效果层 */
  .glass-effect-student {
    position: absolute;
    inset: 0;
    z-index: 0;
    backdrop-filter: blur(5px);
    filter: url(#glass-distortion-student);
    isolation: isolate;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.1);
  }

  .glass-tint-student {
    position: absolute;
    inset: 0;
    z-index: 1;
    background: rgba(0, 0, 0, 0.15);
    border-radius: 24px;
  }

  .glass-shine-student {
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

  .form {
    position: relative;
    z-index: 3;
    padding: 1.5rem;
    color: white;
    width: 100%;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .login-title-glass {
    text-align: center;
    color: #fff;
    margin-bottom: 1rem;
    font-size: 1.8rem;
    font-weight: 600;
    text-shadow: 0 2px 10px rgba(0,0,0,0.3);
  }

  .form-group {
    width: 100%;
    position: relative;
  }

  .input-container {
    position: relative;
    width: 100%;
    transition: all 0.3s ease;
  }

  .input-container.focused {
    transform: translateY(-2px);
  }

  .input-icon {
    position: absolute;
    left: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: rgba(255, 255, 255, 0.7);
    z-index: 2;
    font-size: 1.2rem;
    transition: all 0.3s ease;
  }

  .glass-input {
    width: 100%;
    padding: 12px 15px 12px 40px;
    border: none;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
    font-size: 1rem;
    backdrop-filter: blur(5px);
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-sizing: border-box;
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

  /* 蓝色科技风格按钮 */
  .glass-button-student {
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

  .glass-button-student:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  }

  .glass-button-student:active {
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

  .glass-button-student.clicked .click-gradient {
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
</style>