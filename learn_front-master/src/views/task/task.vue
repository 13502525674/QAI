<template>
  <div class="task">
    <headerPage></headerPage>
    <div class="task-page-content">
      <div class="task-page-search">
        <div class="task-search-item">
          <div>课程名称：</div>
          <div>
            <el-input size="mini" v-model="search.name" placeholder="请输入课程名称"></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>教师名称：</div>
          <div>
            <el-input size="mini" v-model="search.teacherName" placeholder="请输入教师名称"></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>物理分支：</div>
          <div>
            <el-input size="mini" v-model="search.major" placeholder="请输入物理分支"></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <div>简介内容：</div>
          <div>
            <el-input size="mini" v-model="search.taskDescribe" placeholder="请输入课程简介内容"></el-input>
          </div>
        </div>
        <div class="task-search-item">
          <el-button icon="el-icon-search" size="mini" type="primary" plain @click="searchPage()">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" type="warning" plain @click="refresh()">重置</el-button>
        </div>
        <div class="task-search-item" style="width:100%;flex-wrap: wrap;">
          <span style="margin-right:10px">分类：</span>
          <span class="task-search-type" @click="searchClass(item.name)" v-for="(item,index) in classification" :key="index">{{item.name}}</span>
        </div>
      </div>
      <div class="task-font">课程列表</div>
      <div class="task-page-list">
        <div @click="toTaskInfo(item.id)" v-for="(item,index) in tableData" :key="index" class="task-page-item">
          <div class="task-page-image">
            <img style="width:100%;height:100%" :src="$store.state.HOST + item.image">
          </div>
          <div class="task-page-name">
            <div class="task-page-name-one">{{item.name}}</div>
            <div class="task-page-name-two">{{item.taskDescribe}}</div>
          </div>
          <div class="task-page-user">
            <div>讲师：{{item.teacherName}}</div>
            <div>
              学生：
              <i class="el-icon-star-off"></i>
              {{item.num}}
            </div>
          </div>
        </div>
      </div>
      <div class="task-page-fenye">
        <el-pagination
          background
          :page-size="search.pageSize"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
    <div class="task-page-bottom">
      <img style="width:70%" src="../../assets/image/15.png">
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeTaskPage,getApeClassificationList} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        search: {
          name: "",
          state: 0,
          teacherName: "",
          major: "",
          taskDescribe: "",
          classification: "",
          pageSize: 12,
          pageNumber: 1,
          type: 0,
        },
        tableData: [],
        classification: [],
        total: 0
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
      searchPage() {
        this.search.pageNumber = 1
        this.query()
      },
      searchClass(name) {
        this.search.pageNumber = 1
        this.search.classification = name
        this.query()
      },
      query() {
        getApeTaskPage(this.search).then(res => {
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
          name: "",
          teacherName: "",
          major: "",
          state: 0,
          taskDescribe: "",
          classification: "",
          pageSize: 12,
          pageNumber: 1,
          type: 0,
        },
        this.query()
      },
      handleCurrentChange(val) {
        this.search.pageNumber = val
        this.query()
      }, 
      toTaskInfo(id) {
        this.$router.push("taskInfo?id=" + id)
      },
      getApeClassificationList() {
        getApeClassificationList().then(res => {
            if(res.code == 1000) {
                this.classification = res.data
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
     
    },
    mounted() {
      this.getApeClassificationList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      var name = this.$route.query.name
      if (name) {
        this.search.name = name
      }
      var major = this.$route.query.major
      if (major) {
        this.search.major = major
      }
      var branch = this.$route.query.branch
      if (branch) {
        this.search.major = branch
      }
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/task/task.css");
</style>