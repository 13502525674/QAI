<template>
    <div class="header-container">
        <div class="header-content">
            <div class="system-title" @click="toPage('index')">
                <span class="logo-text" data-text="智学伴">智学伴</span>
                <div class="logo-glow"></div>
            </div>
            
            <nav class="menu-list">
                <div @click="toPage('index')" :class="['nav-item', $route.path == '/' ? 'active' : '']">
                    <i class="el-icon-s-home"></i>主页
                </div>
                <div @click="toPage('task')" :class="['nav-item', $route.path == '/task' ? 'active' : '']">
                    <i class="el-icon-reading"></i>课程
                </div>
                <div @click="toPage('teacher')" :class="['nav-item', $route.path == '/teacher' ? 'active' : '']">
                    <i class="el-icon-user"></i>教师
                </div>
                <div @click="toPage('test')" :class="['nav-item', $route.path == '/test' ? 'active' : '']">
                    <i class="el-icon-document-checked"></i>考试
                </div>
                <div v-if="userType === 2" @click="toPage('physicsPractice')" :class="['nav-item', $route.path == '/physicsPractice' ? 'active' : '']">
                    <i class="el-icon-edit-outline"></i>物理题库
                </div>
                <div v-if="userType === 2" @click="toPage('knowledgeGraph')" :class="['nav-item', $route.path == '/knowledgeGraph' ? 'active' : '']">
                    <i class="el-icon-share"></i>知识图谱
                </div>
                <div @click="toPage('physicsLab')" :class="['nav-item', $route.path == '/physicsLab' ? 'active' : '']">
                    <i class="el-icon-magic-stick"></i>实验室
                </div>
                <div v-if="userType === 2" @click="toPage('funphysics')" :class="['nav-item', $route.path == '/funphysics' ? 'active' : '']">
                    <i class="el-icon-sugar"></i>趣味物理
                </div>
                <div v-if="userType === 2" @click="toPage('article')" :class="['nav-item', $route.path == '/article' ? 'active' : '']">
                    <i class="el-icon-notebook-2"></i>笔记
                </div>
                <div @click="toPage('forum')" :class="['nav-item', $route.path == '/forum' ? 'active' : '']">
                    <i class="el-icon-chat-dot-round"></i>讨论
                </div>
                <div @click="toPage('notice')" :class="['nav-item', $route.path == '/notice' ? 'active' : '']">
                    <i class="el-icon-bell"></i>公告
                </div>
                <div @click="toPage('message')" :class="['nav-item', $route.path == '/message' ? 'active' : '']">
                    <i class="el-icon-message"></i>留言
                </div>
                <div @click="toPage('about')" :class="['nav-item', $route.path == '/about' ? 'active' : '']">
                    <i class="el-icon-info"></i>关于
                </div>
            </nav>

            <div class="user-center">
                <el-dropdown @command="handleCommand" placement="bottom" trigger="hover">
                    <div class="avatar-wrapper el-dropdown-link">
                        <img class="avatar-img" :src="$store.state.HOST + userInfo.avatar" alt="avatar" v-if="userInfo && userInfo.avatar">
                        <img class="avatar-img" src="../../assets/image/avatar.jpeg" alt="avatar" v-else>
                        <div class="avatar-ring"></div>
                    </div>
                    <el-dropdown-menu slot="dropdown" class="custom-dropdown">
                        <el-dropdown-item command="a"><div class="drop-item"><i class="el-icon-user"></i>个人中心</div></el-dropdown-item>
                        <el-dropdown-item command="b"><div class="drop-item"><i class="el-icon-s-management"></i>我的课程</div></el-dropdown-item>
                        <el-dropdown-item command="j"><div class="drop-item"><i class="el-icon-star-on"></i>收藏课程</div></el-dropdown-item>
                        <el-dropdown-item command="i"><div class="drop-item"><i class="el-icon-collection-tag"></i>笔记收藏</div></el-dropdown-item>
                        <el-dropdown-item command="k"><div class="drop-item"><i class="el-icon-reading"></i>物理自习室</div></el-dropdown-item>
                        <el-dropdown-item v-if="userType === 2" command="c"><div class="drop-item"><i class="el-icon-edit"></i>记笔记</div></el-dropdown-item>
                        <el-dropdown-item v-if="userType === 2" command="d"><div class="drop-item"><i class="el-icon-notebook-1"></i>我的笔记</div></el-dropdown-item>
                        <el-dropdown-item v-if="userType === 2" command="e"><div class="drop-item"><i class="el-icon-trophy"></i>练习记录</div></el-dropdown-item>
                        <el-dropdown-item v-if="userType === 1" command="g"><div class="drop-item"><i class="el-icon-s-check"></i>练习批改</div></el-dropdown-item>
                        <el-dropdown-item command="f" divided><div class="drop-item logout-item"><i class="el-icon-switch-button"></i>退出登陆</div></el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
                <div class="username-wrapper">
                    <span class="username">{{userInfo ? userInfo.userName : '未登录'}}</span>
                    <span class="user-role" v-if="userType">{{ userType === 1 ? '老师' : '学生' }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
  export default {
    data() {
      return{
        userInfo: {},
        userType: null
      }
    },
    methods: {
        handleCommand(command) {
		  if (command == 'a') {
              this.$router.push("/center")
		  } else if(command == 'b') {
			  this.$router.push("/myTask")
		  } else if(command == 'c') {
              this.$router.push("/addArticle")
		  } else if(command == 'd') {
			  this.$router.push("/myArticle")
		  } else if(command == 'i') {
              this.$router.push("/myFavarArticle")
          } else if(command == 'j') {
              this.$router.push("/myFavor")
          }else if(command == 'k') {
              this.$router.push("/studyRoom") 
          }else if(command == 'e') {
              this.$router.push("/physicsPracticeRecords")
          } else if(command == 'g') {
              this.$router.push("/physicsPracticeGrading")
          } else if(command == 'f') {
              this.$store.dispatch('logout').then(() => {
                window.localStorage.removeItem("user_info")
                window.localStorage.removeItem("user_token")
                this.$message({
                    message: '退出成功',
                    type: 'success'
                });
                setTimeout(function(){
                    window.location.reload()
                },1000)
              })
		  }
	    },
        toPage(name) {
          if(name == 'about') {
              this.$router.push("/about")
          } else if (name == 'index') {
              this.$router.push("/")
          } else if (name == "task") {
              this.$router.push("/task")
          } else if (name == "teacher") {
              this.$router.push("/teacher")
          } else if (name == "message") {
              this.$router.push("/message")
          } else if (name == "test") {
              this.$router.push("/test")
          } else if (name == "physicsPractice") {
              this.$router.push("/physicsPractice")
          } else if (name == "knowledgeGraph") {
              this.$router.push("/knowledgeGraph")
          } else if (name == "physicsLab") {
              this.$router.push("/physicsLab")
          } else if (name == "funphysics") {
              this.$router.push("/funphysics")
          }else if (name == "article") {
              this.$router.push("/article")
          } else if (name == "notice") {
              this.$router.push("/notice")
          } else if (name == "forum") {
              this.$router.push("/forum")
          } 
      },
    },
    created() {
      const userInfo = JSON.parse(window.localStorage.getItem("user_info"));
      if (userInfo) {
        this.userInfo = userInfo;
        this.userType = userInfo.userType;
      }
    },
    mounted() {
      
    }
 }
</script>

<style scoped>
/* Base Reset and Container */
.header-container {
    width: 100%;
    height: 70px;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.04);
    position: sticky;
    top: 0;
    z-index: 1000;
    display: flex;
    justify-content: center;
    transition: all 0.3s ease;
}

