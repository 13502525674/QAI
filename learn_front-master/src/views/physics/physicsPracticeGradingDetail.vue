<template>
  <div class="physics-practice-grading-detail">
    <headerPage></headerPage>
    <div class="grading-detail-content">
      <div class="detail-header" v-if="record">
        <div class="paper-info">
          <h3>{{ paperInfo.paperTitle || '加载中...' }}</h3>
          <div class="info-row">
            <span><i class="el-icon-user"></i> 学生：{{ getStudentName() }}</span>
            <span><i class="el-icon-time"></i> 提交时间：{{ formatDate(record.submittedAt) }}</span>
            <span><i class="el-icon-document"></i> 总题数：{{ questions.length }}</span>
          </div>
        </div>
        <div class="grading-actions">
          <el-button 
            type="primary" 
            @click="saveGrading"
            :loading="saving"
          >
            保存评分
          </el-button>
          <el-button 
            type="success" 
            @click="submitGrading"
            :loading="submitting"
          >
            提交评分
          </el-button>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </div>
      
      <div class="grading-body" v-if="questions.length > 0">
        <div 
          v-for="(question, index) in questions" 
          :key="index" 
          class="question-item"
        >
          <div class="question-header">
            <div class="question-info">
              <h4>第 {{ index + 1 }} 题 <span class="question-type-tag">{{ getQuestionTypeText(question.type) }}</span></h4>
              <div class="question-meta">
                <span>满分：{{ getMaxScore(question) }}分</span>
                <span class="score-display">当前得分：{{ question.score || 0 }}分</span>
              </div>
            </div>
          </div>
          
          <div class="question-content">
            <div class="question-text" v-html="formatQuestionContent(question)"></div>
          </div>
          
          <div class="student-answer">
            <h5>学生答案：</h5>
            <div v-if="question.type === 0">
              <el-radio-group v-model="question.userAnswer" disabled>
                <div 
                  v-for="(option, optIndex) in parseOptions(question)" 
                  :key="optIndex" 
                  class="option-item"
                  :class="{ 'selected': question.userAnswer === option.value }"
                >
                  <el-radio :label="option.value">
                    <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
                    <span class="option-text" v-html="formatAnswerContent(option.option)"></span>
                  </el-radio>
                </div>
              </el-radio-group>
            </div>
            <div v-else-if="question.type === 3">
              <!-- 判断题显示 -->
              <el-radio-group v-model="question.userAnswer" disabled>
                <div class="option-item" :class="{ 'selected': question.userAnswer === '正确' }">
                  <el-radio :label="'正确'">
                    <span class="option-label">正确</span>
                  </el-radio>
                </div>
                <div class="option-item" :class="{ 'selected': question.userAnswer === '错误' }">
                  <el-radio :label="'错误'">
                    <span class="option-label">错误</span>
                  </el-radio>
                </div>
              </el-radio-group>
            </div>
            <div v-else>
              <el-input 
                type="textarea" 
                :rows="question.type === 2 ? 6 : 4" 
                :value="question.userAnswer" 
                disabled
                class="answer-textarea"
              ></el-input>
            </div>
          </div>
          
          <div class="standard-answer" v-if="question.standardAnswer">
            <h5>标准答案：</h5>
            <div class="answer-text" v-html="formatAnswerContent(question.standardAnswer)"></div>
          </div>
          
          <div class="grading-area">
            <h5>评分区域：</h5>
            <div class="score-input">
              <span>得分：</span>
              <el-input-number 
                v-model="question.score" 
                :min="0" 
                :max="getMaxScore(question)"
                :precision="1"
                :step="0.5"
                size="small"
              ></el-input-number>
              <span>/ {{ getMaxScore(question) }}分</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="total-comment" v-if="questions.length > 0">
        <el-card>
          <div class="comment-header">
            <h4>教师评语</h4>
          </div>
          <div class="comment-content">
            <el-input 
              type="textarea" 
              :rows="4" 
              v-model="overallComment"
              placeholder="请输入教师评语（可选）"
            ></el-input>
          </div>
        </el-card>
      </div>
      
      <div class="total-score" v-if="questions.length > 0">
        <el-card>
          <div class="score-info">
            <span>总分：{{ totalScore }}分</span>
          </div>
        </el-card>
      </div>
      
      <div v-loading="loading" v-if="!record || questions.length === 0" class="loading-container">
        加载中...
      </div>
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getPhysicsPracticeRecordsById, updatePhysicsPracticeRecord, getPhysicsQuestionPaperById } from '../../api/api'
import { getQuestionTypeText as getQuestionTypeTextUtil } from '../../utils/questionType'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsPracticeGradingDetail",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      record: null,
      paperInfo: {},
      questions: [],
      loading: false,
      saving: false,
      submitting: false,
      userInfo: null,
      overallComment: ''
    }
  },
  computed: {
    totalScore() {
      return this.questions.reduce((sum, q) => sum + (q.score || 0), 0)
    }
  },
  methods: {
    async loadRecord() {
      this.loading = true;
      try {
        const response = await getPhysicsPracticeRecordsById({ id: this.$route.query.id });
        
        if (response.code === 1000) {
          this.record = response.data;
          
          await this.loadPaperInfo();
          this.parseQuestions();
        } else {
          this.$message.error(response.message || '获取练习记录失败');
        }
      } catch (error) {
        console.error('获取练习记录失败:', error);
        this.$message.error('获取练习记录失败');
      } finally {
        this.loading = false;
      }
    },
    
    async loadPaperInfo() {
      if (!this.record || !this.record.paperId) return;
      
      try {
        const response = await getPhysicsQuestionPaperById({ id: this.record.paperId });
        
        if (response.code === 1000) {
          this.paperInfo = response.data || {};
        }
      } catch (error) {
        console.error('获取试卷信息失败:', error);
      }
    },
    
    parseQuestions() {
      if (!this.record || !this.record.userAnswers) {
        this.questions = [];
        return;
      }
      
      try {
        // 解析学生答案
        const userAnswers = JSON.parse(this.record.userAnswers);
        
        // 解析试卷题目内容
        const questionContent = this.paperInfo.questionContent ? JSON.parse(this.paperInfo.questionContent) : [];
        
        // 解析评分详情
        let gradingDetails = {};
        let questionsGrading = null;
        if (this.record.gradingDetails) {
          gradingDetails = JSON.parse(this.record.gradingDetails);
          
          // 兼容两种格式：新格式（对象包含questions数组）和旧格式（直接是数组）
          if (Array.isArray(gradingDetails)) {
            // 旧格式：直接是数组
            questionsGrading = gradingDetails;
          } else if (gradingDetails.questions && Array.isArray(gradingDetails.questions)) {
            // 新格式：对象包含questions数组
            questionsGrading = gradingDetails.questions;
            if (gradingDetails.overallComment) {
              this.overallComment = gradingDetails.overallComment;
            }
          }
        }
        
        // 处理每个题目的显示
        this.questions = questionContent.map((questionData, index) => {
          const answer = userAnswers[index] || {};
          const questionGrading = questionsGrading ? questionsGrading[index] : {};
          
          // 处理学生答案，确保格式正确
          let processedAnswer = answer.userAnswer || '';
          
          // 对于判断题，处理true/false字符串
          if (questionData.type === 3) {
            if (processedAnswer === 'true') processedAnswer = '正确';
            if (processedAnswer === 'false') processedAnswer = '错误';
          }
          
          // 对于选择题，确保答案格式正确
          if (questionData.type === 0 && typeof processedAnswer === 'string') {
            // 选择题答案应该是A/B/C/D等字母
            processedAnswer = processedAnswer.toUpperCase();
          }
          
          const maxScore = questionGrading.maxScore && questionGrading.maxScore > 0 
            ? questionGrading.maxScore 
            : this.getMaxScore(questionData);
          
          return {
            ...questionData,
            userAnswer: processedAnswer,
            score: questionGrading.score || 0,
            maxScore: maxScore
          };
        });
      } catch (error) {
        console.error('解析题目失败:', error);
        this.questions = [];
      }
    },
    
    getMaxScore(question) {
      if (question.maxScore && question.maxScore > 0) {
        return question.maxScore;
      }
      const type = question.type;
      switch(type) {
        case 0: return 5;
        case 1: return 8;
        case 2: return 10;
        case 3: return 5;
        case 4: return 10;
        default: return 10;
      }
    },
    
    getQuestionTypeText(type) {
      return getQuestionTypeTextUtil(type)
    },
    
    getStudentName() {
      return this.record && this.record.studentName ? this.record.studentName : '未知';
    },
    
    formatQuestionContent(question) {
      let content = question.title || question.content || '';
      
      content = content
        .replace(/v₀/g, 'v<sub>0</sub>')
        .replace(/v₁/g, 'v<sub>1</sub>')
        .replace(/v₂/g, 'v<sub>2</sub>')
        .replace(/²/g, '<sup>2</sup>')
        .replace(/³/g, '<sup>3</sup>')
        .replace(/\n/g, '<br>');
      
      return content;
    },
    
    formatAnswerContent(answer) {
      if (!answer) return '';
      
      let content = answer;
      
      content = content
        .replace(/v₀/g, 'v<sub>0</sub>')
        .replace(/v₁/g, 'v<sub>1</sub>')
        .replace(/v₂/g, 'v<sub>2</sub>')
        .replace(/²/g, '<sup>2</sup>')
        .replace(/³/g, '<sup>3</sup>')
        .replace(/\n/g, '<br>');
      
      return content;
    },
    
    parseOptions(question) {
      const options = [];
      
      if (question.options) {
        if (Array.isArray(question.options)) {
          // 处理数组格式的选项
          question.options.forEach((option, index) => {
            if (typeof option === 'object' && option !== null) {
              options.push({
                value: String.fromCharCode(65 + index), // A, B, C, D
                option: this.formatAnswerContent(option.text || option.option || option)
              });
            } else {
              options.push({
                value: String.fromCharCode(65 + index), // A, B, C, D
                option: this.formatAnswerContent(option)
              });
            }
          });
        } else if (typeof question.options === 'object') {
          // 处理对象格式的选项
          Object.keys(question.options).forEach(key => {
            options.push({
              value: key,
              option: this.formatAnswerContent(question.options[key])
            });
          });
        }
      }
      
      return options;
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },
    
    async saveGrading() {
      this.saving = true;
      try {
        const gradingData = {
          id: this.record.id,
          questions: this.questions,
          totalScore: this.totalScore,
          overallComment: this.overallComment,
          status: 'submitted'
        };
        
        const response = await updatePhysicsPracticeRecord(gradingData);
        
        if (response.code === 1000) {
          this.$message.success('保存成功！');
        } else {
          this.$message.error(response.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败:', error);
        this.$message.error('保存失败');
      } finally {
        this.saving = false;
      }
    },
    
    async submitGrading() {
      this.$confirm('确定要提交评分吗？提交后学生将能看到评分结果。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.submitting = true;
        try {
          const gradingData = {
            id: this.record.id,
            questions: this.questions,
            totalScore: this.totalScore,
            overallComment: this.overallComment,
            status: 'graded'
          };
          
          const response = await updatePhysicsPracticeRecord(gradingData);
          
          if (response.code === 1000) {
            this.$message.success('提交成功！');
            this.$router.back();
          } else {
            this.$message.error(response.message || '提交失败');
          }
        } catch (error) {
          console.error('提交失败:', error);
          this.$message.error('提交失败');
        } finally {
          this.submitting = false;
        }
      }).catch(() => {});
    }
  },
  
  mounted() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
    const userInfo = JSON.parse(window.localStorage.getItem("user_info"));
    if (userInfo) {
      this.userInfo = userInfo;
    }
    this.loadRecord();
  }
}
</script>

