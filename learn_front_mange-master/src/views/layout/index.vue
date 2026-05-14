<template>
  <div class="common-layout">
    <el-container v-show="lock == 0">
      <el-asside></el-asside>
      <el-container>
        <el-header></el-header>
        <div class="main-menu">
          <div class="scroll-left" @click="leftScroll()">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="menu" id="menu">
            <div class="menu-item" :class="item.url == activeMenu ? 'menu-item-active' : ''" v-for="(item,index) in activeMenuArrary" :key="index" @click="openMenu(item.url)">
              <span :class="item.url == activeMenu?'active':''" class="menu-name">{{item.name}}</span>
              <i v-if="item.url != '/index'" class="menu-icon el-icon-close" @click.stop="closeMenu(item.url)"></i>
            </div>
          </div>
          <div class="scroll-right" @click="rightScroll()">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
        <el-main></el-main>
      </el-container>
    </el-container>
    <el-container v-show="lock == 1">
    <div class="lock" >
        <img class="lock-back" src="../../assets/image/lock-back.jpg"/>
        <div class="form">
          <div class="content">
            <el-input prefix-icon="el-icon-star-off" type="password" v-model="pass" size="mini" placeholder="请输入密码"></el-input>
            <div @click="unlock"><i class="el-icon-d-arrow-right"></i></div>
          </div>
        </div>
    </div>
    </el-container>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { getLock,setLock } from '@/utils/lock'
  import { verPassword } from '@/api/api'
  import elAsside from "../../components/layout/aside"
  import elMain from "../../components/layout/main"
  import elHeader from "../../components/layout/header"

  export default {
    name: "index",
    data() {
      return{
        lock: 0,
        pass: "",
      }
    },
    components:{
      elAsside,
      elMain,
      elHeader
    },
    computed: {
      ...mapState({
          activeMenuArrary: state => state.menu.activeMenuArrary,
          activeMenu: state => state.menu.activeMenu
      })
    },
    methods: {
      leftScroll() {
        var container = document.getElementById('menu');
        // 向左滚动100像素
        container.scrollLeft -= 100;
      },
      rightScroll() {
        var container = document.getElementById('menu');
        // 向右滚动100像素
        container.scrollLeft += 100;
      },
      openMenu(url) {
        if (this.activeMenu != url) {
          this.$router.push({
            path: url,
          })
          this.$store.commit('menu/setActiveMenu', url)
        }
      },
      closeMenu(url) {
        var index = this.activeMenuArrary.length - 1
        for(let i = 0;i < this.activeMenuArrary.length;i++) {
          if(this.activeMenuArrary[i].url == url) {
            index = i;
            break;
          }
        }
        if(this.activeMenu == url) {
          this.$router.push({
            path: this.activeMenuArrary[index-1].url,
          })
          this.$store.commit('menu/setActiveMenu', this.activeMenuArrary[index-1].url)
        }
        this.$store.commit('menu/reduceActiveMenu', index)
        this.$bus.$emit("clearKeepAlive", url);
      },
      unlock() {
        if (!this.pass) {
          this.$message({
              message: '请输入密码验证',
              type: 'warning'
          });
          return;
        }
        verPassword({password:this.pass}).then(res => {
          if (res.code == 1000) {
            this.$message({
              message: '验证通过',
              type: 'success'
            });
            this.pass = ""
            this.$bus.$emit('lock', 0)
          } else {
            this.$message({
              message: res.message,
              type: 'error'
            });
          }
        })
      }
    },
    created() {
     
    },
    mounted() {
      // console.clear()
      this.$nextTick(() => {
        this.lock = getLock()
        this.$bus.$on('lock', res=>{
          setLock(res)
          this.lock = res
        })
      })
      
    }
 }
</script>

<style   scoped>
    .common-layout >>> .el-menu-item {
        min-width: 0;
    }
    .main-menu {
      width: 100%;
      height: 38px;
      border-top: 1px solid #DFDBC4;
      border-bottom: 1px solid #DFDBC4;
      background: #FDEBD3;
      display: flex;
      flex-direction: row;
      align-items: center;
    }
    .menu {
      display: flex;
      flex-direction: row;
      align-items: center;
      height: 100%;
      width: calc(100% - 60px);
      overflow: scroll;
      flex-grow: 0;
      flex-basis: auto
    }
    .scroll-left {
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: center;
      width: 30px;
      height: 100%;
      cursor: pointer;
      color: #657166;
    }
    .menu::-webkit-scrollbar {
      width: 0px;
      height: 0px;
    }
    .scroll-right {
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: center;
      width: 30px;
      height: 100%;
      cursor: pointer;
      color: #657166;
    }
    .menu-item {
      width: 116px;
      height: 28px;
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      border: 1px solid rgba(223,219,196,0.5);
      cursor: pointer;
      background: #FDEBD3;
      flex-shrink: 0;
      border-radius: 8px;
      margin-right: 6px;
      transition: all 0.2s ease;
    }
    .menu-item:hover {
      border-color: #DFDBC4;
      background: #FDF5E8;
    }
    .menu-item-active {
      background: #F3C3B2 !important;
      border-color: #e8b3a1 !important;
      box-shadow: 0 4px 10px rgba(243, 195, 178, 0.3);
    }
    .menu-item-active:hover {
      background: #F3C3B2 !important;
      border-color: #e8b3a1 !important;
    }
    .menu-name {
      margin-left:10px;
      font-size: 12px;
      cursor: pointer;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #4a5568;
      font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
    }
    .active {
      color: #657166;
      font-weight: 600;
    }
    .menu-icon {
      cursor: pointer;
      font-size: 12px;
      margin-right: 8px;
      color: #8a8a7a;
    }
    .menu-item-active .menu-icon {
      color: #c47a60;
    }
    .lock {
      width: 100%;
      height: 100vh;
    }
    .lock-back {
      width: 100%;
      height: 100%;
      z-index: -1;
      position: fixed;
      object-fit: cover;
    }
    .form {
      z-index: 999;
      width: 100%;
      height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .content {
      display: flex;
      align-items: center;
      color: black;
      font-size: 20px;
      cursor: pointer;
      padding: 12px 16px;
      border-radius: 12px;
      background: rgba(255,255,255,0.84);
      border: 1px solid #e6ecf7;
      box-shadow: 0 12px 30px rgba(18, 35, 80, 0.16);
    }
</style>