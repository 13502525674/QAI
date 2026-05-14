<template>
  <div class="index">
      <headerPage></headerPage>
      <div class="index-search">
          <div class="index-search-content">
              <div class="search-content">
                <div class="content1">探索物理世界</div>
                <div class="content2">EXPLORE PHYSICS</div>
                <div class="content3">覆盖<span style="color:#20B486">全国</span>物理知识点，配套专属训练题库</div>
                <div class="content4">Covers<span style="color:#20B486">all high school </span> physics knowledge points with exclusive question banks</div>
                <div class="content5"> 从运动学到近代物理，构建完整知识体系</div>
                <div class="content6">From kinematics to modern physics, build a complete knowledge system.</div>
                <el-input class="searchKey" v-model="searchKey" suffix-icon="el-icon-search" placeholder="请输入想要搜索的课程名称并按回车键搜索" @change="searchTask()"></el-input>
              </div>
            </div>
          <div class="index-image">
            <img class="index-search-image" src="../../assets/image/index/index-image.png"></img>
          </div>
      </div>
      <div class="most-major">
          <div class="most-content"> 
              <div class="page-title">
                <div style="letter-spacing: 2px;">热门物理知识点</div>
                <div style="margin-top:10px;color:#92969B">Most <span style="color:#20B486">Popular Physics Knowledge Points</span></div>
              </div>
              <div class="major-list">
                <div class="major-item" v-for="(item,index) in major" :key="index">
                    <div class="major-name">
                        <i v-if="index == 0" style="font-size:21px" class="el-icon-info"></i>
                        <i v-if="index == 1" style="font-size:21px" class="el-icon-arrow-down"></i>
                        <i v-if="index == 2" style="font-size:21px" class="el-icon-finished"></i>
                        <i v-if="index == 3" style="font-size:21px" class="el-icon-sunset"></i>
                        <i v-if="index == 4" style="font-size:21px" class="el-icon-timer"></i>
                        <i v-if="index == 5" style="font-size:21px" class="el-icon-basketball"></i>
                        <i v-if="index == 6" style="font-size:21px" class="el-icon-refresh"></i>
                        <i v-if="index == 7" style="font-size:21px" class="el-icon-bicycle"></i>
                        <i v-if="index == 8" style="font-size:21px" class="el-icon-lightning"></i>
                        <i v-if="index == 9" style="font-size:21px" class="el-icon-cpu"></i>
                        <i v-if="index == 10" style="font-size:21px" class="el-icon-thumb"></i>
                        <i v-if="index == 11" style="font-size:21px" class="el-icon-mouse"></i>
                        <i v-if="index == 12" style="font-size:21px" class="el-icon-moon-night"></i>
                        <i v-if="index == 13" style="font-size:21px" class="el-icon-milk-tea"></i>
                        <i v-if="index == 14" style="font-size:21px" class="el-icon-odometer"></i>
                        <i v-if="index == 15" style="font-size:21px" class="el-icon-sunny"></i>
                        <i v-if="index == 16" style="font-size:21px" class="el-icon-sunny"></i>
                        <i v-if="index == 17" style="font-size:21px" class="el-icon-info"></i>
                        <i v-if="index == 18" style="font-size:21px" class="el-icon-info"></i>
                        <i v-if="index == 19" style="font-size:21px" class="el-icon-odometer"></i>
                        <div style="margin-left:6px;letter-spacing: 1px;">{{item.name}}</div>
                    </div>
                    <el-button @click="majorTask(item.name, index)" style="margin-right:6px" plain size="mini" type="success" icon="el-icon-top-right"></el-button>
                </div>
              </div>
          </div>
      </div>
      <div class="shuju">
          <div class="most-content">
              <div class="page-title">
                <div style="letter-spacing: 2px;">平台数据</div>
                <div style="margin-top:10px;color:#92969B">Our <span style="color:#20B486"> Achievement</span></div>
              </div>
              <div class="shuju-total">
                  <div class="shuju-content">
                    <div class="shuju-item">
                        <img src="../../assets/image/index/Frame 67 (7).png">
                        <div class="shuju-name">
                            <div>{{data.count1}}+</div>
                            <div>教师 Teacher</div>
                        </div>
                    </div>
                    <div class="shuju-item">
                        <img src="../../assets/image/index/Frame 67 (4).png">
                        <div class="shuju-name">
                            <div>{{data.count}}+</div>
                            <div>教程 Video</div>
                        </div>
                    </div>
                    <div class="shuju-item">
                        <img src="../../assets/image/index/Frame 67 (6).png">
                        <div class="shuju-name">
                            <div>{{data.count2}}+</div>
                            <div>学生 Student</div>
                        </div>
                    </div>
                    <div class="shuju-item">
                        <img src="../../assets/image/index/Frame 67 (5).png">
                        <div class="shuju-name">
                            <div>{{data.total}}+</div>
                            <div>用户 Users</div>
                        </div>
                    </div>
                  </div>
                  <div class="shuju-image">
                      <img style="width:50%;height:120%" src="../../assets/image/index/niu.png">
                  </div>
              </div>
          </div>
      </div>
      <div class="liuyan">
          <div class="most-content">
              <div class="liuyan-font">
                <div class="liuyan-image">
                  <img style="width:80%" src="../../assets/image/index/Group 12.png">
                </div>
                <div class="liuyan-content">
                    <div>
                        <div>你的任何疑问都可以留言咨询</div>
                        <div style="margin-top:20px">You can leave a message for consultation if you have any questions</div>
                        <el-button @click="toMessage" style="margin-top:20px" size="mini" icon="el-icon-question" type="success">留言咨询</el-button>
                    </div>
                </div>
              </div>
          </div>
      </div>
      <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeMajorList,getIndexAchievement,getIndexArticleList} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        searchKey: "",
        major: [],
        data: {
            count: 0,
            count1: 0,
            count2: 0,
            total: 0
        },
        article: [],
        userType: null,
        studentId: ''
      }
    },
    components: {
		headerPage,
        bottomPage
	},
    methods: {
        searchTask() {
            if (this.searchKey) {
                this.$router.push("/task?name=" + this.searchKey)
            }
        },
        majorTask(major, index) {
            // 根据索引将知识点映射到对应的物理分支
            let physicsBranch;
            if (index >= 0 && index <= 3) {
                physicsBranch = "力学";  // 索引 0-3 对应力学
            } else if (index >= 4 && index <= 7) {
                physicsBranch = "运动学";  // 索引 4-7 对应运动学
            } else if (index >= 8 && index <= 11) {
                physicsBranch = "电学";  // 索引 8-11 对应电学
            } else if (index >= 12 && index <= 15) {
                physicsBranch = "光学";  // 索引 12-15 对应光学
            } else if (index >= 16 && index <= 19) {
                physicsBranch = "热学";  // 索引 16-19 对应热学
            }
            
            this.$router.push("/task?major=" + physicsBranch)
        },
        toArticle() {
            this.$router.push("/article")
        },
        toMessage() {
            this.$router.push("/message")
        },
        getApeMajorList() {
		getApeMajorList().then(res => {
                if(res.code == 1000) {
                    var major = res.data
                    this.major = major.splice(0,20)
                } else {
                    this.$notify.error({
                    title: '错误',
                    message: res.message
                    });
                }
		})
        },
        getIndexAchievement() {
            getIndexAchievement().then(res => {
                if(res.code == 1000) {
                    this.data = res.data
                } else {
                    this.$notify.error({
                    title: '错误',
                    message: res.message
                    });
                }
            })
        },
        getIndexArticleList() {
            getIndexArticleList().then(res => {
                if(res.code == 1000) {
                    this.article = res.data
                } else {
                    this.$notify.error({
                    title: '错误',
                    message: res.message
                    });
                }
            })
        }
    },
    created() {
      const userInfo = JSON.parse(window.localStorage.getItem("user_info"));
      if (userInfo) {
        this.userType = userInfo.userType;
        this.studentId = userInfo.id;
      }
    },
    mounted() {
      this.getApeMajorList()
      this.getIndexAchievement()
      this.getIndexArticleList()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/index/index.css");

  /* 在组件内部强制覆盖 Element UI 样式 */
  .searchKey >>> .el-input__inner {
      height: 60px !important;
      line-height: 60px !important;
      border-radius: 30px !important;
      background: #ffffff !important;
      border: 4px solid #20B486 !important;
      box-shadow: 0 0 0 4px #1a202c, 0 8px 24px rgba(32, 180, 134, 0.2) !important;
      padding: 0 30px !important;
      font-size: 16px !important;
      font-family: 'PingFang SC', '黑体', sans-serif !important;
      color: #1e293b !important;
      transition: all 0.3s ease !important;
  }

  .searchKey >>> .el-input__inner:focus {
      transform: translateY(-2px) !important;
      box-shadow: 0 0 0 4px #1a202c, 0 12px 30px rgba(32, 180, 134, 0.3) !important;
      border-color: #12d8fa !important;
  }

  .searchKey >>> .el-input__inner::placeholder {
      color: #94a3b8 !important;
      font-weight: 500 !important;
  }

  .searchKey >>> .el-input__suffix {
      right: 15px !important;
      display: flex !important;
      align-items: center !important;
  }

  .searchKey >>> .el-input__icon.el-icon-search {
      font-size: 20px !important;
      color: #1a202c !important;
      font-weight: bold !important;
      cursor: pointer !important;
      background: #20B486 !important;
      width: 44px !important;
      height: 44px !important;
      line-height: 40px !important; /* 调整 line-height 以垂直居中图标 */
      border-radius: 50% !important;
      margin-top: 4px !important; /* 调整 margin-top 使按钮在 60px 的框内居中 */
      border: 3px solid #1a202c !important;
      transition: all 0.3s ease !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
  }

  .searchKey >>> .el-input__icon.el-icon-search:hover {
      background: #12d8fa !important;
      transform: scale(1.1) rotate(5deg) !important;
  }
</style>