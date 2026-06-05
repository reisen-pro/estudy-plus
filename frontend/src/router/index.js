import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/course',
    children: [
      { path: 'course', name: 'Course', component: () => import('../views/CourseList.vue'), meta: { title: '课程中心' } },
      { path: 'course/upload', name: 'CourseUpload', component: () => import('../views/CourseUpload.vue'), meta: { title: '上传课程', permission: 'course:upload' } },
      { path: 'course/:id', name: 'CourseDetail', component: () => import('../views/CourseDetail.vue'), meta: { title: '课程详情' } },
      { path: 'exam', name: 'Exam', component: () => import('../views/ExamList.vue'), meta: { title: '在线考试' } },
      { path: 'task', name: 'TaskCenter', component: () => import('../views/TaskCenter.vue'), meta: { title: '我的任务' } },
      { path: 'exam/take/:recordId', name: 'ExamTake', component: () => import('../views/ExamTake.vue'), meta: { title: '答题' } },
      { path: 'exam/result', name: 'ExamResult', component: () => import('../views/ExamResult.vue'), meta: { title: '考试成绩' } },
      { path: 'community', name: 'Community', component: () => import('../views/CommunityList.vue'), meta: { title: '学习社区' } },
      { path: 'community/post/:id', name: 'PostDetail', component: () => import('../views/PostDetail.vue'), meta: { title: '帖子详情' } },
      { path: 'notification', name: 'Notification', component: () => import('../views/NotificationList.vue'), meta: { title: '消息通知' } },
      { path: 'permission/apply', name: 'PermissionApply', component: () => import('../views/PermissionApply.vue'), meta: { title: '权限申请' } },
      { path: 'permission/review', name: 'PermissionReview', component: () => import('../views/PermissionReview.vue'), meta: { title: '权限审批', permission: 'permission:approve' } },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：登录校验 + 权限校验
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 权限校验
  if (to.meta.permission) {
    const perms = JSON.parse(localStorage.getItem('userPermissions') || '[]')
    if (!perms.includes(to.meta.permission)) {
      next('/permission/apply')
      return
    }
  }

  next()
})

export default router
