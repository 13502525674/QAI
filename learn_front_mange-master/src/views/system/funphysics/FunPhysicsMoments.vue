<template>
  <div class="_fun-physics-moments">
    <div class="search-table">
      <div class="search">
        <el-row :gutter="10" style="padding:10px">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">物理学家:</span>
            <el-input style="margin-top:10px" size="mini" placeholder="请输入物理学家名称" v-model="search.physicistName"></el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <span class="search-title">分类:</span>
            <el-select style="margin-top:10px" size="mini" v-model="search.category" placeholder="请选择" clearable>
              <el-option label="日常搞笑" :value="0"></el-option>
              <el-option label="知识点" :value="1"></el-option>
              <el-option label="历史事件" :value="2"></el-option>
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
          <el-table-column prop="physicistName" label="物理学家" width="120"></el-table-column>
          <el-table-column prop="physicistTitle" label="头衔" width="150"></el-table-column>
          <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
          <el-table-column prop="category" label="分类" width="100">
            <template slot-scope="scope">
              <el-tag size="small">{{ getCategoryName(scope.row.category) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="likesCount" label="点赞数" width="80"></el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 0 ? '显示' : '隐藏' }}
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
    
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="650px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="物理学家" prop="physicistName">
          <el-input v-model="form.physicistName" placeholder="请输入物理学家名称"></el-input>
        </el-form-item>
        <el-form-item label="头衔" prop="physicistTitle">
          <el-input v-model="form.physicistTitle" placeholder="如：经典力学之父"></el-input>
        </el-form-item>
        <el-form-item label="头像" prop="physicistAvatar">
          <el-upload
            :action="uploadImageUrl()"
            :headers="uploadHeaders"
            :show-file-list="true"
            :file-list="avatarFileList"
            :on-success="handleAvatarSuccess"
            :on-remove="handleAvatarRemove"
            :on-error="handleUploadError"
            :before-upload="beforeAvatarUpload"
            list-type="picture-card"
            :limit="1"
            accept="image/*">
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过5MB</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="发布内容" prop="content">
          <el-input type="textarea" :rows="4" v-model="form.content" placeholder="请输入朋友圈内容"></el-input>
        </el-form-item>
        <el-form-item label="朋友圈图片" prop="images">
          <el-upload
            :action="uploadImageUrl()"
            :headers="uploadHeaders"
            :show-file-list="true"
            :file-list="imagesFileList"
            :on-success="handleImagesSuccess"
            :on-remove="handleImagesRemove"
            :on-error="handleUploadError"
            :before-upload="beforeImagesUpload"
            list-type="picture-card"
            :limit="3"
            accept="image/*"
            multiple>
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">最多上传3张图片，每张不超过5MB</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="发布地点" prop="location">
          <el-input v-model="form.location" placeholder="如：剑桥大学"></el-input>
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime">
          <el-input v-model="form.publishTime" placeholder="如：1687年"></el-input>
        </el-form-item>
        <el-form-item label="知识标签" prop="knowledgeTags">
          <el-input v-model="form.knowledgeTags" placeholder="多个标签用逗号分隔，如：万有引力,力学"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width:100%">
            <el-option label="日常搞笑" :value="0"></el-option>
            <el-option label="知识点" :value="1"></el-option>
            <el-option label="历史事件" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="显示" inactive-text="隐藏"></el-switch>
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
import { getFunPhysicsMomentsPage, getFunPhysicsMomentsById, saveFunPhysicsMoments, editFunPhysicsMoments, removeFunPhysicsMoments } from '@/api/api'

