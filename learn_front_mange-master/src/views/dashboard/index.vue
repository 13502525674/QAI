<template>
  <div class="dashboard-container" style="background: #000 url('/dashboard/images/bg.png') no-repeat center center; background-size: cover;">
    <div class="dashboard-bg"></div>
    
    <!-- Header -->
    <header class="dashboard-header" style="background: url('/dashboard/images/head_bg.png') no-repeat center center; background-size: contain;">
      <div class="header-left">
        <el-button 
          type="primary" 
          size="small"
          :loading="aiLoading"
          @click="getAiRecommend"
          icon="el-icon-magic-stick"
          class="ai-recommend-btn">
          {{ aiLoading ? 'AI分析中...' : 'AI学习推荐' }}
        </el-button>
      </div>
      <div class="header-content">
        <h1>物理智能学习伴侣平台数据看板</h1>
      </div>
      <div class="exit-btn" @click="$router.push('/')">退出大屏</div>
    </header>

    <!-- Main Content -->
    <section class="dashboard-content">
      <!-- Loading Indicator -->
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-spinner">数据加载中...</div>
      </div>
      
      <!-- Top Stats Row -->
      <div class="stats-row">
        <!-- Group 1 -->
        <div class="stats-group">
          <div class="stats-bg">
            <div class="stat-item">
              <img src="/dashboard/images/info_1.png" alt="" />
              <div class="stat-info">
                <p>用户总数</p>
                <p class="count-num">{{stats.totalUsers}}</p>
              </div>
            </div>
            <div class="stat-item">
              <img src="/dashboard/images/info_2.png" alt="" />
              <div class="stat-info">
                <p>学生总数</p>
                <p class="count-num">{{stats.totalStudents}}</p>
              </div>
            </div>
            <div class="stat-item">
              <img src="/dashboard/images/info_3.png" alt="" />
              <div class="stat-info">
                <p>教师总数</p>
                <p class="count-num">{{stats.totalTeachers}}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Group 2 -->
        <div class="stats-group center-group">
          <div class="stats-bg">
            <div class="stat-item">
              <img src="/dashboard/images/info_4.png" alt="" />
              <div class="stat-info">
                <p>课程总数</p>
                <p class="count-num">{{stats.totalCourses}}</p>
              </div>
            </div>
            <div class="stat-item">
              <img src="/dashboard/images/info_5.png" alt="" />
              <div class="stat-info">
                <p>课程视频（已看完）</p>
                <p class="count-num">{{stats.totalVideos}}</p>
              </div>
            </div>
            <div class="stat-item">
              <img src="/dashboard/images/info_1.png" alt="" />
              <div class="stat-info">
                <p>试卷总数</p>
                <p class="count-num">{{stats.totalPapers}}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Group 3 -->
        <div class="stats-group">
          <div class="stats-bg">
            <div class="stat-item">
              <img src="/dashboard/images/info_6.png" alt="" />
              <div class="stat-info">
                <p>题目总数</p>
                <p class="count-num">{{stats.totalQuestions}}</p>
              </div>
            </div>
            <div class="stat-item">
              <img src="/dashboard/images/info_7.png" alt="" />
              <div class="stat-info">
                <p>AI模型使用次数</p>
                <p class="count-num">{{stats.aiCalls}}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Grid -->
      <div class="main-grid">
        <!-- Left Column -->
        <div class="col-left">
          <div class="panel public">
            <div class="min-title"><span>用户构成分析</span></div>
            <div class="chart-container" ref="userChart"></div>
            <div class="boxfoot"></div>
          </div>
          <div class="panel public">
            <div class="min-title"><span>课程数量统计</span></div>
            <div class="chart-container" ref="courseChart"></div>
            <div class="boxfoot"></div>
          </div>
          <div class="panel public">
            <div class="min-title"><span>每日刷题趋势</span></div>
            <div class="chart-container" ref="trendChart"></div>
            <div class="boxfoot"></div>
          </div>
        </div>

        <!-- Center Column -->
        <div class="col-center">
          <div class="center-top">
            <!-- Title removed to make space for larger figure -->
            
            <div class="rotate-container">
              <div id="drag-container">
                <div id="spin-container">
                    <div class="physics-card">
                        <div class="card-icon">⚛️</div>
                        <h3>力学</h3>
                    </div>
                    <div class="physics-card">
                        <div class="card-icon">⚡</div>
                        <h3>电磁学</h3>
                    </div>
                    <div class="physics-card">
                        <div class="card-icon">🔥</div>
                        <h3>热学</h3>
                    </div>
                    <div class="physics-card">
                        <div class="card-icon">🌈</div>
                        <h3>光学</h3>
                    </div>
                    <div class="physics-card">
                        <div class="card-icon">🌌</div>
                        <h3>量子物理</h3>
                    </div>
                    <div class="physics-card">
                        <div class="card-icon">🚀</div>
                        <h3>相对论</h3>
                    </div>
                    <!-- Added extra cards for fuller circle -->
                    <div class="physics-card">
                        <div class="card-icon">🔭</div>
                        <h3>天体物理</h3>
                    </div>
                    <div class="physics-card">
                        <div class="card-icon">⚛️</div>
                        <h3>粒子物理</h3>
                    </div>
                </div>
                <div id="ground"></div>
              </div>
              <!-- Central Figure -->
              <img class="base-plate" src="/dashboard/images/bj.png" alt="" />
              <img class="center-person" src="/dashboard/images/newdz.png" alt="" />
            </div>
          </div>
          
          <div class="center-bottom panel public">
            <div class="min-title"><span>学生考试成绩分布</span></div>
            <div class="chart-container" ref="aiChart"></div>
            <div class="boxfoot"></div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="col-right">
          <div class="panel public radar-panel">
            <div class="min-title"><span>学生整体能力雷达图</span></div>
            <div class="radar-wrapper">
              <RadarChartDashboard 
                title=""
                height="180px"
                :showLegend="true" />
            </div>
            <div class="boxfoot"></div>
          </div>
          <div class="panel public">
            <div class="min-title"><span>试卷分支统计</span></div>
            <div class="chart-container" ref="paperChart"></div>
            <div class="boxfoot"></div>
          </div>
          <div class="panel public">
            <div class="min-title"><span>系统负载监控</span></div>
            <div class="chart-container" ref="gaugeChart"></div>
            <div class="boxfoot"></div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- AI推荐弹窗 -->
    <div v-if="aiDialogVisible" class="ai-dialog-overlay" @click.self="aiDialogVisible = false">
      <div class="ai-dialog-panel">
        <div class="ai-dialog-header">
          <span class="ai-dialog-title">
            <i class="el-icon-magic-stick"></i>
            AI个性化学习推荐
          </span>
          <span class="ai-dialog-close" @click="aiDialogVisible = false">×</span>
        </div>
        <div class="ai-dialog-body">
          <div v-if="aiLoading && !aiRecommendText" class="ai-loading">
            <div class="ai-loading-spinner"></div>
            <p>AI正在分析学习数据...</p>
          </div>
          <div v-else class="ai-result" v-html="formatAiText(aiRecommendText)"></div>
        </div>
        <div class="ai-dialog-footer">
          <button class="ai-btn ai-btn-default" @click="aiDialogVisible = false">关闭</button>
          <button class="ai-btn ai-btn-primary" :disabled="aiLoading" @click="getAiRecommend">
            <i v-if="aiLoading" class="el-icon-loading"></i>
            {{ aiLoading ? '分析中...' : '重新推荐' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getIndexData, getApeTaskPage, getLatestComments, getOperLogPage, getPhysicsQuestionPaperPage, getPhysicsPracticeRecordsPage, getLogPage, getAiHistoryCount, getAllStudentScores, getSystemLoad } from '@/api/api';
import RadarChartDashboard from '@/components/charts/RadarChartDashboard.vue';


export default {
  name: 'Dashboard',
  components: {
    RadarChartDashboard
  },
  data() {
    return {
      stats: {
        totalUsers: 0,
        totalStudents: 0,
        totalTeachers: 0,
        totalCourses: 0,
        totalVideos: 0,
        totalQuestions: 0,
        totalPapers: 0,
        aiCalls: 0
      },
      comments: [],
      scrollMargin: 0,
      charts: {},
      pollingTimer: null,
      scrollTimer: null,
      systemLoadTimer: null,
      isLoading: true,
      currentStudentId: '',
      aiLoading: false,
      aiRecommendText: '',
      aiDialogVisible: false,
      currentCpuUsage: 0,
      currentMemoryUsage: 0,
      
      radius: 300,
      rotateSpeed: -60,
      autoRotate: true,
      tX: 0,
      tY: 10,
    };
  },
  // 在组件创建时检查登录状态
  beforeCreate() {
    // 检查是否有有效的token
    const token = localStorage.getItem('token');
    if (!token) {
      // 如果没有token，重定向到登录页面
      this.$router.push('/login');
      return;
    }
  },
  filters: {
    formatDate(val) {
      if(!val) return '';
      return val.substring(5, 10);
    }
  },
  async mounted() {
    this.isLoading = true; // 设置加载状态
    try {
      this.init3D();
      this.initCharts();
      await this.fetchData(); // 等待首次数据加载
      this.startPolling();
      this.startScroll();
      this.startSystemLoadMonitor(); // 启动系统负载监控
    } finally {
      this.isLoading = false; // 结束加载状态
    }
    window.addEventListener('resize', this.resizeCharts);
  },
  beforeDestroy() {
    clearInterval(this.pollingTimer);
    clearInterval(this.scrollTimer);
    this.stopSystemLoadMonitor(); // 停止系统负载监控
    window.removeEventListener('resize', this.resizeCharts);
    Object.values(this.charts).forEach(c => c.dispose());
  },
  methods: {
    resizeCharts() {
      Object.values(this.charts).forEach(c => c.resize());
    },
    getAiRecommend() {
      this.aiLoading = true;
      this.aiRecommendText = '';
      this.aiDialogVisible = true;
      
      const token = localStorage.getItem('token') || localStorage.getItem('user_token') || '';
      if (!token) {
        this.$message.warning('请先登录');
        this.aiLoading = false;
        this.aiDialogVisible = false;
        return;
      }
      
      const baseUrl = 'http://localhost:8080';
      const url = `${baseUrl}/api/ai/recommend/my?conversationId=dashboard_${Date.now()}&x_access_token=${token}`;
      
      const eventSource = new EventSource(url);
      
      eventSource.onmessage = (event) => {
        this.aiRecommendText += event.data;
      };
      
      eventSource.onerror = () => {
        eventSource.close();
        this.aiLoading = false;
        if (!this.aiRecommendText) {
          this.$message.error('获取推荐失败，请检查网络连接或重新登录');
        }
      };
      
      setTimeout(() => {
        eventSource.close();
        this.aiLoading = false;
      }, 60000);
    },
    formatAiText(text) {
      if (!text) return '';
      return text
        .replace(/\n/g, '<br>')
        .replace(/##\s*(.+)/g, '<h4 style="color:#00d8ff;margin:10px 0;">$1</h4>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/(\d+)\.\s*(.+)/g, '<p style="margin:5px 0;"><span style="color:#00d8ff;">$1.</span> $2</p>');
    },
    startPolling() {
      this.fetchData();
      this.pollingTimer = setInterval(() => {
        this.fetchData(true);
      }, 30000);
    },
    normalizeResponse(res) {
      if (!res) return { ok: false, data: null };
      
      // 根据后端Result类的结构处理响应
      const ok = res.success === true || res.code === 1000 || res.code === 200 || res.code === 0;
      
      // 如果响应成功且有data字段，返回data；否则返回整个响应对象
      const data = res.data !== undefined ? res.data : res;
      
      return { ok: !!ok, data: data };
    },
    async fetchData(isUpdate = false) {
      try {
        console.log('开始获取数据...');
        
        // 验证token是否有效
        const token = localStorage.getItem('token');
        if (!token) {
          console.error('Token不存在，跳转到登录页面');
          this.$router.push('/login');
          return;
        }
        
        const [userData, taskData, commentData, paperData, studentScoreData, loginLogData, practiceData, aiHistoryData] = await Promise.allSettled([
           getIndexData({type: 0}),
           getApeTaskPage({pageNumber: 1, pageSize: 1000}),
           getLatestComments(),
           getPhysicsQuestionPaperPage({pageNumber: 1, pageSize: 1000}),
           getAllStudentScores(),
           getLogPage({pageNumber: 1, pageSize: 50}), 
           getPhysicsPracticeRecordsPage({pageNumber: 1, pageSize: 1000}),
           getAiHistoryCount()
        ]);

        console.log('用户数据:', userData.status, userData.value);
        console.log('课程数据:', taskData.status, taskData.value);
        console.log('评论数据:', commentData.status, commentData.value);
        console.log('试卷数据:', paperData.status, paperData.value);
        console.log('学生考试成绩:', studentScoreData.status, studentScoreData.value);
        console.log('AI历史数据:', aiHistoryData.status, aiHistoryData.value);

        // 检查是否有身份验证错误
        const hasAuthError = [userData, taskData, commentData, paperData, studentScoreData, loginLogData, practiceData, aiHistoryData].some(
          result => result.status === 'rejected' || 
          (result.status === 'fulfilled' && result.value && result.value.code === 1006)
        );
        
        if (hasAuthError) {
          console.error('身份验证失败，跳转到登录页面');
          this.$router.push('/login');
          return;
        }

        if(userData.status === 'fulfilled') {
            const { ok, data } = this.normalizeResponse(userData.value);
            console.log('用户数据处理结果:', ok, data);
            if(ok && data) {
              const d = data;
              this.stats.totalUsers = d.userNum || d.totalUsers || d.totalUser || d.userCount || 0;
              this.stats.totalStudents = d.studentNum || d.totalStudents || d.studentCount || 0;
              this.stats.totalTeachers = d.teacherNum || d.totalTeachers || d.teacherCount || 0;
              // 使用后端返回的准确计数
              this.stats.totalCourses = d.courseNum || 0;
              this.stats.totalVideos = d.videoNum || 0;
              
              console.log('用户统计数据:', this.stats.totalUsers, this.stats.totalStudents, this.stats.totalTeachers);
              this.updateUserChart();
            }
        }

        if(taskData.status === 'fulfilled') {
            const { ok, data } = this.normalizeResponse(taskData.value);
            console.log('课程数据处理结果:', ok, data);
            if(ok && data) {
              const records = Array.isArray(data) ? data : (data.records || data.list || data.data || []);
              // 如果getIndexData没有返回courseNum，则尝试从这里获取，但优先使用getIndexData
              if (this.stats.totalCourses === 0) {
                 this.stats.totalCourses = data.total || data.count || data.size || records.length || 0;
              }
              this.updateCourseChart(records);
            } else {
              this.updateCourseChart([]);
            }
        }

        if(commentData.status === 'fulfilled') {
            const { ok, data } = this.normalizeResponse(commentData.value);
            console.log('评论数据处理结果:', ok, data);
            if(ok && data) {
              const records = Array.isArray(data) ? data : (data.records || data.list || data.data || []);
              console.log('评论记录数量:', records.length);
              
              if(!isUpdate || this.comments.length === 0) {
                  // 确保评论数据有正确的字段名
                  const formattedRecords = records.map(item => ({
                      content: item.content || '无内容',
                      createBy: item.createBy || '匿名用户',
                      createTime: item.createTime || new Date().toISOString()
                  }));
                  this.comments = formattedRecords;
                  console.log('更新评论列表:', formattedRecords);
              }
            } else {
              // 添加默认评论数据
              this.comments = [
                  { content: '这个课程很有用！', createBy: '张三', createTime: '2024-01-15' },
                  { content: '老师讲得很透彻', createBy: '李四', createTime: '2024-01-14' },
                  { content: '希望多出一些习题', createBy: '王五', createTime: '2024-01-13' },
                  { content: '物理真的很有趣', createBy: '赵六', createTime: '2024-01-12' },
                  { content: '感谢老师的付出', createBy: '孙七', createTime: '2024-01-11' }
              ];
            }
        }
        
        let papers = [];
        if(paperData.status === 'fulfilled') {
             const { ok, data } = this.normalizeResponse(paperData.value);
             console.log('试卷数据处理结果:', ok, data);
             if(ok && data) {
               const pData = data;
               papers = Array.isArray(pData) ? pData : (pData.records || pData.list || []);
               console.log('试卷记录数量:', papers.length);
               this.stats.totalPapers = Array.isArray(pData) ? pData.length : (pData.total || pData.count || papers.length || 0);
               console.log('试卷总数:', this.stats.totalPapers);
               this.stats.totalQuestions = papers.reduce((sum, p) => sum + (p.totalQuestions || p.questionCount || 0), 0);
               console.log('题目总数:', this.stats.totalQuestions);
               this.updatePaperChart(papers);
             } else {
               this.stats.totalPapers = 0;
               this.stats.totalQuestions = 0;
               this.updatePaperChart([]);
             }
        }

        let practices = [];
        if(practiceData.status === 'fulfilled') {
             const { ok, data } = this.normalizeResponse(practiceData.value);
             if(ok && data) {
               const pData = data;
               practices = Array.isArray(pData) ? pData : (pData.records || pData.list || []);
             }
        }

        let studentScores = [];
        if(studentScoreData.status === 'fulfilled') {
            const { ok, data } = this.normalizeResponse(studentScoreData.value);
            if(ok && data) {
              studentScores = Array.isArray(data) ? data : (data.records || data.list || []);
              console.log('学生考试成绩数量:', studentScores.length);
            }
            this.updateAIChart(studentScores);
        } else {
            this.updateAIChart([]);
        }

        if(aiHistoryData.status === 'fulfilled') {
            const { ok, data } = this.normalizeResponse(aiHistoryData.value);
            console.log('AI历史数据处理结果:', ok, data);
            if(ok && data) {
              this.stats.aiCalls = data.totalMessages || 0;
              console.log('AI调用次数:', this.stats.aiCalls);
            } else {
              this.stats.aiCalls = 0;
            }
        } else {
            this.stats.aiCalls = 0;
        }

        let loginLogs = [];
        if(loginLogData.status === 'fulfilled') {
            const { ok, data } = this.normalizeResponse(loginLogData.value);
            if(ok && data) {
              loginLogs = data.records || data.list || [];
            }
        }

        console.log('更新趋势图...');
        this.updateTrendChart(practices);
        // updateGaugeChart() 现在由独立的定时器调用，不再在这里调用
        
        const userStr = localStorage.getItem('user') || localStorage.getItem('user_info') || '{}';
        console.log('localStorage user string:', userStr);
        try {
          const userInfo = JSON.parse(userStr);
          console.log('Parsed userInfo:', userInfo);
          this.currentStudentId = userInfo.id || userInfo.userId || userInfo.user_id || localStorage.getItem('userId') || '';
          console.log('currentStudentId:', this.currentStudentId);
        } catch (parseError) {
          console.error('解析用户信息失败:', parseError);
          this.currentStudentId = '';
        }

      } catch (e) {
        console.error("Dashboard data fetch error:", e);
        // 检查是否是身份验证错误
        if (e.message && (e.message.includes('1006') || e.message.includes('TOKEN'))) {
          console.error('身份验证失败，跳转到登录页面');
          this.$router.push('/login');
        } else {
          // 即使出错，也要确保所有图表都有默认数据
          this.updateUserChart();
          this.updateCourseChart([]);
          this.updateAIChart([]);
          this.updatePaperChart([]);
          this.updateTrendChart([]);
          this.updateGaugeChart();
        }
      }
    },
    // Mock Data functions removed to ensure real data usage
    startScroll() {
        this.scrollTimer = setInterval(() => {
            if(this.comments.length > 5) {
                this.scrollMargin -= 1;
                if (Math.abs(this.scrollMargin) >= 40) { // Assuming 40px per item height
                    this.comments.push(this.comments.shift());
                    this.scrollMargin = 0;
                }
            }
        }, 50);
    },
    // --- Chart Initializers ---
    initCharts() {
        // More distinct colors
        const chartTheme = {
            color: ['#00d8ff', '#f8b500', '#00ff99', '#ff3d00', '#a800ff', '#005bea']
        };
        echarts.registerTheme('tech', chartTheme);
        
        this.charts.user = echarts.init(this.$refs.userChart, 'tech');
        this.charts.course = echarts.init(this.$refs.courseChart, 'tech');
        this.charts.trend = echarts.init(this.$refs.trendChart, 'tech');
        this.charts.ai = echarts.init(this.$refs.aiChart, 'tech');
        this.charts.paper = echarts.init(this.$refs.paperChart, 'tech');
        this.charts.gauge = echarts.init(this.$refs.gaugeChart, 'tech');
    },
    updateUserChart() {
        const adminCount = Math.max(0, this.stats.totalUsers - this.stats.totalStudents - this.stats.totalTeachers);
        const option = {
            tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
            legend: { 
                bottom: '0%', 
                left: 'center', 
                textStyle: { color: '#fff', fontSize: 11 }, 
                itemWidth: 10, 
                itemHeight: 10 
            },
            series: [{
                name: '用户构成',
                type: 'pie',
                radius: ['40%', '65%'],
                center: ['50%', '42%'],
                avoidLabelOverlap: false,
                label: { 
                    show: false, 
                    position: 'center' 
                },
                emphasis: { 
                    label: { 
                        show: true, 
                        fontSize: 18, 
                        fontWeight: 'bold', 
                        color: '#fff',
                        formatter: '{b}\n{c}人'
                    } 
                },
                labelLine: { show: false },
                data: [
                    { value: this.stats.totalStudents, name: '学生', itemStyle: { color: '#00d8ff' } },
                    { value: this.stats.totalTeachers, name: '教师', itemStyle: { color: '#f8b500' } },
                    { value: adminCount, name: '管理员', itemStyle: { color: '#00ff99' } }
                ].filter(item => item.value > 0)
            }]
        };
        if(option.series[0].data.length === 0) {
            option.series[0].data = [{ value: 0, name: '暂无数据' }];
        }
        this.charts.user.setOption(option);
    },
    updateCourseChart(data = []) {
        console.log('课程数据:', data);
        
        const totalCourses = this.stats.totalCourses || data.length || 0;
        
        console.log('课程总数:', totalCourses);
        
        const option = {
            tooltip: { trigger: 'item', formatter: '{b}: {c}门' },
            series: [{
                name: '课程数量',
                type: 'gauge',
                radius: '70%',
                center: ['50%', '58%'],
                startAngle: 200,
                endAngle: -20,
                min: 0,
                max: Math.max(100, Math.ceil(totalCourses * 1.5 / 10) * 10),
                splitNumber: 5,
                axisLine: {
                    lineStyle: {
                        width: 12,
                        color: [
                            [0.3, '#ff6b6b'],
                            [0.7, '#ffd93d'],
                            [1, '#00d8ff']
                        ]
                    }
                },
                pointer: {
                    icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
                    length: '15%',
                    width: 16,
                    offsetCenter: [0, '-55%'],
                    itemStyle: {
                        color: '#00d8ff'
                    }
                },
                axisTick: {
                    length: 10,
                    lineStyle: {
                        color: 'auto',
                        width: 2
                    }
                },
                splitLine: {
                    length: 18,
                    lineStyle: {
                        color: '#fff',
                        width: 3
                    }
                },
                axisLabel: {
                    color: '#aaa',
                    fontSize: 11,
                    distance: 25,
                    formatter: function (value) {
                        return value;
                    }
                },
                title: {
                    offsetCenter: [0, '-25%'],
                    fontSize: 14,
                    color: '#00d8ff',
                    fontWeight: 'bold'
                },
                detail: {
                    fontSize: 32,
                    offsetCenter: [0, '12%'],
                    valueAnimation: true,
                    formatter: function (value) {
                        return '{value|' + value.toFixed(0) + '}';
                    },
                    rich: {
                        value: {
                            fontSize: 32,
                            fontWeight: 'bolder',
                            color: '#00d8ff',
                            textShadow: '0 0 10px #00d8ff'
                        }
                    }
                },
                data: [{
                    value: totalCourses,
                    name: '课程总数'
                }]
            }]
        };
        
        console.log('设置课程数量图表选项:', option);
        this.charts.course.setOption(option);
    },
    updateTrendChart(data = []) {
        const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        const today = new Date();
        const last7Days = Array.from({length: 7}, (_, i) => {
            const d = new Date(today);
            d.setDate(today.getDate() - (6 - i));
            return d;
        });
        
        const counts = last7Days.map(day => {
            const dateStr = day.toISOString().split('T')[0];
            return data.filter(item => {
                // Support multiple date field names
                const t = item.submittedAt || item.createTime || item.operTime;
                return t && t.startsWith(dateStr);
            }).length;
        });
        
        const xAxisData = last7Days.map(d => days[d.getDay()]);

        const option = {
            tooltip: { trigger: 'axis' },
            grid: { top: '20%', bottom: '10%', left: '10%', right: '5%', containLabel: true },
            xAxis: { 
                type: 'category', 
                data: xAxisData,
                axisLabel: { color: '#fff' },
                axisLine: { lineStyle: { color: '#20558b' } }
            },
            yAxis: { 
                type: 'value',
                minInterval: 1,
                axisLabel: { color: '#fff' },
                splitLine: { lineStyle: { color: '#112e4d', type: 'dashed' } }
            },
            series: [{
                name: '练习提交量',
                data: counts,
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: { width: 3, color: '#00ff99' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(0, 255, 153, 0.5)' },
                        { offset: 1, color: 'rgba(0, 255, 153, 0.0)' }
                    ])
                },
                itemStyle: { color: '#00ff99', borderColor: '#fff', borderWidth: 2 }
            }]
        };
        this.charts.trend.setOption(option);
    },
    updatePaperChart(data = []) {
        console.log('试卷数据:', data);
        
        const types = {};
        data.forEach(item => {
             const type = item.subjectBranch || item.paperType || item.branch || item.type || item.category || '综合试卷';
             types[type] = (types[type] || 0) + 1;
        });
        
        console.log('试卷分支统计:', types);
        
        let chartData = Object.keys(types).map(k => ({ value: types[k], name: k }));
        if(chartData.length === 0) chartData = [{value: 0, name: '暂无数据'}];

        console.log('试卷图表数据:', chartData);

        const option = {
            tooltip: { trigger: 'item', formatter: '{b} : {c}份 ({d}%)' },
            legend: { 
                bottom: '2%', 
                left: 'center', 
                textStyle: { color: '#fff', fontSize: 9 }, 
                icon: 'circle',
                type: 'scroll',
                pageIconColor: '#00d8ff',
                pageTextStyle: { color: '#fff' },
                maxHeight: 50,
                itemGap: 4,
                pageButtonGap: 3,
                itemWidth: 8,
                itemHeight: 8
            },
            series: [{
                name: '试卷分支',
                type: 'pie',
                radius: [25, 55],
                center: ['50%', '40%'],
                roseType: 'area',
                itemStyle: { borderRadius: 6 },
                label: { 
                    show: true,
                    position: 'outside',
                    formatter: '{b}',
                    color: '#fff',
                    fontSize: 8
                },
                labelLine: {
                    show: true,
                    lineStyle: { color: '#20558b' },
                    length: 8,
                    length2: 4
                },
                data: chartData
            }]
        };
        
        console.log('设置试卷图表选项:', option);
        this.charts.paper.setOption(option);
    },
    updateAIChart(data = []) {
        console.log('学生考试成绩数据:', data);
        
        if (!data || data.length === 0) {
            const option = {
                title: {
                    text: '暂无考试数据',
                    left: 'center',
                    top: 'center',
                    textStyle: { color: '#fff', fontSize: 14 }
                }
            };
            this.charts.ai.setOption(option);
            return;
        }
        
        const examMap = {};
        const studentMap = {};
        
        data.forEach(item => {
            const testName = item.testName || '未知考试';
            const studentName = item.studentName || '未知学生';
            
            if (!examMap[testName]) {
                examMap[testName] = { total: 0, count: 0 };
            }
            examMap[testName].total += item.percentage || 0;
            examMap[testName].count += 1;
            
            if (!studentMap[studentName]) {
                studentMap[studentName] = [];
            }
            studentMap[studentName].push({
                testName: testName,
                percentage: item.percentage || 0,
                totalScore: item.totalScore || 0,
                maxScore: item.maxScore || 100
            });
        });
        
        const examNames = Object.keys(examMap);
        const examAvgScores = examNames.map(name => 
            (examMap[name].total / examMap[name].count).toFixed(1)
        );
        const examStudentCounts = examNames.map(name => examMap[name].count);
        
        const students = Object.keys(studentMap);
        const colors = ['#00ff99', '#ff3d00', '#a800ff', '#ff6b6b', '#4ecdc4'];
        
        const lineSeries = students.slice(0, 5).map((student, idx) => {
            const studentData = studentMap[student];
            const dataPoints = examNames.map(examName => {
                const record = studentData.find(d => d.testName === examName);
                return record ? record.percentage : null;
            });
            return {
                name: student,
                type: 'line',
                yAxisIndex: 0,
                symbol: 'circle',
                symbolSize: 6,
                connectNulls: true,
                lineStyle: { width: 2 },
                itemStyle: { color: colors[idx % colors.length] },
                data: dataPoints
            };
        });
        
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'cross' },
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                borderColor: '#00d8ff',
                borderWidth: 1,
                textStyle: { color: '#fff' },
                formatter: function(params) {
                    let result = `<strong>${params[0].axisValue}</strong><br/>`;
                    params.forEach(param => {
                        if (param.value !== null && param.value !== undefined) {
                            const unit = param.seriesName === '参与人数' ? '人' : '%';
                            result += `${param.marker}${param.seriesName}: ${param.value}${unit}<br/>`;
                        }
                    });
                    return result;
                }
            },
            legend: {
                data: ['平均成绩', '参与人数', ...students.slice(0, 5)],
                top: '0%',
                textStyle: { color: '#fff', fontSize: 10 },
                pageTextStyle: { color: '#fff' },
                pageIconColor: '#00d8ff',
                pageIconInactiveColor: '#112e4d',
                type: 'scroll',
                itemWidth: 15,
                itemHeight: 10
            },
            grid: { 
                top: '18%', 
                bottom: '12%', 
                left: '8%', 
                right: '8%', 
                containLabel: true 
            },
            xAxis: {
                type: 'category',
                data: examNames,
                axisLabel: { 
                    color: '#fff', 
                    fontSize: 10,
                    interval: 0,
                    rotate: examNames.length > 4 ? 15 : 0
                },
                axisLine: { lineStyle: { color: '#20558b' } },
                axisTick: { show: false }
            },
            yAxis: [
                {
                    type: 'value',
                    name: '成绩(%)',
                    nameTextStyle: { color: '#fff', fontSize: 10 },
                    nameLocation: 'middle',
                    nameGap: 35,
                    min: 0,
                    max: 100,
                    axisLabel: { color: '#fff', fontSize: 9, formatter: '{value}%' }, 
                    splitLine: { lineStyle: { color: '#112e4d', type: 'dashed' } },
                    axisLine: { show: false }
                },
                {
                    type: 'value',
                    name: '人数',
                    nameTextStyle: { color: '#fff', fontSize: 10 },
                    nameLocation: 'middle',
                    nameGap: 25,
                    min: 0,
                    axisLabel: { color: '#fff', fontSize: 9 }, 
                    splitLine: { show: false },
                    axisLine: { show: false }
                }
            ],
            series: [
                {
                    name: '平均成绩',
                    type: 'bar',
                    barWidth: '30%',
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#00d8ff' },
                            { offset: 1, color: '#005bea' }
                        ]),
                        borderRadius: [3, 3, 0, 0]
                    },
                    label: {
                        show: true,
                        position: 'top',
                        color: '#00d8ff',
                        fontSize: 9,
                        formatter: '{c}%'
                    },
                    data: examAvgScores
                },
                {
                    name: '参与人数',
                    type: 'bar',
                    yAxisIndex: 1,
                    barWidth: '30%',
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#f8b500' },
                            { offset: 1, color: '#ff6b00' }
                        ]),
                        borderRadius: [3, 3, 0, 0]
                    },
                    label: {
                        show: true,
                        position: 'top',
                        color: '#f8b500',
                        fontSize: 9,
                        formatter: '{c}人'
                    },
                    barGap: '20%',
                    data: examStudentCounts
                },
                ...lineSeries
            ]
        };
        
        console.log('设置学生考试成绩混合图表选项:', option);
        this.charts.ai.setOption(option);
    },
    updateActivityChart(data = []) {
         // Replaced by Paper Chart, but keeping method if needed or remove
    },
    async updateGaugeChart() {
        try {
            const res = await getSystemLoad();
            
            if (res.code === 1000 && res.data) {
                this.currentCpuUsage = res.data.cpuUsage || 0;
                this.currentMemoryUsage = res.data.memoryUsage || 0;
                
                const cpuOption = {
                    series: [{
                        type: 'gauge',
                        center: ['50%', '60%'],
                        radius: '80%',
                        startAngle: 200,
                        endAngle: -20,
                        min: 0,
                        max: 100,
                        splitNumber: 10,
                        itemStyle: {
                            color: '#00d8ff'
                        },
                        progress: {
                            show: true,
                            width: 15,
                            itemStyle: {
                                color: {
                                    type: 'linear',
                                    x: 0, y: 0, x2: 1, y2: 0,
                                    colorStops: [
                                        { offset: 0, color: '#00d8ff' },
                                        { offset: 1, color: '#00ff99' }
                                    ]
                                }
                            }
                        },
                        pointer: {
                            show: true,
                            length: '60%',
                            width: 8,
                            itemStyle: { color: '#00d8ff' }
                        },
                        axisLine: {
                            lineStyle: {
                                width: 15,
                                color: [
                                    [0.3, '#67e0e3'],
                                    [0.7, '#37a2da'],
                                    [1, '#fd666d']
                                ]
                            }
                        },
                        axisTick: { show: false },
                        splitLine: {
                            length: 12,
                            lineStyle: { width: 2, color: '#999' }
                        },
                        axisLabel: {
                            distance: 20,
                            color: '#999',
                            fontSize: 11
                        },
                        title: {
                            offsetCenter: [0, '90%'],
                            fontSize: 14,
                            color: '#00d8ff'
                        },
                        detail: {
                            valueAnimation: true,
                            fontSize: 24,
                            offsetCenter: [0, '35%'],
                            color: '#fff',
                            formatter: '{value}%'
                        },
                        data: [{
                            value: this.currentCpuUsage.toFixed(1),
                            name: 'CPU 使用率'
                        }]
                    }]
                };
                
                this.charts.gauge.setOption(cpuOption);
            }
        } catch (error) {
            console.error('获取系统负载失败:', error);
            // 失败时使用模拟数据
            const cpu = Math.floor(Math.random() * 30 + 30);
            const cpuOption = {
                series: [{
                    type: 'gauge',
                    center: ['50%', '60%'],
                    radius: '80%',
                    startAngle: 200,
                    endAngle: -20,
                    min: 0,
                    max: 100,
                    splitNumber: 10,
                    progress: { show: true, width: 15 },
                    axisLine: { lineStyle: { width: 15, color: [[0.3, '#67e0e3'], [0.7, '#37a2da'], [1, '#fd666d']] } },
                    axisTick: { show: false },
                    splitLine: { length: 12, lineStyle: { width: 2, color: '#999' } },
                    axisLabel: { distance: 20, color: '#999', fontSize: 11 },
                    title: { offsetCenter: [0, '90%'], fontSize: 14, color: '#00d8ff' },
                    detail: { valueAnimation: true, fontSize: 24, offsetCenter: [0, '35%'], color: '#fff', formatter: '{value}%' },
                    data: [{ value: cpu, name: 'CPU 使用率 (模拟)' }]
                }]
            };
            this.charts.gauge.setOption(cpuOption);
        }
    },
    startSystemLoadMonitor() {
        this.updateGaugeChart();
        this.systemLoadTimer = setInterval(() => {
            this.updateGaugeChart();
        }, 5000);
    },
    stopSystemLoadMonitor() {
        if (this.systemLoadTimer) {
            clearInterval(this.systemLoadTimer);
            this.systemLoadTimer = null;
        }
    },
    
    // --- 3D Logic ---
    init3D() {
        const radius = 260;
        const autoRotate = true;
        const rotateSpeed = -40;
        const cardWidth = 110;
        const cardHeight = 130;

        const obox = document.getElementById('drag-container');
        const ospin = document.getElementById('spin-container');
        if(!obox || !ospin) return;

        const aEle = ospin.getElementsByClassName('physics-card');

        ospin.style.width = cardWidth + 'px';
        ospin.style.height = cardHeight + 'px';

        const ground = document.getElementById('ground');
        if(ground) {
            ground.style.width = radius * 3 + 'px';
            ground.style.height = radius * 3 + 'px';
        }

        const init = (delayTime) => {
            for (let i = 0; i < aEle.length; i++) {
                aEle[i].style.transform = 'rotateY(' + (i * (360 / aEle.length)) + 'deg) translateZ(' + radius + 'px)';
                aEle[i].style.transition = 'transform 1s';
                aEle[i].style.transitionDelay = delayTime || (aEle.length - i) / 4 + 's';
                aEle[i].style.position = 'absolute';
                aEle[i].style.left = '0';
                aEle[i].style.top = '0';
                aEle[i].style.width = cardWidth + 'px';
                aEle[i].style.height = cardHeight + 'px';
                aEle[i].style.marginLeft = -(cardWidth / 2) + 'px';
                aEle[i].style.marginTop = -(cardHeight / 2) + 'px';
                aEle[i].style.transformStyle = 'preserve-3d';
                aEle[i].style.backfaceVisibility = 'visible';
            }
        };

        const applyTranform = (obj) => {
            if(this.tY > 180) this.tY = 180;
            if(this.tY < 0) this.tY = 0;
            obj.style.transform = 'rotateX(' + (-this.tY) + 'deg) rotateY(' + (this.tX) + 'deg)';
        };

        const playSpin = (yes) => {
            ospin.style.animationPlayState = yes ? 'running' : 'paused';
        };

        if (autoRotate) {
            const animationName = rotateSpeed > 0 ? 'spin' : 'spinRevert';
            ospin.style.animation = animationName + ' ' + Math.abs(rotateSpeed) + 's infinite linear';
        }

        init();

        let desX = 0, desY = 0;
        this.tX = 0;
        this.tY = 10;

        obox.onmousedown = (e) => {
            clearInterval(obox.timer);
            e = e || window.event;
            const sX = e.clientX;
            const sY = e.clientY;

            document.onmousemove = (e) => {
                e = e || window.event;
                const nX = e.clientX;
                const nY = e.clientY;
                desX = nX - sX;
                desY = nY - sY;
                this.tX += desX * 0.1;
                this.tY += desY * 0.1;
                applyTranform(obox);
            };

            document.onmouseup = () => {
                document.onmousemove = document.onmouseup = null;
                obox.timer = setInterval(() => {
                    desX *= 0.95;
                    desY *= 0.95;
                    this.tX += desX * 0.1;
                    this.tY += desY * 0.1;
                    applyTranform(obox);
                    playSpin(false);
                    if (Math.abs(desX) < 0.5 && Math.abs(desY) < 0.5) {
                        clearInterval(obox.timer);
                        playSpin(true);
                    }
                }, 17);
            };

            return false;
        };
    }
  }
};
</script>

