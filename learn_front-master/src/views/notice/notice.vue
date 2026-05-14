<template>
  <div class="notice">
    <headerPage></headerPage>
    <div class="notice-page-content">
        <div class="notice-page-font">
            <div class="notice-page-desc" v-html="notice.content">
            </div>
        </div>
        <div class="notice-page-title">
            公告列表
        </div>
        <div class="notice-page-list">
            <div v-for="(item,index) in tableData" :key="index" class="notice-page-item">
                <div @click="getNoticeInfo(index)" style="margin-left:20px">
                    {{index + 1}}. {{item.title}}
                </div>
                <div style="margin-right:20px">
                    <i class="el-icon-bell"></i>
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
  </div>
</template>

<script>
  import {getAccountPage} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        pageSize: 10,
        pageNumber: 1,
        tableData: [],
        total: 0,
        notice: {}
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        getNoticeInfo(index) {
          this.notice = this.tableData[index] 
        },
        query() {
          var param = {
            pageSize: this.pageSize,
            pageNumber: this.pageNumber,
          }
          getAccountPage(param).then(res => {
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
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/notice/notice.css");
</style>