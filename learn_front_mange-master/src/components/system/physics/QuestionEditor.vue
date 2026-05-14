<template>
  <div class="question-editor">
    <div class="editor-header">
      <h3>题目编辑器</h3>
      <el-button 
        v-if="!showAddForm" 
        type="primary" 
        size="small" 
        @click="showAddQuestionForm" 
        icon="el-icon-plus"
      >
        添加题目
      </el-button>
    </div>

    <!-- 内联添加题目表单 -->
    <div v-if="showAddForm" class="add-question-form">
      <h4>添加新题目</h4>
      <el-form :model="currentQuestion" :rules="questionRules" ref="addQuestionForm" label-width="100px">
        <el-form-item label="题型" prop="type">
          <el-select v-model="currentQuestion.type" placeholder="请选择题型" @change="handleTypeChange">
            <el-option label="选择题" :value="0"></el-option>
            <el-option label="填空题" :value="1"></el-option>
            <el-option label="计算题" :value="2"></el-option>
            <el-option label="判断题" :value="3"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容" prop="title">
          <el-input 
            type="textarea" 
            :rows="4" 
            v-model="currentQuestion.title"
            placeholder="请输入题目内容"
          ></el-input>
          <div class="form-tip">支持使用 v₀、v₁、v₂ 表示下标，使用 ²、³ 表示上标</div>
        </el-form-item>

        <el-form-item label="分值" prop="maxScore">
          <el-input-number v-model="currentQuestion.maxScore" :min="1" :max="100"></el-input-number>
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-rate v-model="currentQuestion.difficulty" :max="5"></el-rate>
        </el-form-item>

        <el-form-item v-if="currentQuestion.type === 0" label="选项" prop="options">
          <div v-for="(option, index) in currentQuestion.options" :key="index" class="option-item">
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <el-input v-model="option.text" placeholder="请输入选项内容"></el-input>
            <el-button 
              size="mini" 
              type="text" 
              @click="removeOption(index)"
              icon="el-icon-delete"
              style="color: #f56c6c;"
              :disabled="currentQuestion.options.length <= 2"
            >
              删除
            </el-button>
          </div>
          <el-button size="small" type="dashed" @click="addOption" icon="el-icon-plus">
            添加选项
          </el-button>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-select v-if="currentQuestion.type === 0" v-model="currentQuestion.answer" placeholder="请选择正确答案">
            <el-option 
              v-for="(option, index) in currentQuestion.options" 
              :key="index"
              :label="String.fromCharCode(65 + index)"
              :value="index"
            >
              {{ String.fromCharCode(65 + index) }}. {{ option.text }}
            </el-option>
          </el-select>
          <el-input 
            v-else-if="currentQuestion.type === 1" 
            v-model="currentQuestion.answer"
            placeholder="请输入填空题答案"
          ></el-input>
          <el-input 
            v-else-if="currentQuestion.type === 2" 
            type="textarea"
            :rows="4"
            v-model="currentQuestion.answer"
            placeholder="请输入计算题答案（可包含解题步骤）"
          ></el-input>
          <el-radio-group v-else-if="currentQuestion.type === 3" v-model="currentQuestion.answer">
            <el-radio :label="true">正确</el-radio>
            <el-radio :label="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="解析" prop="explanation">
          <el-input 
            type="textarea" 
            :rows="3" 
            v-model="currentQuestion.explanation"
            placeholder="请输入题目解析（可选）"
          ></el-input>
        </el-form-item>

        <div class="form-actions">
          <el-button @click="cancelAddQuestion">取 消</el-button>
          <el-button type="primary" @click="saveNewQuestion">保 存</el-button>
        </div>
      </el-form>
    </div>

    <div v-if="questions.length === 0 && !showAddForm" class="empty-state">
      <el-empty description="暂无题目，请点击上方按钮添加"></el-empty>
    </div>

    <div v-else class="questions-list">
      <div 
        v-for="(question, index) in questions" 
        :key="index"
        class="question-item"
        :class="{ 'active': currentQuestionIndex === index }"
      >
        <div class="question-item-header">
          <div class="question-number">
            <span>第{{ index + 1 }}题</span>
            <el-tag size="mini" :type="getQuestionTypeColor(question.type)">
              {{ getQuestionTypeText(question.type) }}
            </el-tag>
            <el-tag size="mini" type="info">{{ question.maxScore || 10 }}分</el-tag>
          </div>
          <div class="question-actions">
            <el-button 
              size="mini" 
              type="text" 
              @click="moveQuestion(index, -1)"
              :disabled="index === 0"
              icon="el-icon-arrow-up"
            >
              上移
            </el-button>
            <el-button 
              size="mini" 
              type="text" 
              @click="moveQuestion(index, 1)"
              :disabled="index === questions.length - 1"
              icon="el-icon-arrow-down"
            >
              下移
            </el-button>
            <el-button 
              size="mini" 
              type="text" 
              @click="editQuestion(index)"
              icon="el-icon-edit"
            >
              编辑
            </el-button>
            <el-button 
              size="mini" 
              type="text" 
              @click="deleteQuestion(index)"
              icon="el-icon-delete"
              style="color: #f56c6c;"
            >
              删除
            </el-button>
          </div>
        </div>

        <div class="question-item-content">
          <div class="question-preview">
            <strong>题目：</strong>
            <span>{{ getQuestionPreview(question) }}</span>
          </div>
          <div v-if="question.type === 0" class="options-preview">
            <strong>选项：</strong>
            <span>{{ getOptionsPreview(question) }}</span>
          </div>
          <div class="answer-preview">
            <strong>答案：</strong>
            <span>{{ getAnswerPreview(question) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QuestionEditor',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      questions: [],
      showAddForm: false,  // 替换 showEditDialog
      editingIndex: -1,
      currentQuestionIndex: -1,
      currentQuestion: {
        type: 0,
        title: '',
        maxScore: 10,
        difficulty: 3,
        options: [
          { text: '' },
          { text: '' },
          { text: '' },
          { text: '' }
        ],
        answer: null,
        explanation: ''
      },
      questionRules: {
        type: [
          { required: true, message: '请选择题型', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入题目内容', trigger: 'blur' }
        ],
        maxScore: [
          { required: true, message: '请输入分值', trigger: 'blur' }
        ],
        answer: [
          { required: true, message: '请输入正确答案', trigger: 'change' }
        ]
      },
      emitTimer: null,
      isEmitting: false
    }
  },
  watch: {
    value: {
      handler(newVal) {
        if (newVal) {
          try {
            // 避免直接赋值导致的潜在问题
            const parsedQuestions = JSON.parse(newVal) || [];
            this.$set(this, 'questions', [...parsedQuestions]);
          } catch (e) {
            this.$set(this, 'questions', []);
          }
        } else {
          this.$set(this, 'questions', []);
        }
      },
      immediate: true
    },
    questions: {
      handler(newVal) {
        // 添加延迟执行，避免在渲染过程中立即触发更新
        this.$nextTick(() => {
          this.debouncedEmitInput();
        });
      },
      deep: true
    }
  },
  methods: {
    debouncedEmitInput() {
      if (this.isEmitting) return;
      
      if (this.emitTimer) {
        clearTimeout(this.emitTimer);
      }
      
      this.emitTimer = setTimeout(() => {
        this.isEmitting = true;
        try {
          this.$emit('input', JSON.stringify(this.questions));
        } catch (error) {
          console.error('序列化题目数据失败:', error);
        } finally {
          this.isEmitting = false;
        }
      }, 300);
    },
    
    showAddQuestionForm() {
      this.editingIndex = -1;
      this.resetCurrentQuestion();
      this.showAddForm = true;
    },

    resetCurrentQuestion() {
      this.currentQuestion = {
        type: 0,
        title: '',
        maxScore: 10,
        difficulty: 3,
        options: [
          { text: '' },
          { text: '' },
          { text: '' },
          { text: '' }
        ],
        answer: null,
        explanation: ''
      };
    },

    cancelAddQuestion() {
      this.showAddForm = false;
      this.resetCurrentQuestion();
    },

    saveNewQuestion() {
      this.$refs.addQuestionForm.validate((valid) => {
        if (valid) {
          const questionCopy = JSON.parse(JSON.stringify(this.currentQuestion));
          this.questions.push(questionCopy);
          this.showAddForm = false;
          this.resetCurrentQuestion();
          this.$message.success('添加成功');
        }
      });
    },

    editQuestion(index) {
      this.editingIndex = index;
      this.currentQuestion = JSON.parse(JSON.stringify(this.questions[index]));
      this.showAddForm = true; // 重用表单来编辑
    },

    updateQuestion() {
      this.$refs.addQuestionForm.validate((valid) => {
        if (valid) {
          const questionCopy = JSON.parse(JSON.stringify(this.currentQuestion));
          this.$set(this.questions, this.editingIndex, questionCopy);
          this.showAddForm = false;
          this.resetCurrentQuestion();
          this.$message.success('更新成功');
        }
      });
    },

    deleteQuestion(index) {
      this.$confirm('确定要删除这道题目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.questions.splice(index, 1);
        this.$message.success('删除成功');
      });
    },

    saveQuestion() {
      if (this.editingIndex >= 0) {
        this.updateQuestion();
      } else {
        this.saveNewQuestion();
      }
    },

    moveQuestion(index, direction) {
      const newIndex = index + direction;
      if (newIndex >= 0 && newIndex < this.questions.length) {
        const temp = this.questions[index];
        this.$set(this.questions, index, this.questions[newIndex]);
        this.$set(this.questions, newIndex, temp);
      }
    },

    handleTypeChange() {
      this.currentQuestion.answer = null;
    },

    addOption() {
      if (this.currentQuestion.options.length < 8) {
        this.currentQuestion.options.push({ text: '' });
      } else {
        this.$message.warning('最多只能添加8个选项');
      }
    },

    removeOption(index) {
      if (this.currentQuestion.options.length > 2) {
        this.currentQuestion.options.splice(index, 1);
        if (this.currentQuestion.answer === index) {
          this.currentQuestion.answer = null;
        } else if (this.currentQuestion.answer > index) {
          this.currentQuestion.answer--;
        }
      }
    },

    getQuestionTypeText(type) {
      const types = ['选择题', '填空题', '计算题', '判断题'];
      return types[type] || '未知';
    },

    getQuestionTypeColor(type) {
      const colors = ['primary', 'success', 'warning', 'info'];
      return colors[type] || 'info';
    },

    getQuestionPreview(question) {
      if (!question.title) return '暂无内容';
      let content = question.title;
      if (content.length > 80) {
        content = content.substring(0, 80) + '...';
      }
      return content;
    },

    getOptionsPreview(question) {
      if (!question.options) return '暂无选项';
      
      if (Array.isArray(question.options)) {
        if (question.options.length === 0) return '暂无选项';
        return question.options.map((opt, i) => {
          const text = typeof opt === 'object' ? (opt.text || opt.option || '') : opt;
          return `${String.fromCharCode(65 + i)}. ${text}`;
        }).join(' | ');
      }
      
      if (typeof question.options === 'object') {
        const keys = Object.keys(question.options);
        if (keys.length === 0) return '暂无选项';
        return keys.map(key => `${key.toUpperCase()}. ${question.options[key]}`).join(' | ');
      }
      
      return '暂无选项';
    },

    getAnswerPreview(question) {
      if (question.answer === null || question.answer === undefined) return '未设置';
      if (question.type === 0) {
        return String.fromCharCode(65 + question.answer);
      } else if (question.type === 3) {
        return question.answer ? '正确' : '错误';
      }
      return question.answer;
    }
  },
  
  beforeDestroy() {
    if (this.emitTimer) {
      clearTimeout(this.emitTimer);
      this.emitTimer = null;
    }
    this.isEmitting = false;
  }
}
</script>

<style scoped>
.question-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  background: #fafafa;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.editor-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.questions-list {
  max-height: 500px;
  overflow-y: auto;
}

.question-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.question-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.question-item.active {
  border-color: #409eff;
  background: #f0f9ff;
}

.question-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.question-number {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  color: #333;
}

.question-actions {
  display: flex;
  gap: 5px;
}

.question-item-content {
  padding-left: 10px;
}

.question-preview,
.options-preview,
.answer-preview {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.question-preview strong,
.options-preview strong,
.answer-preview strong {
  color: #333;
  margin-right: 5px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.option-label {
  font-weight: bold;
  color: #409eff;
  min-width: 30px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.add-question-form {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.add-question-form h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}
</style>
