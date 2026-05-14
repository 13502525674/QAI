<template>
  <div class="physics-document-parser">
    <div class="parser-header">
      <h2>物理试卷文档解析</h2>
      <p>上传Word或PDF文档，系统将自动解析并生成物理试题</p>
    </div>
    
    <div class="parser-content">
        <div class="upload-section">
          <el-card class="upload-card">
            <div slot="header" class="clearfix">
              <span>文档上传</span>
            </div>
            
            <el-form :model="form" :rules="rules" ref="form" label-width="100px">
              <el-form-item label="试卷标题" prop="paperTitle">
                <el-input 
                  v-model="form.paperTitle" 
                  placeholder="请输入试卷标题"
                  style="width: 400px;"
                ></el-input>
              </el-form-item>
              
              <el-form-item label="物理分支" prop="subjectBranch">
                <el-select 
                  v-model="form.subjectBranch" 
                  placeholder="请选择物理分支"
                  style="width: 400px;"
                >
                  <el-option label="运动学" value="kinematics"></el-option>
                  <el-option label="力学" value="mechanics"></el-option>
                  <el-option label="电学" value="electrics"></el-option>
                  <el-option label="光学" value="optics"></el-option>
                  <el-option label="热学" value="thermodynamics"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="文档上传">
                <el-upload
                  class="upload-demo"
                  drag
                  :action="uploadActionUrl"
                  :headers="uploadHeaders"
                  :data="{ paperTitle: form.paperTitle, subjectBranch: form.subjectBranch }"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  :before-upload="beforeUpload"
                  :auto-upload="false"
                  ref="upload"
                  :limit="1"
                >
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                  <div class="el-upload__tip" slot="tip">
                    只能上传Word或PDF文件，且不超过50MB
                  </div>
                </el-upload>
                
                <div class="upload-actions" style="margin-top: 20px;">
                  <el-button 
                    type="primary" 
                    @click="submitUpload"
                    :disabled="!form.paperTitle || !form.subjectBranch"
                  >
                    解析并保存试卷
                  </el-button>
                  <el-button @click="resetForm">重置</el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </div>
  </div>
</template>

<script>
import { parsePhysicsDocument, parseAndSavePhysicsDocument } from '@/api/api'
import { getToken } from '@/utils/token'
import { marked } from 'marked'

export default {
  name: 'physicsDocumentParser',
  data() {
    return {
      form: {
        paperTitle: '',
        subjectBranch: ''
      },
      rules: {
        paperTitle: [
          { required: true, message: '请输入试卷标题', trigger: 'blur' }
        ],
        subjectBranch: [
          { required: true, message: '请选择物理分支', trigger: 'change' }
        ]
      },
      parsedContent: null,
      uploadData: {},
      uploadedFile: null,
      uploadActionUrl: '',
      uploadHeaders: {}
    }
  },
  created() {
      this.uploadActionUrl = `${this.$store.state.configure.HOST}/physics/document/parseAndSave`;
      this.uploadHeaders = {
        'x_access_token': getToken() || ''
      };
    },
  methods: {
    renderMarkdown(text) {
      const md = text == null ? '' : String(text)
      marked.setOptions({
        breaks: true,
        gfm: true,
        mangle: false,
        headerIds: false
      })
      return marked(md)
    },
    beforeUpload(file) {
      const allowedTypes = [
        'application/msword', 
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
        'application/pdf'
      ];
      const isAllowedType = allowedTypes.includes(file.type);
      const isLt50M = file.size / 1024 / 1024 < 50;

      if (!isAllowedType) {
        this.$message.error('只能上传Word或PDF文件!');
        return false;
      }
      if (!isLt50M) {
        this.$message.error('文件大小不能超过50MB!');
        return false;
      }
      
      return isAllowedType && isLt50M;
    },
    
    submitUpload() {
      // 检查是否有文件被选择
      if (!this.$refs.upload.uploadFiles || this.$refs.upload.uploadFiles.length === 0) {
        this.$message.error('请先选择要上传的文档');
        return;
      }

      this.uploadHeaders = {
        'x_access_token': getToken() || ''
      };
       
      // 验证表单
      this.$refs.form.validate((valid) => {
        if (valid) {
          // 设置上传时的附加参数
          this.uploadData = {
            paperTitle: this.form.paperTitle,
            subjectBranch: this.form.subjectBranch
          };
          
          // 手动触发上传
          this.$refs.upload.submit();
        } else {
          this.$message.error('请填写完整信息');
        }
      });
    },
    
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 1000) {
        this.$message.success('试卷解析并保存成功！');
        this.parsedContent = response.data;
        // 清空表单和文件，但不清空解析结果
        this.form = {
          paperTitle: '',
          subjectBranch: ''
        };
        this.uploadData = {};
        this.uploadedFile = null;
        this.$refs.upload.uploadFiles = []; // 清空上传列表
        this.$refs.form.clearValidate();
      } else {
        this.$message.error(response.message || '上传失败');
      }
    },
    
    handleUploadError(error, file, fileList) {
      this.$message.error('上传失败：' + (error.message || '未知错误'));
    },
    
    resetForm() {
      this.form = {
        paperTitle: '',
        subjectBranch: ''
      };
      this.parsedContent = null;
      this.uploadData = {};
      this.uploadedFile = null;
      this.$refs.upload.uploadFiles = []; // 清空上传列表
      this.$refs.form.clearValidate();
    }
  }
}
</script>

<style scoped>
.physics-document-parser {
  padding: 20px;
  background-color: #FDEBD3;
  min-height: calc(100vh - 100px);
}

.parser-header {
  text-align: center;
  margin-bottom: 30px;
}

.parser-header h2 {
  color: #4a5568;
  margin-bottom: 10px;
}

.parser-content {
  max-width: 1000px;
  margin: 0 auto;
}

.upload-card, .preview-card {
  margin-bottom: 20px;
  background-color: #DFDBC4;
  border-color: #D5D1BA;
}

.upload-actions {
  text-align: center;
}

.preview-content {
  max-height: 400px;
  overflow-y: auto;
}

.question-list {
  max-width: 900px;
}

.question-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px 14px;
  background: #e8e6d0;
  border: 1px solid #DFDBC4;
  border-radius: 6px;
  page-break-inside: avoid;
  break-inside: avoid;
}

.question-no {
  flex: 0 0 auto;
  font-weight: 600;
  line-height: 1.8;
}

.question-title {
  word-break: break-word;
  line-height: 1.8;
  flex: 1 1 auto;
}

.answer-content {
  word-break: break-word;
  line-height: 1.8;
}

.markdown-body {
  font-size: 14px;
  line-height: 1.9;
  color: #303133;
}

.markdown-body p {
  margin: 0 0 10px;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 22px;
  margin: 8px 0 10px;
}

.markdown-body li {
  margin: 6px 0;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
  margin: 12px 0 8px;
  font-weight: 600;
}
</style>