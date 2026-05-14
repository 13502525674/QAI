<template>
  <div class="teacher-login-container" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
    <!-- SVG 滤镜定义 -->
    <svg width="0" height="0">
      <defs>
        <filter id="glass-distortion-teacher" x="-50%" y="-50%" width="200%" height="200%">
          <feTurbulence type="fractalNoise" baseFrequency="0.01" numOctaves="1" result="turbulence" />
          <feDisplacementMap in2="turbulence" in="SourceGraphic" scale="2" xChannelSelector="R" yChannelSelector="G" />
        </filter>
      </defs>
    </svg>
    
    <!-- 舒适科技风格背景 -->
    <div class="animated-background-teacher">
      <div class="mesh-gradient"></div>
      <div class="grid-overlay"></div>
      <!-- 物理元素装饰 -->
      <div class="physics-decorations">
        <span class="formula f1">E = mc²</span>
        <span class="formula f2">F = ma</span>
        <span class="formula f3">∇ × B = μ₀J</span>
        <span class="formula f4">λ = h/p</span>
      </div>
    </div>
    
    <!-- 顶部导航 -->
    <div class="login-top">
      <div class="logo">
        <div class="logo-text">智学伴</div>
      </div>
      <div class="btns">
        <div style="padding-right:100px;display:flex">
          <div class="toReg glass-btn" @click="toRegister">
            <div>注册</div>
          </div>
          <div class="toRegTeacher glass-btn" @click="toLogin">
            <div>登陆</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="login-center">
      <!-- 左侧内容区 -->
      <div class="about">
        <div style="text-align:center">
          <div class="huanying">欢迎你的加入！</div>
          <div class="huanying1">书本上有这样一段话："教育的意义在于让人明白作为人类社会中的一员所应承担的责任与义务。</div>
          <div class="huanying1">只有当一个人明白了自己的责任与义务，才能成为对社会有用的人，只有完成了自己的责任与义务，</div>
          <div class="huanying1">才能最终成为真正有用的人。</div>
        </div>
      </div>
      
      <!-- 液态玻璃入驻表单 -->
      <div class="teacher-form-glass" ref="tiltCard">
        <!-- 玻璃效果层 -->
        <div class="glass-effect-teacher"></div>
        <div class="glass-tint-teacher"></div>
        <div class="glass-shine-teacher"></div>
        
        <div class="ruzhu-content">
          <div class="ruzhu-title-glass">入驻信息</div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-user input-icon"></i>
              <input 
                v-model="form.userName" 
                class="glass-input" 
                placeholder="请输入姓名"
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
                v-model="form.password" 
                class="glass-input" 
                placeholder="请输入密码"
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
                v-model="form.passwordNew" 
                class="glass-input" 
                placeholder="请输入确认密码"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-user input-icon"></i>
              <input 
                v-model="form.loginAccount" 
                class="glass-input" 
                placeholder="请输入登陆账号"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-message input-icon"></i>
              <input 
                v-model="form.email" 
                class="glass-input" 
                placeholder="请输入邮箱地址"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-phone input-icon"></i>
              <input 
                v-model="form.tel" 
                class="glass-input" 
                placeholder="请输入联系方式"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-user-solid input-icon"></i>
              <select 
                v-model="form.sex" 
                class="glass-input"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
                <option value="0">男</option>
                <option value="1">女</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-location input-icon"></i>
              <input 
                v-model="form.country" 
                class="glass-input" 
                placeholder="请输入国家"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-school input-icon"></i>
              <select 
                v-model="form.school" 
                class="glass-input"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
                <option value="">请选择院校</option>
                <option v-for="(item,index) in school" :value="item.name">{{ item.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-reading input-icon"></i>
              <select 
                v-model="form.major" 
                class="glass-input"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
                <option value="">请选择专业</option>
                <option v-for="(item,index) in major" :value="item.name">{{ item.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-medal input-icon"></i>
              <select 
                v-model="form.agree" 
                class="glass-input"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
                <option value="">请选择职称</option>
                <option value="初级讲师">初级讲师</option>
                <option value="中级讲师">中级讲师</option>
                <option value="高级讲师">高级讲师</option>
                <option value="副教授">副教授</option>
                <option value="教授">教授</option>
                <option value="院士">院士</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-date input-icon"></i>
              <input 
                type="number" 
                v-model="form.age" 
                class="glass-input" 
                placeholder="请输入年龄"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-date input-icon"></i>
              <input 
                type="date" 
                v-model="form.birth" 
                class="glass-input" 
                placeholder="请输入出生日期"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-time input-icon"></i>
              <input 
                type="number" 
                v-model="form.workDate" 
                class="glass-input" 
                placeholder="请输入教龄"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              >
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-location-outline input-icon"></i>
              <textarea 
                v-model="form.address" 
                class="glass-input" 
                placeholder="请输入地址"
                rows="3"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              ></textarea>
            </div>
          </div>
          <div class="form-group">
            <div class="input-container">
              <i class="el-icon-edit input-icon"></i>
              <textarea 
                v-model="form.remark" 
                class="glass-input" 
                placeholder="请输入自我介绍"
                rows="4"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
              ></textarea>
            </div>
          </div>
          <div class="form-group">
            <div class="upload-container">
              <div class="upload-label">教学资质：</div>
              <el-upload
                :file-list="fileList"
                ref="upload"
                :action="uploadImageUrl()"
                accept="image/*"
                :multiple="false"
                :limit="1"
                :before-upload="beforeAvatorUpload"
                list-type="picture-card"
                :on-preview="handlePictureCardPreview"
                :on-exceed="handleExceed"
                :on-success="handleAvatorSuccess"
                :on-remove="handleRemove">
                <i class="el-icon-plus"></i>
              </el-upload>
              <el-dialog :visible.sync="dialogVisible">
                <img width="100%" :src="dialogImageUrl" alt="">
              </el-dialog>
            </div>
          </div>
          <button class="glass-button-teacher" @click="toRuZhu()" @mousedown="handleButtonClick" @mouseup="handleButtonRelease">
            <span>入 驻</span>
            <div class="click-gradient"></div>
          </button>
        </div>
      </div>
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeSchoolList,getApeMajorList,register} from '../../api/api' 
  import bottomPage from "../../components/bottom/login-bottom"
  import { mixin } from '../../minix/index';
  export default {
	mixins: [mixin],
	data() {
	  return{
		userInfo: {
			username: "",
			password: ""
		},
		school: [],
		major: [],
		dialogImageUrl: '',
		dialogVisible: false,
		isButtonActive: false,
		form: {
			userName: "",
			password: "",
			passwordNew: "",
			loginAccount: "",
			email: "",
			tel: "",
			sex: "0",
			country: "",
			school: "",
			major: "",
			agree: "",
			age: "",
			birth: "",
			flair: "",
			address: "",
			workDate: "",
			remark: "",
			userType: 1,
			status: 1,
		},
		fileList: [],
		rules: {
          userName: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
          ],
          password: [
            { required: true, message: '请输入用户密码', trigger: 'blur' }
		  ],
		  passwordNew: [
            { required: true, message: '请输入确认密码', trigger: 'blur' }
		  ],
		  loginAccount: [
            { required: true, message: '请输入登陆账号', trigger: 'blur' }
		  ],
		  email: [
            { required: true, message: '请输入邮箱', trigger: 'blur' }
		  ],
		  tel: [
            { required: true, message: '请输入联系方式', trigger: 'blur' }
		  ],
		  sex: [
            { required: true, message: '请选择性别', trigger: 'blur' }
		  ],
		  country: [
            { required: true, message: '请选择国家', trigger: 'blur' }
		  ],
		  school: [
            { required: true, message: '请选择院校', trigger: 'blur' }
		  ],
		  major: [
            { required: true, message: '请选择专业', trigger: 'blur' }
		  ],
		  agree: [
            { required: true, message: '请选择职称', trigger: 'blur' }
		  ],
		  age: [
            { required: true, message: '请输入年龄', trigger: 'blur' }
		  ],
		  birth: [
            { required: true, message: '请输入出生日期', trigger: 'blur' }
		  ],
		  address: [
            { required: true, message: '请输入地址', trigger: 'blur' }
		  ],
		  workDate: [
            { required: true, message: '请输入教龄', trigger: 'blur' }
		  ],
		  flair: [
            { required: true, message: '请上传教学资质', trigger: 'blur' },
          ],
		  remark: [
            { required: true, message: '请输入自我介绍', trigger: 'blur' }
          ],
        }
	  }
	},
	components: {
        bottomPage
	},
	methods: {
		handleAvatorSuccess(res) {
			let _this = this;
			if(res.code == 1000){
				_this.$message({
				type: 'success',
				message: '上传成功!'
				});
				this.fileList.push({url:this.$store.state.HOST + res.message})
				this.form.flair = res.message
			}else{
			_this.$notify.error({
				title: '错误',
				message: res.message
			});
			}
		},
		handleRemove(file, fileList) {
			this.form.flair = ""
			this.fileList = []
		},
		handlePictureCardPreview(file) {
			this.dialogImageUrl = file.url;
			this.dialogVisible = true;
		},
		getApeSchoolList() {
			getApeSchoolList().then(res=> {
			if(res.code == 1000) {
				this.school = res.data
			} else {
				this.$notify.error({
				title: '错误',
				message: res.message
			});
			}
			})
		},
		getApeMajorList() {
			getApeMajorList().then(res => {
			if(res.code == 1000) {
				this.major = res.data
			} else {
				this.$notify.error({
				title: '错误',
				message: res.message
			});
			}
			})
		},
		toRegister() {
			this.$router.push("/register")
		},
		toLogin() {
			this.$router.push("/login")
		},
		toRuZhu() {
			if(!this.form.userName) {
				this.$message({
					message: '请输入姓名',
					type: 'warning'
				});
				return;
			}
			if(!this.form.password) {
				this.$message({
					message: '请输入密码',
					type: 'warning'
				});
				return;
			}
			if(!this.form.passwordNew) {
				this.$message({
					message: '请输入确认密码',
					type: 'warning'
				});
				return;
			}
			if (this.form.passwordNew != this.form.password) {
				this.$message({
					message: '两次输入的密码不一致！',
					type: 'warning'
				});
				return;
			}
			if(!this.form.loginAccount) {
				this.$message({
					message: '请输入登陆账号',
					type: 'warning'
				});
				return;
			}
			if(!this.form.email) {
				this.$message({
					message: '请输入邮箱地址',
					type: 'warning'
				});
				return;
			}
			if (this.fileList.length <= 0) {
				this.$message({
					message: '请上传教学资质！',
					type: 'warning'
				});
				return;
			}
			register(this.form).then(res => {
				if(res.code == 1000) {
					this.$message({
						message: '入驻成功,请等待平台审核',
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
	  this.getApeSchoolList()
	  this.getApeMajorList()
	}
 }
</script>

<style scoped>
  .teacher-login-container {
    min-height: 100vh;
    position: relative;
    overflow: hidden;
    background: #0f172a;
    padding: 0;
    font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  }

  /* --- 背景优化 --- */
  .animated-background-teacher {
    position: absolute;
    inset: 0;
    z-index: 0;
  }

  .mesh-gradient {
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(at 0% 0%, #1e293b 0px, transparent 50%),
      radial-gradient(at 50% 0%, #1e1b4b 0px, transparent 50%),
      radial-gradient(at 100% 0%, #0f172a 0px, transparent 50%),
      radial-gradient(at 0% 50%, #1e1b4b 0px, transparent 50%),
      radial-gradient(at 50% 50%, #0f172a 0px, transparent 50%),
      radial-gradient(at 100% 50%, #1e293b 0px, transparent 50%),
      radial-gradient(at 0% 100%, #0f172a 0px, transparent 50%),
      radial-gradient(at 50% 100%, #1e293b 0px, transparent 50%),
      radial-gradient(at 100% 100%, #1e1b4b 0px, transparent 50%);
    filter: blur(40px);
    opacity: 0.8;
  }

  .grid-overlay {
    position: absolute;
    inset: 0;
    background-image: 
      linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
    background-size: 50px 50px;
    mask-image: radial-gradient(circle at center, black, transparent 85%);
  }

  .physics-decorations {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }

  .formula {
    position: absolute;
    color: rgba(255, 255, 255, 0.1);
    font-family: 'Times New Roman', serif;
    font-style: italic;
    user-select: none;
    animation: floatFormula 10s ease-in-out infinite;
  }

  .f1 { top: 15%; left: 10%; font-size: 2rem; }
  .f2 { top: 60%; left: 5%; font-size: 1.5rem; animation-delay: -2s; }
  .f3 { top: 10%; right: 20%; font-size: 1.4rem; animation-delay: -1s; }
  .f4 { bottom: 15%; right: 15%; font-size: 1.5rem; animation-delay: -3s; }

  @keyframes floatFormula {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    50% { transform: translateY(-20px) rotate(2deg); }
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
    width: 30%;
    height: 100%;
    display: flex;
    align-items: center;
  }

  .logo-text {
    font-size: 42px;
    font-weight: 900;
    margin-left: 100px;
    background: linear-gradient(
      to right, 
      #fff 20%, 
      #818cf8 40%, 
      #6366f1 60%, 
      #fff 80%
    );
    background-size: 200% auto;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: 8px;
    animation: shine 3s linear infinite;
    position: relative;
    cursor: pointer;
    user-select: none;
    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    display: inline-block;
    filter: drop-shadow(0 0 10px rgba(99, 102, 241, 0.3));
  }

  @keyframes shine {
    to { background-position: 200% center; }
  }

  .logo-text:hover {
    transform: scale(1.1) rotate(-2deg);
    letter-spacing: 12px;
    filter: drop-shadow(0 0 25px rgba(99, 102, 241, 0.8));
    text-shadow: 2px 2px 0px #4338ca, 4px 4px 0px rgba(0,0,0,0.2);
  }

  .logo-text::after {
    content: 'TEACHER';
    position: absolute;
    font-size: 10px;
    bottom: -15px;
    right: 5px;
    letter-spacing: 2px;
    -webkit-text-fill-color: #818cf8;
    font-weight: 400;
    opacity: 0;
    transition: all 0.4s ease;
  }

  .logo-text:hover::after {
    opacity: 1;
    bottom: -5px;
  }

  .logo-text::before {
    content: '';
    position: absolute;
    bottom: -8px;
    left: 0;
    width: 0%;
    height: 4px;
    background: linear-gradient(to right, #6366f1, #a5b4fc, #6366f1);
    border-radius: 4px;
    transition: width 0.4s ease;
    box-shadow: 0 0 15px rgba(99, 102, 241, 0.6);
  }

  .logo-text:hover::before {
    width: 100%;
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
    padding: 0 5%;
  }

  /* 左侧内容区 */
  .about {
    width: 50%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
  }

  .huanying {
    font-size: 36px;
    font-weight: bold;
    color: white;
    margin-bottom: 20px;
  }

  .huanying1 {
    color: rgba(255, 255, 255, 0.9);
    font-size: 16px;
    margin-bottom: 10px;
    line-height: 1.6;
    font-family: 'Microsoft YaHei', sans-serif;
  }

  /* 液态玻璃入驻表单 */
  .teacher-form-glass {
    width: 450px;
    max-width: 90%;
    height: auto;
    max-height: 95vh;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    transition: transform 0.1s ease-out;
    transform-style: preserve-3d;
    margin: 20px auto;
  }

  /* 玻璃效果层 */
  .glass-effect-teacher {
    position: absolute;
    inset: 0;
    z-index: 0;
    backdrop-filter: blur(5px);
    filter: url(#glass-distortion-teacher);
    isolation: isolate;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.1);
  }

  .glass-tint-teacher {
    position: absolute;
    inset: 0;
    z-index: 1;
    background: rgba(0, 0, 0, 0.15);
    border-radius: 24px;
  }

  .glass-shine-teacher {
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

  .ruzhu-content {
    width: 90%;
    height: auto;
    min-height: 75%;
    max-height: 75vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    gap: 12px;
    position: relative;
    z-index: 3;
    overflow-y: auto;
    padding: 20px 0 15px 0;
    box-sizing: border-box;
  }
  
  .ruzhu-content::-webkit-scrollbar {
    width: 4px;
    height: 0;
  }
  
  .ruzhu-content::-webkit-scrollbar-track {
    background: transparent;
  }
  
  .ruzhu-content::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
  
  .ruzhu-content::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 255, 255, 0.4);
  }
  
  /* 移除水平滚动条 */
  .ruzhu-content::-webkit-scrollbar-horizontal {
    display: none;
  }

  .ruzhu-title-glass {
    text-align: center;
    color: #fff;
    margin-bottom: 2rem;
    font-size: 2rem;
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

  /* 绿色教育风格按钮 */
  .glass-button-teacher {
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
    margin-top: 1rem;
    margin-bottom: 1rem;
    padding-bottom: 2rem;
    z-index: 5;
  }

  .glass-button-teacher:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  }

  .glass-button-teacher:active {
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

  .glass-button-teacher.clicked .click-gradient {
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
  
  /* 上传组件样式 */
  .upload-container {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .upload-label {
    color: white;
    font-size: 16px;
    margin-bottom: 10px;
    font-weight: 500;
  }
  
  /* Element UI 上传组件样式覆盖 */
  .el-upload--picture-card {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border: 1px dashed rgba(255, 255, 255, 0.3) !important;
    color: white !important;
    backdrop-filter: blur(10px) !important;
    transition: all 0.3s ease !important;
    width: 100% !important;
    height: 48px !important;
    line-height: 48px !important;
  }
  
  .el-upload--picture-card:hover {
    background-color: rgba(255, 255, 255, 0.15) !important;
    border-color: rgba(255, 255, 255, 0.5) !important;
  }
  
  .el-upload--picture-card i {
    color: white !important;
    font-size: 20px !important;
  }
  
  .el-upload-list__item {
    background: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    backdrop-filter: blur(10px) !important;
  }
  
  .el-upload-list__item-info {
    background: transparent !important;
  }
  
  .el-upload-list__item-status-label {
    background: rgba(46, 139, 87, 0.8) !important;
  }
  
  .el-upload-list__item:hover {
    box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1) !important;
  }
  
  /* 修复 select 和 textarea 的样式 */
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
  
  .glass-input option {
    background: rgba(46, 139, 87, 0.9);
    color: white;
  }
  
  .glass-input:focus {
    outline: none;
    background: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 15px rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.3);
  }
  
  /* 调整表单高度以适应更多字段 */
  .teacher-form-glass {
    width: 450px;
    max-width: 90%;
    height: auto;
    max-height: 95vh;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    transition: transform 0.1s ease-out;
    transform-style: preserve-3d;
    margin: 20px auto;
  }
</style>
