<template>
  <div class="addArticle">
    <headerPage></headerPage>
    <div class="addArticle-content">
        <div class="addArticle-form">
            <div class="addArticle-title">
                 编辑笔记
            </div>
            <div class="addArticle-list">
                <div class="addArticle-item">
                    <div style="margin-top:10px;margin-left:10px">课程</div>
                    <div style="margin-top:10px;margin-left:10px;margin-bottom:10px">
                        <el-select size="mini" v-model="form.taskId" placeholder="请选择关联课程" style="width:100%">
                            <el-option v-for="(item,index) in taskList" :key="index" :label="item.name" :value="item.id"></el-option>
                        </el-select>
                    </div>
                </div>
                <div class="addArticle-item">
                    <div style="margin-top:10px;margin-left:10px">标题</div>
                    <div style="margin-top:10px;margin-left:10px;margin-bottom:10px">
                        <el-input size="mini" v-model="form.title" placeholder="请输入笔记标题"></el-input>
                    </div>
                </div>
                <div class="addArticle-item">
                    <div style="margin-top:10px;margin-left:10px">摘要</div>
                    <div style="margin-top:10px;margin-left:10px;margin-bottom:10px">
                         <el-input type="textarea" rows=5 v-model="form.articleDesc" size="mini" placeholder="请输入笔记摘要"></el-input>
                    </div>
                </div>
                <div class="addArticle-item" style="height:600px">
                    <div style="margin-top:10px;margin-left:10px">内容</div>
                    <div style="margin-top:10px;margin-left:10px;margin-bottom:10px">
                        <quill-editor style="height:450px" ref="myQuillEditor" :options="editorOption"
                        v-model="form.content"/>
                        <el-upload class="Quill-uploader" :show-file-list="false" accept="image/*" v-show="0" ref="updateUpload" :multiple="false" :action="uploadImageUrl()"
                            :on-success="handleQillSuccess">
                        </el-upload>
                    </div>
                </div>
                <div style="margin-bottom:20px">
                    <el-button @click="submit" size="mini" type="primary">提交</el-button>
                </div>
            </div>
        </div>
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import { getApeArticleById, saveApeArticle, editApeArticle, getApeTaskPage } from '../../api/api'
  import { mixin } from '../../minix/index';
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  export default {
    mixins: [mixin],
    data() {
      return{
        articleId: '',
        taskList: [],
        form: {
            id: '',
            articleDesc: "",
            content: "",
            title: "",
            taskId: ""
        },
        editorOption: {
          placeholder: "请输入文字内容",
          modules: {
            toolbar: {
                container: [
                    ["bold", "italic", "underline", "strike"],
                    ["blockquote", "code-block"],
                    [{ header: 1 }, { header: 2 }],
                    [{ list: "ordered" }, { list: "bullet" }],
                    [{ script: "sub" }, { script: "super" }],
                    [{ indent: "-1" }, { indent: "+1" }],
                    [{ direction: "rtl" }],
                    [{ size: ["small", false, "large", "huge"] }],
                    [{ header: [1, 2, 3, 4, 5, 6, false] }],
                    [{ color: [] }, { background: [] }],
                    [{ font: [] }], [{ align: [] }],
                    ["clean"], ["image"]
                ],
                handlers: {
                    'image': function ( value ) {
                        if ( value ) {
                            // 触发上传插件
                            document.querySelector('.Quill-uploader .el-upload__input').click()
                        } else {
                            this.quill.format( 'image', false );
                        }
                    }
                }
            },
            syntax: {
                highlight: text => {
                    return hljs.highlightAuto(text).value; // 这里就是代码高亮需要配置的地方
                }
            }
          }
        },
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
        getTaskList() {
          getApeTaskPage({ pageNumber: 1, pageSize: 999 }).then(res => {
            if (res.code == 1000) {
              this.taskList = res.data.records
            }
          })
        },
        loadArticle() {
          if (this.articleId) {
            getApeArticleById({ id: this.articleId }).then(res => {
              if (res.code == 1000) {
                this.form.id = res.data.id
                this.form.title = res.data.title
                this.form.articleDesc = res.data.articleDesc
                this.form.content = res.data.content
                this.form.taskId = res.data.taskId
              } else {
                this.$message.error('获取笔记详情失败')
              }
            })
          }
        },
        submit() {
          if (!this.form.title) {
            this.$message({
              message: '请输入笔记标题',
              type: 'warning'
            })
            return
          }
          if (!this.form.content) {
            this.$message({
              message: '请输入笔记内容',
              type: 'warning'
            })
            return
          }
          if (!this.form.articleDesc) {
            this.$message({
              message: '请输入笔记摘要',
              type: 'warning'
            })
            return
          }

          // 判断是编辑还是新增
          const apiCall = this.articleId ? editApeArticle(this.form) : saveApeArticle(this.form)

          apiCall.then(res => {
            if(res.code == 1000) {
              this.$message({
                message: this.articleId ? '编辑成功！' : '保存成功！',
                type: 'success'
              })
              this.$router.push('/myArticle')
            } else {
              this.$message.error(res.message || (this.articleId ? '编辑失败' : '保存失败'))
            }
          })
        },
        handleQillSuccess(res){
            let _this = this;
            if(res.code == 1000){
                _this.$notify({
                    title: '上传成功',
                    type: 'success'
                });
                let imgurl = this.$store.state.configure.HOST + res.message
                let quill = this.$refs.myQuillEditor.quill
                let position = quill.getSelection().index
                quill.insertEmbed(position, 'image', imgurl)
                quill.setSelection(position + 1)
            }else{
                _this.$notify({
                    title: '上传失败',
                    type: 'error'
                });
            }
        },
    },
    created() {

    },
    mounted() {
      this.getTaskList()
      this.articleId = this.$route.query.id
      if (this.articleId) {
        this.loadArticle()
      }
    }
 }
</script>

<style scoped>
  @import url("../../assets/css/article/addArticle.css");
</style>