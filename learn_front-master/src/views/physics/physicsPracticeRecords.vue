<template>
  <div class="physics-practice-records">
    <headerPage></headerPage>
    <div class="records-content">
      <div class="records-header">
        <h2>我的练习记录</h2>
      </div>
      
      <div class="records-filter">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="试卷名称">
            <el-input 
              v-model="filterForm.paperTitle" 
              placeholder="请输入试卷名称"
              @keyup.enter.native="loadRecords"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRecords">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="records-list">
        <el-table 
          :data="records" 
          stripe 
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="paperTitle" label="试卷名称">
          </el-table-column>
          <el-table-column prop="subjectBranch" label="物理分支" align="center">
            <template slot-scope="scope">
              <span>{{ getBranchText(scope.row.subjectBranch) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="submittedAt" label="提交时间" align="center">
            <template slot-scope="scope">
              <span>{{ formatDate(scope.row.submittedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template slot-scope="scope">
              <span 
                class="status-tag"
                :class="scope.row.status === 'graded' ? 'status-graded' : 'status-pending'"
              >
                <i :class="scope.row.status === 'graded' ? 'el-icon-circle-check' : 'el-icon-time'"></i>
                {{ scope.row.status === 'graded' ? '已批改' : '待批改' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" align="center">
            <template slot-scope="scope">
              <span v-if="scope.row.status === 'graded'">
                {{ scope.row.score !== null ? scope.row.score : '-' }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template slot-scope="scope">
              <div class="action-btns">
                <el-button 
                  size="mini" 
                  type="primary"
                  icon="el-icon-view"
                  class="btn-view"
                  @click="viewDetails(scope.row)"
                >
                  查看详情
                </el-button>
                <el-button 
                  size="mini" 
                  type="success"
                  icon="el-icon-refresh"
                  class="btn-redo"
                  @click="redoPractice(scope.row)"
                >
                  重新练习
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getPhysicsUserRecordsWithDetails, getPhysicsPracticeRecordsById } from '../../api/api'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsPracticeRecords",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      records: [],
      loading: false,
      filterForm: {
        paperTitle: ''
      },
      currentPage:1,
      pageSize: 10,
      total: 0,
      userInfo: null
    }
  },
  methods: {
    async loadRecords() {
      this.loading = true;
      try {
        const params = {
          userId: this.userInfo ? this.userInfo.id : ''
        };
        
        if (this.filterForm.paperTitle !== '') {
          params.paperTitle = this.filterForm.paperTitle;
        }
        
        const response = await getPhysicsUserRecordsWithDetails(params);
        
        if (response && response.code === 1000) {
          this.records = response.data || [];
          this.total = response.data.length || 0;
        } else {
          this.$message.error(response && response.message || '获取练习记录失败');
        }
      } catch (error) {
        console.error('获取练习记录失败:', error);
        this.$message.error('获取练习记录失败');
      } finally {
        this.loading = false;
      }
    },
    
    getBranchText(branchValue) {
      const branches = [
        { label: '运动学', value: 'kinematics' },
        { label: '力学', value: 'mechanics' },
        { label: '电学', value: 'electrics' },
        { label: '光学', value: 'optics' },
        { label: '热学', value: 'thermodynamics' }
      ];
      const branch = branches.find(b => b.value === branchValue);
      return branch ? branch.label : branchValue;
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },
    
    viewDetails(record) {
      this.$router.push(`/physicsPracticeRecordDetail?id=${record.id}`);
    },
    
    redoPractice(record) {
      this.$router.push(`/physicsPracticeDetail?id=${record.paperId}`);
    }
  },
  
  mounted() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
    const userInfo = JSON.parse(window.localStorage.getItem("user_info"));
    if (userInfo && userInfo !== null) {
      this.userInfo = userInfo;
    }
    this.loadRecords();
  }
}
</script>

<style scoped>
.physics-practice-records {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0fdfa 50%, #ecfeff 100%);
  position: relative;
}

.physics-practice-records::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(20, 184, 166, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.records-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.records-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px 45px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.12);
  margin-bottom: 30px;
  border: 2px solid rgba(59, 130, 246, 0.1);
  position: relative;
  overflow: hidden;
}

.records-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #14b8a6, #06b6d4);
}

.records-header h2 {
  margin: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.records-filter {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 26px 35px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  margin-bottom: 30px;
  border: 2px solid rgba(59, 130, 246, 0.08);
}

.records-filter .el-form-item {
  margin-bottom: 0;
}

.records-filter .el-input {
  width: 280px;
}

.records-filter .el-input__inner {
  border-radius: 10px;
  border: 2px solid #e0f2fe;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8fafc;
}

.records-filter .el-input__inner:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  background: #ffffff;
}

.records-filter .el-button--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  border: none;
  border-radius: 10px;
  padding: 10px 28px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.records-filter .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.35);
}

.records-list {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.1);
  padding: 30px;
  border: 2px solid rgba(59, 130, 246, 0.08);
}

.records-list .el-table {
  border-radius: 12px;
  overflow: hidden;
}

.records-list .el-table thead {
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdfa 100%);
}

.records-list .el-table th {
  background: transparent;
  color: #1e40af;
  font-weight: 600;
  font-size: 14px;
  padding: 16px 0;
  border-bottom: 2px solid #e0f2fe;
}

.records-list .el-table td {
  padding: 18px 0;
  color: #475569;
  border-bottom: 1px solid #f1f5f9;
}

.records-list .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #f8fafc;
}

.records-list .el-table__row:hover > td {
  background-color: #eff6ff !important;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.status-graded {
  background: linear-gradient(135deg, #dcfce7, #d1fae5);
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.status-pending {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #b45309;
  border: 1px solid #fde68a;
}

.action-btns {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.action-btns .el-button {
  width: 110px;
}

.records-list .el-button--mini {
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 600;
  font-size: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
}

.action-btns .el-button--success {
  margin-left: -10px;
}

.records-list .btn-view {
  border: none;
  box-shadow: 0 3px 10px rgba(59, 130, 246, 0.25);
  margin-right: 8px;
}

.records-list .btn-view:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(59, 130, 246, 0.4);
}

.records-list .btn-redo {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  border: none;
  box-shadow: 0 3px 10px rgba(16, 185, 129, 0.25);
}

.records-list .btn-redo:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(16, 185, 129, 0.4);
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .records-content {
    padding: 10px;
  }
  
  .records-header {
    padding: 24px;
  }
  
  .records-header h2 {
    font-size: 24px;
  }
  
  .records-list {
    padding: 16px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table th, .el-table td {
    padding: 12px 8px;
  }
}
</style>