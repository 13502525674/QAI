<template>
  <div class="message">
    <headerPage></headerPage>
    <div class="message-page-content">
        <div class="message-page-add">
            <div style="width:90%;margin-top:20px">
                <div style="margin-bottom:10px">留言：</div>
                <el-input type="textarea" resize="none" rows="12" v-model="content" placeholder="请输入内容"></el-input>
                <div class="message-page-btns">
                    <el-button size="mini" icon="el-icon-message" type="primary" plain @click="add">提交</el-button>
                    <el-button size="mini" icon="el-icon-refresh" type="warning" plain @click="refresh">清除</el-button>
                </div>
            </div>
        </div>
        <div class="message-page-title">
            我的留言
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
  import {getApeMessagePage,saveApeMessage} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
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
            content: this.content
          }
          saveApeMessage(param).then(res => {
            if(res.code == 1000) {
              this.$message({
								message: '留言成功，请等待回复',
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
            flag: 0,
            pageSize: this.pageSize,
            pageNumber: this.pageNumber,
          }
          getApeMessagePage(param).then(res => {
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
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/message/message.css");
</style>