<style scoped>
/* Reset & Base */
.dashboard-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    color: #fff;
    font-family: "Microsoft YaHei", sans-serif;
    z-index: 1000;
}

/* Header */
.dashboard-header {
    height: 10vh;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
}
.header-content h1 {
    font-size: 2rem;
    color: #fff;
    text-shadow: 0 0 10px #00d8ff;
    margin: 0;
    font-weight: bold;
}
.exit-btn {
    position: absolute;
    right: 20px;
    top: 20px;
    border: 1px solid #00d8ff;
    color: #00d8ff;
    padding: 5px 15px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    background: rgba(0,0,0,0.5);
}
.exit-btn:hover {
    background: #00d8ff;
    color: #000;
}

/* Content Layout */
.dashboard-content {
    height: 90vh;
    padding: 0 15px 20px 5px;
    display: flex;
    flex-direction: column;
}

/* Stats Row */
.stats-row {
    height: 12vh;
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
}
.stats-group {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
}
.stats-bg {
    display: flex;
    width: 90%;
    height: 100%;
    background: rgba(12, 22, 41, 0.5);
    align-items: center;
    justify-content: space-around;
}
.stat-item {
    display: flex;
    align-items: center;
    padding: 0 15px;
}
.stat-item img {
    width: 36px;
    height: 36px;
    margin-right: 8px;
}
.stat-info p {
    margin: 0;
    font-size: 0.75rem;
    color: #aaa;
}
.stat-info .count-num {
    font-size: 1.3rem;
    color: #00fbfe;
    font-weight: bold;
    text-shadow: 0 0 10px #00fbfe;
}

