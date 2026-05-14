<template>
  <div class="rateOfLearning">
    <headerPage></headerPage>
    <div class="learning-content">
        <div class="learning-top">
            <div class="learning-taskName">{{task.name}}</div>
            <div class="learning-desc">
                <div>视频：{{wancheng}}/{{video}}</div>
                <div>练习：{{wancheng1}}/{{home}}</div>
                <div>得分：{{getScore()}}</div>
                <div>成绩：{{getAgree()}}</div>
            </div>
            <div class="learning-list">
                 <div v-for="(item,index) in chapter" :key="index" class="learning-item">
                     <div style="margin-left:20px">{{item.name}}</div>
                     <div>视频：{{item.videoFlag}}</div>
                     <div style="margin-right:20px">练习：{{item.home}}</div>
                 </div>
            </div>
        </div>
    </div> 
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeTaskById,getTaskChapterStudy} from "../../api/api"
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        taskId: "",
        task: {},
        chapter: [],
        video: 0,
        wancheng: 0,
        home: 0,
        wancheng1: 0
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        getAgree() {
            var total = this.video + this.home
            var wan = this.wancheng1 + this.wancheng
            var score = (wan/total * this.task.proportion).toFixed(2)
            var title = "不及格"
            if(score < 60) {
                title = "不及格"
            }
            if(score >= 60) {
                title = "及格"
            }
            if(score >= 75) {
                title = "良好"
            }
            if(score >= 90) {
                title = "优"
            }
            return title
        },
        getScore() {
            var total = this.video + this.home
            var wan = this.wancheng1 + this.wancheng
            return (wan/total * this.task.proportion).toFixed(2)
        },
        getApeTaskById() {
            getApeTaskById({id: this.taskId}).then(res => {
                if(res.code == 1000) {
                    this.task = res.data
                }
            })
        },
        getTaskChapterStudy() {
            getTaskChapterStudy({id:this.taskId}).then(res => {
                if(res.code == 1000) {
                    this.chapter = res.data
                    this.video = this.chapter.length
                    for(let i = 0;i<this.chapter.length;i++) {
                        var item = this.chapter[i]
                        if (item.homework == 1) {
                            this.home = this.home + 1
                        }
                        if (item.home == '已完成') {
                            this.wancheng1 = this.wancheng1 + 1
                        }
                        if (item.videoFlag == '已完成') {
                            this.wancheng = this.wancheng + 1
                        }
                    }
                }
            })
        }
    },
    created() {
     
    },
    mounted() {
      var taskId = this.$route.query.id
      if (taskId) {
          this.taskId = taskId
      }
      this.getApeTaskById()
      this.getTaskChapterStudy()
    }
 }
</script>

<style scoped>
  .rateOfLearning {
      width: 100%;
      height: 100%;
  }
  .learning-content {
      width: 100%;
      min-height: 550px;
      background: url('../../assets/image/index/index_back.png');
      display: flex;
      justify-content: center;
      font-family: '黑体';
  }
  .learning-top {
      width: 70%;
  }
  .learning-taskName {
      margin-top: 30px;
      font-size: 30px;
  }
  .learning-desc {
      font-size: 20px;
      display: flex;
      justify-content: space-around;
      align-items: center;
      margin-top: 20px;
      width: 100%;
      height: 100px;
      border: 2px solid #EFF1F2;
      border-radius: 10px;
  }
  .learning-list {
      margin-top: 20px;
      width: 100%;
      min-height: 400px;
      border: 2px solid #EFF1F2;
      border-radius: 10px;
      display: flex;
      flex-direction: column;
      align-items: center
  }
  .learning-item {
      width: 98%;
      height: 40px;
      background-color: #EAECFE;
      margin-top: 10px;
      border-radius: 7px;
      display: flex;
      justify-content: space-between;
      align-items: center;
  }
</style>