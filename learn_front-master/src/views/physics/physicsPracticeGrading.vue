<template>
  <div class="physics-practice-grading">
    <headerPage></headerPage>
    <div class="grading-content">
      <div class="grading-header">
        <h2>练习批改</h2>
      </div>
      
      <div class="grading-filter">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="试卷标题">
            <el-input 
              v-model="filterForm.paperTitle" 
              placeholder="请输入试卷标题"
              @keyup.enter.native="loadRecords"
            ></el-input>
          </el-form-item>
          <el-form-item label="学生姓名">
            <el-input 
              v-model="filterForm.studentName" 
              placeholder="请输入学生姓名"
              @keyup.enter.native="loadRecords"
            ></el-input>
          </el-form-item>
          <el-form-item label="批改状态">
            <el-select v-model="filterForm.status" placeholder="请选择批改状态" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="待批改" value="submitted"></el-option>
              <el-option label="已批改" value="graded"></el-option>
            </el-select>
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
          :empty-text="loading ? '加载中...' : '暂无数据'"
        >
          <el-table-column prop="paperTitle" label="试卷标题" width="250">
            <template slot-scope="scope">
              {{ getPaperTitle(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="studentName" label="学生姓名" width="120">
            <template slot-scope="scope">
              {{ getStudentName(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="submittedAt" label="提交时间" width="180">
            <template slot-scope="scope">
              <span>{{ formatDate(scope.row.submittedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag 
                :type="scope.row.status === 'graded' ? 'success' : 'warning'"
                size="small"
              >
                {{ scope.row.status === 'graded' ? '已批改' : '待批改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="100">
            <template slot-scope="scope">
              <span v-if="scope.row.status === 'graded'">
                {{ scope.row.score !== null ? scope.row.score : '-' }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button 
                size="mini" 
                type="primary"
                @click="toGrade(scope.row)"
              >
                批改
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="!loading && records.length === 0" class="no-data">
          <el-empty description="暂无练习记录"></el-empty>
        </div>
      </div>
      
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
import { getPhysicsGradingList, updatePhysicsPracticeRecord } from '../../api/api'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsPracticeGrading",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      records: [],
      loading: false,
      filterForm: {
        paperTitle: '',
        studentName: '',
        status: ''
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
          pageNumber: this.currentPage,
          pageSize: this.pageSize
        };
        
        if (this.filterForm.paperTitle !== '') {
          params.paperTitle = this.filterForm.paperTitle;
        }
        if (this.filterForm.studentName !== '') {
          params.studentName = this.filterForm.studentName;
        }
        if (this.filterForm.status !== '') {
          params.status = this.filterForm.status;
        }
        
        const response = await getPhysicsGradingList(params);
        
        if (response && response.code === 1000) {
          this.records = response.data.records || [];
          this.total = response.data.total || 0;
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
    
    getPaperTitle(record) {
      return record.paperTitle || '未知试卷';
    },
    
    getStudentName(record) {
      return record.studentName || '未知学生';
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },
    
    async toGrade(record) {
      this.$router.push(`/physicsPracticeGradingDetail?id=${record.id}`);
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadRecords();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadRecords();
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
.physics-practice-grading {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.grading-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.grading-header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.grading-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.grading-filter {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.records-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .grading-content {
    padding: 10px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table th, .el-table td {
    padding: 6px 0;
  }
}
</style>
