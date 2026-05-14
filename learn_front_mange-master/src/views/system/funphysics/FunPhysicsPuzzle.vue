<template>
  <div class="_fun-physics-puzzle">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding:10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">标题:</span>
            <el-input style="margin-top:10px" size="mini" placeholder="请输入标题" v-model="search.title"></el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">类型:</span>
            <el-select style="margin-top:10px" size="mini" v-model="search.type" placeholder="请选择" clearable>
              <el-option label="公式拼图" :value="0"></el-option>
              <el-option label="概念拼图" :value="1"></el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">难度:</span>
            <el-select style="margin-top:10px" size="mini" v-model="search.difficulty" placeholder="请选择" clearable>
              <el-option label="简单" :value="1"></el-option>
              <el-option label="中等" :value="2"></el-option>
              <el-option label="困难" :value="3"></el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">分支:</span>
            <el-select style="margin-top:10px" size="mini" v-model="search.branch" placeholder="请选择" clearable>
              <el-option label="力学" value="mechanics"></el-option>
              <el-option label="电磁学" value="electrics"></el-option>
              <el-option label="光学" value="optics"></el-option>
              <el-option label="热学" value="thermodynamics"></el-option>
              <el-option label="近代物理" value="modern"></el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button style="margin-top:10px" size="mini" icon="el-icon-search" type="primary" @click="searchPage">查询</el-button>
            <el-button style="margin-top:10px" size="mini" icon="el-icon-refresh" @click="refresh">重置</el-button>
          </el-col>
        </el-row>
      </div>
      <div class="table">
        <el-row style="padding-top:10px;margin-left:10px">
          <el-button type="primary" size="mini" icon="el-icon-plus" @click="add">新增</el-button>
          <el-button type="danger" :disabled="remove.length <= 0" size="mini" icon="el-icon-delete" plain @click="deleteDataBtn">删除</el-button>
        </el-row>
        <el-table
          v-loading="loading"
          :data="tableData"
          :header-cell-style="{'color': '#4A2B90','background-color': '#ECE9F4'}"
          :row-style="{'color': '#888897','font-size': '15px'}"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="title" label="标题" width="150"></el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template slot-scope="scope">
              <el-tag size="small" :type="scope.row.type === 0 ? 'primary' : 'success'">
                {{ scope.row.type === 0 ? '公式拼图' : '概念拼图' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="branch" label="分支" width="100">
            <template slot-scope="scope">
              {{ getBranchName(scope.row.branch) }}
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="120">
            <template slot-scope="scope">
              <el-rate :value="scope.row.difficulty" disabled :max="3"></el-rate>
            </template>
          </el-table-column>
          <el-table-column prop="piecesCount" label="碎片数" width="80">
            <template slot-scope="scope">
              <el-tag size="small">{{ scope.row.piecesCount }}块</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 0 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="updateData(scope.row.id)">编辑</el-button>
              <el-popconfirm style="margin-left:5px" confirm-button-text='确认' cancel-button-text='取消' title="确认删除？" @confirm="deleteDate(scope.row.id)">
                <el-button size="mini" slot="reference" type="danger">删除</el-button>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="search.pageSize"
          :current-page="search.pageNumber"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="total">
        </el-pagination>
      </div>
    </div>
    
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="750px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入拼图标题"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width:100%" @change="handleTypeChange">
                <el-option label="公式拼图" :value="0"></el-option>
                <el-option label="概念拼图" :value="1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分支" prop="branch">
              <el-select v-model="form.branch" placeholder="请选择分支" style="width:100%">
                <el-option label="力学" value="mechanics"></el-option>
                <el-option label="电磁学" value="electrics"></el-option>
                <el-option label="光学" value="optics"></el-option>
                <el-option label="热学" value="thermodynamics"></el-option>
                <el-option label="近代物理" value="modern"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-rate v-model="form.difficulty" :max="3" show-text :texts="['简单', '中等', '困难']"></el-rate>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 公式拼图配置 -->
        <div v-if="form.type === 0">
          <el-divider content-position="left">公式拼图配置</el-divider>
          
          <el-form-item label="公式内容" prop="content">
            <el-input v-model="form.content" placeholder="请输入公式，如: F=ma 或 E=mc²" @input="autoParseFormula">
              <el-button slot="append" @click="autoParseFormula">智能拆分</el-button>
            </el-input>
            <div class="formula-tips">
              <span>支持的符号: ² ³ ₀ ₁ ₂ Δ ∇ · ℏ ∂ → ← ↑ ↓</span>
            </div>
          </el-form-item>
          
          <el-form-item label="碎片数据" prop="piecesData">
            <div class="pieces-editor">
              <div class="pieces-display">
                <el-tag 
                  v-for="(piece, index) in piecesArray" 
                  :key="index"
                  closable
                  @close="removePiece(index)"
                  style="margin: 3px;"
                >
                  {{ piece }}
                </el-tag>
              </div>
              <div class="pieces-input">
                <el-input v-model="newPiece" placeholder="添加碎片" size="small" style="width: 150px;" @keyup.enter.native="addPiece"></el-input>
                <el-button type="primary" size="small" @click="addPiece">添加</el-button>
              </div>
            </div>
            <div class="pieces-info">当前碎片数: {{ piecesArray.length }}</div>
          </el-form-item>
          
          <el-form-item label="正确顺序">
            <div class="order-display">
              <span v-for="(piece, index) in piecesArray" :key="index" class="order-item">
                {{ index + 1 }}. {{ piece }}
              </span>
            </div>
            <div class="order-tips">碎片将按此顺序排列，用户需要将打乱的碎片按正确顺序放回</div>
          </el-form-item>
        </div>
        
        <!-- 概念拼图配置 -->
        <div v-if="form.type === 1">
          <el-divider content-position="left">概念拼图配置</el-divider>
          
          <el-form-item label="内容描述" prop="content">
            <el-input type="textarea" :rows="2" v-model="form.content" placeholder="请输入拼图描述"></el-input>
          </el-form-item>
          
          <el-form-item label="碎片数量" prop="piecesCount">
            <el-radio-group v-model="form.piecesCount" @change="updateGridInfo">
              <el-radio-button :label="9">3×3 (9块)</el-radio-button>
              <el-radio-button :label="16">4×4 (16块)</el-radio-button>
              <el-radio-button :label="25">5×5 (25块)</el-radio-button>
            </el-radio-group>
            <span style="margin-left:10px;color:#909399">{{ gridInfo }}</span>
          </el-form-item>
          
          <el-form-item label="拼图图片" prop="imageUrl">
            <el-upload
              :action="uploadImageUrl()"
              :headers="uploadHeaders"
              :show-file-list="true"
              :file-list="imageFileList"
              :on-success="handleImageSuccess"
              :on-remove="handleImageRemove"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              list-type="picture-card"
              :limit="1"
              accept="image/*">
              <i class="el-icon-plus"></i>
              <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过5MB</div>
            </el-upload>
            <div class="image-tips">
              <span>建议图片尺寸: {{ imageSizeTip }}</span>
            </div>
          </el-form-item>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="提示" prop="hint">
              <el-input type="textarea" :rows="2" v-model="form.hint" placeholder="请输入拼图提示"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="启用" inactive-text="禁用"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getFunPhysicsPuzzlePage, getFunPhysicsPuzzleById, saveFunPhysicsPuzzle, editFunPhysicsPuzzle, removeFunPhysicsPuzzle } from '@/api/api'

export default {
  name: 'FunPhysicsPuzzleManage',
  data() {
    return {
      loading: false,
      remove: [],
      dialogVisible: false,
      dialogTitle: '新增拼图',
      search: {
        title: '',
        type: null,
        difficulty: null,
        branch: '',
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      tableData: [],
      form: {
        id: '',
        title: '',
        type: 0,
        branch: 'mechanics',
        content: '',
        difficulty: 1,
        piecesCount: 4,
        piecesData: '[]',
        correctOrder: '[]',
        hint: '',
        imageUrl: '',
        status: 0
      },
      piecesArray: [],
      newPiece: '',
      imageFileList: [],
      uploadHeaders: {
        'x_access_token': window.localStorage.getItem('token')
      },
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        type: [{ required: true, message: '请选择类型', trigger: 'change' }],
        branch: [{ required: true, message: '请选择分支', trigger: 'change' }]
      }
    }
  },
  computed: {
    gridInfo() {
      const size = Math.sqrt(this.form.piecesCount)
      return `网格大小: ${size}×${size}`
    },
    imageSizeTip() {
      const size = Math.sqrt(this.form.piecesCount)
      return `${size * 100}×${size * 100} 像素或等比例图片`
    }
  },
  methods: {
    uploadImageUrl() {
      return this.$store.state.configure.HOST + '/common/uploadFunPhysicsImg'
    },
    beforeImageUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isJPG) {
        this.$message.error('上传图片只能是 JPG/PNG/GIF 格式!')
      }
      if (!isLt5M) {
        this.$message.error('上传图片大小不能超过 5MB!')
      }
      return isJPG && isLt5M
    },
    handleImageSuccess(res) {
      if (res.code == 1000) {
        this.$message({
          type: 'success',
          message: '图片上传成功!'
        })
        const fullUrl = this.$store.state.configure.HOST + res.message
        this.form.imageUrl = res.message
        this.imageFileList = [{ name: '拼图图片', url: fullUrl }]
      } else {
        this.$notify.error({
          title: '错误',
          message: res.message
        })
      }
    },
    handleImageRemove(file, fileList) {
      this.form.imageUrl = ''
      this.imageFileList = []
    },
    handleUploadError(err, file, fileList) {
      this.$message.error('上传失败: ' + (err.message || '未知错误'))
    },
    getTypeName(type) {
      const names = { 0: '公式拼图', 1: '概念拼图' }
      return names[type] || '其他'
    },
    getBranchName(branch) {
      const names = { mechanics: '力学', electrics: '电磁学', optics: '光学', thermodynamics: '热学', modern: '近代物理', electromagnetism: '电磁学' }
      return names[branch] || branch
    },
    handleTypeChange(type) {
      if (type === 0) {
        // 公式拼图默认设置
        this.form.piecesCount = 4
        this.form.imageUrl = ''
      } else {
        // 概念拼图默认设置
        this.form.piecesCount = 9
        this.piecesArray = []
      }
    },
    autoParseFormula() {
      if (!this.form.content) return
      
      // 智能拆分公式
      const formula = this.form.content
      const result = []
      let current = ''
      let i = 0
      
      while (i < formula.length) {
        const char = formula[i]
        
        // 处理运算符和分隔符
        if (['=', '+', '-', '·', '/', '×', '÷'].includes(char)) {
          if (current.trim()) result.push(current.trim())
          result.push(char)
          current = ''
          i++
          continue
        }
        
        // 处理上下标等特殊符号
        if (['²', '³', '⁴', '₀', '₁', '₂', '₃', '⁻', '⁺'].includes(char)) {
          current += char
          if (current.trim()) result.push(current.trim())
          current = ''
          i++
          continue
        }
        
        // 处理空格
        if (char === ' ') {
          if (current.trim()) result.push(current.trim())
          current = ''
          i++
          continue
        }
        
        current += char
        i++
      }
      
      if (current.trim()) result.push(current.trim())
      
      this.piecesArray = result.filter(s => s.length > 0)
      this.updatePiecesData()
    },
    addPiece() {
      if (this.newPiece.trim()) {
        this.piecesArray.push(this.newPiece.trim())
        this.newPiece = ''
        this.updatePiecesData()
      }
    },
    removePiece(index) {
      this.piecesArray.splice(index, 1)
      this.updatePiecesData()
    },
    updatePiecesData() {
      this.form.piecesData = JSON.stringify(this.piecesArray)
      this.form.piecesCount = this.piecesArray.length
      // 正确顺序就是 [0, 1, 2, ...]
      this.form.correctOrder = JSON.stringify([...Array(this.piecesArray.length).keys()])
    },
    updateGridInfo() {
      // 概念拼图的正确顺序就是 [0, 1, 2, ...]
      const order = [...Array(this.form.piecesCount).keys()]
      this.form.correctOrder = JSON.stringify(order)
      this.form.piecesData = JSON.stringify(order)
    },
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    query() {
      this.loading = true
      getFunPhysicsPuzzlePage(this.search).then(res => {
        if (res.code === 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      }).finally(() => {
        this.loading = false
      })
    },
    refresh() {
      this.search.title = ''
      this.search.type = null
      this.search.difficulty = null
      this.search.branch = ''
      this.query()
    },
    handleCurrentChange(val) {
      this.search.pageNumber = val
      this.query()
    },
    handleSizeChange(val) {
      this.search.pageSize = val
      this.query()
    },
    handleSelectionChange(val) {
      this.remove = val.map(item => item.id)
    },
    add() {
      this.dialogTitle = '新增拼图'
      this.form = {
        id: '',
        title: '',
        type: 0,
        branch: 'mechanics',
        content: '',
        difficulty: 1,
        piecesCount: 4,
        piecesData: '[]',
        correctOrder: '[]',
        hint: '',
        imageUrl: '',
        status: 0
      }
      this.piecesArray = []
      this.newPiece = ''
      this.imageFileList = []
      this.dialogVisible = true
    },
    updateData(id) {
      this.dialogTitle = '编辑拼图'
      getFunPhysicsPuzzleById({ id }).then(res => {
        if (res.code === 1000) {
          this.form = res.data
          // 解析碎片数据
          try {
            this.piecesArray = JSON.parse(res.data.piecesData || '[]')
          } catch (e) {
            this.piecesArray = []
          }
          // 设置图片文件列表
          if (res.data.imageUrl) {
            const fullUrl = this.$store.state.configure.HOST + res.data.imageUrl
            this.imageFileList = [{ name: '拼图图片', url: fullUrl }]
          } else {
            this.imageFileList = []
          }
          this.dialogVisible = true
        }
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 更新碎片数量
          if (this.form.type === 0) {
            this.form.piecesCount = this.piecesArray.length
          }
          
          const apiMethod = this.form.id ? editFunPhysicsPuzzle : saveFunPhysicsPuzzle
          apiMethod(this.form).then(res => {
            if (res.code === 1000) {
              this.$message.success('操作成功')
              this.dialogVisible = false
              this.query()
            }
          })
        }
      })
    },
    deleteDataBtn() {
      this.$confirm('确定删除选中的' + this.remove.length + '条数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.deleteDate(this.remove.join(','))
      })
    },
    deleteDate(ids) {
      removeFunPhysicsPuzzle({ id: ids }).then(res => {
        if (res.code === 1000) {
          this.$message.success('删除成功')
          this.query()
        }
      })
    }
  },
  mounted() {
    this.query()
  }
}
</script>

