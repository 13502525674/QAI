<template>
  <div class="testInfo">
    <headerPage></headerPage>
    <div class="testInfo-content">
        <div class="test-page-desc">
            <div class="test-page-image">
                <img style="widthL:100%" src="../../assets/image/testP/Accent.png">
            </div>
            <div class="test-page-num" v-if="!flag">
                 <div class="test-page-num1">剩余时间</div>
                 <div class="test-page-num2">{{hr+'小时'+min+'分'+sec+'秒'}}</div>
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
        <div class="testInfo-top">
            <div class="testInfo-title">
                 {{test.taskName}} - {{test.name}}
            </div>
            <div class="testInfo-state">
                 总分：{{total}}分  得分：{{defen}}分
            </div> 
            <div class="testInfo-list">
                <div class="testInfo-item" v-for="(item,index) in assign" :key="index">
                    <div class="question-title">
                        <span class="question-index">{{index + 1}}</span>
                        <span class="question-text">{{item.title}}</span>
                        <span class="answer-tag wrong" v-if="flag && item.score != item.point">正确答案：{{item.answer}}</span>
                        <span class="answer-tag correct" v-if="flag && item.score == item.point">正确答案：{{item.answer}}</span>
                        <span class="score-tag" v-if="flag">得分：{{item.point}}</span>
                    </div>
                    <div class="question-options">
                        <el-radio-group v-model="item.solution" v-if="item.type == 0">
                            <el-radio v-for="(e,s) in item.content" :key="s" :label="e.value">{{e.value}}.{{e.option}}</el-radio>
                        </el-radio-group>
                        <el-checkbox-group v-model="item.solution" v-if="item.type == 1">
                            <el-checkbox v-for="(e,s) in item.content" :key="s" :label="e.value">{{e.value}}.{{e.option}}</el-checkbox>
                        </el-checkbox-group>
                        <el-input v-model="item.solution" v-if="item.type == 2" size="mini" placeholder="请输入答案"></el-input>
                        <el-radio-group v-model="item.solution" v-if="item.type == 3">
                            <el-radio label="正确">正确</el-radio>
                            <el-radio label="错误">错误</el-radio>
                        </el-radio-group>
                        <el-input type="textarea" v-model="item.solution" v-if="item.type == 4" size="mini" placeholder="请输入答案"></el-input>
                        <el-input type="textarea" v-model="item.solution" v-if="item.type == 5" size="mini" placeholder="请输入答案"></el-input>
                    </div>
                    <!-- AI 解析展示区域 -->
                    <div class="ai-analysis" v-if="flag && item.aiAnalysis">
                        <div class="ai-analysis-header">
                            <i class="el-icon-cpu"></i> AI 智能错题解析 <span class="ai-grader">(批改人: {{item.gradedBy || 'AI'}})</span>
                        </div>
                        <div class="ai-analysis-content">{{item.aiAnalysis}}</div>
                    </div>
                </div>
                <div class="submit-area">
                    <el-button v-if="!flag" @click="submit" class="submit-btn" type="primary">提交试卷</el-button>
                </div>
            </div>
        </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {getApeTestItemByTestId,saveApeTestStudent,getTestUserState} from '../../api/api' 
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    data() {
      return{
        id: "",
        assign: [],
        test: {},
        total: 0,
        hr: "",
        min: "",
        sec: "",
        end: "",
        timeOut: null,
        five: false,
        ten: false,
        flag: false,
        defen: 0
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        submit() {
            this.$confirm('确定要提交考试内容吗？','交卷提醒', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
                }).then(() => {
                    this.saveApeTestStudent()
                }).catch(() => {
                     
            });
            
        },
        submitEnd() {
            this.$message({
                message: '考试已结束，系统自动提交...',
                type: 'success'
            });
            this.saveApeTestStudent()
        },
        saveApeTestStudent() {
            for(let i = 0;i<this.assign.length;i++) {
                var item = this.assign[i]
                if(item.type == 1) {
                    item.solution = item.solution.join(",")
                }
            }
            var param = {
                list: this.assign
            }
            saveApeTestStudent(param).then(res => {
                if (res.code == 1000) {
                    this.$message({
                        type: 'success',
                        message: '交卷成功!'
                    });
                    this.$router.push("/test")
                } else {
                    this.$message({
                        type: 'warning',
                        message: res.message
                    });
                }
            }) 
        },
        countdown () {
            const now = Date.parse(new Date())
            const msec = this.end - now

            if(msec <= (10 * 60 * 1000)) {
                if(!this.ten) {
                    this.$message('考试时间还剩十分钟');
                    this.ten = true
                }
            }

            if(msec <= (5 * 60 * 1000)) {
                if(!this.five) {
                    this.$message('考试时间还剩五分钟');
                    this.five = true
                }
            }

            if(msec<=0) {
                //考试结束
                this.submitEnd()
            };
        
            let day = parseInt(msec / 1000 / 60 / 60 / 24)
            let hr = parseInt(msec / 1000 / 60 / 60 % 24)
            let min = parseInt(msec / 1000 / 60 % 60)
            let sec = parseInt(msec / 1000 % 60)
            this.hr = hr > 9 ? hr : '0' + hr
            this.min = min > 9 ? min : '0' + min
            this.sec = sec > 9 ? sec : '0' + sec
            const that = this
            if(min>=0 && sec>=0){
                this.timeOut = setTimeout(function () {
                    that.countdown()
                }, 1000)
            }
        },
        getApeTestItemByTestId(){
            getApeTestItemByTestId({id:this.id}).then(res => {
                if (res.code == 1000) {
                    this.assign = res.data.testItem
                    this.test = res.data.test
                    for(let i = 0;i<this.assign.length;i++) {
                        var item = this.assign[i]
                        this.total = this.total + item.score
                        this.defen = this.defen + item.point
                        if (item.type == "0" || item.type == "1") {
                          if (item.content) {
                              item.content = JSON.parse(item.content)
                          }
                          if(item.type == 1) {
                              if(item.solution) {
                                item.solution = item.solution.split(",")
                              } else {
                                item.solution = []
                              }
                          }
                      }
                    }
                }
            })
        },
        getTestUserState() {
            getTestUserState({id:this.id}).then(res => {
                if (res.code == 1000) {
                    this.flag = true
                } else {
                    this.countdown()
                    this.flag = false
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
        this.end = Date.parse(new Date()) + 120 * 60 * 1000
        var id = this.$route.query.id
        if (id) {
            this.id = id
        }
        this.getTestUserState()
        this.getApeTestItemByTestId()
    },
    destroyed() {
        clearTimeout(this.timeOut)
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/testP/testInfo.css");
</style>