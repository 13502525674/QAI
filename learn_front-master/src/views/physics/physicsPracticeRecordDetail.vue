<template>
  <div class="physics-practice-record-detail">
    <headerPage></headerPage>
    <div class="record-detail-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated></el-skeleton>
      </div>
      <div v-else-if="record" class="detail-container">
        <div class="detail-header">
          <div class="paper-info">
            <h2>{{ record.paperTitle || '未知试卷' }}</h2>
            <div class="paper-meta">
              <span>提交时间：{{ formatDate(record.submittedAt) }}</span>
              <span v-if="record.gradedAt">批改时间：{{ formatDate(record.gradedAt) }}</span>
            </div>
          </div>
          <div class="score-info">
            <el-tag 
              :type="record.status === 'graded' ? 'success' : 'warning'" 
              size="large"
            >
              {{ record.status === 'graded' ? '已批改' : '待批改' }}
            </el-tag>
            <div v-if="record.status === 'graded'" class="score-display">
              <span class="score-label">得分：</span>
              <span class="score-value">{{ record.score || 0 }}</span>
              <span class="score-total">/ {{ totalScore || 100 }}分</span>
            </div>
          </div>
        </div>

        <div class="questions-container">
          <div 
            v-for="(question, index) in questions" 
            :key="index" 
            class="question-item"
          >
            <div class="question-header">
              <span class="question-number">第{{ index + 1 }}题</span>
              <el-tag :type="getQuestionTypeColor(question.type)" size="small">
                {{ getQuestionTypeText(question.type) }}
              </el-tag>
              <span class="question-score">{{ getMaxScore(question) }}分</span>
            </div>
            
            <div class="question-content" v-html="formatQuestionContent(question)"></div>
            
            <div class="answer-section">
              <div class="answer-item">
                <span class="answer-label">你的答案：</span>
                <div class="answer-content">
                  <span v-if="question.type === 0 || question.type === 3">
                    {{ getAnswerText(question, question.userAnswer) }}
                  </span>
                  <span v-else class="text-answer">{{ question.userAnswer || '未作答' }}</span>
                </div>
              </div>
              
              <div v-if="record.status === 'graded'" class="answer-item grading-result">
                <span class="answer-label">评分结果：</span>
                <div class="grading-content">
                  <div class="question-score-display">
                    <span>得分：</span>
                    <el-tag type="success">{{ question.score || 0 }}</el-tag>
                    <span>/ {{ getMaxScore(question) }}分</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="overall-comment" v-if="record.status === 'graded' && overallComment">
          <el-card>
            <div class="comment-header">
              <h4>教师评语</h4>
            </div>
            <div class="comment-content">
              <p>{{ overallComment }}</p>
            </div>
          </el-card>
        </div>

        <div class="detail-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="redoPractice">重新练习</el-button>
        </div>
      </div>
      <div v-else class="no-data">
        <el-empty description="未找到练习记录"></el-empty>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { getPhysicsPracticeRecordsById } from '../../api/api'
