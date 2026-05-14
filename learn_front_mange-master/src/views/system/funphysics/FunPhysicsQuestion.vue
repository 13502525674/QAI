<template>
  <div class="_fun-physics-question">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding:10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">题目内容:</span>
            <el-input style="margin-top:10px" size="mini" placeholder="请输入题目内容" v-model="search.title"></el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">题目类型:</span>
            <el-select style="margin-top:10px" size="mini" v-model="search.type" placeholder="请选择" clearable>
              <el-option label="物理直觉挑战" :value="0"></el-option>
              <el-option label="生活中的物理" :value="1"></el-option>
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
          <el-table-column prop="title" label="题目内容" show-overflow-tooltip></el-table-column>
          <el-table-column prop="type" label="类型" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.type === 0 ? 'primary' : 'success'" size="small">
                {{ scope.row.type === 0 ? '物理直觉挑战' : '生活中的物理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="120">
            <template slot-scope="scope">
              <el-rate :value="scope.row.difficulty" disabled :max="3" :texts="['简单', '中等', '困难']" text-color="#ff9900"></el-rate>
            </template>
          </el-table-column>
          <el-table-column prop="correctAnswer" label="正确答案" width="80"></el-table-column>
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
    
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width:100%">
            <el-option label="物理直觉挑战" :value="0"></el-option>
            <el-option label="生活中的物理" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="title">
          <el-input type="textarea" :rows="3" v-model="form.title" placeholder="请输入题目内容"></el-input>
        </el-form-item>
        <el-form-item label="选项" prop="options">
          <div v-for="(opt, key) in form.optionsObj" :key="key" style="margin-bottom:10px">
            <el-input v-model="form.optionsObj[key]" placeholder="请输入选项内容" style="width:calc(100% - 40px)">
              <template slot="prepend">{{ key }}</template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="正确答案" prop="correctAnswer">
          <el-select v-model="form.correctAnswer" placeholder="请选择正确答案">
            <el-option label="A" value="A"></el-option>
            <el-option label="B" value="B"></el-option>
            <el-option label="C" value="C"></el-option>
            <el-option label="D" value="D"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="趣味解析" prop="explanation">
          <el-input type="textarea" :rows="3" v-model="form.explanation" placeholder="请输入趣味解析"></el-input>
        </el-form-item>
        <el-form-item label="知识点" prop="knowledgePoint">
          <el-input v-model="form.knowledgePoint" placeholder="请输入相关知识点"></el-input>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-rate v-model="form.difficulty" :max="3" show-text :texts="['简单', '中等', '困难']"></el-rate>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getFunPhysicsQuestionPage, getFunPhysicsQuestionById, saveFunPhysicsQuestion, editFunPhysicsQuestion, removeFunPhysicsQuestion } from '@/api/api'

export default {
  name: 'FunPhysicsQuestionManage',
  data() {
    return {
      loading: false,
      remove: [],
      dialogVisible: false,
      dialogTitle: '新增题目',
      search: {
        title: '',
        type: null,
        difficulty: null,
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      tableData: [],
      form: {
        id: '',
        title: '',
        type: 0,
        optionsObj: { A: '', B: '', C: '', D: '' },
        correctAnswer: '',
        explanation: '',
        knowledgePoint: '',
        difficulty: 1,
        status: 0
      },
      rules: {
        title: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
        type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
        correctAnswer: [{ required: true, message: '请选择正确答案', trigger: 'change' }],
        explanation: [{ required: true, message: '请输入趣味解析', trigger: 'blur' }]
      }
    }
  },
  methods: {
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    query() {
      this.loading = true
      getFunPhysicsQuestionPage(this.search).then(res => {
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
      this.dialogTitle = '新增题目'
      this.form = {
        id: '',
        title: '',
        type: 0,
        optionsObj: { A: '', B: '', C: '', D: '' },
        correctAnswer: '',
        explanation: '',
        knowledgePoint: '',
        difficulty: 1,
        status: 0
      }
      this.dialogVisible = true
    },
    updateData(id) {
      this.dialogTitle = '编辑题目'
      getFunPhysicsQuestionById({ id }).then(res => {
        if (res.code === 1000) {
          const data = res.data
          let optionsObj = { A: '', B: '', C: '', D: '' }
          try {
            const parsed = typeof data.options === 'string' ? JSON.parse(data.options) : data.options
            if (Array.isArray(parsed)) {
              parsed.forEach(opt => {
                if (opt.label && opt.content) {
                  optionsObj[opt.label] = opt.content
                }
              })
            } else if (typeof parsed === 'object') {
              optionsObj = { ...optionsObj, ...parsed }
            }
          } catch (e) {
            console.error('解析选项失败:', e)
          }
          this.form = { ...data, optionsObj }
          this.dialogVisible = true
        }
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const submitData = { ...this.form }
          submitData.options = JSON.stringify(this.form.optionsObj)
          const apiMethod = this.form.id ? editFunPhysicsQuestion : saveFunPhysicsQuestion
          apiMethod(submitData).then(res => {
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
      removeFunPhysicsQuestion({ id: ids }).then(res => {
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
.search-title { font-family: '黑体'; float: right; white-space: nowrap; font-size: 14px; margin-top:10px; width: 70px; text-align: right; }
.el-table { padding: 10px; }
</style>
