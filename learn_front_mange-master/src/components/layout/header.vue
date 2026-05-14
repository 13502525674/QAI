<template>
    <el-header class="header">
      <div class="icon-div" @click="changeCollapse">
        <i class="icon" :class="collapse ? 'el-icon-s-unfold':'el-icon-s-fold'"></i>
      </div>
      <div class="header-right">
        <div class="theme-toggle" role="group" aria-label="主题">
          <button type="button" class="theme-btn" :class="{ active: themeMode === 'light' }" @click="setTheme('light')">白天</button>
          <button type="button" class="theme-btn" :class="{ active: themeMode === 'dark' }" @click="setTheme('dark')">黑夜</button>
        </div>
        <div class="search-input">
          <el-input v-model="search" size="mini" placeholder="Search..." suffix-icon="el-icon-search"></el-input>
        </div>
        <div class="icon-div" @click="full">
          <i class="icon el-icon-full-screen"></i>
        </div>
        <div class="icon-div">
          <i class="icon el-icon-bell"></i>
        </div>
        <div class="icon-div">
          <div>
            <el-dropdown  trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">
                <img style="width:40px;height:40px;border-radius:50%;object-fit: cover;" :src="$store.state.configure.HOST + user.avatar"></img>
              </span>
              <el-dropdown-menu slot="dropdown" style="font-weight:bold">
                <el-dropdown-item command="password"><i class="el-icon-ship" ></i>修改密码</el-dropdown-item>
                <!-- <el-dropdown-item command="setting"><i class="el-icon-setting" ></i>设置</el-dropdown-item> -->
                <el-dropdown-item command="lock"><i class="el-icon-lock" ></i>锁屏</el-dropdown-item>
                <el-dropdown-item divided style="color:red" command="logout"><i class="el-icon-umbrella"></i>退出</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <div class="icon-div" style="width:80px;">
          <div style="font-size:13px;font-family:'黑体'">{{user.userName}}</div>
        </div>
      </div>
    </el-header>
</template>

<script>
  import { removeToken } from '@/utils/token'
  import { getLock } from '@/utils/lock'
  import { mapState,mapGetters } from 'vuex'
  import { logout } from '@/api/api'
  export default {
    name: "header",
    data() {
      return{
        user: {
          avatar: "/img/avatar.jpg"
        },
        search: "",
        collapse: false,
      }
    },
    computed: {
      ...mapGetters('user',['getUser']),
      ...mapState('theme', {
        themeMode: 'mode'
      })
    },
    methods: {
      setTheme(mode) {
        this.$store.commit('theme/setMode', mode)
      },
      changeCollapse() {
        this.collapse = !this.collapse
        this.$bus.$emit('collapse', this.collapse)
      },
      full() {
        let de = document.documentElement
        if (de.requestFullscreen) {
          de.requestFullscreen()
        } else if (de.mozRequestFullScreen) {
          de.mozRequestFullScreen()
        } else if (de.webkitRequestFullScreen) {
          de.webkitRequestFullScreen()
        }
      },
      handleCommand(command) {
        if(command == "center") {

        } else if(command == "password") {
          //修改密码
          this.$bus.$emit('password', true)
        } else if(command == "setting") {

        } else if(command == "lock") {
          this.$bus.$emit('lock', 1)
        } else if(command == "logout") {
          //退出登陆
          logout().then(res => { 
            if(res.code == 1000) {
              this.$store.commit('user/setToken', "")
              this.$store.commit('user/setUser', "")
              this.$store.commit('menu/setMenus', [])
              this.$store.commit('menu/setRoutes', [])
              this.$store.commit('menu/setDisplayMenus', [])
              this.$store.commit('menu/setBtnMenus', [])
              this.$store.commit("menu/setFlag",false)
              this.$store.commit('menu/setActiveMenuArrary', [])
              this.$store.commit('menu/setActiveMenu', "/index")
              removeToken()
              this.$router.push("/login")
              this.$message({
                  message: "退出登陆",
                  type: 'success'
              });
              setTimeout(function(){
                window.location.reload()
              },1000)
            } else {
              this.$message({
                  message: res.message,
                  type: 'warning'
              });
            }
          })
        }
      },
      
    },
    created() {
     
    },
    mounted() {
      this.user = JSON.parse(this.getUser)
    }
 }
</script>

<style scoped>
    .header {
      background: #FDEBD3;
      border-bottom: 1px solid #DFDBC4;
      width: 100%;
      height: 50px !important;
      display: flex;
      flex-direction: row;
      align-items: center;
      padding-left: 0;
      justify-content: space-between;
      box-shadow: none;
    }
    .icon-div {
      width: 50px;
      height: 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      border-radius: 10px;
      transition: all 0.2s ease;
      margin: 0 2px;
    }
    .icon-div:hover {
      background: rgba(101, 113, 102, 0.1);
      color: #657166;
    }
    .icon {
      font-size: 19px;
    }
    .header-right {
      width: 46%;
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: flex-end;
      padding-right: 12px;
      gap: 8px;
    }
    .theme-toggle {
      display: inline-flex;
      flex-shrink: 0;
      border-radius: 10px;
      padding: 2px;
      background: rgba(255, 255, 255, 0.55);
      border: 1px solid #DFDBC4;
    }
    .theme-btn {
      border: none;
      background: transparent;
      color: #4a5568;
      font-size: 12px;
      font-weight: 600;
      padding: 5px 10px;
      border-radius: 8px;
      cursor: pointer;
      font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
      transition: background 0.2s ease, color 0.2s ease;
    }
    .theme-btn:hover {
      color: #657166;
      background: rgba(101, 113, 102, 0.12);
    }
    .theme-btn.active {
      background: #ffffff;
      color: #3d3d3d;
      box-shadow: 0 1px 4px rgba(101, 113, 102, 0.15);
    }
    .search-input {
      width: 54%;
    }
    .search-input >>> .el-input__inner {
      background: #FDEBD3;
      border: 1px solid #DFDBC4;
      border-radius: 10px;
      color: #3d3d3d;
    }
    .search-input >>> .el-input__inner:focus {
      border-color: #99CDDB;
      box-shadow: 0 0 0 3px rgba(153, 205, 219, 0.2);
    }
    .header-right .icon-div:last-child {
      width: auto !important;
      padding: 0 10px;
      font-weight: 600;
      color: #4a5568;
    }
</style>