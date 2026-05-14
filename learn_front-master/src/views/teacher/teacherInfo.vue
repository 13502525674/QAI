<template>
  <div class="teacherInfo">
    <headerPage></headerPage>
    <div class="teacherInfo-content">
        <div class="teacherInfo-name">
            <div class="teacherInfo-header">
                <div class="teacherInfo-avatar">
                    <img :src="$store.state.HOST + teacher.avatar">
                </div>
                <div class="teacherInfo-basic">
                    <div class="teacherInfo-title">{{teacher.userName}}</div>
                    <div class="teacherInfo-subtitle" v-if="teacher.agree">{{teacher.agree}}</div>
                    <div class="teacherInfo-tags">
                        <el-tag size="small" v-if="teacher.school" type="success">{{teacher.school}}</el-tag>
                        <el-tag size="small" v-if="teacher.major" type="primary">{{teacher.major}}</el-tag>
                    </div>
                </div>
            </div>
            <div class="teacherInfo-body">
                <div class="teacherInfo-left">
                    <div class="teacherInfo-card">
                        <div class="card-title"><i class="el-icon-user"></i> 基本信息</div>
                        <div class="card-content">
                            <div class="info-item" v-if="teacher.country">
                                <span class="info-label">国家：</span>
                                <span class="info-value">{{teacher.country}}</span>
                            </div>
                            <div class="info-item" v-if="teacher.school">
                                <span class="info-label">学校：</span>
                                <span class="info-value">{{teacher.school}}</span>
                            </div>
                            <div class="info-item" v-if="teacher.major">
                                <span class="info-label">物理分支：</span>
                                <span class="info-value">{{teacher.major}}</span>
                            </div>
                            <div class="info-item" v-if="teacher.agree">
                                <span class="info-label">职称：</span>
                                <span class="info-value">{{teacher.agree}}</span>
                            </div>
                            <div class="info-item" v-if="teacher.age">
                                <span class="info-label">年龄：</span>
                                <span class="info-value">{{teacher.age}}岁</span>
                            </div>
                            <div class="info-item" v-if="teacher.workDate">
                                <span class="info-label">教龄：</span>
                                <span class="info-value">{{teacher.workDate}}年</span>
                            </div>
                            <div class="info-item" v-if="teacher.address">
                                <span class="info-label">地址：</span>
                                <span class="info-value">{{teacher.address}}</span>
                            </div>
                        </div>
                    </div>
                    <div class="teacherInfo-card">
                        <div class="card-title"><i class="el-icon-phone"></i> 联系方式</div>
                        <div class="card-content">
                            <div class="info-item" v-if="teacher.tel">
                                <span class="info-label">手机：</span>
                                <span class="info-value">{{teacher.tel}}</span>
                            </div>
                            <div class="info-item" v-if="teacher.email">
                                <span class="info-label">邮箱：</span>
                                <span class="info-value">{{teacher.email}}</span>
                            </div>
                        </div>
                    </div>
                    <div class="teacherInfo-card" v-if="teacher.remark">
                        <div class="card-title"><i class="el-icon-document"></i> 个人简介</div>
                        <div class="card-content">
                            <div class="info-remark">{{teacher.remark}}</div>
                        </div>
                    </div>
                    <div class="teacherInfo-card" v-if="teacher.flair">
                        <div class="card-title"><i class="el-icon-medal"></i> 教学资质</div>
                        <div class="card-content">
                            <div class="flair-image" @click="previewFlair">
                                <img :src="$store.state.HOST + teacher.flair" alt="教学资质证书">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="teacherInfo-right">
                    <div class="teacherInfo-card course-card">
                        <div class="card-title"><i class="el-icon-reading"></i> 课程列表</div>
                        <div class="course-list">
                            <div v-if="task.length <= 0" class="course-empty">
                                <el-empty description="暂无课程" :image-size="80"></el-empty>
                            </div>
                            <div @click="toTask(item.id)" class="course-item" v-for="(item,index) in task" :key="index">
                                <div class="course-name">{{item.name}}</div>
                                <i class="el-icon-arrow-right"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <bottomPage></bottomPage>
    <el-dialog :visible.sync="flairDialogVisible" title="教学资质证书" width="60%">
      <img v-if="teacher.flair" :src="$store.state.HOST + teacher.flair" style="width: 100%;">
    </el-dialog>
  </div>
</template>

