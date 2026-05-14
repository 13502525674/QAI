<template>
  <div class="test">
    <headerPage></headerPage>
    <div class="test-page-content">
        <div class="test-page-desc">
            <div class="test-page-image">
                <img style="widthL:100%" src="../../assets/image/testP/Accent.png">
            </div>
            <div class="test-page-num">
                 <div class="test-page-num1">我的考试</div>
                 <div class="test-page-num2"><span style="color:#67AEF7;font-size:35px">{{test.length}}</span> 场</div>
            </div>
            <div class="test-page-font">
                <div class="test-page-font1">
                    <div style="font-weight:bold;margin-bottom:5px">考前须知：</div>
                    <div>1、请提前进行基础设备检测，包括鼠标、键盘、显示器或其他硬件设备；</div>
                    <div>2、请拔掉U盘、移动硬盘或其他与考试无关的外接设备。</div>
                    <div>3、如果您使用的是移动电脑，请确保电源连接正常。</div>
                    <div>4、请确保系统内未开启与考试无关的非必要软件；</div>
                    <div>5、请确保当前使用的是谷歌浏览器，且页面（浏览器）是最大化状态；</div>
                    <div>6、考试过程中，如非特殊要求，请勿执行任何会导致考试页面失焦的操作，包括关闭、切换或移动标签页、关闭或最小化浏览器；</div>
                </div>
                <div class="test-page-font2">
                    <div>7、考试过程中，如非特殊要求，请勿修改浏览器和页面的大小，包括还原（非最大化）浏览器，手动拖拽浏览器尺寸等；</div>
                    <div>8、有些考试可能会禁用复制粘贴功能。</div>
                    <div>9、如果有考试开启了防作弊视频监控，请提前完成摄像头检测工作，且考试过程中一直保持面部处于监控画面中心位置，如果存在面部偏离、转头，面部消失或出现非考生面部信息等异常情况，系统会自动记录这些信息，并通知给监考教师。</div>
                </div>
            </div>
        </div>
        <div class="test-page-title">
            考试中心
        </div>
        <div class="test-page-list">
            <div v-for="(item,index) in test" :key="index" class="test-page-item">
                <span>考试名称：{{item.name}}</span>
                <span>所属课程：{{item.taskName}}</span>
                <span>发布讲师：{{item.createBy}}</span>
                <span>考试时间：{{item.totalTime}}分钟</span>
                <span>总分：{{item.totalScore}}分</span>
                <span>参与时间：{{item.startTime}}</span>
                <span>截止时间：{{item.ednTime}}</span>
                <span>考试进度：{{item.schedule}}</span>
                <span>成绩：{{item.scoreTotal}}分</span>
                <span>
                  <el-button v-if="item.schedule == '未开始'" @click="toTestInfo(item.id)" icon="el-icon-edit" size="mini" type="success">开始考试</el-button>
                  <el-button v-if="item.schedule == '已完成'" @click="toTestInfo(item.id)" icon="el-icon-edit" size="mini" type="success">查看详情</el-button>
                </span>
            </div>
        </div>
        <div class="test-bottom-image">
            <img style="width:100%" src="../../assets/image/20.png">
        </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script> 
  import {getTestListByUser} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        test: [],
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        getTestListByUser() {
          getTestListByUser().then(res => {
            if(res.code == 1000) {
              this.test = res.data
            }
          })
        },
        toTestInfo(id) {
          this.$router.push("/testInfo?id="+id)
        }
    },
    created() {
     
    },
    mounted() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
      this.getTestListByUser()
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/testP/test.css");
</style>