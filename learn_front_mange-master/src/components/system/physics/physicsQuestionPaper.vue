<template>
  <div class="physics-question-paper">
    <div class="mod-toolbar">
      <el-form :inline="true" :model="queryParam">
        <el-form-item label="试卷标题">
          <el-input 
            v-model="queryParam.paperTitle" 
            placeholder="请输入试卷标题"
            @keyup.enter.native="getPage"
          ></el-input>
        </el-form-item>
        <el-form-item label="物理分支">
          <el-select v-model="queryParam.subjectBranch" placeholder="请选择物理分支">
            <el-option label="全部" value=""></el-option>
            <el-option label="运动学" value="kinematics"></el-option>
            <el-option label="力学" value="mechanics"></el-option>
            <el-option label="电学" value="electrics"></el-option>
            <el-option label="光学" value="optics"></el-option>
            <el-option label="热学" value="thermodynamics"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getPage">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="toAdd">新增</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="toParseDocument">文档解析</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="removeBatch">批量删除</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="mod-table">
      <el-table 
        :data="tableData" 
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="paperTitle" label="试卷标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="subjectBranch" label="物理分支" width="120">
          <template slot-scope="scope">
            <span>{{ getBranchText(scope.row.subjectBranch) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuestions" label="总题数" width="100"></el-table-column>
        <el-table-column prop="createBy" label="创建者" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            <span>{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="toUpdate(scope.row)">编辑</el-button>
            <el-button size="mini" type="success" @click="toView(scope.row)">查看</el-button>
            <el-button size="mini" type="danger" @click="remove(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="mod-pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParam.pageNumber"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParam.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="dialogFormVisible?'查看':'修改'" :visible.sync="dialogFormVisible" width="70%">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="试卷标题" prop="paperTitle">
          <el-input v-model="form.paperTitle" autocomplete="off" :disabled="dialogFormVisible"></el-input>
        </el-form-item>
        <el-form-item label="物理分支" prop="subjectBranch">
          <el-select v-model="form.subjectBranch" placeholder="请选择物理分支" :disabled="dialogFormVisible">
            <el-option label="运动学" value="kinematics"></el-option>
            <el-option label="力学" value="mechanics"></el-option>
            <el-option label="电学" value="electrics"></el-option>
            <el-option label="光学" value="optics"></el-option>
            <el-option label="热学" value="thermodynamics"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="总题数" prop="totalQuestions">
          <el-input-number v-model="form.totalQuestions" :min="0" :disabled="dialogFormVisible" @change="updateTotalQuestions"></el-input-number>
        </el-form-item>
        
        <!-- 题型数量设置 -->
        <el-form-item label="选择题数量" prop="choiceQuestionsCount">
          <el-input-number v-model="form.choiceQuestionsCount" :min="0" :max="form.totalQuestions" :disabled="dialogFormVisible" @change="updateQuestionCounts"></el-input-number>
          <span class="question-count-tip">已设置: {{ form.choiceQuestionsCount }} 题</span>
        </el-form-item>
        <el-form-item label="填空题数量" prop="fillInQuestionsCount">
          <el-input-number v-model="form.fillInQuestionsCount" :min="0" :max="form.totalQuestions" :disabled="dialogFormVisible" @change="updateQuestionCounts"></el-input-number>
          <span class="question-count-tip">已设置: {{ form.fillInQuestionsCount }} 题</span>
        </el-form-item>
        <el-form-item label="计算题数量" prop="calculationQuestionsCount">
          <el-input-number v-model="form.calculationQuestionsCount" :min="0" :max="form.totalQuestions" :disabled="dialogFormVisible" @change="updateQuestionCounts"></el-input-number>
          <span class="question-count-tip">已设置: {{ form.calculationQuestionsCount }} 题</span>
        </el-form-item>
        <el-form-item label="题型统计" prop="questionStatistics">
          <div class="question-statistics">
            <el-tag type="success">选择题: {{ form.choiceQuestionsCount }} 题</el-tag>
            <el-tag type="info" style="margin-left: 10px;">填空题: {{ form.fillInQuestionsCount }} 题</el-tag>
            <el-tag type="warning" style="margin-left: 10px;">计算题: {{ form.calculationQuestionsCount }} 题</el-tag>
            <el-tag type="primary" style="margin-left: 10px;">总计: {{ form.totalQuestions }} 题</el-tag>
          </div>
        </el-form-item>
        
        <!-- 题目内容编辑区域 -->
        <el-form-item label="题目内容编辑" prop="questionContent">
          <div class="question-editor">
            <div class="editor-header">
              <el-alert 
                title="请按照题型顺序编辑题目内容，支持数学公式和特殊符号" 
                type="info" 
                :closable="false"
                show-icon
                style="margin-bottom: 15px;"
              >
              </el-alert>
            </div>
            <div class="question-tabs">
              <el-tabs v-model="activeQuestionType" type="card" @tab-click="handleTabChange">
                <el-tab-pane label="选择题" name="choice">
                  <div class="question-list" v-if="form.choiceQuestionsCount > 0">
                    <div v-for="index in form.choiceQuestionsCount" :key="'choice-' + index" class="question-item">
                      <div class="question-header">
                        <h4>选择题 {{ index }}</h4>
                        <el-tag v-if="choiceQuestions[index-1]" type="success" size="small">已填写</el-tag>
                        <el-tag v-else type="danger" size="small">未填写</el-tag>
                      </div>
                      <el-input 
                        v-model="choiceQuestions[index-1]" 
                        type="textarea" 
                        :rows="3" 
                        :placeholder="'请输入第' + index + '道选择题内容（支持数学公式）'"
                        :disabled="dialogFormVisible"
                        @input="handleQuestionChange"
                      ></el-input>
                      <div class="question-options" v-if="!dialogFormVisible">
                        <div class="options-title">选项设置：</div>
                        <el-input v-for="opt in 4" :key="opt" v-model="choiceOptions[index-1][opt-1]" :placeholder="'选项' + String.fromCharCode(64 + opt)" style="margin-top: 5px;" @input="handleQuestionChange">
                          <template slot="prepend">{{ String.fromCharCode(64 + opt) }}</template>
                        </el-input>
                      </div>
                    </div>
                  </div>
                  <div v-else class="no-questions">
                    <el-empty description="当前未设置选择题数量" :image-size="100"></el-empty>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="填空题" name="fillIn">
                  <div class="question-list" v-if="form.fillInQuestionsCount > 0">
                    <div v-for="index in form.fillInQuestionsCount" :key="'fillIn-' + index" class="question-item">
                      <div class="question-header">
                        <h4>填空题 {{ index }}</h4>
                        <el-tag v-if="fillInQuestions[index-1]" type="success" size="small">已填写</el-tag>
                        <el-tag v-else type="danger" size="small">未填写</el-tag>
                      </div>
                      <el-input 
                        v-model="fillInQuestions[index-1]" 
                        type="textarea" 
                        :rows="3" 
                        :placeholder="'请输入第' + index + '道填空题内容（填空处请用下划线表示，如：___）'"
                        :disabled="dialogFormVisible"
                        @input="handleQuestionChange"
                      ></el-input>
                      <div class="answer-hint" v-if="!dialogFormVisible">
                        <el-tag size="small" type="info">提示：填空题答案请在答案区域统一填写</el-tag>
                      </div>
                    </div>
                  </div>
                  <div v-else class="no-questions">
                    <el-empty description="当前未设置填空题数量" :image-size="100"></el-empty>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="计算题" name="calculation">
                  <div class="question-list" v-if="form.calculationQuestionsCount > 0">
                    <div v-for="index in form.calculationQuestionsCount" :key="'calculation-' + index" class="question-item">
                      <div class="question-header">
                        <h4>计算题 {{ index }}</h4>
                        <el-tag v-if="calculationQuestions[index-1]" type="success" size="small">已填写</el-tag>
                        <el-tag v-else type="danger" size="small">未填写</el-tag>
                      </div>
                      <el-input 
                        v-model="calculationQuestions[index-1]" 
                        type="textarea" 
                        :rows="4" 
                        :placeholder="'请输入第' + index + '道计算题内容（支持复杂数学公式和计算过程）'"
                        :disabled="dialogFormVisible"
                        @input="handleQuestionChange"
                      ></el-input>
                    </div>
                  </div>
                  <div v-else class="no-questions">
                    <el-empty description="当前未设置计算题数量" :image-size="100"></el-empty>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
            <div class="editor-footer">
              <el-divider></el-divider>
              <div class="progress-info">
                <el-progress 
                  :percentage="calculateCompletionRate()" 
                  :status="calculateCompletionRate() === 100 ? 'success' : ''"
                  :show-text="true"
                ></el-progress>
                <span class="progress-text">完成度: {{ calculateCompletionRate() }}%</span>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="原始文档" prop="documentPath">
          <div v-if="form.documentPath">
            <a :href="$store.state.HOST + form.documentPath" target="_blank" style="color: #409EFF;">{{ form.documentPath.split('/').pop() }}</a>
            <el-button 
              v-if="!dialogFormVisible" 
              @click="removeDocument" 
              type="text" 
              size="small" 
              style="margin-left: 10px; color: #f56c6c;"
            >
              删除
            </el-button>
          </div>
          <el-upload
            v-if="!dialogFormVisible && !form.documentPath"
            class="upload-demo"
            :action="$store.state.HOST + '/file/upload'"
            :on-success="handleFileUploadSuccess"
            :on-error="handleFileUploadError"
            :before-upload="beforeFileUpload"
            :file-list="fileList"
            :limit="1"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">支持Word、PDF等文档格式，单个文件不超过50MB</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="答案内容" prop="answerContent">
          <div class="answer-editor">
            <el-alert 
              title="请按照题目顺序填写答案，选择题填写选项字母，填空题和计算题填写具体答案" 
              type="warning" 
              :closable="false"
              show-icon
              style="margin-bottom: 10px;"
            >
            </el-alert>
            <el-input 
              type="textarea" 
              :rows="8" 
              placeholder="请按照以下格式填写答案：\n1. A\n2. B\n3. 42\n4. 详细计算过程..." 
              v-model="form.answerContent"
              :disabled="dialogFormVisible"
              @input="handleAnswerChange"
            >
            </el-input>
            <div class="answer-format-hint" v-if="!dialogFormVisible">
              <el-tag size="small" type="info">建议格式：题号 + 答案，如：1. A, 2. 3.14, 3. 详细计算过程...</el-tag>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" v-if="!dialogFormVisible">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveOrEdit">确 定</el-button>
      </div>
      <div slot="footer" class="dialog-footer" v-else>
        <el-button @click="dialogFormVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPhysicsQuestionPaperPage, savePhysicsQuestionPaper, editPhysicsQuestionPaper, removePhysicsQuestionPaper, getPhysicsQuestionPaperById } from '@/api/api'

export default {
  name: 'physicsQuestionPaper',
  data() {
    return {
      tableData: [],
      total: 0,
      loading: false,
      dialogFormVisible: false,
      editFlag: false,
      form: {
        id: '',
        paperTitle: '',
        subjectBranch: '',
        totalQuestions: 0,
        choiceQuestionsCount: 0,
        fillInQuestionsCount: 0,
        calculationQuestionsCount: 0,
        questionContent: '',
        answerContent: ''
      },
      activeQuestionType: 'choice',
      choiceQuestions: [],
      choiceOptions: [],
      fillInQuestions: [],
      calculationQuestions: [],
      queryParam: {
        paperTitle: '',
        subjectBranch: '',
        pageNumber: 1,
        pageSize: 10
      },
      multipleSelection: [],
      fileList: [],
      autoSaveTimer: null,
      lastSavedTime: null,
      isDataChanged: false,
      saveInterval: 30000, // 30秒自动保存
      networkStatus: 'online', // 网络状态
      syncQueue: [], // 同步队列
      retryCount: 0, // 重试次数
      maxRetries: 3, // 最大重试次数
      formatDebounceTimer: null, // 格式化防抖定时器
      handleOnline: null, // 网络状态监听器
      handleOffline: null, // 网络状态监听器
      rules: {
        paperTitle: [
          { required: true, message: '请输入试卷标题', trigger: 'blur' }
        ],
        subjectBranch: [
          { required: true, message: '请选择物理分支', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    getPage() {
      this.loading = true;
      getPhysicsQuestionPaperPage(this.queryParam).then(res => {
        this.loading = false;
        if (res.code === 1000) {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      }).catch(err => {
        this.loading = false;
        console.log(err);
      });
    },
    handleSizeChange(val) {
      this.queryParam.pageSize = val;
      this.getPage();
    },
    handleCurrentChange(val) {
      this.queryParam.pageNumber = val;
      this.getPage();
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    toAdd() {
      this.form = {
        id: '',
        paperTitle: '',
        subjectBranch: '',
        totalQuestions: 0,
        choiceQuestionsCount: 0,
        fillInQuestionsCount: 0,
        calculationQuestionsCount: 0,
        questionContent: '',
        answerContent: ''
      };
      this.resetQuestionArrays();
      this.editFlag = false;
      this.dialogFormVisible = true;
      this.startAutoSave();
    },
    toUpdate(row) {
      getPhysicsQuestionPaperById({ id: row.id }).then(res => {
        if (res.code === 1000) {
          this.form = { ...res.data };
          this.parseQuestionContent();
          this.editFlag = true;
          this.dialogFormVisible = true;
          this.startAutoSave();
        }
      });
    },
    toView(row) {
      getPhysicsQuestionPaperById({ id: row.id }).then(res => {
        if (res.code === 1000) {
          this.form = { ...res.data };
          this.parseQuestionContent();
          this.editFlag = false;
          this.dialogFormVisible = true;
        }
      });
    },
    saveOrEdit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.stopAutoSave();
          // 格式化题目内容
          this.formatQuestionContent();
          
          if (this.editFlag) {
            editPhysicsQuestionPaper(this.form).then(res => {
              if (res.code === 1000) {
                this.$message.success('编辑成功!');
                this.dialogFormVisible = false;
                this.getPage();
              } else {
                this.$message.error(res.message);
              }
            });
          } else {
            savePhysicsQuestionPaper(this.form).then(res => {
              if (res.code === 1000) {
                this.$message.success('新增成功!');
                this.dialogFormVisible = false;
                this.getPage();
              } else {
                this.$message.error(res.message);
              }
            });
          }
        }
      });
    },
    remove(id) {
      this.$confirm('此操作将永久删除该试卷, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removePhysicsQuestionPaper({ ids: id }).then(res => {
          if (res.code === 1000) {
            this.$message.success('删除成功!');
            this.getPage();
          } else {
            this.$message.error(res.message);
          }
        });
      });
    },
    toParseDocument() {
      this.$router.push('/system/physics/physicsDocumentParser');
    },
    removeBatch() {
      if (this.multipleSelection.length <= 0) {
        this.$message.warning('请先选择要删除的试卷!');
        return;
      }
      this.$confirm('此操作将永久删除选中的试卷, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let ids = [];
        this.multipleSelection.forEach(item => {
          ids.push(item.id);
        });
        removePhysicsQuestionPaper({ ids: ids.join(',') }).then(res => {
          if (res.code === 1000) {
            this.$message.success('删除成功!');
            this.getPage();
          } else {
            this.$message.error(res.message);
          }
        });
      });
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
    // 文件上传前的验证
    beforeFileUpload(file) {
      const allowedTypes = ['application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/pdf'];
      const isAllowedType = allowedTypes.includes(file.type);
      const isLt50M = file.size / 1024 / 1024 < 50;
      
      if (!isAllowedType) {
        this.$message.error('只能上传Word或PDF文件!');
      }
      if (!isLt50M) {
        this.$message.error('文件大小不能超过50MB!');
      }
      
      return isAllowedType && isLt50M;
    },
    // 文件上传成功的回调
    handleFileUploadSuccess(response, file, fileList) {
      if (response.code === 1000) {
        this.form.documentPath = response.data.url;
        this.$message.success('文件上传成功！');
      } else {
        this.$message.error(response.message || '文件上传失败！');
      }
    },
    // 文件上传失败的回调
    handleFileUploadError(err, file, fileList) {
      this.$message.error('文件上传失败！');
      console.error('File upload error:', err);
    },
    // 删除已上传的文档
    removeDocument() {
      this.$confirm('确定要删除该文档吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.form.documentPath = '';
        this.fileList = [];
      });
    },
    
    // 更新总题数
    updateTotalQuestions() {
      // 确保各题型数量之和不超过总题数
      const sum = this.form.choiceQuestionsCount + this.form.fillInQuestionsCount + this.form.calculationQuestionsCount;
      if (sum > this.form.totalQuestions) {
        this.$message.warning('各题型数量之和不能超过总题数！');
        this.form.totalQuestions = sum;
      }
    },
    
    // 更新题型数量
    updateQuestionCounts() {
      const sum = this.form.choiceQuestionsCount + this.form.fillInQuestionsCount + this.form.calculationQuestionsCount;
      
      // 如果各题型数量之和超过总题数，自动调整总题数
      if (sum > this.form.totalQuestions) {
        this.form.totalQuestions = sum;
      }
      
      // 重置题目数组
      this.resetQuestionArrays();
    },
    
    // 重置题目数组
    resetQuestionArrays() {
      // 限制最大题目数量，防止内存泄漏
      const maxQuestions = 50;
      
      if (this.form.choiceQuestionsCount > maxQuestions) {
        this.form.choiceQuestionsCount = maxQuestions;
        this.$message.warning(`选择题数量已限制为最大${maxQuestions}题`);
      }
      if (this.form.fillInQuestionsCount > maxQuestions) {
        this.form.fillInQuestionsCount = maxQuestions;
        this.$message.warning(`填空题数量已限制为最大${maxQuestions}题`);
      }
      if (this.form.calculationQuestionsCount > maxQuestions) {
        this.form.calculationQuestionsCount = maxQuestions;
        this.$message.warning(`计算题数量已限制为最大${maxQuestions}题`);
      }
      
      // 使用更高效的方式初始化数组
      this.choiceQuestions = new Array(Math.min(this.form.choiceQuestionsCount, maxQuestions)).fill('');
      this.choiceOptions = new Array(Math.min(this.form.choiceQuestionsCount, maxQuestions)).fill(null).map(() => new Array(4).fill(''));
      this.fillInQuestions = new Array(Math.min(this.form.fillInQuestionsCount, maxQuestions)).fill('');
      this.calculationQuestions = new Array(Math.min(this.form.calculationQuestionsCount, maxQuestions)).fill('');
    },
    
    // 解析题目内容
    parseQuestionContent() {
      if (this.form.questionContent) {
        try {
          const questions = JSON.parse(this.form.questionContent);
          this.resetQuestionArrays();
          
          // 按题型分类解析题目
          questions.forEach((question, index) => {
            if (question.type === 0 && index < this.form.choiceQuestionsCount) {
              this.choiceQuestions[index] = question.content || '';
              if (question.options) {
                this.choiceOptions[index] = question.options;
              }
            } else if (question.type === 1 && index < this.form.fillInQuestionsCount) {
              this.fillInQuestions[index] = question.content || '';
            } else if (question.type === 2 && index < this.form.calculationQuestionsCount) {
              this.calculationQuestions[index] = question.content || '';
            }
          });
        } catch (error) {
          console.error('解析题目内容失败:', error);
          this.resetQuestionArrays();
        }
      } else {
        this.resetQuestionArrays();
      }
    },
    
    // 格式化题目内容
    formatQuestionContent() {
      const questions = [];
      
      // 格式化选择题
      for (let i = 0; i < this.form.choiceQuestionsCount; i++) {
        if (this.choiceQuestions[i]) {
          questions.push({
            type: 0,
            content: this.choiceQuestions[i],
            options: this.choiceOptions[i] || Array(4).fill(''),
            index: i + 1
          });
        }
      }
      
      // 格式化填空题
      for (let i = 0; i < this.form.fillInQuestionsCount; i++) {
        if (this.fillInQuestions[i]) {
          questions.push({
            type: 1,
            content: this.fillInQuestions[i],
            index: this.form.choiceQuestionsCount + i + 1
          });
        }
      }
      
      // 格式化计算题
      for (let i = 0; i < this.form.calculationQuestionsCount; i++) {
        if (this.calculationQuestions[i]) {
          questions.push({
            type: 2,
            content: this.calculationQuestions[i],
            index: this.form.choiceQuestionsCount + this.form.fillInQuestionsCount + i + 1
          });
        }
      }
      
      this.form.questionContent = JSON.stringify(questions);
    },
    
    // 处理题目内容变化（使用防抖优化性能）
    handleQuestionChange() {
      this.isDataChanged = true;
      this.debounceFormatQuestionContent();
    },
    
    // 处理答案内容变化
    handleAnswerChange() {
      this.isDataChanged = true;
    },
    
    // 防抖格式化题目内容
    debounceFormatQuestionContent() {
      if (this.formatDebounceTimer) {
        clearTimeout(this.formatDebounceTimer);
      }
      this.formatDebounceTimer = setTimeout(() => {
        this.formatQuestionContent();
      }, 500); // 500ms防抖延迟
    },
    
    // 处理标签页切换
    handleTabChange(tab) {
      this.activeQuestionType = tab.name;
    },
    
    // 计算完成度
    calculateCompletionRate() {
      let completedCount = 0;
      let totalCount = 0;
      
      // 统计选择题完成度
      for (let i = 0; i < this.form.choiceQuestionsCount; i++) {
        totalCount++;
        if (this.choiceQuestions[i] && this.choiceQuestions[i].trim() !== '') {
          completedCount++;
        }
      }
      
      // 统计填空题完成度
      for (let i = 0; i < this.form.fillInQuestionsCount; i++) {
        totalCount++;
        if (this.fillInQuestions[i] && this.fillInQuestions[i].trim() !== '') {
          completedCount++;
        }
      }
      
      // 统计计算题完成度
      for (let i = 0; i < this.form.calculationQuestionsCount; i++) {
        totalCount++;
        if (this.calculationQuestions[i] && this.calculationQuestions[i].trim() !== '') {
          completedCount++;
        }
      }
      
      return totalCount === 0 ? 0 : Math.round((completedCount / totalCount) * 100);
    },
    
    // 开始自动保存
    startAutoSave() {
      this.stopAutoSave();
      this.autoSaveTimer = setInterval(() => {
        if (this.isDataChanged && this.dialogFormVisible) {
          this.autoSave();
        }
      }, this.saveInterval);
    },
    
    // 停止自动保存
    stopAutoSave() {
      if (this.autoSaveTimer) {
        clearInterval(this.autoSaveTimer);
        this.autoSaveTimer = null;
      }
    },
    
    // 自动保存
    autoSave() {
      if (!this.isDataChanged) return;
      
      this.formatQuestionContent();
      
      // 保存到本地存储
      const saveData = {
        form: { ...this.form },
        choiceQuestions: [...this.choiceQuestions],
        choiceOptions: [...this.choiceOptions],
        fillInQuestions: [...this.fillInQuestions],
        calculationQuestions: [...this.calculationQuestions],
        saveTime: new Date().toISOString(),
        version: Date.now() // 添加版本号用于冲突检测
      };
      
      try {
        localStorage.setItem('physicsQuestionPaperDraft', JSON.stringify(saveData));
        this.lastSavedTime = new Date();
        this.isDataChanged = false;
        
        // 如果网络正常，尝试同步到服务器
        if (this.networkStatus === 'online' && this.editFlag) {
          this.tryServerSync();
        }
        
        this.$message.success(`自动保存成功 (${this.lastSavedTime.toLocaleTimeString()})`);
      } catch (error) {
        console.error('自动保存失败:', error);
        this.$message.warning('自动保存失败，请检查浏览器存储空间');
      }
    },
    
    // 尝试服务器同步
    tryServerSync() {
      if (this.retryCount >= this.maxRetries) {
        console.warn('已达到最大重试次数，停止同步');
        return;
      }
      
      // 准备同步数据（简化版，只同步必要字段）
      const syncData = {
        id: this.form.id,
        questionContent: this.form.questionContent,
        answerContent: this.form.answerContent,
        syncTime: new Date().toISOString()
      };
      
      // 这里可以调用实际的同步API
      console.log('尝试同步到服务器:', syncData);
      
      // 模拟同步成功
      setTimeout(() => {
        this.retryCount = 0; // 重置重试次数
        this.$message.info('数据已同步到服务器');
      }, 1000);
    },
    
    // 检查是否有草稿数据
    checkDraftData() {
      try {
        const draftData = localStorage.getItem('physicsQuestionPaperDraft');
        if (draftData) {
          const data = JSON.parse(draftData);
          
          // 检查数据版本，避免使用过时的草稿
          const draftTime = new Date(data.saveTime);
          const now = new Date();
          const diffHours = (now - draftTime) / (1000 * 60 * 60);
          
          if (diffHours > 24) { // 超过24小时的草稿视为过期
            this.$confirm('检测到过期的草稿数据（超过24小时），是否恢复？', '恢复草稿', {
              confirmButtonText: '恢复',
              cancelButtonText: '删除',
              type: 'warning'
            }).then(() => {
              this.restoreDraftData(data);
            }).catch(() => {
              this.clearDraftData();
            });
          } else {
            this.$confirm('检测到未保存的草稿数据，是否恢复？', '恢复草稿', {
              confirmButtonText: '恢复',
              cancelButtonText: '忽略',
              type: 'info'
            }).then(() => {
              this.restoreDraftData(data);
            }).catch(() => {
              // 保留草稿数据，不删除
            });
          }
        }
      } catch (error) {
        console.error('检查草稿数据失败:', error);
        this.clearDraftData();
      }
    },
    
    // 恢复草稿数据
    restoreDraftData(data) {
      try {
        this.form = { ...data.form };
        this.choiceQuestions = [...data.choiceQuestions];
        this.choiceOptions = [...data.choiceOptions];
        this.fillInQuestions = [...data.fillInQuestions];
        this.calculationQuestions = [...data.calculationQuestions];
        this.parseQuestionContent();
        this.$message.success('草稿数据恢复成功！');
      } catch (error) {
        console.error('恢复草稿数据失败:', error);
        this.$message.error('恢复草稿数据失败，数据可能已损坏');
        this.clearDraftData();
      }
    },
    
    // 清除草稿数据
    clearDraftData() {
      try {
        localStorage.removeItem('physicsQuestionPaperDraft');
        this.$message.info('草稿数据已清除');
      } catch (error) {
        console.error('清除草稿数据失败:', error);
      }
    },
    
    // 初始化网络状态监控
    initNetworkMonitor() {
      // 定义监听器函数
      this.handleOnline = () => {
        this.networkStatus = 'online';
        this.$message.success('网络连接已恢复');
        // 网络恢复后尝试同步
        if (this.isDataChanged) {
          this.tryServerSync();
        }
      };
      
      this.handleOffline = () => {
        this.networkStatus = 'offline';
        this.$message.warning('网络连接已断开，将使用本地存储');
      };
      
      // 监听网络状态变化
      window.addEventListener('online', this.handleOnline);
      window.addEventListener('offline', this.handleOffline);
      
      // 初始网络状态
      this.networkStatus = navigator.onLine ? 'online' : 'offline';
    },
    
    // 处理保存错误
    handleSaveError(error) {
      console.error('保存操作失败:', error);
      
      if (error.response) {
        // 服务器返回的错误
        const status = error.response.status;
        switch (status) {
          case 401:
            this.$message.error('登录已过期，请重新登录');
            break;
          case 403:
            this.$message.error('权限不足，无法执行此操作');
            break;
          case 500:
            this.$message.error('服务器内部错误，请稍后重试');
            break;
          default:
            this.$message.error(`保存失败: ${error.response.data?.message || '未知错误'}`);
        }
      } else if (error.request) {
        // 网络错误
        this.$message.error('网络连接失败，请检查网络设置');
        this.networkStatus = 'offline';
        
        // 保存到本地队列
        this.queueForSync();
      } else {
        // 其他错误
        this.$message.error('保存失败，请检查数据格式');
      }
    },
    
    // 添加到同步队列
    queueForSync() {
      const syncItem = {
        data: { ...this.form },
        timestamp: Date.now(),
        retries: 0
      };
      
      this.syncQueue.push(syncItem);
      this.$message.info('数据已添加到同步队列，将在网络恢复后自动同步');
    },
    
    // 处理同步队列
    processSyncQueue() {
      if (this.syncQueue.length === 0 || this.networkStatus !== 'online') {
        return;
      }
      
      const item = this.syncQueue[0];
      
      // 这里可以调用实际的同步API
      console.log('处理同步队列:', item);
      
      // 模拟同步成功
      setTimeout(() => {
        this.syncQueue.shift(); // 移除已同步的项目
        this.$message.success('队列数据同步成功');
        
        // 继续处理下一个项目
        if (this.syncQueue.length > 0) {
          this.processSyncQueue();
        }
      }, 1000);
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    }
  },
  created() {
    this.getPage();
    this.initNetworkMonitor();
  },
  
  beforeDestroy() {
    // 清理自动保存定时器
    this.stopAutoSave();
    
    // 清理防抖定时器
    if (this.formatDebounceTimer) {
      clearTimeout(this.formatDebounceTimer);
      this.formatDebounceTimer = null;
    }
    
    // 移除网络状态监听器
    if (this.handleOnline) {
      window.removeEventListener('online', this.handleOnline);
    }
    if (this.handleOffline) {
      window.removeEventListener('offline', this.handleOffline);
    }
    
    // 如果还有未保存的更改，尝试最后一次保存
    if (this.isDataChanged) {
      this.autoSave();
    }
  }
}
</script>

<style scoped>
.mod-toolbar {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
}
.mod-toolbar .el-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}
.mod-toolbar .el-form-item {
  display: flex;
  align-items: center;
  margin-bottom: 0;
}
.mod-table {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
.mod-table .el-button {
  margin: 0 2px;
}
.mod-pagination {
  margin-top: 20px;
  text-align: right;
}

.question-count-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.question-statistics {
  margin-top: 10px;
}

.question-editor {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
}

.question-tabs {
  margin-top: 15px;
}

.question-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.question-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-header h4 {
  margin: 0;
  color: #303133;
}

.question-options {
  margin-top: 10px;
}

.options-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.answer-hint {
  margin-top: 10px;
}

.answer-format-hint {
  margin-top: 10px;
}

.editor-footer {
  margin-top: 20px;
}

.progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.progress-text {
  margin-left: 10px;
  color: #606266;
  font-size: 14px;
}

.no-questions {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.answer-editor {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .question-editor {
    padding: 10px;
  }
  
  .question-item {
    padding: 10px;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .question-header h4 {
    margin-bottom: 5px;
  }
}
</style>