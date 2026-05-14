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
    <el-dialog :title="editFlag ? '编辑试卷' : (dialogFormVisible ? '查看试卷' : '新增试卷')" :visible.sync="dialogFormVisible" width="70%">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="试卷标题" prop="paperTitle">
          <el-input v-model="form.paperTitle" autocomplete="off" :disabled="dialogFormVisible && !editFlag"></el-input>
        </el-form-item>
        <el-form-item label="物理分支" prop="subjectBranch">
          <el-select v-model="form.subjectBranch" placeholder="请选择物理分支" :disabled="dialogFormVisible && !editFlag">
            <el-option label="运动学" value="kinematics"></el-option>
            <el-option label="力学" value="mechanics"></el-option>
            <el-option label="电学" value="electrics"></el-option>
            <el-option label="光学" value="optics"></el-option>
            <el-option label="热学" value="thermodynamics"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="questionContent">
          <question-editor v-model="form.questionContent" :disabled="dialogFormVisible && !editFlag"></question-editor>
        </el-form-item>
        <el-form-item label="原始文档" prop="documentPath">
          <div v-if="form.documentPath">
            <a :href="$store.state.HOST + form.documentPath" target="_blank" style="color: #409EFF;">{{ form.documentPath.split('/').pop() }}</a>
            <el-button 
              v-if="editFlag" 
              @click="removeDocument" 
              type="text" 
              size="small" 
              style="margin-left: 10px; color: #f56c6c;"
            >
              删除
            </el-button>
          </div>
          <el-upload
            v-if="editFlag && !form.documentPath"
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
      </el-form>
      <div slot="footer" class="dialog-footer" v-if="editFlag">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveOrEdit">确 定</el-button>
      </div>
      <div slot="footer" class="dialog-footer" v-else-if="!dialogFormVisible">
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
import QuestionEditor from '@/components/system/physics/QuestionEditor'

export default {
  name: 'physicsQuestionPaper',
  components: {
    QuestionEditor
  },
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
        questionContent: ''
      },
      queryParam: {
        paperTitle: '',
        subjectBranch: '',
        pageNumber: 1,
        pageSize: 10
      },
      multipleSelection: [],
      fileList: [],
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
        questionContent: '',
        answerContent: ''
      };
      this.editFlag = true;
      this.dialogFormVisible = true;
    },
    toUpdate(row) {
      getPhysicsQuestionPaperById({ id: row.id }).then(res => {
        if (res.code === 1000) {
          this.form = { ...res.data };
          this.editFlag = true;
          this.dialogFormVisible = true;
        }
      });
    },
    toView(row) {
      getPhysicsQuestionPaperById({ id: row.id }).then(res => {
        if (res.code === 1000) {
          this.form = { ...res.data };
          this.editFlag = false;
          this.dialogFormVisible = true;
        }
      });
    },
    saveOrEdit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          // 计算总题数
          try {
            if (this.form.questionContent) {
              const questions = JSON.parse(this.form.questionContent);
              this.form.totalQuestions = questions.length;
            } else {
              this.form.totalQuestions = 0;
            }
          } catch (e) {
            this.$message.error('题目内容格式错误，请检查');
            return;
          }
          
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
        this.$message.success('文档已删除！');
      }).catch(() => {
        // 用户取消删除
      });
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    }
  },
  created() {
    this.getPage();
  }
}
</script>

<style scoped>
.mod-toolbar {
  background: #FDEBD3;
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
  background: #FDEBD3;
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
</style>
