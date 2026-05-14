import Vue from 'vue'
import Vuex from 'vuex'
import menu from './modules/menu' 
import user from './modules/user' 
import configure from './modules/configure' 
import theme from './modules/theme'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    menu,
    user,
    configure,
    theme
  }
})