/* Main Grid */
.main-grid {
    flex: 1;
    display: flex;
    gap: 10px;
    overflow: hidden;
    position: relative;
    padding: 0 30px;
}
.col-left {
    flex: 0 0 26%;
    display: flex;
    flex-direction: column;
    gap: 10px;
    position: relative;
    z-index: 10;
    margin-left: -20px;
}
.col-right {
    flex: 0 0 26%;
    display: flex;
    flex-direction: column;
    gap: 10px;
    position: relative;
    z-index: 10;
}
.col-center {
    flex: 0 0 48%;
    display: flex;
    flex-direction: column;
    gap: 10px;
    z-index: 5;
    position: relative;
}

/* Public Panel Styles - 模仿车辆综合管控平台 */
.public {
    position: relative;
    border: 1px solid #20558b !important;
    border-right: 1px solid #20558b !important;
    background: rgba(1, 33, 44, 0.6);
    padding: 30px 15px 15px 15px;
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: visible !important;
    box-sizing: border-box;
}

.col-right .public {
    border: 1px solid #20558b !important;
    overflow: visible !important;
}

.min-title {
    position: absolute;
    top: 0;
    left: 15px;
    height: 30px;
    line-height: 30px;
    padding: 0 15px;
    background: rgba(0, 100, 150, 0.2);
    border-left: 3px solid #00d8ff;
    border-right: 3px solid #00d8ff;
    color: #00d8ff;
    font-size: 14px;
    font-weight: bold;
    z-index: 10;
}

