<template>
  <div class="article">
    <headerPage></headerPage>
    <div class="article-page-content">
        <div class="article-page-title">
            收藏笔记
        </div>
        <div class="article-page-list">
           <div @click="toArticleInfo(item.articleId)" v-for="(item,index) in tableData" :key="index" class="article-page-item">
                <div class="article-page-image"><img src="../../assets/image/index/Vector (1).png"></div>
                <div class="article-page-user">
                    <img class="article-page-avatar" :src="$store.state.HOST + item.avatar">
                    <div class="article-page-username">
                        <div>{{item.createBy}} - {{item.createTime}}</div>
                        <div>{{item.title}}</div>
                    </div>
                </div>
                <div class="article-page-font">
                  {{item.articleDesc}}
                </div>
            </div> 
        </div>
        <div class="article-page-fenye">
            <el-pagination
            background
            :page-size="pageSize"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            :total="total">
            </el-pagination>
        </div>
        <div class="article-page-bottom">
            <img style="width:100%" src="../../assets/image/38.png">
        </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>
 
<script>
  import {getApeArticleFavorPage} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        user: {},
        title: "",
        pageSize: 10,
        pageNumber: 1,
        tableData: [],
        total: 0
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        query() {
          var param = {
            userId: this.user.id,
            pageSize: this.pageSize,
            pageNumber: this.pageNumber
          }
          getApeArticleFavorPage(param).then(res => {
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
        },
        toArticleInfo(id) {
          this.$router.push("/articleInfo?id="+id)
        }
    },
    created() {
     
    },
    mounted() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      this.user = JSON.parse(window.localStorage.getItem("user_info"))
      this.query()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/article/article.css");
</style>