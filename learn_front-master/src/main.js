import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './permission'
import elementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import videoPlayer from 'vue-video-player'
import "video.js/dist/video-js.css";
import VueQuillEditor from 'vue-quill-editor'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'
import 'katex/dist/katex.min.css'

Vue.use(videoPlayer)
Vue.use(VueQuillEditor)
Vue.config.productionTip = false
Vue.use(elementUI)

//EventBus事件中转
Vue.prototype.$bus = new Vue()  

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