.min-title:before, .min-title:after {
    content: "";
    position: absolute;
    top: 0;
    width: 10px;
    height: 100%;
    border-top: 2px solid #00d8ff;
    border-bottom: 2px solid #00d8ff;
}

.min-title:before {
    left: -5px;
    border-left: 2px solid #00d8ff;
}

.min-title:after {
    right: -5px;
    border-right: 2px solid #00d8ff;
}

.boxfoot {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 10px;
}

.public:before, .public:after, .boxfoot:before, .boxfoot:after {
    position: absolute;
    width: 10px;
    height: 10px;
    content: "";
    border-color: #02a6b5;
    border-style: solid;
}

.public:before { top: 0; left: 0; border-width: 2px 0 0 2px; }
.public:after { top: 0; right: 0; border-width: 2px 2px 0 0; }
.boxfoot:before { bottom: 0; left: 0; border-width: 0 0 2px 2px; }
.boxfoot:after { bottom: 0; right: 0; border-width: 0 2px 2px 0; }

.chart-container {
    flex: 1;
    width: 100%;
    min-height: 0;
    padding: 0;
    overflow: hidden;
}

/* Center Specific */
.center-top {
    flex: 2;
    position: relative;
    overflow: hidden;
}
.center-bottom {
    flex: 1;
}

