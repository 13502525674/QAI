<template>
  <div class="centerPage">
    <headerPage></headerPage>
    <div class="centerPage-content">
    <el-card class="box-card">
        <div class="content">
            <div class="master">
              <el-form style="margin-right:20px" :model="user" :rules="rules" ref="ruleForm" label-width="140px">
                <el-form-item label="登陆账号" prop="loginAccount">
                  <el-input size="mini" disabled v-model="user.loginAccount"></el-input>
                </el-form-item>
                <el-form-item label="用户名" prop="userName">
                  <el-input size="mini" v-model="user.userName"></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input size="mini" v-model="user.email"></el-input>
                </el-form-item>
                <el-form-item label="联系电话" prop="tel">
                  <el-input size="mini" v-model="user.tel"></el-input>
                </el-form-item>
                <el-form-item label="性别" prop="tel">
                  <el-radio-group v-model="user.sex">
                    <el-radio label="0">男</el-radio>
                    <el-radio label="1">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item prop="school" label="院校">
                  <el-select size="mini" v-model="user.school">
                    <el-option v-for="(item,index) in school" :label="item.name" :value="item.name"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item prop="major" label="专业">
                  <el-select size="mini" v-model="user.major">
                    <el-option v-for="(item,index) in major" :label="item.name" :value="item.name"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="上次修改密码时间" prop="pwdUpdateDate">
                  <el-input size="mini" disabled v-model="user.pwdUpdateDate"></el-input>
                </el-form-item>
              </el-form>
              <div class="submit">
                  <el-button type="primary" plain size="mini" @click="submit">保存</el-button>
              </div>
            </div>
            <div class="slave">
                <div class="img">
                  <el-image 
                    style="object-fit: cover;width: 300px; height: 300px;overflow: hidden;border-radius: 50%;"
                    :src="$store.state.HOST + user.avatar" 
                    :preview-src-list="avatar">
                  </el-image>
                </div>
                <div class="btns">
                  <div>
                    <el-upload
                      ref="upload"
                      :action="uploadAvatarUrl()+ '/'+ this.user.id"
                      :show-file-list="false"
                      :before-upload="beforeAvatorUpload"
                      :on-success="handleAvatorSuccess"
                      accept="image/*"
                      >
                      <el-button style="margin-top:15px" size="mini" icon="el-icon-picture-outline-round">修改头像</el-button>
                    </el-upload>
                  </div>
                  <div style="margin-top:15px">
                    <el-button size="mini" icon="el-icon-key" @click="changePassword">修改密码</el-button>
                  </div>
                </div>
            </div>
        </div>
    </el-card>
    </div>
    
    <el-dialog
        title="修改密码"
        :visible.sync="passwordDialogVisible"
        width="30%"
        :before-close="handlePasswordClose">
        <span>请输入{{user.userName}}的旧密码：</span>
        <el-input style="margin-top:10px" show-password v-model="oldPassword" size="mini" autocomplete="off"></el-input>
        <span>请输入{{user.userName}}的新密码：</span>
        <el-input style="margin-top:10px" show-password v-model="newPassword" size="mini" autocomplete="off"></el-input>
        <span slot="footer" class="dialog-footer">
          <el-button size="mini" @click="handlePasswordClose">取 消</el-button>
          <el-button size="mini" type="primary" @click="passwordSubmit">确 定</el-button>
        </span>
    </el-dialog>
    <div class="task-page-bottom">
      <img style="width:70%" src="../../assets/image/48.png">
    </div>
    <bottomPage></bottomPage>
  </div>
</template>

