<template>
  <div class="fullscreen-labs">
    <headerPage></headerPage>
    
    <div class="lab-container">
      <div class="lab-header">
        <h2>物理实验室</h2>
        <button @click="exitLab" class="exit-btn">
          退出实验室
        </button>
      </div>
      
      <div class="lab-content" ref="fullscreenRoot" :class="{ 'is-fullscreen': isFullscreen }">
        <div v-if="loading" class="loading-screen">
          <div class="loading-content">
            <div class="spinner"></div>
            <h2>正在启动物理实验室...</h2>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progress + '%' }"></div>
            </div>
            <p class="progress-text">{{ Math.round(progress) }}%</p>
          </div>
        </div>
        
        <canvas id="tuanjie-canvas" ref="unityCanvas" class="unity-canvas"></canvas>
        <button @click="toggleFullscreen" class="fullscreen-btn">
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </button>
      </div>
    </div>
    
    <bottomPage></bottomPage>
  </div>
</template>

<script>
import headerPage from "../../components/header/header"
import bottomPage from "../../components/bottom/bottom"

export default {
  name: "PhysicsLab",
  components: {
    headerPage,
    bottomPage
  },
  data() {
    return {
      unityInstance: null,
      loading: true,
      progress: 0,
      loadTimeout: null,
      loadTimeoutMs: 180000,
      isFullscreen: false
    }
  },
  
  mounted() {
    this.loadUnity();
    this.tryAutoFullscreenFromEntry();
    // 监听全屏变化事件
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  
  methods: {
  getUnityFactory() {
    return window.createUnityInstance || window.createTuanjieInstance || null;
  },

  requestFullscreenForElement(el) {
    if (!el) return Promise.reject(new Error('Fullscreen target missing'));

    if (el.requestFullscreen) return el.requestFullscreen();
    if (el.webkitRequestFullscreen) return el.webkitRequestFullscreen();
    if (el.msRequestFullscreen) return el.msRequestFullscreen();

    return Promise.reject(new Error('Fullscreen API not supported'));
  },

  tryAutoFullscreenFromEntry() {
    let shouldAutoFullscreen = false;
    try {
      shouldAutoFullscreen = window.sessionStorage.getItem('physicsLab_autoFullscreen') === '1';
      window.sessionStorage.removeItem('physicsLab_autoFullscreen');
    } catch (e) {}

    if (!shouldAutoFullscreen) return;

    this.$nextTick(() => {
      const target = this.$refs.fullscreenRoot || this.$refs.unityCanvas;
      this.requestFullscreenForElement(target)
        .catch((err) => {
          // 可能被浏览器手势策略拦截：不影响Unity加载，只做提示降级
          console.warn('Auto fullscreen blocked:', err);
          if (this && this.$message) {
            this.$message.info('浏览器限制自动全屏，请点击右下角“全屏”按钮进入全屏模式');
          }
        });
    });
  },

  setLoadTimeout() {
    this.clearLoadTimeout();
    this.loadTimeout = setTimeout(() => {
      // WebGL资源体积大，弱网下可能超过30秒；提示但不直接判定失败
      this.$message.warning('加载较慢，请耐心等待（弱网可能需要1-3分钟）');
    }, this.loadTimeoutMs);
  },

  clearLoadTimeout() {
    if (this.loadTimeout) {
      clearTimeout(this.loadTimeout);
      this.loadTimeout = null;
    }
  },

  loadUnity() {
    const buildName = process.env.VUE_APP_UNITY_BUILD_NAME || 'NewUnityBuild';
    const baseUrl = '/physics';
    const cacheBust = process.env.NODE_ENV === 'development' ? `?v=${Date.now()}` : '';
    const unityFactory = this.getUnityFactory();
    if (unityFactory) {
      this.initUnity(unityFactory);
      return;
    }

    this.setLoadTimeout();
    const scriptId = 'physics-lab-loader-script';
    let script = document.getElementById(scriptId);
    const onLoad = () => {
      this.clearLoadTimeout();
      const factory = this.getUnityFactory();
      if (!factory) {
        this.$message.error('加载器已加载，但未找到 Unity 创建函数');
        this.loading = false;
        return;
      }
      this.initUnity(factory);
    };

    const onError = () => {
      this.clearLoadTimeout();
      this.$message.error('实验室加载失败，请检查静态资源路径');
      this.loading = false;
    };

    if (script) {
      script.addEventListener('load', onLoad, { once: true });
      script.addEventListener('error', onError, { once: true });
      return;
    }

    script = document.createElement('script');
    script.id = scriptId;
    script.src = `${baseUrl}/Build/${buildName}.loader.js${cacheBust}`;
    script.async = true;
    script.onload = onLoad;
    script.onerror = onError;
    document.body.appendChild(script);
  },
  
  initUnity(unityFactory) {
    const buildName = process.env.VUE_APP_UNITY_BUILD_NAME || 'NewUnityBuild';
    const baseUrl = '/physics';
    const cacheBust = process.env.NODE_ENV === 'development' ? `?v=${Date.now()}` : '';
    const config = {
      dataUrl: `${baseUrl}/Build/${buildName}.data${cacheBust}`,
      frameworkUrl: `${baseUrl}/Build/${buildName}.framework.js${cacheBust}`,
      codeUrl: `${baseUrl}/Build/${buildName}.wasm${cacheBust}`,
      cacheControl: () => 'no-store',
      streamingAssetsUrl: `${baseUrl}/StreamingAssets`,
      companyName: 'YourSchool',
      productName: 'PhysicsLab',
      productVersion: '1.0',
      
      onProgress: (progress) => {
        this.progress = progress * 100;
      }
    };

    this.setLoadTimeout();
    unityFactory(this.$refs.unityCanvas, config)
      .then((instance) => {
        this.clearLoadTimeout();
        this.unityInstance = instance;
        // 延长加载界面显示时间以覆盖Unity开场动画
        setTimeout(() => {
          this.loading = false;
        }, 4000);
      })
      .catch((error) => {
        this.clearLoadTimeout();
        console.error('加载失败:', error);
        this.$message.error('实验室加载失败，请刷新重试');
        this.loading = false;
      });
  },
  
  safeQuitUnity() {
    if (this.unityInstance && typeof this.unityInstance.Quit === 'function') {
      this.unityInstance.Quit().catch(() => {});
      this.unityInstance = null;
    }
  },

  exitLab() {
    this.safeQuitUnity();
    this.$router.push('/');
  },

  toggleFullscreen() {
    if (!document.fullscreenElement) {
      // 进入全屏
      const target = this.$refs.fullscreenRoot || this.$refs.unityCanvas;
      this.requestFullscreenForElement(target).catch(() => {});
    } else {
      // 退出全屏
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) { /* Safari */
        document.webkitExitFullscreen();
      } else if (document.msExitFullscreen) { /* IE11 */
        document.msExitFullscreen();
      }
    }
  },

  handleFullscreenChange() {
    this.isFullscreen = !!document.fullscreenElement;
    try {
      window.dispatchEvent(new Event('resize'));
    } catch (e) {}
  }
},
    
  
  beforeDestroy() {
    this.clearLoadTimeout();
    this.safeQuitUnity();
    // 移除全屏事件监听器
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  }
}
</script>

<style scoped>
.fullscreen-labs {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.lab-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.lab-header {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lab-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.exit-btn {
  padding: 10px 24px;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.exit-btn:hover {
  background: #f78989;
}

.lab-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  min-height: 600px;
}

.lab-content.is-fullscreen {
  width: 100vw;
  height: 100vh;
}

.lab-content.is-fullscreen .unity-canvas {
  height: 100vh;
}

.lab-content.is-fullscreen .loading-screen {
  height: 100vh;
}

.unity-canvas {
  width: 100%;
  height: 600px;
  display: block;
}

.fullscreen-btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 10px 15px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s;
}

.fullscreen-btn:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: scale(1.05);
}

.loading-screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 600px;
  background: linear-gradient(135deg, #1a2a3a 0%, #0a1a2a 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
  color: white;
  width: 400px;
}

.loading-content h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
}

.spinner {
  width: 60px;
  height: 60px;
  margin: 0 auto 30px;
  border: 4px solid rgba(255,255,255,0.1);
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
  margin: 20px 0 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #409eff;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #8ba5c4;
  margin: 0;
}
</style>
