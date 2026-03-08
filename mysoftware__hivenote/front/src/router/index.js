import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login' // 访问根目录默认跳去登录页
  },
  {
    path: '/login',
    name: 'Login',
    // 懒加载：只有用到这个页面时才去加载代码，路径对应 src/views/auth/Login.vue
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    // 路径对应 src/views/auth/Register.vue
    component: () => import('../views/auth/Register.vue')
  },
  {
  path: '/forgot-password',
  name: 'ForgotPassword',
  component: () => import('../views/auth/ForgotPassword.vue')
},
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/note/NoteList.vue')
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/community/Community.vue')
  },
  {
    // 动态路由：:id 是参数，比如 /note/detail/123
    path: '/note/detail/:id',
    name: 'NoteDetail',
    // 路径对应 src/views/note/NoteDetail.vue
    component: () => import('../views/note/NoteDetail.vue')
  },
  {
    path: '/note/upload',
    name: 'Upload',
    // 路径对应 src/views/note/Upload.vue
    component: () => import('../views/note/Upload.vue')
  },
  {
    path: '/user/profile',
    name: 'Profile',
    // 路径对应 src/views/user/Profile.vue
    component: () => import('../views/user/Profile.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/auth/AdminLogin.vue')
  },
  {
    path: '/admin/review',
    name: 'ReviewManage',
    component: () => import('../views/admin/ReviewManage.vue'),
    meta: { requiresAdmin: true }
  }
]

const router = createRouter({
  // 使用 HTML5 History 模式 (URL里没有 # 号)
  history: createWebHistory(),
  routes
})

// 全局前置守卫 (用于拦截未登录用户)
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}
  
  // 白名单：不需要登录就能访问的页面
  const whiteList = ['/login', '/register', '/forgot-password', '/admin/login', '/community']
  
  if (token) {
    // 已登录
    if (to.path === '/login') {
      // 普通用户登录页：如果是管理员，跳转到审核页；否则跳转到首页
      if (userInfo.type === 1) {
        next('/admin/review')
      } else {
        next('/home')
      }
    } else if (to.path === '/admin/login') {
      // 管理员登录页：如果已经是管理员，跳转到审核页；否则跳转到首页
      if (userInfo.type === 1) {
        next('/admin/review')
      } else {
        next('/home')
      }
    } else if (to.meta.requiresAdmin) {
      // 需要管理员权限的页面
      if (userInfo.type === 1) {
        next()
      } else {
        // 普通用户访问管理员页面，跳转到首页
        next('/home')
      }
    } else {
      next()
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 在白名单中，直接放行
      next()
    } else {
      // 不在白名单中，跳转到登录页
      if (to.path.startsWith('/admin')) {
        next('/admin/login')
      } else {
        next('/login')
      }
    }
  }
})

export default router