<style scoped>
.physics-practice-grading-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.grading-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.detail-header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.paper-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
}

.info-row {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.grading-actions {
  display: flex;
  gap: 10px;
}

.grading-body {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.question-item {
  border-bottom: 1px solid #eee;
  padding: 20px 0;
}

.question-item:last-child {
  border-bottom: none;
}

.question-header {
  margin-bottom: 15px;
}

.question-info h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.question-type-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #409EFF;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 10px;
}

.question-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.score-display {
  color: #409EFF;
  font-weight: bold;
}

.question-content {
  margin-bottom: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.student-answer,
.standard-answer,
.grading-area {
  margin-bottom: 15px;
}

.student-answer h5,
.standard-answer h5,
.grading-area h5 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
}

.option-item {
  padding: 10px;
  margin: 5px 0;
  border: 1px solid #eee;
  border-radius: 4px;
}

.option-item.selected {
  background: #e6f7ff;
  border-color: #1890ff;
}

.option-label {
  font-weight: bold;
  margin-right: 10px;
}

.answer-textarea {
  width: 100%;
}

.answer-text {
  padding: 15px;
  background: #f0f9ff;
  border-radius: 4px;
  color: #333;
}

.score-input {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.total-score {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.total-comment {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.total-comment .comment-header h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.total-comment .comment-content {
  color: #666;
}

.score-info {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.loading-container {
  text-align: center;
  padding: 100px 0;
  color: #999;
}

@media (max-width: 768px) {
  .grading-detail-content {
    padding: 10px;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 5px;
  }
  
  .grading-actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