export default {
  name: 'FunPhysicsMomentsManage',
  data() {
    return {
      loading: false,
      remove: [],
      dialogVisible: false,
      dialogTitle: '新增朋友圈',
      search: {
        physicistName: '',
        category: null,
        pageNumber: 1,
        pageSize: 10
      },
      total: 0,
      tableData: [],
      form: {
        id: '',
        physicistName: '',
        physicistTitle: '',
        physicistAvatar: '',
        content: '',
        images: '',
        location: '',
        publishTime: '',
        knowledgeTags: '',
        category: 0,
        status: 0
      },
      avatarFileList: [],
      imagesFileList: [],
      uploadHeaders: {
        'x_access_token': window.localStorage.getItem('token')
      },
      rules: {
        physicistName: [{ required: true, message: '请输入物理学家名称', trigger: 'blur' }],
        content: [{ required: true, message: '请输入发布内容', trigger: 'blur' }]
      }
    }
    },
    methods: {
      uploadImageUrl() {
        return this.$store.state.configure.HOST + '/common/uploadFunPhysicsImg'
      },
      getCategoryName(category) {
      const names = { 0: '日常搞笑', 1: '知识点', 2: '历史事件' }
      return names[category] || '其他'
    },
    searchPage() {
      this.search.pageNumber = 1
      this.query()
    },
    query() {
      this.loading = true
      getFunPhysicsMomentsPage(this.search).then(res => {
        if (res.code === 1000) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      }).finally(() => {
        this.loading = false
      })
    },
    refresh() {
      this.search.physicistName = ''
      this.search.category = null
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
      this.dialogTitle = '新增朋友圈'
      this.form = {
        id: '',
        physicistName: '',
        physicistTitle: '',
        physicistAvatar: '',
        content: '',
        images: '',
        location: '',
        publishTime: '',
        knowledgeTags: '',
        category: 0,
        status: 0
      }
      this.avatarFileList = []
      this.imagesFileList = []
      this.dialogVisible = true
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isJPG) {
        this.$message.error('上传头像只能是 JPG/PNG 格式!')
      }
      if (!isLt5M) {
        this.$message.error('上传头像大小不能超过 5MB!')
      }
      return isJPG && isLt5M
    },
    handleAvatarSuccess(res) {
      let _this = this;
      if(res.code == 1000){
        _this.$message({
          type: 'success',
          message: '头像上传成功!'
        });
        const fullUrl = this.$store.state.configure.HOST + res.message
        this.form.physicistAvatar = res.message
        this.avatarFileList = [{ name: '头像', url: fullUrl }]
      }else{
        _this.$notify.error({
          title: '错误',
          message: res.message
        });
      }
    },
    handleAvatarRemove(file, fileList) {
      this.form.physicistAvatar = ""
      this.avatarFileList = []
    },
    beforeImagesUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isJPG) {
        this.$message.error('上传图片只能是 JPG/PNG 格式!')
      }
      if (!isLt5M) {
        this.$message.error('上传图片大小不能超过 5MB!')
      }
      return isJPG && isLt5M
    },
    handleImagesSuccess(res, file, fileList) {
      let _this = this;
      if(res.code == 1000){
        _this.$message({
          type: 'success',
          message: '图片上传成功!'
        });
        const fullUrl = this.$store.state.configure.HOST + res.message
        this.imagesFileList.push({ name: file.name, url: fullUrl })
        if (!this.form.images) {
          this.form.images = res.message
        } else {
          this.form.images += ',' + res.message
        }
      }else{
        _this.$notify.error({
          title: '错误',
          message: res.message
        });
      }
    },
    handleImagesRemove(file, fileList) {
      const removedUrl = file.url.replace(this.$store.state.configure.HOST, '')
      const urls = this.form.images.split(',').filter(url => url !== removedUrl)
      this.form.images = urls.join(',')
      this.imagesFileList = fileList
    },
    handleUploadError(err, file, fileList) {
      this.$message.error('上传失败: ' + (err.message || '未知错误'))
    },
    updateData(id) {
      this.dialogTitle = '编辑朋友圈'
      getFunPhysicsMomentsById({ id }).then(res => {
        if (res.code === 1000) {
          const data = res.data.moments || res.data
          this.form = data
          if (this.form.physicistAvatar) {
            let avatarUrl = this.form.physicistAvatar
            if (!avatarUrl.startsWith('http')) {
              if (!avatarUrl.startsWith('/images/')) {
                avatarUrl = this.$store.state.configure.HOST + avatarUrl
              }
            }
            this.avatarFileList = [{ 
              name: '头像', 
              url: avatarUrl,
              status: 'success'
            }]
          }
          if (this.form.images) {
            this.imagesFileList = this.form.images.split(',').map((url, index) => {
              let fullUrl = url.trim()
              if (!fullUrl.startsWith('http')) {
                if (!fullUrl.startsWith('/images/')) {
                  fullUrl = this.$store.state.configure.HOST + fullUrl
                }
              }
              return {
                name: `图片${index + 1}`,
                url: fullUrl,
                status: 'success'
              }
            })
          } else {
            this.imagesFileList = []
          }
          this.dialogVisible = true
        }
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const apiMethod = this.form.id ? editFunPhysicsMoments : saveFunPhysicsMoments
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
      removeFunPhysicsMoments({ id: ids }).then(res => {
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
.el-upload__tip { font-size: 12px; color: #909399; margin-top: 5px; }
</style>