<script>
  import {mixin} from "../../minix";
  import headerPage from "../../components/header/header"
  import bottomPage from "../../components/bottom/bottom"
  import {getUserInfo,setUserInfo,setUserAvatar,changePassword,getApeSchoolList,getApeMajorList} from '../../api/api' 
  export default {
    mixins: [mixin],
    data() {
      var checkPhone = (rule, value, callback) => {
          if (!value) {
              return callback(new Error('请输入联系电话'));
          } else {
              const reg = /^1[3|4|5|7|8][0-9]\d{8}$/
              if (reg.test(value)) {
                  callback();
              } else {
                  return callback(new Error('请输入正确的联系电话'));
              }
          }
      };
      return{
        school: [],
        major: [],
        user: {},
        avatar: [],
        rules: {
          userName: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
          ],
          email: [
            { required: true, message: '请输入邮箱地址', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
          ],
          tel: [
            { required: true, validator:checkPhone, message: '请输入正确的联系电话', trigger: 'blur' },
          ],
          school: [
            { required: true, message: '请选择院校', trigger: 'blur' },
          ],
          major: [
            { required: true, message: '请选择专业', trigger: 'blur' },
          ],
        },
        oldPassword: "",
        newPassword: "",
        passwordDialogVisible: false,
      }
    },
    components: {
      headerPage,
      bottomPage
    },
    methods: {
      getApeSchoolList() {
        getApeSchoolList().then(res=> {
          if(res.code == 1000) {
            this.school = res.data
          } else {
            this.$notify.error({
            title: '错误',
            message: res.message
            });
          }
        })
      },
      getApeMajorList() {
        getApeMajorList().then(res => {
          if(res.code == 1000) {
            this.major = res.data
          } else {
            this.$notify.error({
            title: '错误',
            message: res.message
            });
          }
        })
      },
      handlePasswordClose() {
        this.$bus.$emit('password', false)
      },
      passwordSubmit() {
        var param = {
          id: this.user.id,
          password: this.oldPassword,
          newPassword: this.newPassword
        }
        changePassword(param).then(res => {
          if(res.code == 1000) {
            this.$notify.success({
              title: '成功',
              message: "密码修改成功"
            });
            this.$bus.$emit('password', false)
          } else {
            this.$notify.error({
              title: '错误',
              message: res.message
            });
          }
        })
      },
      submit() {
        this.$refs["ruleForm"].validate((valid) => {
          if (valid) {
            setUserInfo(this.user).then(res => {
              if(res.code == 1000) {
                this.$message({
                  type: 'success',
                  message: '保存成功!'
                });
                this.getUserInfo()
              } else {
                this.$notify.error({
                  title: '错误',
                  message: res.message
                });
              }
            })
          } else {
            return false;
          }
        });
      },
      getUserInfo() {
        getUserInfo().then(res => {
            if(res.code == 1000) {
              this.user = res.data
              this.user.sex = res.data.sex + ""
              this.avatar[0] = this.$store.state.HOST + this.user.avatar
            } else {
              this.$notify.error({
                title: '错误',
                message: res.message
              });
            }
        })
      },
      changePassword() {
        //修改密码
        this.$bus.$emit('password', true)
      },
      handleAvatorSuccess(res){
        let _this = this;
        if(res.code == 1000){
            _this.$message({
              type: 'success',
              message: '上传成功!'
            });
        }else{
          _this.$notify.error({
            title: '错误',
            message: res.message
          });
        }
      },
    },
    created() {
     
    },
    mounted() {
      // 监听collapse
      this.$bus.$on('password', res=>{
        this.passwordDialogVisible = res
      })
      this.getUserInfo()
      this.getApeSchoolList()
      this.getApeMajorList()
    }
 }
</script>

<style scoped>
.centerPage {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0fdfa 50%, #ecfeff 100%);
  position: relative;
}

.centerPage::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(20, 184, 166, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.centerPage-content {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  position: relative;
  z-index: 1;
}

.box-card {
  margin-top: 30px;
  margin-bottom: 30px;
  width: 70%;
  max-width: 1000px;
  border-radius: 20px;
  border: 2px solid rgba(59, 130, 246, 0.1);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  overflow: hidden;
  position: relative;
}

.box-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #14b8a6, #06b6d4);
}

.box-card /deep/ .el-card__body {
  padding: 0;
}

