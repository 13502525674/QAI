<template>
  <div class="notice">
    <headerPage></headerPage>
    <div class="notice-page-content">
        <div class="notice-page-title">
            讨论列表
        </div>
        <div class="notice-page-add">
            <el-button type="primary" size="small" icon="el-icon-plus" @click="add">发起讨论</el-button>
        </div>
        <div class="notice-page-list">
            <div v-for="(item,index) in tableData" :key="index" class="forum-item" @click="toForumInfo(item.id)">
                <div class="forum-item-header">
                    <span class="forum-item-title">{{item.name}}</span>
                    <span class="forum-item-time">{{item.createTime}}</span>
                </div>
                <div class="forum-item-content">{{item.content}}</div>
                <div class="forum-item-footer">
                    <span class="forum-item-author">发起人：{{item.createBy}}</span>
                    <i class="el-icon-arrow-right"></i>
                </div>
            </div>
        </div>
        <div class="notice-page-fenye">
            <el-pagination
            background
            :page-size="pageSize"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            :total="total">
            </el-pagination>
        </div>
        <div class="notice-page-bottom">
            <img style="width:100%" src="../../assets/image/20.png">
        </div>
    </div>
    <bottomPage></bottomPage>
    <add @addFalse="addFalse" :addVisible="addVisible"></add>
  </div>
</template>

<script>
  import {getApeForumPage} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  import add from "./addForum"
  export default {
    data() {
      return{
        pageSize: 10,
        pageNumber: 1,
        tableData: [],
        total: 0,
        notice: {},
        addVisible: false
      }
    },
    components: {
      headerPage,
      bottomPage,
      add
    },
    methods: {
        add() {
            this.addVisible = true
        },
        addFalse() {
            this.addVisible = false
            this.query()
        },
        toForumInfo(id) {
            this.$router.push("forumInfo?id="+id)
        },
        query() {
          var param = {
            pageSize: this.pageSize,
            pageNumber: this.pageNumber,
          }
          getApeForumPage(param).then(res => {
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
        handleCurrentChange(val) {
          this.pageNumber = val
          this.query()
        }
    },
    created() {
     
    },
    mounted() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/notice/notice.css");
  .notice-page-add {
      width: 70%;
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
  }
  .forum-item {
      margin-top: 15px;
      padding: 15px 20px;
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
      cursor: pointer;
      transition: all 0.3s;
  }
  .forum-item:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px 0 rgba(0,0,0,.15);
  }
  .forum-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
  }
  .forum-item-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
  }
  .forum-item-time {
      font-size: 12px;
      color: #999;
  }
  .forum-item-content {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 10px;
  }
  .forum-item-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
  }
  .forum-item-author {
      font-size: 12px;
      color: #20B486;
  }
</style>