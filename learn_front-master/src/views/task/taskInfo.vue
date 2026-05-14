<template>
  <div class="taskInfo">
    <headerPage></headerPage>
    <div class="taskInfo-content">
        <div class="taskInfo-name">
            <div class="taskInfo-desc">
                <div class="taskInfo-title">{{task.name}}
                    <el-button size="mini" v-if="flag" type="warning" @click="removeFavor" icon="el-icon-star-off" circle></el-button>
                    <el-button size="mini" v-if="!flag" type="success" @click="addFavor" icon="el-icon-star-on" circle></el-button>
                </div>
                <div class="taskInfo-title1">
                    <el-tooltip class="item" effect="dark" :content="task.taskDescribe" placement="bottom">
                        <i class="el-icon-info"></i>
                    </el-tooltip>
                    {{task.taskDescribe}}
                </div>
            </div> 
            <div class="taskInfo-list">
                <div class="taskInfo-image">
                    <div class="taskInfo-fengmian">
                        <video v-if="select" ref="videoPlayer" id="videoPlayer" class="video-js vjs-default-skin vjs-big-play-centered" style="width:100%;height:100%">
                            <source :src="selectSrc" type="video/mp4">
                        </video>
                        <img v-if="!select" style="width:100%;height:100%" :src="$store.state.HOST + task.image">
                    </div>
                    <div class="taskInfo-btns">
                        <div @click="addTaskStudent" title="报名课程" class="taskInfo-icon">
                            <i class="el-icon-document-checked"></i>
                        </div>
                        <div @click="toQuestion(task.id)" title="课程答疑" class="taskInfo-icon">
                            <i class="el-icon-question"></i>
                        </div>
                        <div @click="toTeacher(task.teacherId)" title="课程讲师" class="taskInfo-icon">
                            <i class="el-icon-link"></i>
                        </div>
                        <div @click="toArticle" title="课程笔记" class="taskInfo-icon">
                            <i class="el-icon-notebook-1"></i>
                        </div>

                        <div @click="downloadfile" title="下载课件" class="taskInfo-icon">
                            <i class="el-icon-download"></i>
                        </div>
                        <div @click="toLearning" title="学习进度" class="taskInfo-icon">
                            <i class="el-icon-data-line"></i>
                        </div>
                    </div>
                </div>
                <div class="taskInfo-video">
                    <div @click="changeChapter(index)" class="taskInfo-video-item" v-for="(item,index) in chapter" :key="index">
                        <div :title="item.name" class="taskInfo-video-item1">{{item.name}}</div>
                        <div class="taskInfo-video-item2"><i class="el-icon-arrow-right"></i></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="taskInfo-bottom">
        <div class="taskInfo-comment">
            <div class="taskInfo-comment-font">评论：</div>
            <div class="taskInfo-comment-content">
                <el-input type="textarea" resize="none" rows="7" v-model="content" placeholder="请输入评论内容"></el-input>
                <div class="taskInfo-comment-btn">
                    <el-button @click="saveApeTaskComment" style="margin-top:10px" size="mini" type="success" plain>提交评论</el-button>
                </div>
            </div>
        </div>
    </div>
    <div class="taskInfo-bottom">
        <div class="taskInfo-comment">
            <div class="taskInfo-comment-font">评论列表：</div>
            <div v-if="commentList.length <= 0" style="border: 2px solid #EFF1F2;border-radius: 7px;">
                <el-empty description="课程还没有评论，快去发表评论吧！"></el-empty>
            </div>
            <div v-if="commentList.length > 0" class="taskInfo-comment-list">
                <div v-for="(item,index) in commentList" :key="index">
                    <div v-if="(index+1)%2 == 1" class="taskInfo-comment-item">
                        <div>
                            <img style="width:40px;height:40px;border-radius:50%" :src="$store.state.HOST + item.avatar">
                        </div>
                        <div style="width:80%">
                            <div class="taskInfo-comment-item1">{{item.createBy}}</div>
                            <div class="taskInfo-comment-item2"><div style="padding-top:10px;padding-bottom:10px;margin-left:10px">{{item.content}}</div></div>
                        </div>
                    </div>
                    <div v-if="(index+1)%2 == 0" class="taskInfo-comment-item-right">
                        <div style="width:60%;display:flex;flex-direction: column;align-items: flex-end;margin-right:15px">
                            <div class="taskInfo-comment-item1">{{item.createBy}}</div>
                            <div class="taskInfo-comment-item3"><div style="padding-top:10px;padding-bottom:10px;margin-left:10px">{{item.content}}</div></div>
                        </div>
                        <div>
                            <img style="width:40px;height:40px;border-radius:50%" :src="$store.state.HOST + item.avatar">
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
  import {saveApeTaskFavor,removeApeTaskFavor,getApeTaskFavorById,saveApeChapterVideo,getApeTaskById,saveApeTaskStudent,getTaskStudent,getApeChapterByTaskId,saveApeTaskComment,getApeTaskCommentListByTaskId} from "../../api/api"
  import videojs from 'video.js'
  import 'video.js/dist/video-js.css'
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        user: {},
        taskId: "",
        state: 2,
        task: {},
        chapter: [],
        selectChapter: "",
        selectSrc: "",
        select: false,
        commentList: [],
        content: "",
        player: null,
        favor: {},
        flag: false
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        removeFavor() {
            removeApeTaskFavor({ids:this.favor.id}).then(res => {
                if (res.code == 1000) {
                    this.$message({
                        type: 'success',
                        message: '取消收藏成功'
                    });
                    this.getApeTaskFavorById()
                }
            })
        },
        addFavor() {
            var param = {
                taskId: this.taskId,
                userId: this.user.id
            }
            saveApeTaskFavor(param).then(res => {
                if (res.code == 1000) {
                    this.$message({
                        type: 'success',
                        message: '收藏成功'
                    });
                    this.getApeTaskFavorById()
                }
            })
        },
        getApeTaskFavorById() {
            getApeTaskFavorById({taskId: this.taskId,userId:this.user.id}).then(res => {
                if (res.code == 1000) {
                    this.favor = res.data
                    this.flag = true
                } else {
                    this.favor = {}
                    this.flag = false
                }
            })
        },
        getApeTaskCommentListByTaskId() {
            getApeTaskCommentListByTaskId({id:this.taskId}).then(res => {
                if (res.code == 1000) {
                    this.commentList = res.data
                }
            })
        },
        toQuestion(id) {
            if (this.state != 0) {
                this.$message({
                    type: 'warning',
                    message: '报名课程通过后才能进行答疑'
                });
                return
            }
            this.$router.push("/question?id=" + id)
        },
        saveApeTaskComment() {
            if (this.state != 0) {
                this.$message({
                    type: 'warning',
                    message: '报名课程通过后才能发表评论'
                });
                return
            }
            var param = {
                content: this.content,
                taskId: this.taskId
            }
            saveApeTaskComment(param).then(res => {
                if (res.code == 1000) {
                    this.$message({
                        type: 'success',
                        message: '评论成功'
                    });
                    this.content = ""
                    this.getApeTaskCommentListByTaskId()
                }
            })
        },
        toTeacher(id) {
            this.$router.push("/teacherInfo?id=" + id)
        },
        toArticle() {
            this.$router.push("/article?name="+this.task.name)
        },
        toLearning() {
            if (this.state != 0) {
                this.$message({
                    type: 'warning',
                    message: '请先报名课程'
                });
                return
            }
            this.$router.push("/rateOfLearning?id="+this.task.id)
        },
        downloadfile() {
            if (this.state != 0) {
                this.$message({
                    type: 'warning',
                    message: '报名课程通过后才能下载课件'
                });
                return
            }
            if (this.selectChapter == "") {
                this.$message({
                    type: 'warning',
                    message: '请先选择章节再下载课件'
                });
                return
            } 
            if (this.selectChapter.courseware) {
                window.location.href = this.$store.state.HOST + this.selectChapter.courseware
            } else {
                this.$message({
                    type: 'warning',
                    message: '该章节暂无课件'
                });
                return
            }
        },
        changeChapter(index) {
            if (this.state != 0) {
                this.$message({
                    type: 'warning',
                    message: '报名课程通过后才能观看视频'
                });
                return
            }
            this.selectChapter = this.chapter[index]
            this.selectSrc = this.$store.state.HOST + this.selectChapter.video
            
            if (this.player) {
                this.player.src({ src: this.selectSrc, type: 'video/mp4' })
                this.player.load()
                this.player.play()
            } else {
                this.select = true
                this.$nextTick(() => {
                    this.initPlayer()
                })
            }
        },
        initPlayer() {
            if (this.player) {
                return
            }
            const videoElement = document.getElementById('videoPlayer')
            if (!videoElement) {
                return
            }
            this.player = videojs(videoElement, {
                controls: true,
                autoplay: true,
                preload: 'auto',
                fluid: false,
                playbackRates: [0.5, 1, 1.5, 2],
                controlBar: {
                    children: [
                        'playToggle',
                        'volumePanel',
                        'currentTimeDisplay',
                        'timeDivider',
                        'durationDisplay',
                        'progressControl',
                        'playbackRateMenuButton',
                        'fullscreenToggle'
                    ]
                }
            }, function onPlayerReady() {
                this.play()
            })
            this.player.on('ended', () => {
                var param = {
                    chapterId: this.selectChapter.id,
                }
                saveApeChapterVideo(param)
            })
        },
        addTaskStudent() {
            if (this.state == 0) {
                this.$message({
                    type: 'success',
                    message: '报名已通过无需重复操作'
                });
            } else if(this.state == 1) {
                this.$message({
                    type: 'success',
                    message: '已存在报名记录，请耐心等待教师通过！'
                });
            } else {
                var param = {
                    taskId: this.taskId
                }
                saveApeTaskStudent(param).then(res => {
                    if (res.code == 1000) {
                        this.$message({
                            type: 'success',
                            message: '报名成功，请等待教师通过!'
                        });
                        getTaskStudent({id:this.taskId}).then(res => {
                            if (res.code == 1000) {
                                this.state = res.data
                            }
                        })
                    }
                }) 
            }
        },

    },
    created() {
     
    },
    mounted() {
      window.scrollTo({
         top: 0,
         behavior: 'smooth'
      });
      this.user = JSON.parse(window.localStorage.getItem("user_info"))
      var that = this
      this.taskId = this.$route.query.id
      getApeTaskById({id:this.taskId}).then(res => {
          if (res.code == 1000) {
            this.task = res.data
          }
      })
      getTaskStudent({id:this.taskId}).then(res => {
          if (res.code == 1000) {
            this.state = res.data
          }
      })
      getApeChapterByTaskId({id: this.taskId}).then(res => {
          if (res.code == 1000) {
            this.chapter = res.data
          }
      })
      this.getApeTaskCommentListByTaskId()
      this.getApeTaskFavorById()
    },
    beforeDestroy() {
      if (this.player) {
        this.player.dispose()
      }
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/task/taskInfo.css");
</style>