.header-content {
    width: 100%;
    max-width: 1400px;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
}

/* Logo Styles */
.system-title {
    position: relative;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 5px 10px;
}

.logo-text {
    font-size: 28px;
    font-weight: 900;
    letter-spacing: 2px;
    background: linear-gradient(120deg, #20B486, #12d8fa, #20B486);
    background-size: 200% auto;
    color: transparent;
    -webkit-background-clip: text;
    background-clip: text;
    animation: shine 4s linear infinite;
    position: relative;
    z-index: 2;
    font-family: 'Poppins', 'PingFang SC', sans-serif;
    text-shadow: 0px 4px 15px rgba(32, 180, 134, 0.2);
}

@keyframes shine {
    to { background-position: 200% center; }
}

.logo-glow {
    position: absolute;
    width: 100%;
    height: 100%;
    background: #20B486;
    filter: blur(25px);
    opacity: 0.15;
    z-index: 1;
    border-radius: 50%;
    transition: opacity 0.4s ease;
}

.system-title:hover .logo-glow {
    opacity: 0.4;
}

/* Navigation Menu */
.menu-list {
    display: flex;
    gap: 4px;
    align-items: center;
}

.nav-item {
    padding: 8px 12px;
    border-radius: 12px;
    font-family: 'Montserrat', 'PingFang SC', sans-serif;
    font-weight: 600;
    font-size: 15px;
    color: #64748B;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    position: relative;
    z-index: 1;
    overflow: hidden;
    display: flex;
    align-items: center;
    gap: 6px;
}

.nav-item i {
    font-size: 16px;
    transition: transform 0.3s ease;
}

.nav-item::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(32, 180, 134, 0.1);
    border-radius: 12px;
    transform: scale(0);
    transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    z-index: -1;
}

