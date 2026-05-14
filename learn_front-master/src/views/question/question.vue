<template>
  <div class="message">
    <headerPage></headerPage>
    <div class="message-page-content">
        <div class="message-page-add">
            <div style="width:90%;margin-top:20px">
                <div style="margin-bottom:10px">答疑：</div>
                <el-input type="textarea" resize="none" rows="12" v-model="content" placeholder="请输入内容"></el-input>
                <div class="message-page-btns">
                    <el-button size="mini" icon="el-icon-message" type="primary" plain @click="add">提交</el-button>
                    <el-button size="mini" icon="el-icon-refresh" type="warning" plain @click="refresh">清除</el-button>
                </div>
            </div>
        </div>
        <div class="message-page-title">
            我的答疑
        </div>
        <div class="message-page-list">
            <div v-for="(item,index) in tableData" :key="index" class="message-page-item">
                <div class="message-page-top">
                    <div style="margin-left:20px">
                        {{index + 1}}. {{item.content}}
                    </div>
                    <div style="margin-right:20px">
                        <i class="el-icon-success"></i>
                    </div>
                </div>
                <div class="messsage-page-huifu">
                    <span style="margin-left: 20px;margin-right: 10px;font-weight:bold">回复内容：</span>{{item.answer?item.answer:"未回复"}}
                </div>
            </div>
        </div>
        <div class="message-page-fenye">
            <el-pagination
            background
            :page-size="pageSize"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            :total="total">
            </el-pagination>
        </div>
        <div class="message-page-bottom">
            <img style="width:100%" src="../../assets/image/02.png">
        </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeQuestionPage,saveApeQuestion} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        taskId: "",
        user: {},
        pageSize: 10,
        pageNumber: 1,
        tableData: [],
        total: 0,
        content: ""
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        add() {
          var param = {
            taskId: this.taskId,
            content: this.content
          }
          saveApeQuestion(param).then(res => {
            if(res.code == 1000) {
              this.$message({
								message: '提问成功，请等待教师回复',
								type: 'success'
              });
              this.refresh()
              this.query()
            } else {
              this.$notify.error({
                title: '错误',
                message: res.message
              });
            }
          })
        },
        refresh() {
            this.content = ""
        },
        handleCurrentChange(val) {
          this.pageNumber = val
          this.query()
        },
        query() {
          var param = {
            taskId: this.taskId,
            userId: this.user.id,
            pageSize: this.pageSize,
            pageNumber: this.pageNumber,
          }
          getApeQuestionPage(param).then(res => {
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
        }
    },
    created() {
     
    },
    mounted() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      this.taskId = this.$route.query.id
      this.user = JSON.parse(window.localStorage.getItem("user_info"))
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/message/message.css");
</style>