/* 3D Spin Styles  */
#drag-container, #spin-container {
    position: absolute;
    display: flex;
    margin: auto;
    top: 48%;
    left: 50%;
    transform-style: preserve-3d;
    transform: rotateX(-10deg);
}

.physics-card {
    transform-style: preserve-3d;
    position: absolute;
    left: 0;
    top: 0;
    width: 110px;
    height: 130px;
    background: linear-gradient(135deg, rgba(0, 50, 120, 0.4) 0%, rgba(0, 100, 180, 0.3) 100%);
    border: 1px solid #00d8ff;
    box-shadow: 0 0 15px rgba(0, 216, 255, 0.4);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    border-radius: 8px;
}

.card-icon {
    font-size: 2.2rem;
    margin-bottom: 8px;
}

.physics-card h3 {
    font-size: 0.85rem;
    margin: 0;
    text-shadow: 0 0 8px rgba(0, 216, 255, 0.6);
}

#ground {
    width: 800px;
    height: 800px;
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translate(-50%, -50%) rotateX(90deg);
    background: -webkit-radial-gradient(center center, farthest-side, rgba(0, 216, 255, 0.15), transparent);
}

.center-person {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    height: 280px;
    width: auto;
    z-index: 100;
    pointer-events: none;
}

.base-plate {
    position: absolute;
    bottom: 10%;
    width: 60%;
    transform: rotateX(70deg);
    opacity: 0.4;
    pointer-events: none;
}

