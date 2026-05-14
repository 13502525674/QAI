<template>
  <div class="articleInfo">
      <headerPage></headerPage>
      <div class="articleInfo-content">
          <div class="articleInfo-title">
              <div>
                  <div class="articleInfo-title1">{{forum.name}}</div>
                  <div class="articleInfo-title2">{{forum.createBy}} - {{forum.createTime}}</div>
                  <div class="articleInfo-title2">{{forum.content}}</div>
              </div>
          </div>
      </div>
      <div class="articleInfo-bottom">
        <div class="articleInfo-comment">
            <div class="articleInfo-comment-font">发布讨论内容：</div>
            <div class="articleInfo-comment-content">
                <el-input type="textarea" resize="none" rows="7" v-model="content" placeholder="请输入讨论内容"></el-input>
                <div class="articleInfo-comment-btn">
                    <el-button @click="saveApeForumItem" style="margin-top:10px" size="mini" type="success" plain>提交</el-button>
                </div>
            </div>
        </div>
      </div>
      <div class="articleInfo-bottom">
        <div class="articleInfo-comment">
            <div class="articleInfo-comment-font">内容列表：</div>
            <div v-if="forumList.length <= 0" style="border: 2px solid #EFF1F2;border-radius: 7px;">
                <el-empty description="讨论还没有内容，快去发表吧！"></el-empty>
            </div>
            <div v-if="forumList.length > 0" class="taskInfo-comment-list">
                <div v-for="(item,index) in forumList" :key="index">
                    <div v-if="(index+1)%2 == 1" class="taskInfo-comment-item">
                        <div>
                            <img style="width:40px;height:40px;border-radius:50%" :src="$store.state.HOST + item.userAvatar">
                        </div>
                        <div style="width:80%">
                            <div class="taskInfo-comment-item1">{{item.userName}}</div>
                            <div class="taskInfo-comment-item2"><div style="padding-top:10px;padding-bottom:10px;margin-left:10px">{{item.content}}</div></div>
                        </div>
                    </div>
                    <div v-if="(index+1)%2 == 0" class="taskInfo-comment-item-right">
                        <div style="width:60%;display:flex;flex-direction: column;align-items: flex-end;margin-right:15px">
                            <div class="taskInfo-comment-item1">{{item.userName}}</div>
                            <div class="taskInfo-comment-item3"><div style="padding-top:10px;padding-bottom:10px;margin-left:10px">{{item.content}}</div></div>
                        </div>
                        <div>
                            <img style="width:40px;height:40px;border-radius:50%" :src="$store.state.HOST + item.userAvatar">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
      <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeForumById,getApeForumItemList,saveApeForumItem} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        id: "",
        user: {},
        forum: {},
        content: "",
        forumList: []
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
      saveApeForumItem() {
          if (!this.content) {
            this.$message({
              type: 'warning',
              message: '请输入讨论内容'
            });
            return
          }
          var param = {
              forumId:this.id,
              content: this.content
          }
          saveApeForumItem(param).then(res => {
              if (res.code == 1000) {
                  this.$message({
                    type: 'success',
                    message: '发布讨论内容成功!'
                  });
                  this.content = ""
                  this.getApeForumItemList()
              }
          })
      },
      getApeForumById() {
          getApeForumById({id:this.id}).then(res => {
              if (res.code == 1000) {
                  this.forum = res.data
              }
          })
      },
      getApeForumItemList() {
          var param = {
              forumId: this.id
          }
          getApeForumItemList().then(res => {
              if (res.code == 1000) {
                  this.forumList = res.data
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
      this.id = this.$route.query.id
      this.getApeForumById()
      this.getApeForumItemList()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/article/articleInfo.css"); 
  ::v-deep img {
    max-width: 100% !important;
  }
</style>