import { getQuestionTypeText as getQuestionTypeTextUtil, getQuestionTypeColor as getQuestionTypeColorUtil } from '../../utils/questionType'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsPracticeRecordDetail",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      recordId: '',
      record: null,
      questions: [],
      loading: false,
      totalScore: 0,
      overallComment: ''
    }
  },
  methods: {
    async loadRecord() {
      this.loading = true;
      try {
        const response = await getPhysicsPracticeRecordsById({ id: this.recordId });
        if (response && response.code === 1000) {
          this.record = response.data;
          
          if (this.record.questionContent) {
            const questionContent = JSON.parse(this.record.questionContent) || [];
            
            if (this.record.userAnswers) {
              const userAnswers = JSON.parse(this.record.userAnswers) || [];
              
              this.questions = questionContent.map((question, index) => {
                const userAnswerData = userAnswers[index] || {};
                return {
                  ...question,
                  userAnswer: userAnswerData.userAnswer,
                  maxScore: this.getMaxScore(question)
                };
              });
            } else {
              this.questions = questionContent.map((question) => {
                return {
                  ...question,
                  maxScore: this.getMaxScore(question)
                };
              });
            }
            
            if (this.record.gradingDetails) {
              const gradingDetails = JSON.parse(this.record.gradingDetails) || {};
              
              // 兼容两种格式：新格式（对象包含questions数组）和旧格式（直接是数组）
              let questionsGrading = null;
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
              
              if (questionsGrading) {
                this.questions.forEach((question, index) => {
                  const gradingQuestion = questionsGrading[index];
                  if (gradingQuestion) {
                    question.score = gradingQuestion.score;
                    if (gradingQuestion.maxScore && gradingQuestion.maxScore > 0) {
                      question.maxScore = gradingQuestion.maxScore;
                    }
                  }
                });
              }
            }
            
            this.totalScore = this.questions.reduce((sum, q) => sum + this.getMaxScore(q), 0);
          }
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
    
    getQuestionTypeColor(type) {
      return getQuestionTypeColorUtil(type)
    },
    
    getAnswerText(question, answer) {
      if (answer === null || answer === undefined || answer === '') return '未作答';
      
      if (question.type === 0) {
        // 选择题：显示选项字母和内容，格式如：A：是滴是滴
        const options = this.parseOptions(question);
        const selectedOption = options.find(opt => opt.value === answer);
        if (selectedOption) {
          return `${answer}：${selectedOption.option}`;
        }
        return `${answer}：未知`;
      } else if (question.type === 3) {
        // 判断题
        if (answer === 'true' || answer === '正确') return '正确';
        if (answer === 'false' || answer === '错误') return '错误';
        return answer;
      }
      return answer;
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
                option: option.text || option.option || option
              });
            } else {
              options.push({
                value: String.fromCharCode(65 + index), // A, B, C, D
                option: option
              });
            }
          });
        } else if (typeof question.options === 'object') {
          // 处理对象格式的选项
          Object.keys(question.options).forEach(key => {
            options.push({
              value: key,
              option: question.options[key]
            });
          });
        }
      }
      
      return options;
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
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },
    
    goBack() {
      this.$router.back();
    },
    
    redoPractice() {
      if (this.record && this.record.paperId) {
        this.$router.push(`/physicsPracticeDetail?id=${this.record.paperId}`);
      }
    }
  },
  
  mounted() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
    this.recordId = this.$route.query.id;
    if (this.recordId) {
      this.loadRecord();
    } else {
      this.$message.error('缺少记录ID');
      this.$router.push('/physicsPracticeRecords');
    }
  }
}
</script>

<style scoped>
.physics-practice-record-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0fdfa 50%, #ecfeff 100%);
  position: relative;
}

.physics-practice-record-detail::before {
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

.record-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.loading-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.12);
  border: 2px solid rgba(59, 130, 246, 0.1);
}

.detail-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(59, 130, 246, 0.15);
  overflow: hidden;
  border: 2px solid rgba(59, 130, 246, 0.1);
}