.nav-item:hover::before,
.nav-item.active::before {
    transform: scale(1);
}

.nav-item:hover,
.nav-item.active {
    color: #20B486;
    transform: translateY(-2px);
}

.nav-item:hover i {
    transform: scale(1.15) rotate(-5deg);
}

/* User Center */
.user-center {
    display: flex;
    align-items: center;
    gap: 12px;
}

.avatar-wrapper {
    position: relative;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-img {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    object-fit: cover;
    z-index: 2;
    border: 2px solid #fff;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    transition: transform 0.3s ease;
}

.avatar-ring {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    border-radius: 50%;
    background: linear-gradient(45deg, #20B486, #12d8fa);
    z-index: 1;
    opacity: 0;
    transform: scale(0.9);
    transition: all 0.4s ease;
}

.avatar-wrapper:hover .avatar-img {
    transform: scale(0.95);
}

.avatar-wrapper:hover .avatar-ring {
    opacity: 1;
    transform: scale(1.05);
    animation: spin 3s linear infinite;
}

@keyframes spin {
    100% { transform: scale(1.05) rotate(360deg); }
}

.username-wrapper {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.username {
    font-family: 'Poppins', 'PingFang SC', sans-serif;
    font-weight: 700;
    color: #1e293b;
    font-size: 14px;
}

.user-role {
    font-size: 11px;
    color: #20B486;
    font-weight: 600;
    background: rgba(32, 180, 134, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
    width: fit-content;
    margin-top: 2px;
}

/* Dropdown Customization */
.custom-dropdown {
    border-radius: 12px !important;
    padding: 8px !important;
    border: none !important;
    box-shadow: 0 10px 40px rgba(0,0,0,0.08) !important;
    background: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(10px);
}

.drop-item {
    font-family: 'Montserrat', 'PingFang SC', sans-serif;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 6px;
    border-radius: 8px;
    transition: all 0.2s ease;
}

.drop-item i {
    font-size: 16px;
    color: #64748B;
    transition: color 0.2s ease;
}

.el-dropdown-menu__item:hover .drop-item {
    background: rgba(32, 180, 134, 0.08);
    color: #20B486;
    transform: translateX(4px);
}

.el-dropdown-menu__item:hover .drop-item i {
    color: #20B486;
}

.logout-item {
    color: #ef4444 !important;
}
.el-dropdown-menu__item:hover .logout-item {
    background: rgba(239, 68, 68, 0.08) !important;
    color: #ef4444 !important;
}
.el-dropdown-menu__item:hover .logout-item i {
    color: #ef4444 !important;
}
</style>