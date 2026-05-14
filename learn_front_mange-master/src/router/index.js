import Vue from 'vue'
import VueRouter from 'vue-router'
import login from '@/views/system/login/login'
import error404 from '@/views/error/404'
import error403 from '@/views/error/403'


Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: '登陆',
    component: login,
  },
  {
    path: '/403',
    name: '403',
    component: error403,
  },
  {
    path: '/404',
    name: '404',
    component: error404,
  },
  {
    path: '/admin/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index'),
    meta: { requiresAuth: true } // 添加需要认证的元信息
  }
]

// 防止连续点击多次路由报错
let routerPush = VueRouter.prototype.push;
let routerReplace = VueRouter.prototype.replace;
// push
VueRouter.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
VueRouter.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

const router = new VueRouter({
  mode: 'history',
  routes,
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  // 检查目标路由是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否有有效的token
    const token = localStorage.getItem('token');
    if (!token) {
      // 没有token，重定向到登录页面
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    } else {
      // 有token，允许访问
      next();
    }
  } else {
    // 不需要认证的路由，直接访问
    next();
  }
});

export default router
