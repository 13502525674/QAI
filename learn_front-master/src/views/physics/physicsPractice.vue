<template>
  <div class="physics-practice">
    <headerPage></headerPage>
    <div class="physics-practice-content">
      <div class="physics-practice-header">
        <div class="header-top">
          <h2>物理智能题库</h2>
        </div>
        <div class="physics-branches">
          <el-button 
            v-for="branch in branches" 
            :key="branch.value"
            :type="selectedBranch === branch.value ? 'primary' : 'info'"
            size="small"
            @click="selectBranch(branch.value)"
          >
            {{ branch.label }}
          </el-button>
        </div>
      </div>
      
      <div class="physics-papers-container">
        <div v-if="papers.length === 0" class="no-papers">
          暂无{{ selectedBranchText }}相关的试卷
        </div>
        <div v-else class="papers-list">
          <div 
            v-for="(paper, index) in papers" 
            :key="index" 
            class="paper-card"
          >
            <div class="paper-info">
              <h3>{{ paper.paperTitle }}</h3>
              <div class="paper-meta">
                <span>题数：{{ paper.totalQuestions || 0 }}</span>
                <span>发布时间：{{ formatDate(paper.createTime) }}</span>
                <span>分支：{{ getBranchText(paper.subjectBranch) }}</span>
              </div>
              <div class="paper-actions">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="startPractice(paper.id)"
                >
                  开始练习
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页控件 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getPhysicsQuestionPaperPage, getPhysicsUserPapers, getPhysicsPapersByBranch } from '../../api/api'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsPractice",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      papers: [],
      branches: [
        { label: '全部', value: 'all' },
        { label: '运动学', value: 'kinematics' },
        { label: '力学', value: 'mechanics' },
        { label: '电学', value: 'electrics' },
        { label: '光学', value: 'optics' },
        { label: '热学', value: 'thermodynamics' }
      ],
      selectedBranch: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  computed: {
    selectedBranchText() {
      const branch = this.branches.find(b => b.value === this.selectedBranch);
      return branch ? branch.label : '';
    }
  },
  methods: {
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadPapers();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadPapers();
    },
    
    async loadPapers() {
      this.loading = true;
      try {
        const params = {
          pageNumber: this.currentPage,
          pageSize: this.pageSize
        };
        
        if (this.selectedBranch && this.selectedBranch !== 'all') {
          params.subjectBranch = this.selectedBranch;
        }
        
        const response = await getPhysicsQuestionPaperPage(params);
        
        if (response.code === 1000) {
          this.papers = response.data.records || [];
          this.total = response.data.total || 0;
        } else {
          this.$message.error(response.message || '获取试卷失败');
        }
      } catch (error) {
        console.error('获取试卷失败:', error);
        this.$message.error('获取试卷失败');
      } finally {
        this.loading = false;
      }
    },
    
    selectBranch(branch) {
      this.selectedBranch = branch;
      this.loadPapers();
    },
    
    getBranchText(branchValue) {
      const branch = this.branches.find(b => b.value === branchValue);
      return branch ? branch.label : branchValue;
    },
    
    startPractice(paperId) {
      this.$router.push(`/physicsPracticeDetail?id=${paperId}`)
    },
    
    goToUpload() {
      this.$router.push('/physicsDocumentUpload')
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    }
  },
  
  mounted() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
    
    const branch = this.$route.query.branch;
    if (branch) {
      const branchMap = {
        '力学': 'mechanics',
        '电学': 'electrics',
        '热学': 'thermodynamics',
        '光学': 'optics',
        '运动学': 'kinematics'
      };
      if (branchMap[branch]) {
        this.selectedBranch = branchMap[branch];
      }
    }
    
    this.loadPapers();
  }
}
</script>

<style scoped>
.physics-practice {
  min-height: 100vh;
  background: linear-gradient(180deg, #fef7f5 0%, #fff5f0 100%);
}

.physics-practice-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.physics-practice-header {
  background: #ffffff;
  padding: 36px 40px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(255, 107, 107, 0.1);
  margin-bottom: 28px;
  border: 1px solid rgba(255, 107, 107, 0.08);
  position: relative;
  overflow: hidden;
}

.physics-practice-header::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -20%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.05) 0%, transparent 70%);
  border-radius: 50%;
}

.physics-practice-header::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -20%;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(255, 159, 107, 0.05) 0%, transparent 70%);
  border-radius: 50%;
}

.physics-practice-header h2 {
  margin: 0 0 24px 0;
  color: #2d3748;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.physics-branches {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.physics-branches .el-button {
  margin-bottom: 10px;
  padding: 12px 26px;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.2px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid #f0e0dc;
  background: #faf9f8;
  color: #6b7280;
}

.physics-branches .el-button:hover {
  background: #fff5f0;
  border-color: #ff6b6b;
  color: #ff6b6b;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.2);
}

.physics-branches .el-button--primary {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
}

.physics-branches .el-button--primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(255, 107, 107, 0.4);
}

.no-papers {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.papers-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 28px;
}

.paper-card {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.45s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f5f0ee;
  position: relative;
}

.paper-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #ff6b6b 0%, #ff9f6b 100%);
  opacity: 0;
  transition: opacity 0.35s ease;
}

.paper-card:hover {
  transform: translateY(-10px) scale(1.015);
  box-shadow: 0 18px 45px rgba(255, 107, 107, 0.18);
  border-color: #fde0d8;
}

.paper-card:hover::before {
  opacity: 1;
}

.paper-info {
  padding: 28px;
}

.paper-info h3 {
  margin: 0 0 18px 0;
  color: #2d3748;
  font-size: 19px;
  font-weight: 600;
  line-height: 1.5;
  letter-spacing: -0.2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.paper-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.paper-meta span {
  color: #78716c;
  font-size: 14px;
  line-height: 1.7;
  display: flex;
  align-items: center;
  font-weight: 400;
  letter-spacing: 0.1px;
}

.paper-meta span::before {
  content: '';
  width: 7px;
  height: 7px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%);
  border-radius: 50%;
  margin-right: 10px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.3);
}

.paper-actions {
  text-align: right;
}

.paper-actions .el-button {
  padding: 11px 32px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.2px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%);
  border: none;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.3);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.paper-actions .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(255, 107, 107, 0.4);
}

.paper-actions .el-button:active {
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .papers-list {
    grid-template-columns: 1fr;
  }
  
  .physics-branches {
    justify-content: center;
  }
}
</style>