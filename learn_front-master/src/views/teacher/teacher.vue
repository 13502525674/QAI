<template>
  <div class="teacher"> 
    <headerPage></headerPage>
    <div class="teacher-page-content">
      <!-- 标题区域 -->
      <div class="teacher-page-header">
        <div class="teacher-page-title-wrapper">
          <span class="teacher-page-subtitle">EXPLORE</span>
          <h1 class="teacher-page-main-title">我们的<span class="highlight-text">教师团队</span></h1>
        </div>
      </div>

      <!-- 搜索区域 - 现代化卡片设计 -->
      <div class="teacher-page-search">
        <div class="search-card">
          <div class="search-input-group">
            <div class="input-wrapper">
              <i class="el-icon-user"></i>
              <el-input 
                v-model="search.userName" 
                placeholder="搜索教师姓名"
                class="modern-input"
                @keyup.enter.native="searchPage()">
              </el-input>
            </div>
            <div class="search-actions">
              <el-button 
                icon="el-icon-search" 
                type="primary" 
                class="search-btn"
                @click="searchPage()">
                搜索
              </el-button>
              <el-button 
                icon="el-icon-refresh" 
                class="reset-btn" 
                @click="refresh()">
                重置
              </el-button>
            </div>
          </div>
          <div class="school-tags">
            <span class="tags-label">热门学校：</span>
            <span 
              class="school-tag" 
              :class="{ active: search.school === item.name }"
              @click="searchSchool(item.name)" 
              v-for="(item, index) in school" 
              :key="index">
              {{ item.name }}
            </span>
          </div>
        </div>
      </div>

      <!-- 教师卡片轮播区域 -->
      <div class="teacher-carousel-section">
        <div class="carousel-header">
          <h2 class="carousel-title">明星教师</h2>
          <div class="carousel-controls">
            <button class="carousel-btn prev" @click="prevSlide" :disabled="currentIndex === 0">
              <i class="el-icon-arrow-left"></i>
            </button>
            <button class="carousel-btn next" @click="nextSlide" :disabled="currentIndex >= maxIndex">
              <i class="el-icon-arrow-right"></i>
            </button>
          </div>
        </div>

        <div class="carousel-container">
          <div class="carousel-track-wrapper">
            <div class="carousel-track" :style="trackStyle">
              <div 
                v-for="(item, index) in displayTeachers" 
                :key="item.id || index"
                class="carousel-slide"
                :style="getSlideStyle(index)">
                <div class="teacher-card" @click="toTeacherInfo(item.id)">
                  <div class="card-image-wrapper">
                    <img :src="$store.state.HOST + item.avatar" class="teacher-avatar">
                    <div class="card-overlay">
                      <i class="el-icon-view"></i>
                      <span>查看详情</span>
                    </div>
                  </div>
                  <div class="card-content">
                    <h3 class="teacher-name">{{ item.userName }}</h3>
                    <div class="teacher-school">
                      <i class="el-icon-location"></i>
                      <span>{{ item.school }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 轮播指示器 -->
        <div class="carousel-indicators">
          <span 
            v-for="(item, index) in maxIndex + 1" 
            :key="index"
            class="indicator"
            :class="{ active: currentIndex === index }"
            @click="goToSlide(index)">
          </span>
        </div>
      </div>

      <div class="teacher-bottom-image">
        <img style="width:100%" src="../../assets/image/17.png">
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script> 
  import {getUserPage,getApeSchoolList} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        search: {
            userName: "",
            userType: 1,
            school: "",
            pageSize: 12,
            pageNumber: 1
        },
        tableData: [],
        school: [],
        total: 0,
        // 轮播图相关
        currentIndex: 0,
        itemsPerSlide: 4,
        autoPlay: null,
        autoPlayInterval: 4000
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    computed: {
      // 计算显示的轮播数据
      displayTeachers() {
        return this.tableData
      },
      // 计算最大索引
      maxIndex() {
        const totalSlides = Math.ceil(this.tableData.length / this.itemsPerSlide)
        return Math.max(0, totalSlides - 1)
      },
      // 计算轮播轨道样式
      trackStyle() {
        const offset = -this.currentIndex * (100 / this.itemsPerSlide)
        return {
          transform: `translateX(${offset}%)`,
          transition: 'transform 0.5s ease-in-out'
        }
      }
    },
    methods: {
        searchPage() {
          this.search.pageNumber = 1
          this.currentIndex = 0
          this.query()
        },
        searchSchool(name) {
          this.search.pageNumber = 1
          this.search.school = name
          this.currentIndex = 0
          this.query()
        },
        query() {
          getUserPage(this.search).then(res => {
            if(res.code == 1000) {
              this.tableData = res.data.records
              this.total = res.data.total
            } else {
              this.$notify.error({
                title: '错误',
                message: res.message
              });
            }
          })
        },
        refresh() {
          this.search = {
            userName: "",
            userType: 1,
            school: "",
            pageSize: 12,
            pageNumber: 1
          },
          this.currentIndex = 0
          this.query()
        },

        toTeacherInfo(id) {
          this.$router.push("/teacherInfo?id=" + id)
        },
        getApeSchoolList() {
          getApeSchoolList().then(res => {
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
        // 轮播图控制方法
        nextSlide() {
          if (this.currentIndex < this.maxIndex) {
            this.currentIndex++
          } else {
            this.currentIndex = 0
          }
        },
        prevSlide() {
          if (this.currentIndex > 0) {
            this.currentIndex--
          } else {
            this.currentIndex = this.maxIndex
          }
        },
        goToSlide(index) {
          this.currentIndex = index
        },
        getSlideStyle(index) {
          return {}
        },
        // 启动自动播放
        startAutoPlay() {
          this.autoPlay = setInterval(() => {
            this.nextSlide()
          }, this.autoPlayInterval)
        },
        // 停止自动播放
        stopAutoPlay() {
          if (this.autoPlay) {
            clearInterval(this.autoPlay)
            this.autoPlay = null
          }
        }
    },
    created() {
     
    },
    mounted() {
      this.getApeSchoolList()
      this.query()
      // 启动自动播放
      this.startAutoPlay()
    },
    beforeDestroy() {
      this.stopAutoPlay()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/teacher/teacher.css");
</style>