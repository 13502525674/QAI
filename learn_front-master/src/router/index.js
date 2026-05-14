import Vue from 'vue'
import VueRouter from 'vue-router'
import login from '@/views/login/login'
import index from '@/views/index/index'
import register from '@/views/register/register'
import teacherRegister from '@/views/teacherRegister/teacherRegister'
import task from '@/views/task/task'
import teacher from '@/views/teacher/teacher'
import test from '@/views/testP/test'
import notice from '@/views/notice/notice'
import message from '@/views/message/message'
import article from '@/views/article/article'
import taskInfo from '@/views/task/taskInfo'
import teacherInfo from '@/views/teacher/teacherInfo'

import testInfo from '@/views/testP/testInfo'
import articleInfo from '@/views/article/articleInfo'
import center from '@/views/center/center'
import myTask from '@/views/task/myTask'

import myArticle from '@/views/article/myArticle'


import addArticle from '@/views/article/addArticle'
import editArticle from '@/views/article/editArticle'
import about from '@/views/about/about'
import forum from '@/views/forum/forum'
import forumInfo from '@/views/forum/forumInfo'
import question from '@/views/question/question'
import myFavor from '@/views/task/myFavor'
import myFavarArticle from '@/views/article/myFavarArticle'
import rateOfLearning from '@/views/task/rateOfLearning'
import physicsPractice from '@/views/physics/physicsPractice'
import physicsPracticeDetail from '@/views/physics/physicsPracticeDetail'
import physicsPracticeRecords from '@/views/physics/physicsPracticeRecords'
import physicsPracticeRecordDetail from '@/views/physics/physicsPracticeRecordDetail'
import physicsPracticeGrading from '@/views/physics/physicsPracticeGrading'
import physicsPracticeGradingDetail from '@/views/physics/physicsPracticeGradingDetail'
import physicsDocumentUpload from '@/views/physics/physicsDocumentUpload'
import knowledgeGraph from '@/views/knowledge/knowledgeGraph'
import physicsLab from '@/views/physics/physicsLab'
import studyRoom from '@/views/studyRoom/studyRoom'

import error404 from '@/views/error/404'
import FunPhysics from '@/views/funphysics/FunPhysics'
import FunPhysicsQuiz from '@/views/funphysics/FunPhysicsQuiz'
import FunPhysicsMoments from '@/views/funphysics/FunPhysicsMoments'
import FunPhysicsPuzzle from '@/views/funphysics/FunPhysicsPuzzle'
import FunPhysicsText2Image from '@/views/funphysics/FunPhysicsText2Image'

Vue.use(VueRouter)


const routes = [
  {
    path: '/',
    name: 'index',
    component: index
  },
  {
    path: '/myFavor',
    name: 'myFavor',
    component: myFavor
  },
  {
    path: '/myFavarArticle',
    name: 'myFavarArticle',
    component: myFavarArticle
  },
  {
    path: '/forum',
    name: 'forum',
    component: forum
  },
  {
    path: '/question',
    name: 'question',
    component: question
  },
  {
    path: '/forumInfo',
    name: 'forumInfo',
    component: forumInfo
  },
  {
    path: '/rateOfLearning',
    name: 'rateOfLearning',
    component: rateOfLearning
  },
  {
    path: '/login',
    name: 'login',
    component: login
  },
  {
    path: '/register',
    name: 'register',
    component: register
  },
  {
    path: '/teacherRegister',
    name: 'teacherRegister',
    component: teacherRegister
  },
  {
    path: '/task',
    name: 'task',
    component: task
  },
  {
    path: '/taskInfo',
    name: 'taskInfo',
    component: taskInfo
  },
  {
    path: '/teacher',
    name: 'teacher',
    component: teacher
  },
  {
    path: '/teacherInfo',
    name: 'teacherInfo',
    component: teacherInfo
  },
  {
    path: '/test',
    name: 'test',
    component: test
  },
  {
    path: '/testInfo',
    name: 'testInfo',
    component: testInfo
  },
  {
    path: '/article',
    name: 'article',
    component: article
  },
  {
    path: '/articleInfo',
    name: 'articleInfo',
    component: articleInfo
  },
  {
    path: '/notice',
    name: 'notice',
    component: notice
  },
  {
    path: '/message',
    name: 'message',
    component: message
  },

  {
    path: '/about',
    name: 'about',
    component: about
  },
  {
    path: '/center',
    name: 'center',
    component: center
  },
  {
    path: '/myTask',
    name: 'myTask',
    component: myTask
  },
  {
    path: '/physicsPractice',
    name: 'physicsPractice',
    component: physicsPractice
  },
  {
    path: '/physicsPracticeDetail',
    name: 'physicsPracticeDetail',
    component: physicsPracticeDetail
  },
  {
    path: '/physicsPracticeRecords',
    name: 'physicsPracticeRecords',
    component: physicsPracticeRecords
  },
  {
    path: '/physicsPracticeRecordDetail',
    name: 'physicsPracticeRecordDetail',
    component: physicsPracticeRecordDetail
  },
  {
    path: '/physicsPracticeGrading',
    name: 'physicsPracticeGrading',
    component: physicsPracticeGrading
  },
  {
    path: '/physicsPracticeGradingDetail',
    name: 'physicsPracticeGradingDetail',
    component: physicsPracticeGradingDetail
  },
  {
    path: '/physicsDocumentUpload',
    name: 'physicsDocumentUpload',
    component: physicsDocumentUpload
  },
  {
    path: '/knowledgeGraph',
    name: 'knowledgeGraph',
    component: knowledgeGraph
  },
  {
    path: '/myArticle',
    name: 'myArticle',
    component: myArticle
  },


  {
    path: '/addArticle',
    name: 'addArticle',
    component: addArticle
  },
  {
    path: '/editArticle',
    name: 'editArticle',
    component: editArticle
  },
  {
  path: '/physicsLab',
  name: 'physicsLab',
  component: physicsLab
  },
  {
    path: '/funphysics',
    name: 'FunPhysics',
    component: FunPhysics
  },
  {
    path: '/funphysics/quiz',
    name: 'FunPhysicsQuiz',
    component: FunPhysicsQuiz
  },
  {
    path: '/funphysics/moments',
    name: 'FunPhysicsMoments',
    component: FunPhysicsMoments
  },
  {
    path: '/funphysics/puzzle',
    name: 'FunPhysicsPuzzle',
    component: FunPhysicsPuzzle
  },
  {
    path: '/funphysics/text2image',
    name: 'FunPhysicsText2Image',
    component: FunPhysicsText2Image
  },
  {
    path: '/studyRoom',
    name: 'studyRoom',
    component: studyRoom
  },
  {
    path: '/404',
    name: '404',
    component: error404,
  },
  { 
    path: '/:pathMatch(.*)',
    redirect: '/404'
 },
  
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
  routes
})

export default router
