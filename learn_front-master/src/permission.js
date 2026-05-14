import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken,removeToken } from '@/utils/token'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register','/teacherRegister']

router.beforeEach((to, from, next) => {
    NProgress.start()
    console.log(getToken())
    if(getToken()) {
        if (whiteList.indexOf(to.path) !== -1) {
            next(`/`)
        }
        next()
    } else {
        console.log("111111")
        // 没有token
        if (whiteList.indexOf(to.path) !== -1) {
            // 在免登录白名单，直接进入
            next()
        } else {
            next(`/login`) // 否则全部重定向到登录页
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done()
})