<script>
  import {getUserById,getApeTaskByTeacher} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        teacherId: "",
        teacher: {},
        task: [],
        flairDialogVisible: false
      }
    },
    components: {
      headerPage,
      bottomPage,
    },
    methods: {
      toTask(id) {
        this.$router.push("/taskInfo?id=" + id)
      },
      previewFlair() {
        this.flairDialogVisible = true
      }
    },
    created() {
     
    },
    mounted() {
      var teacherId = this.$route.query.id
      if (teacherId) {
        this.teacherId = teacherId
      }
      getUserById({id:this.teacherId}).then(res => {
        if (res.code == 1000) {
          this.teacher = res.data
        }
      })
      getApeTaskByTeacher({id:this.teacherId}).then(res => {
        if (res.code == 1000) {
          this.task = res.data
        }
      })
    }
 }
</script>

<style scoped>
  .teacherInfo {
      width: 100%;
      min-height: 100vh;
      background: #f5f7fa;
  }
  .teacherInfo-content {
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-bottom: 30px;
  }
  .teacherInfo-name {
      width: 75%;
      margin-top: 20px;
  }
  .teacherInfo-header {
      display: flex;
      align-items: center;
      background: linear-gradient(135deg, #20B486 0%, #3dd9a8 100%);
      border-radius: 12px;
      padding: 30px;
      box-shadow: 0 4px 15px rgba(32, 180, 134, 0.3);
  }
  .teacherInfo-avatar {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      overflow: hidden;
      border: 4px solid #fff;
      box-shadow: 0 4px 10px rgba(0,0,0,0.1);
      flex-shrink: 0;
  }
  .teacherInfo-avatar img {
      width: 100%;
      height: 100%;
      object-fit: cover;
  }
  .teacherInfo-basic {
      margin-left: 25px;
      color: #fff;
  }
  .teacherInfo-title {
      font-size: 28px;
      font-weight: bold;
      margin-bottom: 8px;
  }
  .teacherInfo-subtitle {
      font-size: 16px;
      opacity: 0.9;
      margin-bottom: 12px;
  }
  .teacherInfo-tags {
      display: flex;
      gap: 10px;
  }
  .teacherInfo-body {
      display: flex;
      margin-top: 20px;
      gap: 20px;
  }
  .teacherInfo-left {
      width: 55%;
  }
  .teacherInfo-right {
      width: 45%;
  }
  .teacherInfo-card {
      background: #fff;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 15px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  }
  .card-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid #eee;
  }
  .card-title i {
      margin-right: 8px;
      color: #20B486;
  }
  .card-content {
      display: flex;
      flex-wrap: wrap;
  }
  .info-item {
      width: 50%;
      display: flex;
      margin-bottom: 12px;
  }
  .info-label {
      color: #999;
      font-size: 14px;
      width: 70px;
      flex-shrink: 0;
  }
  .info-value {
      color: #333;
      font-size: 14px;
  }
  .info-remark {
      color: #666;
      font-size: 14px;
      line-height: 1.8;
      width: 100%;
  }
  .flair-image {
      width: 100%;
      cursor: pointer;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.3s;
  }
  .flair-image:hover {
      box-shadow: 0 4px 15px rgba(0,0,0,0.15);
      transform: scale(1.02);
  }
  .flair-image img {
      width: 100%;
      max-height: 300px;
      object-fit: contain;
      border-radius: 8px;
      border: 1px solid #eee;
  }
  .course-card {
      height: calc(100% - 15px);
      min-height: 300px;
  }
  .course-list {
      max-height: 400px;
      overflow-y: auto;
  }
  .course-list::-webkit-scrollbar {
      width: 4px;
  }
  .course-list::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 2px;
  }
  .course-empty {
      padding: 20px 0;
  }
  .course-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 15px;
      background: #f8f9fa;
      border-radius: 8px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all 0.3s;
  }
  .course-item:hover {
      background: #e8f5f0;
      transform: translateX(5px);
  }
  .course-name {
      font-size: 14px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 80%;
  }
  .course-item i {
      color: #20B486;
  }
  @media screen and (max-width: 768px) {
      .teacherInfo-name {
          width: 95%;
      }
      .teacherInfo-body {
          flex-direction: column;
      }
      .teacherInfo-left,
      .teacherInfo-right {
          width: 100%;
      }
      .teacherInfo-header {
          flex-direction: column;
          text-align: center;
      }
      .teacherInfo-basic {
          margin-left: 0;
          margin-top: 15px;
      }
      .teacherInfo-tags {
          justify-content: center;
      }
      .info-item {
          width: 100%;
      }
  }
</style>
