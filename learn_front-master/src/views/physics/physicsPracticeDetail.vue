<template>
  <div class="physics-practice-detail">
    <headerPage></headerPage>
    <div class="practice-detail-content">
    <div class="practice-header" v-if="!isSubmitted">
      <div class="paper-title">{{ paperInfo.paperTitle }}</div>
      <div class="paper-branch">分支：{{ getBranchText(paperInfo.subjectBranch) }}</div>
      <div class="practice-info">
        <div class="progress-info">
          <span>完成进度：{{ progress }}%</span>
          <el-progress :percentage="progress" :stroke-width="8"></el-progress>
        </div>
        <div class="timer" v-if="showTimer">
          <span>剩余时间：</span>
          <span class="time-display">{{ formatTime(remainingTime) }}</span>
        </div>
        <div class="auto-save-info" v-if="lastSavedTime">
          <span>自动保存：{{ formatLastSavedTime(lastSavedTime) }}</span>
        </div>
      </div>
    </div>
      
      <div v-if="!isSubmitted">
        <div class="practice-body">
          <div class="questions-container">
            <!-- 题目导航 -->
            <div class="question-navigation">
              <div class="navigation-header">
                <span class="nav-title">题目导航</span>
                <el-tag type="info">共 {{ questions.length }} 题</el-tag>
                <el-tag type="success">已答: {{ getAnsweredCount() }}</el-tag>
                <el-tag type="warning">未答: {{ questions.length - getAnsweredCount() }}</el-tag>
              </div>
              <div class="nav-items-container">
                <div 
                  v-for="(question, index) in questions" 
                  :key="index" 
                  class="nav-item"
                  :class="{ 
                    'active': currentQuestionIndex === index, 
                    'answered': isQuestionAnswered(question),
                    'current': currentQuestionIndex === index
                  }"
                  @click="currentQuestionIndex = index"
                >
                  <div class="nav-item-content">
                    <span class="nav-number">{{ index + 1 }}</span>
                    <span class="nav-type" :class="getQuestionTypeClass(question.type)">{{ getQuestionTypeText(question.type) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 题目内容区域 -->
            <div class="question-content-area">
              <div 
                v-for="(question, index) in questions" 
                :key="index" 
                class="question-item"
                v-show="currentQuestionIndex === index"
              >
                <div class="question-header">
                  <div class="question-info">
                    <div class="question-index">{{ index + 1 }}. <span class="question-type-tag" :class="getQuestionTypeClass(question.type)">{{ getQuestionTypeText(question.type) }}</span><span class="question-score-tag">（{{ getMaxScore(question) }}分）</span></div>
                    <div class="question-status" :class="{ 'answered': isQuestionAnswered(question) }">
                      {{ isQuestionAnswered(question) ? '已作答' : '未作答' }}
                    </div>
                  </div>
                  <div class="question-navigation-buttons">
                    <el-button 
                      size="small" 
                      @click="currentQuestionIndex = Math.max(0, index - 1)"
                      :disabled="index === 0"
                      icon="el-icon-arrow-left"
                    >
                      上一题
                    </el-button>
                    <el-button 
                      size="small" 
                      @click="currentQuestionIndex = Math.min(questions.length - 1, index + 1)"
                      :disabled="index === questions.length - 1"
                    >
                      下一题 <i class="el-icon-arrow-right"></i>
                    </el-button>
                    <el-button 
                      size="small" 
                      type="text"
                      @click="markCurrentQuestion"
                      :icon="question.marked ? 'el-icon-star-on' : 'el-icon-star-off'"
                    >
                      {{ question.marked ? '取消标记' : '标记本题' }}
                    </el-button>
                  </div>
                </div>
                <div class="question-content" @keydown="handleKeyDown($event, question)">
                  <div class="question-text" v-html="formatQuestionContent(question)"></div>
                  
                  <!-- 选择题（单选） -->
                  <div v-if="question.type === 0" class="question-options">
                    <div class="options-title">请选择正确答案：<span class="keyboard-hint">（快捷键：A/B/C/D 或 1/2/3/4）</span></div>
                    <el-radio-group v-model="question.userAnswer" class="options-group" @change="handleAnswerChange(question)">
                      <div 
                        v-for="(option, optIndex) in parseOptions(question)" 
                        :key="optIndex" 
                        class="option-item"
                        :class="{ 'selected': question.userAnswer === option.value }"
                      >
                        <el-radio :label="option.value">
                          <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
                          <span class="option-text" v-html="option.option"></span>
                        </el-radio>
                      </div>
                    </el-radio-group>
                  </div>
                  
                  <!-- 填空题 -->
                  <div v-if="question.type === 1" class="question-input fill-blank">
                    <div class="input-title">请在下方输入答案：</div>
                    <el-input 
                      v-model="question.userAnswer" 
                      placeholder="请输入您的答案..."
                      type="text"
                      clearable
                      size="medium"
                      class="answer-input"
                      @input="handleAnswerChange(question)"
                      @change="handleAnswerChange(question)"
                    ></el-input>
                    <div class="input-hint">提示：填空题请直接填写计算结果或数值</div>
                  </div>
                  
                  <!-- 计算题 -->
                  <div v-if="question.type === 2" class="question-input calculation">
                    <div class="input-title">请在下方书写解题过程和答案：</div>
                    <el-input 
                      v-model="question.userAnswer" 
                      type="textarea"
                      :rows="8"
                      placeholder="请详细书写解题步骤、公式推导和最终答案..."
                      resize="vertical"
                      class="calculation-textarea"
                      @input="handleAnswerChange(question)"
                      @change="handleAnswerChange(question)"
                    ></el-input>
                    <div class="input-hint">提示：计算题请详细展示解题过程，包括公式应用和计算步骤</div>
                  </div>
                  
                  <!-- 判断题 -->
                  <div v-if="question.type === 3" class="question-options true-false">
                    <div class="options-title">请选择正确答案：<span class="keyboard-hint">（快捷键：T/F 或 1/2）</span></div>
                    <el-radio-group v-model="question.userAnswer" class="options-group" @change="handleAnswerChange(question)">
                      <el-radio :label="'true'" class="true-false-option">
                        <span class="option-label">正确</span>
                      </el-radio>
                      <el-radio :label="'false'" class="true-false-option">
                        <span class="option-label">错误</span>
                      </el-radio>
                    </el-radio-group>
                  </div>
                  
                  <!-- 简答题 -->
                  <div v-if="question.type === 4" class="question-input short-answer">
                    <div class="input-title">请在下方输入答案：</div>
                    <el-input 
                      v-model="question.userAnswer" 
                      type="textarea"
                      :rows="6"
                      placeholder="请输入您的答案..."
                      resize="vertical"
                      class="answer-textarea"
                      @input="handleAnswerChange(question)"
                      @change="handleAnswerChange(question)"
                    ></el-input>
                    <div class="input-hint">提示：简答题请简明扼要地回答问题要点</div>
                  </div>
                </div>
                
                <!-- 题目操作区域 -->
                <div class="question-actions">
                  <el-button 
                    size="small" 
                    type="text" 
                    @click="clearAnswer(index)"
                    icon="el-icon-delete"
                  >
                    清空答案
                  </el-button>
                  <el-button 
                    size="small" 
                    type="text" 
                    @click="saveAndNext"
                    icon="el-icon-check"
                  >
                    保存并下一题
                  </el-button>
                  <el-button 
                    size="small" 
                    type="text" 
                    @click="showMarkedQuestions"
                    icon="el-icon-star-on"
                  >
                    查看标记题目 ({{ markedQuestions.length }})
                  </el-button>
                </div>
              </div>
            </div>
            
            <div class="practice-actions">
              <el-button 
                type="primary" 
                @click="submitPractice"
                :disabled="submitting"
              >
                {{ submitting ? '提交中...' : '提交练习' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 已提交状态 -->
      <div v-else>
        <div class="submission-result">
          <div class="result-message">
            <i class="el-icon-success result-icon"></i>
            <h3>练习已提交！</h3>
            <p>您的练习已成功提交，等待老师评分</p>
          </div>
          <div class="result-actions">
            <el-button @click="$router.push('/physicsPractice')">返回题库</el-button>
            <el-button type="primary" @click="$router.push('/physicsPracticeRecords')">查看记录</el-button>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
    
    <!-- 标记题目对话框 -->
    <el-dialog
      title="标记题目列表"
      :visible.sync="showMarkedDialog"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="markedQuestions.length === 0" class="no-marked">
        <el-empty description="暂无标记题目"></el-empty>
      </div>
      <div v-else class="marked-list">
        <div 
          v-for="(questionIndex, index) in markedQuestions" 
          :key="questionIndex"
          class="marked-item"
          @click="goToQuestion(questionIndex)"
        >
          <div class="marked-item-header">
            <span class="marked-item-number">第{{ questionIndex + 1 }}题</span>
            <el-tag size="mini" :type="getQuestionTypeColor(questions[questionIndex]?.type)">
              {{ getQuestionTypeText(questions[questionIndex]?.type) }}
            </el-tag>
            <el-tag 
              size="mini" 
              :type="questions[questionIndex]?.userAnswer ? 'success' : 'info'"
            >
              {{ questions[questionIndex]?.userAnswer ? '已作答' : '未作答' }}
            </el-tag>
          </div>
          <div class="marked-item-content">
            {{ getQuestionPreview(questions[questionIndex]) }}
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showMarkedDialog = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getPhysicsQuestionPaperById, submitPhysicsPractice } from '../../api/api'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsPracticeDetail",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      paperId: '',
      paperInfo: {},
      questions: [],
      isSubmitted: false,
      submitting: false,
      showTimer: false,
      remainingTime: 0,
      timerInterval: null,
      autoSaveInterval: null,
      lastSavedTime: null,
      progress: 0,
      currentQuestionIndex: 0,
      markedQuestions: [],
      showMarkedDialog: false
    }
  },
  methods: {
    async loadPaper() {
      try {
        const response = await getPhysicsQuestionPaperById({ id: this.paperId });
        if (response && response.code === 1000) {
          this.paperInfo = response.data || {};
          
          // 尝试从本地存储恢复作答进度
          const savedProgress = this.loadProgressFromStorage();
          
          // 解析试卷内容，将JSON字符串转换为题目数组
          if (this.paperInfo.questionContent) {
            try {
              // 假设questionContent是JSON格式的题目数组
              const parsedContent = JSON.parse(this.paperInfo.questionContent);
              this.questions = Array.isArray(parsedContent) ? parsedContent : [];
              const hasProgress = savedProgress && savedProgress.answers && savedProgress.answers.length > 0;
              if (!hasProgress) {
                this.questions = this.sortQuestionsByType(this.questions);
              }
              
              // 初始化每个题目的用户答案
              this.questions.forEach((question, index) => {
                // 尝试从本地存储恢复答案
                if (savedProgress && savedProgress.answers && savedProgress.answers[index]) {
                  question.userAnswer = savedProgress.answers[index];
                } else {
                  if ([0, 3].includes(question.type)) {
                    // 单选题和判断题
                    question.userAnswer = '';
                  } else {
                    // 填空题（type=1）、计算题（type=2）、简答题（type=4）、证明题（type=5）
                    question.userAnswer = '';
                  }
                }
                
                // 恢复标记状态
                if (savedProgress && savedProgress.markedQuestions) {
                  question.marked = savedProgress.markedQuestions.includes(index);
                } else {
                  question.marked = false;
                }
              });
              
              // 恢复当前题目索引和标记题目列表
              if (savedProgress) {
                if (savedProgress.currentIndex !== undefined) {
                  this.currentQuestionIndex = savedProgress.currentIndex;
                }
                if (savedProgress.markedQuestions) {
                  this.markedQuestions = [...savedProgress.markedQuestions];
                }
              }
              
              // 计算进度
              this.calculateProgress();
              
              // 启动自动保存
              this.startAutoSave();
              
            } catch (e) {
              console.error('解析题目内容失败:', e);
              this.$message.error('试卷内容格式错误，无法解析');
              // 如果解析失败，尝试其他方式解析
              this.questions = [];
            }
          } else {
            this.$message.warning('试卷内容为空');
            this.questions = [];
          }
        } else {
          this.$message.error(response?.message || '获取试卷失败');
        }
      } catch (error) {
        console.error('获取试卷失败:', error);
        this.$message.error(error?.response?.data?.message || error?.message || '网络错误或服务器异常');
      }
    },
    sortQuestionsByType(list) {
      if (!Array.isArray(list)) return [];
      return list.slice().sort((a, b) => {
        const orderA = this.getQuestionTypeOrder(a && a.type);
        const orderB = this.getQuestionTypeOrder(b && b.type);
        if (orderA !== orderB) return orderA - orderB;
        return 0;
      });
    },
    getQuestionTypeOrder(type) {
      const value = Number(type);
      if (value === 0) return 0;
      if (value === 1) return 1;
      if (value === 3) return 2;
      if (value === 2) return 3;
      return 4;
    },
    
    formatQuestionContent(question) {
      // 处理题目内容，支持数学符号和格式
      let content = question.title || question.content || '';
      
      // 对于填空题，特殊处理下划线和括号
      if (question.type === 1) {
        // 将多个连续下划线替换为带样式的输入框表示
        content = content.replace(/_{4,}/g, '<span class="blank-placeholder">________</span>');
        // 处理中文括号形式的填空
        content = content.replace(/（\s*）/g, '<span class="blank-placeholder">（____）</span>');
        content = content.replace(/（\s*.{0,4}\s*）/g, '<span class="blank-placeholder">（____）</span>');
      }
      
      // 处理数学符号
      content = content
        .replace(/v₀/g, 'v<sub>0</sub>')
        .replace(/v₁/g, 'v<sub>1</sub>')
        .replace(/v₂/g, 'v<sub>2</sub>')
        .replace(/t₀/g, 't<sub>0</sub>')
        .replace(/t₁/g, 't<sub>1</sub>')
        .replace(/t₂/g, 't<sub>2</sub>')
        .replace(/x₀/g, 'x<sub>0</sub>')
        .replace(/x₁/g, 'x<sub>1</sub>')
        .replace(/x₂/g, 'x<sub>2</sub>')
        .replace(/a₀/g, 'a<sub>0</sub>')
        .replace(/a₁/g, 'a<sub>1</sub>')
        .replace(/a₂/g, 'a<sub>2</sub>')
        .replace(/²/g, '<sup>2</sup>')
        .replace(/³/g, '<sup>3</sup>')
        .replace(/½/g, '&frac12;')
        .replace(/¼/g, '&frac14;')
        .replace(/¾/g, '&frac34;');
      
      // 处理分数格式
      content = content.replace(/(\d+)\/(\d+)/g, '<sup>$1</sup>&frasl;<sub>$2</sub>');
      
      // 处理换行符
      content = content.replace(/\\n/g, '<br>');
      
      return content;
    },
    
    parseOptions(question) {
      const options = [];
      const content = question.title || question.content || '';
      
      if (question.options) {
        if (Array.isArray(question.options)) {
          question.options.forEach((option, index) => {
            if (typeof option === 'object' && option !== null) {
              options.push({
                value: String.fromCharCode(65 + index),
                option: this.formatOptionText(option.text || option.option || option)
              });
            } else {
              options.push({
                value: String.fromCharCode(65 + index),
                option: this.formatOptionText(option)
              });
            }
          });
        } else if (typeof question.options === 'object') {
          const sortedKeys = Object.keys(question.options).sort();
          sortedKeys.forEach(key => {
            const upperKey = key.toUpperCase();
            if (['A', 'B', 'C', 'D', 'E', 'F'].includes(upperKey)) {
              options.push({
                value: upperKey,
                option: this.formatOptionText(question.options[key])
              });
            }
          });
        }
        if (options.length > 0) {
          return options;
        }
      }
      
      const lines = content.split(/\\n|\n/);
      const optionPatterns = [
        /^\s*([A-D])[\.．、）)]\s*(.+)$/,
        /^\s*([A-D])\s+(.+)$/,
        /([A-D])[\.．、）)]\s*([^A-D\.．、）)]+)/g
      ];
      
      lines.forEach(line => {
        for (let i = 0; i < optionPatterns.length - 1; i++) {
          const match = line.match(optionPatterns[i]);
          if (match) {
            const existingOption = options.find(o => o.value === match[1].toUpperCase());
            if (!existingOption) {
              options.push({
                value: match[1].toUpperCase(),
                option: this.formatOptionText(match[2].trim())
              });
            }
            break;
          }
        }
      });
      
      if (options.length === 0) {
        const inlinePattern = /([A-D])[\.．、）)]\s*([^A-D\.．、）)]+)/g;
        let match;
        while ((match = inlinePattern.exec(content)) !== null) {
          const existingOption = options.find(o => o.value === match[1].toUpperCase());
          if (!existingOption) {
            options.push({
              value: match[1].toUpperCase(),
              option: this.formatOptionText(match[2].trim())
            });
          }
        }
      }
      
      options.sort((a, b) => a.value.localeCompare(b.value));
      
      return options;
    },
    
    formatOptionText(text) {
      // 处理选项中的数学符号
      return text
        .replace(/v₀/g, 'v<sub>0</sub>')
        .replace(/v₁/g, 'v<sub>1</sub>')
        .replace(/v₂/g, 'v<sub>2</sub>')
        .replace(/t₀/g, 't<sub>0</sub>')
        .replace(/t₁/g, 't<sub>1</sub>')
        .replace(/t₂/g, 't<sub>2</sub>')
        .replace(/²/g, '<sup>2</sup>')
        .replace(/³/g, '<sup>3</sup>');
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
    
    formatTime(seconds) {
      const hrs = Math.floor(seconds / 3600);
      const mins = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;
      
      let timeStr = '';
      if (hrs > 0) timeStr += `${hrs.toString().padStart(2, '0')}:`;
      timeStr += `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
      
      return timeStr;
    },
    
    async submitPractice() {
      if (this.submitting) return;
      
      // 检查是否所有题目都已作答
      const unansweredQuestions = this.questions.filter(q => 
        !q.userAnswer || (Array.isArray(q.userAnswer) ? q.userAnswer.length === 0 : (typeof q.userAnswer === 'string' && q.userAnswer.trim() === ''))
      );
      
      if (unansweredQuestions.length > 0) {
        this.$confirm(`还有 ${unansweredQuestions.length} 道题目未作答，确定要提交吗？`, '提示', {
          confirmButtonText: '确定提交',
          cancelButtonText: '继续作答',
          type: 'warning'
        }).then(() => {
          this.doSubmit();
        });
      } else {
        this.doSubmit();
      }
    },
    
    async doSubmit() {
      this.submitting = true;
      
      try {
        // 准备提交数据
        const submitData = {
          paperId: this.paperId,
          userAnswers: JSON.stringify(this.questions.map(q => ({
            questionId: q.id,
            userAnswer: q.userAnswer,
            type: q.type
          })))
        };
        
        const response = await submitPhysicsPractice(submitData);
        if (response && response.code === 1000) {
          this.isSubmitted = true;
          // 清除本地存储的进度
          this.clearProgressFromStorage();
          this.$message.success('练习提交成功！');
          // 可选：延迟一小段时间后刷新页面或跳转到练习记录
          setTimeout(() => {
            // 如果用户想查看练习记录，可以提示他们前往练习记录页面
          }, 1500);
        } else {
          this.$message.error(response?.message || '提交失败');
        }
      } catch (error) {
        console.error('提交练习失败:', error);
        this.$message.error(error?.response?.data?.message || error?.message || '提交失败');
      } finally {
        this.submitting = false;
      }
    },

    // 计算进度
    calculateProgress() {
      const answeredCount = this.questions.filter(q => 
        q.userAnswer && (Array.isArray(q.userAnswer) ? q.userAnswer.length > 0 : (typeof q.userAnswer === 'string' && q.userAnswer.trim() !== ''))
      ).length;
      this.progress = Math.round((answeredCount / this.questions.length) * 100);
    },
    
    // 启动自动保存
    startAutoSave() {
      // 每30秒自动保存一次
      this.autoSaveInterval = setInterval(() => {
        this.saveProgressToStorage();
      }, 30000);
      
      // 页面卸载时保存
      window.addEventListener('beforeunload', this.handleBeforeUnload);
    },
    
    // 页面卸载处理
    handleBeforeUnload(e) {
      this.saveProgressToStorage();
    },
    
    // 保存进度到本地存储
    saveProgressToStorage() {
      const progressData = {
        paperId: this.paperId,
        answers: this.questions.map(q => q.userAnswer),
        markedQuestions: this.markedQuestions,
        currentIndex: this.currentQuestionIndex,
        saveTime: new Date().getTime()
      };
      localStorage.setItem(`physics_practice_${this.paperId}`, JSON.stringify(progressData));
      this.lastSavedTime = new Date().getTime();
    },
    
    // 从本地存储加载进度
    loadProgressFromStorage() {
      const savedData = localStorage.getItem(`physics_practice_${this.paperId}`);
      if (savedData) {
        return JSON.parse(savedData);
      }
      return null;
    },
    
    // 清除本地存储的进度
    clearProgressFromStorage() {
      localStorage.removeItem(`physics_practice_${this.paperId}`);
    },
    
    // 格式化最后保存时间
    formatLastSavedTime(timestamp) {
      const now = new Date().getTime();
      const diff = Math.floor((now - timestamp) / 1000);
      
      if (diff < 60) {
        return '刚刚';
      } else if (diff < 3600) {
        return `${Math.floor(diff / 60)}分钟前`;
      } else {
        return `${Math.floor(diff / 3600)}小时前`;
      }
    },
    
    // 获取已作答题目数量
    getAnsweredCount() {
      return this.questions.filter(q => 
        q.userAnswer && (Array.isArray(q.userAnswer) ? q.userAnswer.length > 0 : (typeof q.userAnswer === 'string' && q.userAnswer.trim() !== ''))
      ).length;
    },
    
    // 获取题目类型文本
    getQuestionTypeText(type) {
      const typeMap = {
        0: '选择题',
        1: '填空题',
        2: '计算题',
        3: '判断题',
        4: '简答题',
        5: '证明题'
      };
      return typeMap[type] || '未知题型';
    },
    
    // 获取题目满分
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
    
    // 获取题目类型样式类
    getQuestionTypeClass(type) {
      const classMap = {
        0: 'choice',
        1: 'fill-blank',
        2: 'calculation',
        3: 'true-false',
        4: 'short-answer',
        5: 'proof'
      };
      return classMap[type] || 'other';
    },
    
    // 判断题目是否已作答
    isQuestionAnswered(question) {
      // 防御性检查：确保question存在且是对象
      if (!question || typeof question !== 'object') {
        return false;
      }
      
      // 检查userAnswer是否存在
      if (!question.userAnswer) {
        return false;
      }
      
      // 对于判断题（type=3），userAnswer是 'true' 或 'false' 字符串
      if (question.type === 3) {
        return question.userAnswer === 'true' || question.userAnswer === 'false';
      }
      
      // 对于选择题（type=0），userAnswer应该是非空字符串
      if (question.type === 0) {
        return typeof question.userAnswer === 'string' && question.userAnswer.trim() !== '';
      }
      
      // 对于填空题（type=1），检查是否为非空字符串
      if (question.type === 1) {
        return typeof question.userAnswer === 'string' && question.userAnswer.trim() !== '';
      }
      
      // 对于计算题（type=2）、简答题（type=4）、证明题（type=5），检查是否为非空字符串
      if ([2, 4, 5].includes(question.type)) {
        return typeof question.userAnswer === 'string' && question.userAnswer.trim() !== '';
      }
      
      // 默认情况：检查是否为非空字符串
      return typeof question.userAnswer === 'string' && question.userAnswer.trim() !== '';
    },
    
    // 处理键盘事件
    handleKeyDown(event, question) {
      // 只处理选择题（type=0）和判断题（type=3）的键盘事件
      if (question.type === 0 || question.type === 3) {
        const key = event.key.toLowerCase();
        
        // 选择题的键盘快捷键：A/B/C/D 或 1/2/3/4
        if (question.type === 0) {
          const options = this.parseOptions(question);
          
          // A/B/C/D 快捷键
          if (key >= 'a' && key <= 'd') {
            const optionIndex = key.charCodeAt(0) - 'a'.charCodeAt(0);
            if (optionIndex < options.length) {
              question.userAnswer = options[optionIndex].value;
              this.handleAnswerChange(question);
              event.preventDefault();
            }
          }
          
          // 1/2/3/4 快捷键
          else if (key >= '1' && key <= '4') {
            const optionIndex = parseInt(key) - 1;
            if (optionIndex < options.length) {
              question.userAnswer = options[optionIndex].value;
              this.handleAnswerChange(question);
              event.preventDefault();
            }
          }
        }
        
        // 判断题的键盘快捷键：T/F 或 1/2
        else if (question.type === 3) {
          if (key === 't' || key === '1') {
            question.userAnswer = 'true';
            this.handleAnswerChange(question);
            event.preventDefault();
          } else if (key === 'f' || key === '2') {
            question.userAnswer = 'false';
            this.handleAnswerChange(question);
            event.preventDefault();
          }
        }
      }
    },
    
    // 处理答案变化
    handleAnswerChange(question) {
      // 更新进度
      this.calculateProgress();
      // 保存到本地存储
      this.saveProgressToStorage();
    },
    
    // 保存并下一题
    saveAndNext() {
      this.saveProgressToStorage();
      if (this.currentQuestionIndex < this.questions.length - 1) {
        this.currentQuestionIndex++;
      }
    },
    
    // 标记当前题目
    markCurrentQuestion() {
      const question = this.questions[this.currentQuestionIndex];
      if (question) {
        question.marked = !question.marked;
        if (question.marked) {
          this.markedQuestions.push(this.currentQuestionIndex);
        } else {
          const index = this.markedQuestions.indexOf(this.currentQuestionIndex);
          if (index > -1) {
            this.markedQuestions.splice(index, 1);
          }
        }
        this.saveProgressToStorage();
        this.$message.success(question.marked ? '题目已标记' : '已取消标记');
      }
    },
    
    // 显示标记题目对话框
    showMarkedQuestions() {
      this.showMarkedDialog = true;
    },
    
    // 跳转到指定题目
    goToQuestion(index) {
      this.currentQuestionIndex = index;
      this.showMarkedDialog = false;
    },
    
    // 获取题目预览文本
    getQuestionPreview(question) {
      if (!question) return '';
      let content = question.title || question.content || '';
      if (content.length > 100) {
        content = content.substring(0, 100) + '...';
      }
      return content;
    },
    
    // 获取题目类型颜色
    getQuestionTypeColor(type) {
      const colorMap = {
        0: 'primary',
        1: 'success',
        2: 'warning',
        3: 'info',
        4: 'danger'
      };
      return colorMap[type] || 'info';
    },
    
    // 清空答案
    clearAnswer(index) {
      this.$confirm('确定要清空本题的答案吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.questions[index].userAnswer = '';
        this.saveProgressToStorage();
        this.$message.success('答案已清空');
      });
    },
    
    // 保存并下一题
    saveAndNext() {
      this.saveProgressToStorage();
      if (this.currentQuestionIndex < this.questions.length - 1) {
        this.currentQuestionIndex++;
        this.$message.success('答案已保存，已跳转到下一题');
      } else {
        this.$message.success('答案已保存，这是最后一题');
      }
    }
  },
  
  beforeDestroy() {
    // 清理定时器和事件监听器
    if (this.autoSaveInterval) {
      clearInterval(this.autoSaveInterval);
    }
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    window.removeEventListener('beforeunload', this.handleBeforeUnload);
  },
  
  watch: {
    // 监听题目答案变化，自动计算进度
    questions: {
      handler() {
        this.calculateProgress();
        // 答案变化时立即保存
        this.saveProgressToStorage();
      },
      deep: true
    },
    
    // 监听当前题目索引变化
    currentQuestionIndex() {
      this.saveProgressToStorage();
    }
  },
  
  mounted() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
    
    const routeId = this.$route.query.id;
    if (routeId) {
      this.paperId = routeId;
      this.loadPaper();
    } else {
      this.$message.error('试卷ID不能为空');
      this.$router.push('/physicsPractice');
    }
  },
  
  beforeDestroy() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }
}
</script>

<style scoped>
.physics-practice-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #fef7f5 0%, #fff5f0 100%);
}

.practice-detail-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 28px 24px;
}

.practice-header {
  background: #ffffff;
  padding: 32px 36px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(255, 107, 107, 0.1);
  margin-bottom: 28px;
  border: 1px solid rgba(255, 107, 107, 0.08);
  position: relative;
  overflow: hidden;
}

.practice-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.06) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.practice-header::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -5%;
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255, 159, 107, 0.05) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.paper-title {
  font-size: 26px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 12px;
  letter-spacing: 0.3px;
  position: relative;
  z-index: 1;
}

.paper-branch {
  color: #78716c;
  margin-bottom: 16px;
  font-size: 15px;
  position: relative;
  z-index: 1;
}

.practice-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 200px;
}

.progress-info span {
  color: #78716c;
  font-size: 15px;
  font-weight: 600;
}

.timer {
  color: #ff6b6b;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 107, 107, 0.08);
  padding: 8px 16px;
  border-radius: 12px;
}

.time-display {
  font-size: 17px;
  font-family: 'Courier New', monospace;
  font-weight: 700;
}

.auto-save-info {
  color: #67c23a;
  font-size: 14px;
  font-weight: 500;
  background: rgba(103, 194, 58, 0.08);
  padding: 6px 14px;
  border-radius: 12px;
}

.questions-container {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.08);
  padding: 32px;
  margin-bottom: 28px;
  border: 1px solid #f5f0ee;
}

/* 题目导航样式 */
.question-navigation {
  margin-bottom: 28px;
  padding: 24px;
  background: linear-gradient(135deg, #faf9f8 0%, #fff5f0 100%);
  border-radius: 20px;
  border: 1px solid #f0e0dc;
}

.navigation-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0e0dc;
}

.nav-title {
  font-weight: 700;
  color: #2d3748;
  font-size: 17px;
  letter-spacing: 0.2px;
}

.nav-items-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.nav-item {
  min-width: 75px;
  height: 55px;
  padding: 6px;
  border: 2px solid #e8e0dc;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-item:hover {
  border-color: #ff6b6b;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.18);
  background: #fff5f0;
}

.nav-item.current {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff0eb 0%, #ffe8e0 100%);
  box-shadow: 0 6px 18px rgba(255, 107, 107, 0.2);
}

.nav-item.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 8px 22px rgba(255, 107, 107, 0.35);
}

.nav-item.answered {
  background: linear-gradient(135deg, #67c23a 0%, #95d464 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 6px 18px rgba(103, 194, 58, 0.3);
}

.nav-item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.nav-number {
  font-weight: 700;
  font-size: 15px;
}

.nav-type {
  font-size: 10px;
  margin-top: 3px;
  opacity: 0.85;
  font-weight: 600;
}

/* 题目类型样式 */
.nav-type.choice { color: #ff6b6b; }
.nav-type.fill-blank { color: #ff9f6b; }
.nav-type.calculation { color: #f56c6c; }
.nav-type.true-false { color: #ffb140; }
.nav-type.short-answer { color: #67c23a; }
.nav-type.proof { color: #909399; }

.nav-item.active .nav-type,
.nav-item.answered .nav-type {
  color: rgba(255, 255, 255, 0.95);
}

/* 判断题选项样式 */
.question-options.true-false {
  margin-top: 15px;
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #d9ecff;
}

.options-group {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.true-false-option {
  margin-right: 20px;
}

/* 题目内容区域 */
.question-content-area {
  min-height: 450px;
}

.question-item {
  padding: 32px;
  border: 1px solid #f0e0dc;
  border-radius: 20px;
  margin-bottom: 28px;
  background: #ffffff;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.06);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.question-item:hover {
  box-shadow: 0 12px 38px rgba(255, 107, 107, 0.12);
  border-color: #fde0d8;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f5f0ee;
}

.question-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-index {
  font-weight: 700;
  color: #ff6b6b;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-type-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 14px;
  color: #ffffff;
  font-weight: 600;
}

.question-type-tag.choice { background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%); }
.question-type-tag.fill-blank { background: linear-gradient(135deg, #ff9f6b 0%, #ffb140 100%); }
.question-type-tag.calculation { background: linear-gradient(135deg, #f56c6c 0%, #ff6b6b 100%); }
.question-type-tag.true-false { background: linear-gradient(135deg, #ffb140 0%, #ff9f6b 100%); }
.question-type-tag.short-answer { background: linear-gradient(135deg, #67c23a 0%, #95d464 100%); }
.question-type-tag.proof { background: linear-gradient(135deg, #909399 0%, #a0a4ac 100%); }

.question-score-tag {
  font-size: 13px;
  color: #78716c;
  margin-left: 6px;
  font-weight: 500;
}

.question-status {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 8px;
  background: #f5f0ee;
  color: #909399;
  display: inline-block;
  font-weight: 500;
}

.question-status.answered {
  background: linear-gradient(135deg, #fff0eb 0%, #ffe8e0 100%);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.15);
}

.question-navigation-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.question-content {
  flex: 1;
}

.question-text {
  margin: 0 0 24px 0;
  color: #2d3748;
  line-height: 1.9;
  font-size: 17px;
  padding: 20px;
  background: linear-gradient(135deg, #faf9f8 0%, #fff5f0 100%);
  border-radius: 12px;
  border-left: 5px solid #ff6b6b;
  font-weight: 400;
}

/* 选择题样式 */
.question-options {
  margin-top: 20px;
}

.options-title {
  font-weight: 600;
  color: #78716c;
  margin-bottom: 18px;
  font-size: 15px;
  letter-spacing: 0.2px;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.option-item {
  border: 2px solid #e8e0dc;
  border-radius: 14px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0;
  background: #ffffff;
}

.option-item:hover {
  border-color: #ff6b6b;
  transform: translateX(6px);
  box-shadow: 0 6px 18px rgba(255, 107, 107, 0.15);
  background: #fff5f0;
}

.option-item.selected {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff0eb 0%, #ffe8e0 100%);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.2);
}

.option-item .el-radio {
  margin-right: 0;
  padding: 16px 18px;
  width: 100%;
  margin: 0;
}

.option-label {
  font-weight: 700;
  color: #ff6b6b;
  margin-right: 12px;
  font-size: 15px;
}

.option-text {
  line-height: 1.7;
  color: #2d3748;
  font-size: 16px;
}

/* 填空题样式 */
.question-input.fill-blank {
  margin-top: 20px;
  width: 100%;
}

.input-title {
  font-weight: 600;
  color: #78716c;
  margin-bottom: 12px;
  font-size: 15px;
  letter-spacing: 0.2px;
}

.answer-input {
  max-width: 350px;
}

.answer-input .el-input__inner {
  border-radius: 12px;
  border: 2px solid #e8e0dc;
  padding: 12px 18px;
  font-size: 15px;
  transition: all 0.3s;
}

.answer-input .el-input__inner:focus {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}

.input-hint {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
  font-style: italic;
  background: rgba(255, 107, 107, 0.05);
  padding: 8px 12px;
  border-radius: 8px;
  border-left: 3px solid #ff6b6b;
}

/* 计算题样式 */
.question-input.calculation {
  margin-top: 20px;
  width: 100%;
}

.calculation-textarea {
  font-family: 'Courier New', monospace;
}

.calculation-textarea .el-textarea__inner {
  border-radius: 14px;
  border: 2px solid #e8e0dc;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
  transition: all 0.3s;
}

.calculation-textarea .el-textarea__inner:focus {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}

/* 判断题选项样式 */
.question-options.true-false {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #fff0eb 0%, #ffe8e0 100%);
  border-radius: 14px;
  border: 2px solid #fde0d8;
}

.options-group {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.true-false-option {
  margin-right: 24px;
}

.true-false-option .el-radio {
  padding: 12px 20px;
  background: #ffffff;
  border-radius: 12px;
  border: 2px solid #e8e0dc;
  transition: all 0.3s;
}

.true-false-option .el-radio:hover {
  border-color: #ff6b6b;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.15);
}

.true-false-option .el-radio.is-checked {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff0eb 0%, #ffe8e0 100%);
}

.option-label {
  font-weight: 700;
  color: #ff6b6b;
  margin-right: 12px;
}

/* 简答题样式 */
.question-input.short-answer {
  margin-top: 20px;
  width: 100%;
}

.answer-textarea .el-textarea__inner {
  border-radius: 14px;
  border: 2px solid #e8e0dc;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
  transition: all 0.3s;
}

.answer-textarea .el-textarea__inner:focus {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}

.marked-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.marked-item-number {
  font-weight: 700;
  color: #ff6b6b;
  font-size: 15px;
}

.marked-item-content {
  color: #78716c;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.no-marked {
  padding: 30px 20px;
  text-align: center;
}

/* 题目操作区域 */
.question-actions {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 2px solid #f5f0ee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.practice-actions {
  text-align: center;
  padding: 36px 0 28px 0;
}

.practice-actions .el-button {
  padding: 14px 48px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.3px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%);
  border: none;
  box-shadow: 0 8px 28px rgba(255, 107, 107, 0.35);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.practice-actions .el-button:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 40px rgba(255, 107, 107, 0.45);
}

.practice-actions .el-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submission-result {
  text-align: center;
  padding: 48px 32px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.08);
  border: 1px solid #f5f0ee;
}

.result-message {
  margin-bottom: 36px;
}

.result-icon {
  font-size: 72px;
  color: #67c23a;
  margin-bottom: 20px;
  display: inline-block;
  animation: bounce 0.6s ease;
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.result-message h3 {
  color: #2d3748;
  margin: 12px 0;
  font-size: 24px;
  font-weight: 700;
}

.result-message p {
  color: #78716c;
  margin: 8px 0;
  font-size: 15px;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 18px;
}

.result-actions .el-button {
  padding: 12px 32px;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.result-actions .el-button--primary {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f6b 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.3);
}

.result-actions .el-button--primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(255, 107, 107, 0.4);
}

.result-actions .el-button {
  border: 2px solid #e8e0dc;
  background: #faf9f8;
  color: #6b7280;
}

.result-actions .el-button:hover {
  border-color: #ff6b6b;
  background: #fff5f0;
  color: #ff6b6b;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.15);
}

@media (max-width: 768px) {
  .question-item {
    flex-direction: column;
  }
  
  .question-index {
    margin-bottom: 10px;
  }
  
  .result-actions {
    flex-direction: column;
    align-items: center;
  }
}

.marked-list {
  max-height: 420px;
  overflow-y: auto;
  padding: 8px;
}

.marked-item {
  padding: 16px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #faf9f8 0%, #fff5f0 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f0e0dc;
}

.marked-item:hover {
  background: linear-gradient(135deg, #fff0eb 0%, #ffe8e0 100%);
  transform: translateX(6px);
  border-color: #ff6b6b;
  box-shadow: 0 6px 18px rgba(255, 107, 107, 0.15);
}

/* 填空题占位符样式 */
.blank-placeholder {
  display: inline-block;
  min-width: 60px;
  padding: 2px 8px;
  margin: 0 4px;
  border-bottom: 2px dashed #409eff;
  color: #409eff;
  font-weight: bold;
  text-align: center;
  vertical-align: bottom;
}

/* 键盘快捷键提示样式 */
.keyboard-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
  font-style: italic;
}
</style>
