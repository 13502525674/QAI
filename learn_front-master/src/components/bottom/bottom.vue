<template>
  <footer class="footer-container">
    <div class="footer-content">
      <!-- Brand Section -->
      <div class="footer-section brand-section">
        <div class="system-title">
          <span class="logo-text">智学伴</span>
          <div class="logo-glow"></div>
        </div>
        <div class="brand-desc">
          <h3 class="section-title">关于我们 <span>Contact Us</span></h3>
          <p class="desc-text">致力于提供智能化、个性化的学习体验，成为您学习路上的得力伙伴。</p>
          <p class="contact-email"><i class="el-icon-message"></i> 3030828146@qq.com</p>
        </div>
      </div>

      <!-- Quick Links 1 -->
      <div class="footer-section link-section">
        <h3 class="section-title">核心导航 <span>Menu</span></h3>
        <ul class="footer-links">
          <li @click="toPage(0)"><i class="el-icon-s-home"></i>主页</li>
          <li @click="toPage(1)"><i class="el-icon-reading"></i>课程</li>
          <li @click="toPage(2)"><i class="el-icon-user"></i>教师</li>
          <li @click="toPage(3)"><i class="el-icon-document-checked"></i>考试</li>
          <li v-if="userType === 2" @click="toPage(4)"><i class="el-icon-notebook-2"></i>笔记</li>
        </ul>
      </div>

      <!-- Quick Links 2 -->
      <div class="footer-section link-section">
        <h3 class="section-title">更多服务 <span>Explore</span></h3>
        <ul class="footer-links">
          <li @click="toPage(5)"><i class="el-icon-bell"></i>公告</li>
          <li @click="toPage(6)"><i class="el-icon-chat-dot-round"></i>留言</li>
          <li @click="toPage(7)"><i class="el-icon-info"></i>关于</li>
          <li @click="toPage(8)"><i class="el-icon-s-custom"></i>个人中心</li>
        </ul>
      </div>

      <!-- Contact Info -->
      <div class="footer-section contact-section">
        <h3 class="section-title">联系方式 <span>Contact</span></h3>
        <ul class="contact-list">
          <li>
            <div class="contact-icon"><i class="el-icon-message"></i></div>
            <div class="contact-detail">
              <span class="label">邮箱</span>
              <span class="value">3030828146@qq.com</span>
            </div>
          </li>
          <li>
            <div class="contact-icon"><i class="el-icon-chat-round"></i></div>
            <div class="contact-detail">
              <span class="label">QQ / 微信</span>
              <span class="value">3030828146 / chaungzhi...</span>
            </div>
          </li>
          <li>
            <div class="contact-icon"><i class="el-icon-phone-outline"></i></div>
            <div class="contact-detail">
              <span class="label">电话</span>
              <span class="value">17728356872</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2026 智学伴 AI Learning Buddy. All rights reserved.</p>
    </div>
  </footer>
</template>

<script>
  export default {
    data() {
      return{
        userType: null
      }
    },
    methods: {
        toPage(index) {
          if (index == 0) {
            this.$router.push("/")
          } else if (index == 1) {
            this.$router.push("/task")
          } else if (index == 2) {
            this.$router.push("/teacher")
          } else if (index == 3) {
            this.$router.push("/test")
          } else if (index == 4) {
            this.$router.push("/article")
          } else if (index == 5) {
            this.$router.push("/notice")
          } else if (index == 6) {
            this.$router.push("/message")
          } else if (index == 7) {
            this.$router.push("/about")
          } else if (index == 8) {
            this.$router.push("/center")
          }
        }
    },
    created() {
      const userInfo = JSON.parse(window.localStorage.getItem("user_info"));
      if (userInfo) {
        this.userType = userInfo.userType;
      }
    },
    mounted() {
      
    }
 }
</script>

<style scoped>
/* Base Footer Container */
.footer-container {
    width: 100%;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
    border-top: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 -4px 30px rgba(0, 0, 0, 0.03);
    position: relative;
    overflow: hidden;
    color: #475569;
    font-family: 'Montserrat', 'PingFang SC', sans-serif;
    margin-top: auto;
}

/* Add a subtle background glow to match the vibe */
.footer-container::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -10%;
    width: 50%;
    height: 100%;
    background: radial-gradient(circle, rgba(32, 180, 134, 0.06) 0%, transparent 70%);
    z-index: 0;
    pointer-events: none;
}

.footer-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 60px 20px 40px;
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1.5fr;
    gap: 40px;
    position: relative;
    z-index: 1;
}

/* Typography & Titles */
.section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 24px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    position: relative;
}

.section-title span {
    font-size: 12px;
    color: #20B486;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1px;
}

/* Brand Section */
.system-title {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    cursor: default;
}

.logo-text {
    font-size: 32px;
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

.desc-text {
    color: #64748B;
    line-height: 1.6;
    margin-bottom: 16px;
    font-size: 14px;
    max-width: 80%;
}

.contact-email {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #20B486;
    font-weight: 600;
    font-size: 14px;
    transition: transform 0.3s ease;
    cursor: pointer;
    width: fit-content;
}

.contact-email:hover {
    transform: translateX(4px);
}

/* Links Section */
.footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.footer-links li {
    color: #64748B;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    width: fit-content;
    position: relative;
}

.footer-links li i {
    font-size: 16px;
    color: #94a3b8;
    transition: all 0.3s ease;
}

.footer-links li::after {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 26px;
    width: 0;
    height: 2px;
    background: #20B486;
    transition: width 0.3s ease;
    border-radius: 2px;
}

.footer-links li:hover {
    color: #20B486;
    transform: translateX(8px);
}

.footer-links li:hover i {
    color: #20B486;
    transform: scale(1.15) rotate(-5deg);
}

.footer-links li:hover::after {
    width: 20px;
}

/* Contact Info Section */
.contact-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.contact-list li {
    display: flex;
    align-items: center;
    gap: 16px;
    cursor: default;
    transition: transform 0.3s ease;
}

.contact-list li:hover {
    transform: translateX(6px);
}

.contact-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    background: rgba(32, 180, 134, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #20B486;
    font-size: 20px;
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.contact-list li:hover .contact-icon {
    background: linear-gradient(135deg, #20B486, #12d8fa);
    color: #fff;
    transform: translateY(-4px) rotate(5deg);
    box-shadow: 0 8px 16px rgba(32, 180, 134, 0.25);
}

.contact-detail {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.contact-detail .label {
    font-size: 12px;
    color: #94a3b8;
    font-weight: 600;
}

.contact-detail .value {
    font-size: 14px;
    color: #1e293b;
    font-weight: 600;
}

/* Footer Bottom */
.footer-bottom {
    border-top: 1px solid rgba(226, 232, 240, 0.6);
    padding: 24px 20px;
    text-align: center;
    position: relative;
    z-index: 1;
}

.footer-bottom p {
    color: #94a3b8;
    font-size: 13px;
    margin: 0;
    font-weight: 500;
}

@media (max-width: 992px) {
    .footer-content {
        grid-template-columns: 1fr 1fr;
    }
}

@media (max-width: 576px) {
    .footer-content {
        grid-template-columns: 1fr;
    }
}
</style>