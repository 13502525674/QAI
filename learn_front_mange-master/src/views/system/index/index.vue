<template>
  <div>
    <el-row :gutter="20" class="index-top">
      <!-- 第一行 -->
      <el-col :span="6">
        <div class="grid-content bg-purple index-flow-card-shell">
          <el-card shadow="always" class="item-01"> 
            <!-- 用户数量 -->
            <div class="item-01-top">
              <img src="../../../assets/image/index-01.png" style="width:110px">
              <div class="item-01-top-center">
                <div style="color:#1f2d30;font-size:20px;font-weight:bold">用户数量</div>
                <div style="color:#1f2d30;font-size:30px;font-weight:bold">{{top.userNum}}人</div>
              </div>
            </div>
            <div class="item-01-under">
              <!-- <div style="color:#B1B7EA;font-size:14px;font-weight:bold">查看用户列表</div> -->
              <!-- <i class="el-icon-right" style="color:#B1B7EA;font-size:14px;font-weight:bold"></i> -->
            </div>
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple index-flow-card-shell">
          <el-card shadow="always" class="item-03"> 
            <div class="item-01-top">
              <img src="../../../assets/image/index-02.png" style="width:110px">
              <div class="item-01-top-center">
                <div style="color:#000000;font-size:20px;font-weight:bold">学生数量</div>
                <div style="color:#000000;font-size:30px;font-weight:bold">{{top.studentNum}}人</div>
              </div>
            </div>
            <div class="item-01-under" @click="toStudent()">
              <div style="color:black;font-size:14px;font-weight:bold">查看全部</div>
              <i class="el-icon-right" style="color:black;font-size:14px;font-weight:bold"></i>
            </div>
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple index-flow-card-shell">
          <el-card shadow="always" class="item-05"> 
            <div class="item-01-top">
              <img src="../../../assets/image/avator.png" style="width:110px">
              <div class="item-01-top-center">
                <div style="color:#1f2d30;font-size:20px;font-weight:bold">教师数量</div>
                <div style="color:#1f2d30;font-size:30px;font-weight:bold">{{top.teacherNum}}人</div>
              </div>
            </div>
            <div class="item-01-under" @click="toTeacher()">
              <div style="color:black;font-size:14px;font-weight:bold">查看全部</div>
              <i class="el-icon-right" style="color:black;font-size:14px;font-weight:bold"></i>
            </div>
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple index-flow-card-shell">
          <el-card shadow="always" class="item-06"> 
            <div class="item-01-top">
              <img src="../../../assets/image/index-05.png" style="width:110px">
              <div class="item-01-top-center">
                <div style="color:#000000;font-size:12px;font-weight:bold;display:flex;align-item:center">
                  <img src="../../../assets/image/index-06.png" style="width:20px">：1008611
                </div>
                <div style="color:#000000;font-size:12px;font-weight:bold;display:flex;align-item:center">
                  <img src="../../../assets/image/index-07.png" style="width:20px">：1008611
                </div>
              </div>
            </div>
            <div class="item-01-under">
              <div style="color:black;font-size:14px;font-weight:bold">联系我们</div>
              <i class="el-icon-phone-outline" style="color:black;font-size:14px;font-weight:bold"></i>
            </div>
          </el-card>
        </div>
      </el-col> 
    </el-row>
    <el-row :gutter="20" class="index-center">
      <el-col :span="16">
        <div class="index-flow-card-shell">
          <el-card shadow="always" class="item-07">
            <div id="chart">

            </div>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="index-flow-card-shell">
          <el-card shadow="always" class="item-08">
            <div id="pie-chart">

            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="index-center-01">
      <el-col :span="24">
        <div class="index-flow-card-shell">
        <el-card shadow="always" class="item-09 theme-light-embed">
          <el-table
            :data="taskList"
            :header-cell-style="{
              'color': '#4a5568',
              'background-color': '#FDEBD3',
            }"
            :row-style="{
              'color': '#6b6b6b',
              'font-size': '15px',
              'font-family':'黑体'
            }"
            stripe
            style="width: 100%">
            <el-table-column
              prop="name"
              label="课程名称"
              >
            </el-table-column>
            <el-table-column
              prop="teacherName"
              label="教师"
              >
              <template slot-scope="scope">
                <div style="display:flex;align-items: center">
                  <img style="width:35px;height:35px" src="../../../assets/image/avator.png">
                  <div style="margin-left:10px">{{scope.row.teacherName}}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="major"
              label="物理分支">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" style="background:#99CDDB;border-color:#99CDDB;color:#3d3d3d">{{scope.row.major}}</el-button>
              </template>
            </el-table-column>
            <el-table-column
              prop="classification"
              label="课程分类">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" style="background:#99CDDB;border-color:#99CDDB;color:#3d3d3d">{{scope.row.classification}}</el-button>
              </template>
            </el-table-column>
            <el-table-column
              prop="num"
              label="学生数量">
            </el-table-column>
            <el-table-column
              prop="createTime"
              label="创建时间">
            </el-table-column>
          </el-table>
        </el-card>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="index-under">
      <el-col :span="16">
        <div class="index-flow-card-shell">
        <el-card shadow="always" class="item-10">
          <div style="width:100%;height:50px;display:flex;justify-content: space-between">
            <span>课程 列表</span>
            <el-button style="height:35px;background:#657166;border-color:#657166" size="mini" type="primary" @click="toTask()">查看全部</el-button>
          </div>
          <div style="width:100%;height:260px">
            <el-row>
              <el-col class="item-10-item" :span="7" v-for="(item,index) in taskCard" :key="index">
                <img :src="$store.state.configure.HOST + item.image" style="border-radius: 20px;width:90%">
                <div>{{item.name}}</div>
                <div style="width:90%;display:flex;justify-content: space-around;">
                  <div style="height:35px;font-size:13px;display:flex;align-items:center"><span>{{item.createTime}}</span>
                  </div>
                  <el-button size="mini" style="height:30px" type="danger">{{item.teacherName}}</el-button>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="index-flow-card-shell">
        <el-card shadow="always" class="item-11">
          <el-calendar v-model="today">
          </el-calendar>
        </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {getIndexData,getIndexSexData,getTaskChart,getTaskIndexList} from '../../../api/api' 
  import * as echarts from "echarts";
  export default {
    data() {
      return{
        top: {},
        sex: [],
        tasks: [],
        nums: [],
        taskList: [],
        taskCard: [],
        today: new Date(),
        myChart: "",
        pieChart: "",
      }
    },
    methods: {
      toTask() {
        var param = {
            "name": "课程管理",
            "url": "/task" 
        }
        this.$store.commit('menu/addActiveMenu', param)
        this.$router.push("/task")
        this.$store.commit('menu/setActiveMenu', "/task")
      },
      toStudent() {
        var param = {
            "name": "学生管理",
            "url": "/student" 
        }
        this.$store.commit('menu/addActiveMenu', param)
        this.$router.push("/student")
        this.$store.commit('menu/setActiveMenu', "/student")
      },
      toTeacher() {
        var param = {
            "name": "教师管理",
            "url": "/teacher"
        }
        this.$store.commit('menu/addActiveMenu', param)
        this.$router.push("/teacher")
        this.$store.commit('menu/setActiveMenu', "/teacher")
      }
    },
    created() {
      
    },
    mounted() {
      getIndexData({type:0}).then(res => {
        if (res.code == 1000) {
          this.top = res.data
        }
      })
      getIndexSexData({type:0}).then(res => {
        if (res.code == 1000) {
          this.sex  =res.data
          this.pieChart = echarts.init(document.getElementById("pie-chart"))
          var optionPie = {
            title: {
              text: '学生性别分布',
              left: 'center',
              top: 10,
              textStyle: {
                color: '#333',
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            tooltip: {
              trigger: 'item',
              formatter: '{b}: {c}人 ({d}%)'
            },
            legend: {
              orient: 'horizontal',
              bottom: 10,
              left: 'center'
            },
            series: [
              {
                name: '学生性别分析',
                type: 'pie',
                radius: '45%',
                center: ['50%', '50%'],
                data: this.sex,
                label: {
                  show: true,
                  formatter: '{b}: {c}人'
                },
                emphasis: {
                  itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                }
              }
            ]
          };
          this.pieChart.setOption(optionPie)
        }
      })
      getTaskChart({type:0}).then(res => {
        if (res.code == 1000) {
          this.tasks = res.data.tasks
          this.nums = res.data.nums
          this.myChart = echarts.init(document.getElementById("chart"))
          var option = {
              title: {
                text: '课程学生数量统计',
                left: 'center',
                top: 10,
                textStyle: {
                  color: '#333',
                  fontSize: 16,
                  fontWeight: 'bold'
                }
              },
              tooltip: {
                trigger: 'axis',
                backgroundColor: '#FDEBD3',
                borderColor: '#DFDBC4',
                axisPointer: {
                  type: 'cross'
                },
                textStyle: {
                  color: '#4a5568',
                  fontWeight: 'bold'
                }
              },
              backgroundColor: '#FDEBD3',
              legend: {
                data: ['学生数量'],
                top: 40,
                textStyle: {
                  color: '#111111',
                  fontWeight: 'bold'
                }
              },
              grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                top: 80,
                containLabel: true
              },
              xAxis: [{
                  type: 'category',
                  color: '#59588D',
                  data: this.tasks,
                  axisLine: {
                      lineStyle: {
                          color: 'rgba(107,107,107,0.37)',
                      }
                  },
                  axisLabel: {
                      interval: 0,
                      rotate: this.tasks && this.tasks.length > 5 ? 20 : 0,
                      color: '#111111',
                      fontWeight: 'bold'
                  },
                  axisTick: {
                      show: true
                  },
              }],
              yAxis: [{
                  type: 'value',
                  axisLine: {
                      lineStyle: {
                          color: 'rgba(107,107,107,0.37)',
                      }
                  },
                  axisTick: {
                      show: true
                  },
                  splitLine: {
                      lineStyle: {
                          color: 'rgba(131,101,101,0.2)',
                          type: 'dashed',
                      }
                  },
                  axisLabel: {
                      color: '#111111',
                      fontWeight: 'bold'
                  }
              }],
              series: [{
                  data: this.nums,
                  type: 'line',
                  name: '学生数量',
                  symbol: 'circle',
                  symbolSize: 6,
                  smooth: true,
                  areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                      { offset: 0, color: 'rgba(153, 205, 219, 0.3)' },
                      { offset: 1, color: 'rgba(153, 205, 219, 0.05)' }
                    ])
                  },
                  lineStyle: {
                      color: '#99CDDB',
                      width: 2
                  },
                  itemStyle: {
                    color: '#99CDDB'
                  }
              },{
                  type: 'bar',
                  data: this.nums,
                  barWidth: '20%',
                  name: '学生数量',
                  itemStyle: {
                      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                          offset: 0,
                          color: '#F3C3B2'
                      }, {
                          offset: 1,
                          color: '#f7d3c5'
                      }]),
                      borderRadius: [8, 8, 0, 0]
                  },
                  label: {
                      show: true,
                      fontSize: 12,
                      color: '#111111',
                      fontWeight: 'bold',
                      position: 'top',
                  }
              }
            ]
          };
          this.myChart.setOption(option);
        }
      })
      getTaskIndexList({type:0}).then(res => {
        if (res.code == 1000) {
          this.taskList = res.data.slice(0,5)
          this.taskCard = res.data.slice(0,3)
        }
      })
      var that = this
      window.onresize = function() {
        if (that.myChart && typeof that.myChart.resize === 'function') {
          that.myChart.resize(); 
        }
        if (that.pieChart && typeof that.pieChart.resize === 'function') {
          that.pieChart.resize(); 
        }
      }
    },
    beforeDestroy() {
      // 清理ECharts实例
      if (this.myChart && typeof this.myChart.dispose === 'function') {
        this.myChart.dispose();
        this.myChart = null;
      }
      if (this.pieChart && typeof this.pieChart.dispose === 'function') {
        this.pieChart.dispose();
        this.pieChart = null;
      }
      
      // 移除窗口大小改变事件监听器
      if (window.onresize) {
        window.onresize = null;
      }
    }
 }
