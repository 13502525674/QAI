<template>
  <div class="physics-document-upload">
    <headerPage></headerPage>
    <div class="upload-content">
      <div class="upload-container">
        <h2>物理题库文档上传</h2>
        <p>支持上传Word文档(.doc, .docx)和PDF文件(.pdf)，用于自动解析生成物理题库</p>
        
        <div class="upload-section">
          <div class="upload-form">
            <el-form :model="form" :rules="rules" ref="uploadForm" label-width="100px">
              <el-form-item label="试卷标题" prop="paperTitle">
                <el-input v-model="form.paperTitle" placeholder="请输入试卷标题" />
              </el-form-item>
              
              <el-form-item label="物理分支" prop="subjectBranch">
                <el-select v-model="form.subjectBranch" placeholder="请选择物理分支">
                  <el-option label="运动学" value="kinematics"></el-option>
                  <el-option label="力学" value="mechanics"></el-option>
                  <el-option label="电学" value="electrics"></el-option>
                  <el-option label="光学" value="optics"></el-option>
                  <el-option label="热学" value="thermodynamics"></el-option>
                  <el-option label="其他" value="other"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="文档上传" prop="file">
                <el-upload
                  class="upload-demo"
                  drag
                  :file-list="fileList"
                  :on-change="handleFileChange"
                  :on-remove="handleFileRemove"
                  :before-upload="beforeUpload"
                  :auto-upload="false"
                  accept=".pdf,.doc,.docx"
                  multiple>
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                  <div class="el-upload__tip" slot="tip">只能上传PDF、DOC、DOCX文件，且不超过10MB</div>
                </el-upload>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="submitUpload"
                  :loading="uploading"
                  :disabled="!form.file || !form.paperTitle || !form.subjectBranch">
                  {{ uploading ? '上传中...' : '开始上传解析' }}
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="preview-section" v-if="parsedData">
            <h3>解析预览</h3>
            <div class="preview-content">
              <div class="preview-stats">
                <el-tag type="success">共解析 {{ parsedData.questions.length }} 道题</el-tag>
                <el-tag type="info">答案部分：{{ parsedData.answers ? '已解析' : '未找到' }}</el-tag>
              </div>
              
              <div class="preview-questions" v-if="parsedData.questions && parsedData.questions.length > 0">
                <h4>题目预览（前3道）：</h4>
                <div 
                  v-for="(question, index) in parsedData.questions.slice(0, 3)" 
                  :key="index" 
                  class="preview-question">
                  <div class="question-header">
                    <span class="question-number">{{ question.questionNumber }}.</span>
                    <el-tag 
                      size="mini" 
                      :type="getQuestionTypeTag(question.type)">
                      {{ getQuestionTypeName(question.type) }}
                    </el-tag>
                  </div>
                  <div class="question-content" v-html="formatQuestionContent(question)"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import { parseAndSavePhysicsDocument } from '../../api/api'
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsDocumentUpload",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      form: {
        paperTitle: '',
        subjectBranch: '',
        file: null
      },
      rules: {
        paperTitle: [
          { required: true, message: '请输入试卷标题', trigger: 'blur' }
        ],
        subjectBranch: [
          { required: true, message: '请选择物理分支', trigger: 'change' }
        ],
        file: [
          { required: true, message: '请选择要上传的文件', trigger: 'change' }
        ]
      },
      fileList: [],
      uploading: false,
      parsedData: null
    }
  },
  methods: {
    handleFileChange(file, fileList) {
      // 只保留最后一个上传的文件
      if (fileList.length > 1) {
        this.fileList = [fileList[fileList.length - 1]]
      } else {
        this.fileList = fileList
      }
      
      this.form.file = file.raw
    },
    
    handleFileRemove(file, fileList) {
      this.form.file = null
      this.fileList = []
    },
    
    beforeUpload(file) {
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        this.$message.error('上传文件大小不能超过 10MB!')
        return false
      }
      return true
    },
    
    async submitUpload() {
      // 检查用户是否已登录
      const userToken = window.localStorage.getItem("user_token");
      if (!userToken) {
        this.$message.error('请先登录后再上传文档');
        this.$router.push('/login');
        return;
      }
      
      this.$refs.uploadForm.validate((valid) => {
        if (!valid) {
          this.$message.error('请填写完整的信息')
          return
        }
        
        if (!this.form.file) {
          this.$message.error('请选择要上传的文件')
          return
        }
        
        this.uploadDocument()
      })
    },
    
    async uploadDocument() {
      this.uploading = true
      this.parsedData = null
      
      try {
        // 检查token是否存在
        const token = window.localStorage.getItem("user_token");
        if (!token) {
          this.$message.error('用户未登录，请先登录');
          this.$router.push('/login');
          return;
        }
        
        const formData = new FormData()
        formData.append('file', this.form.file)
        formData.append('paperTitle', this.form.paperTitle)
        formData.append('subjectBranch', this.form.subjectBranch)
        
        console.log('Sending request with token:', token); // 调试信息
        console.log('Form data keys:', [...formData.keys()]); // 调试信息
        
        const response = await parseAndSavePhysicsDocument(formData)
        
        console.log('Response received:', response); // 调试信息
        
        if (response.code === 1000) {
          this.$message.success('文档上传并解析成功！')
          // 如果响应中包含解析的数据，显示预览
          if (response.data && response.data.questionContent) {
            try {
              this.parsedData = {
                questions: JSON.parse(response.data.questionContent),
                answers: response.data.answerContent
              }
            } catch (e) {
              console.error('解析题目数据失败:', e)
            }
          }
        } else {
          this.$message.error(response.message || '上传失败')
        }
      } catch (error) {
        console.error('上传错误:', error)
        console.error('Error details:', error.response || error.message); // 更详细的错误信息
        if (error.response && error.response.data && error.response.data.code === 1006) {
          this.$message.error('登录已过期，请重新登录');
          this.$router.push('/login');
        } else {
          this.$message.error(error.message || '上传过程中发生错误')
        }
      } finally {
        this.uploading = false
      }
    },
    
    resetForm() {
      this.form = {
        paperTitle: '',
        subjectBranch: '',
        file: null
      }
      this.fileList = []
      this.parsedData = null
      this.$refs.uploadForm.resetFields()
    },
    
    getQuestionTypeTag(type) {
      const types = {
        0: 'primary', // 选择题
        1: 'warning', // 填空题
        2: 'danger',  // 计算题
        3: 'info'     // 判断题
      }
      return types[type] || 'info'
    },
    
    getQuestionTypeName(type) {
      const types = {
        0: '选择题',
        1: '填空题',
        2: '计算题',
        3: '判断题'
      }
      return types[type] || '未知题型'
    },
    
    formatQuestionContent(question) {
      let content = question.title || ''
      
      if (question.options) {
        if (typeof question.options === 'object' && !Array.isArray(question.options)) {
          const keys = Object.keys(question.options);
          if (keys.length > 0) {
            content += '<br/><div class="options">'
            const sortedKeys = keys.sort();
            for (const key of sortedKeys) {
              content += `<div class="option"><strong>${key.toUpperCase()}.</strong> ${question.options[key]}</div>`
            }
            content += '</div>'
          }
        } else if (Array.isArray(question.options) && question.options.length > 0) {
          content += '<br/><div class="options">'
          question.options.forEach((opt, index) => {
            const text = typeof opt === 'object' ? (opt.text || opt.option || '') : opt;
            content += `<div class="option"><strong>${String.fromCharCode(65 + index)}.</strong> ${text}</div>`
          });
          content += '</div>'
        }
      }
      
      return content
    }
  }
}
</script>

<style scoped>
.physics-document-upload {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.upload-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.upload-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.upload-container h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 24px;
  text-align: center;
}

.upload-container p {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
}

.upload-section {
  display: flex;
  gap: 30px;
}

.upload-form {
  flex: 1;
  min-width: 400px;
}

.preview-section {
  flex: 1;
  min-width: 400px;
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.preview-section h3 {
  margin-top: 0;
  color: #333;
}

.preview-stats {
  margin-bottom: 20px;
}

.preview-question {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  background: white;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.question-number {
  font-weight: bold;
  margin-right: 10px;
  color: #409eff;
}

.question-content {
  line-height: 1.6;
  color: #333;
}

.options {
  margin-top: 10px;
}

.option {
  margin: 5px 0;
  padding: 5px 0;
}

@media (max-width: 768px) {
  .upload-section {
    flex-direction: column;
  }
  
  .upload-form, .preview-section {
    min-width: auto;
  }
}
</style>