@keyframes spin {
    from { transform: rotateY(0deg); }
    to { transform: rotateY(360deg); }
}
@keyframes spinRevert {
    from { transform: rotateY(360deg); }
    to { transform: rotateY(0deg); }
}

/* List Styles */
.list-header {
    display: flex;
    padding: 8px 10px;
    color: #61d2f7;
    font-weight: bold;
    font-size: 13px;
    margin-top: 0;
    background: rgba(0, 216, 255, 0.1);
    border-bottom: 1px solid #20558b;
}
.list-container {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
    padding: 5px;
    min-height: 0;
}
.list-container::-webkit-scrollbar {
    width: 6px;
}
.list-container::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
}
.list-container::-webkit-scrollbar-thumb {
    background: rgba(0, 216, 255, 0.4);
    border-radius: 3px;
}
.list-container ul {
    list-style: none;
    padding: 0;
    margin: 0;
}
.list-container li {
    display: flex;
    padding: 8px 10px;
    border-bottom: 1px dashed rgba(32, 85, 139, 0.5);
    font-size: 12px;
    color: #ddd;
    align-items: flex-start;
}
.list-container li:nth-child(odd) {
    background: rgba(255, 255, 255, 0.02);
}
.list-container span {
    padding-right: 5px;
}
.col-1 { width: 48%; color: #fff; white-space: normal; word-wrap: break-word; word-break: break-all; line-height: 1.5; }
.col-2 { width: 22%; color: #00d8ff; white-space: nowrap; flex-shrink: 0; padding-top: 2px; text-align: center; }
.col-3 { width: 30%; text-align: right; color: #aaa; font-size: 11px; white-space: nowrap; flex-shrink: 0; padding-top: 2px; }

/* Radar & AI Recommend Wrappers */
.radar-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 5px;
    min-height: 0;
    overflow: hidden;
}

.radar-panel {
    flex: 0.8;
    min-height: 200px;
}

/* Header Layout */
.header-left {
    position: absolute;
    left: 20px;
    top: 50%;
    transform: translateY(-50%);
}

.header-right {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
}

.ai-recommend-btn {
    background: linear-gradient(135deg, #00d8ff 0%, #005bea 100%);
    border: none;
    font-weight: bold;
    box-shadow: 0 0 15px rgba(0, 216, 255, 0.5);
}

.ai-recommend-btn:hover {
    background: linear-gradient(135deg, #00b8df 0%, #004bca 100%);
    box-shadow: 0 0 20px rgba(0, 216, 255, 0.7);
}

/* AI Dialog Styles - 大屏风格 */
.ai-dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10000;
}

.ai-dialog-panel {
    width: 500px;
    max-height: 70vh;
    background: linear-gradient(180deg, rgba(0, 30, 60, 0.95) 0%, rgba(0, 20, 40, 0.98) 100%);
    border: 1px solid #00d8ff;
    border-radius: 8px;
    box-shadow: 0 0 30px rgba(0, 216, 255, 0.3);
    display: flex;
    flex-direction: column;
}

.ai-dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid rgba(0, 216, 255, 0.3);
    background: rgba(0, 216, 255, 0.1);
}

.ai-dialog-title {
    font-size: 16px;
    font-weight: bold;
    color: #00d8ff;
    display: flex;
    align-items: center;
    gap: 8px;
}

.ai-dialog-close {
    font-size: 24px;
    color: #00d8ff;
    cursor: pointer;
    transition: all 0.3s;
}

.ai-dialog-close:hover {
    color: #fff;
    transform: rotate(90deg);
}

.ai-dialog-body {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    max-height: 400px;
}

.ai-loading {
    text-align: center;
    padding: 60px 20px;
    color: #00d8ff;
}

.ai-loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid rgba(0, 216, 255, 0.3);
    border-top-color: #00d8ff;
    border-radius: 50%;
    margin: 0 auto 20px;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.ai-loading p {
    margin: 0;
    font-size: 14px;
}

.ai-result {
    font-size: 14px;
    line-height: 1.8;
    color: #fff;
}

.ai-result >>> h4 {
    color: #00d8ff;
    margin: 15px 0 10px;
    font-size: 15px;
}

.ai-result >>> p {
    margin: 8px 0;
    padding-left: 10px;
    border-left: 2px solid rgba(0, 216, 255, 0.5);
}

.ai-result >>> strong {
    color: #00d8ff;
}

.ai-dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 15px 20px;
    border-top: 1px solid rgba(0, 216, 255, 0.3);
}

.ai-btn {
    padding: 8px 20px;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.3s;
}

.ai-btn-default {
    background: transparent;
    border: 1px solid #00d8ff;
    color: #00d8ff;
}

.ai-btn-default:hover {
    background: rgba(0, 216, 255, 0.1);
}

.ai-btn-primary {
    background: linear-gradient(135deg, #00d8ff 0%, #005bea 100%);
    border: none;
    color: #fff;
}

.ai-btn-primary:hover {
    box-shadow: 0 0 15px rgba(0, 216, 255, 0.5);
}

.ai-btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Loading Overlay */
.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

.loading-spinner {
    color: #00d8ff;
    font-size: 1.5rem;
    font-weight: bold;
    text-align: center;
}

/* Responsive */
@media screen and (max-width: 1200px) {
    .main-grid { flex-direction: column; overflow-y: auto; }
    .col-left, .col-center, .col-right { flex: none; height: 50vh; }
    .dashboard-container { overflow-y: auto; }
}
</style>