.detail-header {
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  color: white;
  padding: 38px 45px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.detail-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.detail-header::after {
  content: '';
  position: absolute;
  bottom: -50%;
  left: -10%;
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.paper-info {
  position: relative;
  z-index: 1;
}

.paper-info h2 {
  margin: 0 0 14px 0;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.paper-meta {
  display: flex;
  gap: 24px;
  font-size: 15px;
  opacity: 0.95;
  font-weight: 500;
}

.score-info {
  text-align: right;
  position: relative;
  z-index: 1;
}

.score-info .el-tag {
  border-radius: 10px;
  padding: 7px 18px;
  font-weight: 600;
  font-size: 14px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.score-info .el-tag--success {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: #ffffff;
}

.score-info .el-tag--warning {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: #ffffff;
}

.score-display {
  margin-top: 18px;
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.score-label {
  font-size: 16px;
  font-weight: 600;
}

.score-value {
  font-size: 44px;
  margin: 0 4px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.score-total {
  font-size: 18px;
  font-weight: 600;
}

.questions-container {
  padding: 35px 45px;
}

.question-item {
  border: 2px solid #e0f2fe;
  border-radius: 16px;
  padding: 26px;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.question-item:hover {
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
  border-color: #3b82f6;
  transform: translateY(-2px);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e0f2fe;
}

.question-number {
  font-weight: 700;
  color: #1e40af;
  font-size: 17px;
}

.question-header .el-tag {
  border-radius: 8px;
  padding: 5px 14px;
  font-weight: 600;
  border: none;
}

.question-score {
  margin-left: auto;
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 15px;
  font-weight: 700;
  background-color: #eff6ff;
  padding: 5px 14px;
  border-radius: 8px;
}

.question-content {
  margin-bottom: 24px;
  line-height: 1.8;
  color: #334155;
  font-size: 16px;
  padding: 18px;
  background: #ffffff;
  border-radius: 12px;
  border: 2px solid #f1f5f9;
}

.answer-section {
  background: #ffffff;
  padding: 22px;
  border-radius: 12px;
  border: 2px solid #f1f5f9;
}

.answer-item {
  margin-bottom: 20px;
}

.answer-item:last-child {
  margin-bottom: 0;
}

.answer-label {
  font-weight: 600;
  color: #475569;
  display: block;
  margin-bottom: 12px;
  font-size: 14px;
}

.answer-content {
  padding: 16px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdfa 100%);
  border-radius: 10px;
  color: #334155;
  border: 2px solid #e0f2fe;
  font-weight: 500;
}

.text-answer {
  white-space: pre-wrap;
  word-break: break-all;
}

.grading-result {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  padding: 20px;
  border-radius: 10px;
  border-left: 4px solid #10b981;
}

.grading-content {
  color: #334155;
}

.question-score-display {
  margin-bottom: 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.question-score-display .el-tag {
  border-radius: 8px;
  padding: 5px 14px;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border: none;
  color: #ffffff;
  font-weight: 600;
}

.detail-actions {
  padding: 35px 45px;
  text-align: center;
  border-top: 2px solid #f1f5f9;
  display: flex;
  justify-content: center;
  gap: 18px;
}

.detail-actions .el-button {
  border-radius: 12px;
  padding: 14px 36px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.detail-actions .el-button--default {
  border: 2px solid #e0f2fe;
  background: #f8fafc;
  color: #475569;
}

.detail-actions .el-button--default:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.detail-actions .el-button--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}

.detail-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.overall-comment {
  margin: 0 45px 35px 45px;
}

.overall-comment .el-card {
  border-radius: 14px;
  border: 2px solid #e0f2fe;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
}

.overall-comment .comment-header h4 {
  margin: 0 0 18px 0;
  color: #1e40af;
  font-size: 18px;
  font-weight: 700;
}

.overall-comment .comment-content {
  color: #475569;
  line-height: 1.8;
  padding: 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-radius: 10px;
  border-left: 4px solid #f59e0b;
}

.overall-comment .comment-content p {
  margin: 0;
  font-weight: 500;
}

.no-data {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 80px 40px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.12);
  text-align: center;
  border: 2px solid rgba(59, 130, 246, 0.1);
}

@media (max-width: 768px) {
  .record-detail-content {
    padding: 10px;
  }
  
  .detail-header {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  
  .score-info {
    text-align: center;
    margin-top: 16px;
  }
  
  .paper-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .questions-container {
    padding: 16px;
  }
  
  .question-item {
    padding: 16px;
  }
  
  .detail-actions {
    padding: 20px 16px;
    flex-direction: column;
  }
  
  .detail-actions .el-button {
    width: 100%;
  }
  
  .overall-comment {
    margin: 0 16px 24px 16px;
  }
}
</style>