</script>

<style scoped>
  @property --item01-flow-angle {
    syntax: '<angle>';
    inherits: false;
    initial-value: 0deg;
  }
  .index-top {
    height: 210px;
  }
  .index-flow-card-shell {
    position: relative;
    overflow: visible;
    z-index: 1;
    isolation: isolate;
    transition: transform 0.28s ease;
    width: 100%;
    box-sizing: border-box;
  }
  .index-flow-card-shell:hover {
    z-index: 3;
    transform: translateY(-4px);
  }
  .index-flow-card-shell::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: calc(8px + 2px);
    padding: 2px;
    box-sizing: border-box;
    pointer-events: none;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.22s ease;
    --item01-flow-angle: 0deg;
    background: conic-gradient(
      from var(--item01-flow-angle) at 50% 50%,
      #99CDDB 0deg,
      #DFDBC4 45deg,
      #F3C3B2 90deg,
      #DADBC3 135deg,
      #FDEBD3 180deg,
      #DFDBC4 225deg,
      #99CDDB 270deg,
      #F3C3B2 315deg,
      #99CDDB 360deg
    );
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: exclude;
    filter:
      drop-shadow(0 0 1px rgba(153, 205, 219, 0.4))
      drop-shadow(0 0 4px rgba(223, 219, 196, 0.3))
      drop-shadow(0 0 4px rgba(243, 195, 178, 0.2));
    animation: item01-conic-flow 5s linear infinite;
    animation-play-state: paused;
  }
  .index-flow-card-shell:hover::before {
    opacity: 1;
    animation-play-state: running;
  }
  @keyframes item01-conic-flow {
    from {
      --item01-flow-angle: 0deg;
    }
    to {
      --item01-flow-angle: 360deg;
    }
  }
  .item-01,
  .item-05 {
    border-radius: 8px !important;
    height: 170px;
    background-color: #fde9df;
    background-image: linear-gradient(
      165deg,
      #fff5f0 0%,
      #fceee5 22%,
      #fde9df 52%,
      #f9e0d2 100%
    );
    position: relative;
    z-index: 1;
    border: none !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.88),
      inset 0 -1px 0 rgba(243, 195, 178, 0.1),
      0 1px 1px rgba(150, 100, 80, 0.04),
      0 4px 12px rgba(243, 195, 178, 0.1),
      0 12px 32px rgba(243, 195, 178, 0.08);
    transition: box-shadow 0.28s ease;
  }
  .item-05 {
    background-size: 100% 100%;
    color: #1f2d30;
  }
  .index-flow-card-shell:hover .item-01,
  .index-flow-card-shell:hover .item-05,
  .index-flow-card-shell:hover .item-07 {
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.95),
      inset 0 -1px 0 rgba(243, 195, 178, 0.08),
      0 2px 4px rgba(150, 100, 80, 0.05),
      0 8px 20px rgba(243, 195, 178, 0.14),
      0 16px 44px rgba(243, 195, 178, 0.2),
      0 0 0 1px rgba(243, 195, 178, 0.4),
      0 0 36px rgba(243, 195, 178, 0.3);
  }
  .item-02 {
    border-radius: 8px;
    margin-top: 20px;
    height: 90px;
    background-color: #ffffff;
  }
  .item-03,
  .item-06 {
    border-radius: 8px !important;
    height: 170px;
    background-color: #e8e6d0;
    background-image: linear-gradient(
      165deg,
      #f4f3e8 0%,
      #eeeccb 25%,
      #e8e6d0 52%,
      #dfdcc5 100%
    );
    position: relative;
    z-index: 1;
    border: none !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.82),
      inset 0 -1px 0 rgba(100, 100, 80, 0.08),
      0 1px 1px rgba(80, 80, 60, 0.05),
      0 4px 12px rgba(100, 100, 80, 0.1),
      0 12px 32px rgba(100, 100, 80, 0.08);
    transition: box-shadow 0.28s ease;
  }
  .item-04 {
    border-radius: 8px;
    margin-top: 20px;
    height: 90px;
    background-color: #ffffff;
  }
  .index-flow-card-shell:hover .item-03,
  .index-flow-card-shell:hover .item-06,
  .index-flow-card-shell:hover .item-08 {
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(100, 100, 80, 0.06),
      0 2px 4px rgba(80, 80, 60, 0.06),
      0 8px 20px rgba(100, 100, 80, 0.14),
      0 16px 44px rgba(100, 100, 80, 0.18),
      0 0 0 1px rgba(223, 219, 196, 0.45),
      0 0 34px rgba(223, 219, 196, 0.35);
  }
  .index-center {
    margin-top: 20px;
    height: 400px;
  }
  .item-07 {
    border-radius: 8px !important;
    height: 400px;
    background-color: #fde9df;
    background-image: linear-gradient(
      165deg,
      #fff5f0 0%,
      #fceee5 22%,
      #fde9df 52%,
      #f9e0d2 100%
    );
    position: relative;
    z-index: 1;
    border: none !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.88),
      inset 0 -1px 0 rgba(243, 195, 178, 0.1),
      0 1px 1px rgba(150, 100, 80, 0.04),
      0 4px 12px rgba(243, 195, 178, 0.1),
      0 12px 32px rgba(243, 195, 178, 0.08);
    transition: box-shadow 0.28s ease;
  }
  .item-08 {
    border-radius: 8px !important;
    height: 400px;
    background-color: #e8e6d0;
    background-image: linear-gradient(
      165deg,
      #f4f3e8 0%,
      #eeeccb 25%,
      #e8e6d0 52%,
      #dfdcc5 100%
    );
    position: relative;
    z-index: 1;
    border: none !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.82),
      inset 0 -1px 0 rgba(100, 100, 80, 0.08),
      0 1px 1px rgba(80, 80, 60, 0.05),
      0 4px 12px rgba(100, 100, 80, 0.1),
      0 12px 32px rgba(100, 100, 80, 0.08);
    transition: box-shadow 0.28s ease;
  }
  .index-center-01 {
    margin-top: 20px;
    height: 300px;
  }
  .item-09,
  .item-10,
  .item-11 {
    border-radius: 8px !important;
    background-color: #DADBC3;
    background-image: linear-gradient(
      165deg,
      #f0eed8 0%,
      #e8e6d0 28%,
      #DADBC3 52%,
      #cec9b3 100%
    );
    position: relative;
    z-index: 1;
    border: none !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.88),
      inset 0 -1px 0 rgba(100, 100, 80, 0.09),
      0 1px 1px rgba(80, 80, 60, 0.05),
      0 4px 12px rgba(100, 100, 80, 0.1),
      0 12px 32px rgba(100, 100, 80, 0.08);
    transition: box-shadow 0.28s ease;
  }
  .item-09 {
    height: 300px;
    overflow: scroll;
  }
  .index-under {
    margin-top: 20px;
    height: 400px;
  }
  .item-10 {
    height: 350px;
  }
  .item-11 {
    height: 450px;
  }
  .index-flow-card-shell:hover .item-09,
  .index-flow-card-shell:hover .item-10,
  .index-flow-card-shell:hover .item-11 {
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.95),
      inset 0 -1px 0 rgba(100, 100, 80, 0.07),
      0 2px 4px rgba(80, 80, 60, 0.06),
      0 8px 20px rgba(100, 100, 80, 0.14),
      0 16px 44px rgba(100, 100, 80, 0.18),
      0 0 0 1px rgba(223, 219, 196, 0.45),
      0 0 36px rgba(223, 219, 196, 0.35);
  }
  .item-01-top {
    height: 90px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-around;
  }
  .item-01-top-center {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
  }
  .item-01-under {
    height: 60px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
  .item-06-top {
    display: flex;
    justify-content: center;
  }
  .item-06-under {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .item-05-top {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  #chart,#pie-chart {
    width: 100%;
    height: 380px;
  }
  #pie-chart {
    background-color: #e8e6d0;
    border-radius: 10px;
  }
  #chart {
    background-color: #fde9df;
    border-radius: 10px;
  }
  .item-07::v-deep .el-card__body {
    background-color: #fde9df;
    padding: 12px;
  }
  .item-08::v-deep .el-card__body {
    background-color: #e8e6d0;
    padding: 12px;
  }
  .item-09::-webkit-scrollbar {
      width: 0px;
      height: 0px;
  }
  .el-row {
    width: 100%;
    height: 100%;
  }
  .item-10-item {
    position: relative;
    isolation: isolate;
    overflow: visible;
    z-index: 1;
    border: none;
    border-radius: 20px;
    height: 100%;
    margin: 0 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-around;
    box-sizing: border-box;
    background-color: #d4edf2;
    background-image: linear-gradient(
      165deg,
      #eaf6f9 0%,
      #dff3f8 35%,
      #d0ebf3 100%
    );
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.9),
      inset 0 -1px 0 rgba(100, 160, 175, 0.06),
      0 2px 6px rgba(100, 160, 175, 0.08),
      0 10px 24px rgba(100, 160, 175, 0.1);
    transition: transform 0.28s ease, box-shadow 0.28s ease;
  }
  .item-10-item:hover {
    z-index: 6;
    transform: translateY(-4px);
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.95),
      inset 0 -1px 0 rgba(100, 160, 175, 0.05),
      0 4px 12px rgba(100, 160, 175, 0.12),
      0 16px 40px rgba(100, 160, 175, 0.18),
      0 0 28px rgba(153, 205, 219, 0.35);
  }
  .item-10-item::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 22px;
    padding: 2px;
    box-sizing: border-box;
    pointer-events: none;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.22s ease;
    --item01-flow-angle: 0deg;
    background: conic-gradient(
      from var(--item01-flow-angle) at 50% 50%,
      #99CDDB 0deg,
      #DFDBC4 45deg,
      #F3C3B2 90deg,
      #DADBC3 135deg,
      #FDEBD3 180deg,
      #DFDBC4 225deg,
      #99CDDB 270deg,
      #F3C3B2 315deg,
      #99CDDB 360deg
    );
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: exclude;
    filter:
      drop-shadow(0 0 1px rgba(153, 205, 219, 0.4))
      drop-shadow(0 0 4px rgba(223, 219, 196, 0.3))
      drop-shadow(0 0 4px rgba(243, 195, 178, 0.2));
    animation: item01-conic-flow 5s linear infinite;
    animation-play-state: paused;
  }
  .item-10-item:hover::before {
    opacity: 1;
    animation-play-state: running;
  }
  .item-10-item > * {
    position: relative;
    z-index: 1;
  }
  ::v-deep .el-calendar-day {
    height: 45px !important;
    border: none;
  }
</style>