.content {
  width: 100%;
  min-height: 550px;
  display: flex;
  flex-direction: row;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

.master {
  width: 60%;
  padding: 40px 45px;
  border-right: 2px solid #e0f2fe;
  background: rgba(255, 255, 255, 0.6);
}

.master .el-form {
  margin-right: 20px;
}

.master /deep/ .el-form-item {
  margin-bottom: 28px;
}

.master /deep/ .el-form-item__label {
  color: #475569;
  font-weight: 600;
  font-size: 14px;
  padding-right: 12px;
}

.master /deep/ .el-form-item__content {
  line-height: 40px;
}

.master /deep/ .el-input__inner {
  border-radius: 10px;
  border: 2px solid #e0f2fe;
  background: #f8fafc;
  color: #334155;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 40px;
  line-height: 40px;
}

.master /deep/ .el-input__inner:focus {
  border-color: #3b82f6;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.master /deep/ .el-input.is-disabled .el-input__inner {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
  border-color: #e2e8f0;
}

.master /deep/ .el-select {
  width: 100%;
}

.master /deep/ .el-radio-group {
  display: flex;
  gap: 20px;
}

.master /deep/ .el-radio {
  margin-right: 0;
}

.master /deep/ .el-radio__input.is-checked .el-radio__inner {
  border-color: #3b82f6;
  background: #3b82f6;
}

.master /deep/ .el-radio__input.is-checked + .el-radio__label {
  color: #3b82f6;
}

.master /deep/ .el-radio__inner:hover {
  border-color: #3b82f6;
}

.submit {
  width: 100%;
  text-align: center;
  margin-top: 32px;
}

.submit .el-button--primary.is-plain {
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  border: none;
  color: #ffffff;
  padding: 14px 52px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.submit .el-button--primary.is-plain:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.submit .el-button--primary.is-plain:active {
  transform: translateY(0);
}

.slave {
  width: 40%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdfa 50%, #ffffff 100%);
}

.img {
  width: 100%;
  height: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32px;
  position: relative;
}

.img::before {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.img .el-image {
  position: relative;
  z-index: 1;
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.2);
  border: 4px solid rgba(59, 130, 246, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.img .el-image:hover {
  transform: scale(1.05);
  box-shadow: 0 14px 36px rgba(59, 130, 246, 0.3);
  border-color: rgba(59, 130, 246, 0.2);
}

.btns {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.btns .el-upload {
  width: 100%;
}

.btns .el-button {
  width: 100%;
  border-radius: 10px;
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btns .el-button--default {
  border: 2px solid #e0f2fe;
  background: #ffffff;
  color: #475569;
}

.btns .el-button--default:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.btns .el-button--default .el-icon-picture-outline-round {
  color: #3b82f6;
}

.btns .el-button--default .el-icon-key {
  color: #14b8a6;
}

/* 修改密码对话框 */
/deep/ .el-dialog {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(59, 130, 246, 0.25);
}

/deep/ .el-dialog__header {
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  border-radius: 16px 16px 0 0;
  padding: 22px 26px;
}

/deep/ .el-dialog__title {
  color: #ffffff;
  font-weight: 600;
  font-size: 18px;
}

/deep/ .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
  font-size: 20px;
}

/deep/ .el-dialog__body {
  padding: 32px 26px;
}

/deep/ .el-dialog__body span {
  display: block;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

/deep/ .el-dialog__body .el-input {
  margin-bottom: 20px;
}

/deep/ .el-dialog__body .el-input__inner {
  border-radius: 10px;
  border: 2px solid #e0f2fe;
  background: #f8fafc;
  height: 40px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/deep/ .el-dialog__body .el-input__inner:focus {
  border-color: #3b82f6;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/deep/ .dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/deep/ .dialog-footer .el-button {
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/deep/ .dialog-footer .el-button--default {
  border: 2px solid #e0f2fe;
  background: #ffffff;
  color: #475569;
}

/deep/ .dialog-footer .el-button--default:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

/deep/ .dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #14b8a6 100%);
  border: none;
  box-shadow: 0 3px 10px rgba(59, 130, 246, 0.3);
}

/deep/ .dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(59, 130, 246, 0.4);
}

.task-page-bottom {
  text-align: center;
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 30px 20px;
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, rgba(219, 234, 254, 0.5) 0%, rgba(204, 251, 241, 0.5) 50%, rgba(207, 250, 254, 0.5) 100%);
  border-radius: 16px;
  border: 2px solid rgba(59, 130, 246, 0.1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.task-page-bottom img {
  opacity: 0.9;
  filter: drop-shadow(0 4px 8px rgba(59, 130, 246, 0.15));
}

@media (max-width: 768px) {
  .centerPage-content {
    padding: 10px;
  }
  
  .box-card {
    width: 95%;
    margin-top: 15px;
    margin-bottom: 15px;
  }
  
  .content {
    flex-direction: column;
    min-height: auto;
  }
  
  .master {
    width: 100%;
    padding: 24px;
    border-right: none;
    border-bottom: 2px solid #e0f2fe;
  }
  
  .slave {
    width: 100%;
    padding: 32px 24px;
  }
  
  .img .el-image {
    width: 200px !important;
    height: 200px !important;
  }
  
  .btns .el-button {
    width: 80%;
  }
}
</style>