<style lang="scss" scoped>
.search-table { width: 100%; }
.search { background: #ffffff; border-radius: 7px; box-shadow: 0 2px 12px 0 rgba(0,0,0,.1) }
.table { background: #ffffff; border-radius: 7px; box-shadow: 0 2px 12px 0 rgba(0,0,0,.1); margin-top: 10px }
.el-col { display: flex; flex-direction: row; align-items: center; }
.search-title { font-family: '黑体'; float: right; white-space: nowrap; font-size: 14px; margin-top:10px; width: 60px; text-align: right; }
.el-table { padding: 10px; }

.formula-tips {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.pieces-editor {
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  padding: 10px;
  
  .pieces-display {
    min-height: 40px;
    margin-bottom: 10px;
    padding: 5px;
    background: #f5f7fa;
    border-radius: 4px;
  }
  
  .pieces-input {
    display: flex;
    gap: 10px;
  }
}

.pieces-info {
  margin-top: 5px;
  font-size: 12px;
  color: #409EFF;
}

.order-display {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  
  .order-item {
    display: inline-block;
    margin: 5px 10px;
    padding: 5px 10px;
    background: #409EFF;
    color: white;
    border-radius: 4px;
    font-size: 13px;
  }
}

.order-tips {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.image-tips {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.image-preview {
  max-width: 200px;
  max-height: 200px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: auto;
  }
}

.el-divider {
  margin: 15px 0;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
