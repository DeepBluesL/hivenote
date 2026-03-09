import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUserInfo } from '@/utils/auth'

const routes = [
  {
    path: '/',
    redirect: '/note/list'
  },
  
  // 认证相关
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/auth/AdminLogin.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue')
  },
  
  // 笔记相关
  {
    path: '/note/list',
    name: 'NoteList',
    component: () => import('@/views/note/NoteList.vue')
  },
  {
    path: '/note/detail/:id',
    name: 'NoteDetail',
    component: () => import('@/views/note/NoteDetail.vue')
  },
  {
    path: '/note/upload',
    name: 'UploadNote',
    component: () => import('@/views/note/Upload.vue'),
    meta: { requiresAuth: true }
  },
  
  // 社区相关
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/community/Community.vue')
  },
  
  // 用户相关
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { requiresAuth: true }
  },
  
  // 管理员相关
  {
    path: '/admin/review',
    name: 'ReviewManage',
    component: () => import('@/views/admin/ReviewManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/audit',
    name: 'Audit',
    component: () => import('@/views/admin/AuditView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/note/list'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 公开页面列表
  const publicPages = [
    '/login',
    '/admin/login',
    '/register',
    '/forgot-password',
    '/note/list',
    '/community'
  ]
  
  // 笔记详情页公开
  if (to.path.startsWith('/note/detail/')) {
    return next()
  }
  
  const token = getToken()
  const userInfo = getUserInfo()
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    if (!token) {
      // 未登录，去管理员登录页
      return next('/admin/login')
    }
    
    // 检查用户类型是否为管理员 (type === 1)
    if (userInfo && userInfo.type === 1) {
      return next()
    } else {
      // 不是管理员，去普通登录页
      ElMessage.error('无权访问管理员页面')
      return next('/login')
    }
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }
  
  next()
})

export default router
