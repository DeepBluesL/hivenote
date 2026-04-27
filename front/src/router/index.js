import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/auth/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/auth/Register.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/auth/ForgotPassword.vue') },
  { path: '/home', name: 'Home', component: () => import('../views/note/NoteList.vue') },
  { path: '/community', name: 'Community', component: () => import('../views/community/Community.vue') },
  { path: '/community/posts', name: 'PostList', component: () => import('../views/community/PostList.vue') },
  { path: '/community/post/create', name: 'PostCreate', component: () => import('../views/community/PostCreate.vue') },
  { path: '/community/post/edit/:id', name: 'PostEdit', component: () => import('../views/community/PostEdit.vue') },
  { path: '/community/post/:id', name: 'PostDetail', component: () => import('../views/community/PostDetail.vue') },
  { path: '/note/detail/:id', name: 'NoteDetail', component: () => import('../views/note/NoteDetail.vue') },
  { path: '/note/upload', name: 'Upload', component: () => import('../views/note/Upload.vue') },
  { path: '/user/profile', name: 'Profile', component: () => import('../views/user/Profile.vue') },
  { path: '/google-callback', name: 'GoogleCallback', component: () => import('../views/auth/GoogleCallback.vue') },
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/auth/AdminLogin.vue') },
  { path: '/admin/review', name: 'ReviewManage', component: () => import('../views/admin/ReviewManage.vue'), meta: { requiresAdmin: true } },
  // 新增：我的上传页面
  {
    path: '/my-uploads',
    name: 'MyUploads',
    component: () => import('../views/note/MyUploads.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}

  const whiteList = ['/login', '/register', '/forgot-password', '/admin/login', '/community', '/google-callback']
  const isWhiteListed = whiteList.includes(to.path) || to.path.startsWith('/community/')

  if (token) {
    if (to.path === '/login') {
      if (userInfo.type === 1) next('/admin/review')
      else next('/home')
    } else if (to.path === '/admin/login') {
      if (userInfo.type === 1) next('/admin/review')
      else next('/home')
    } else if (to.meta.requiresAdmin) {
      if (userInfo.type === 1) next()
      else next('/home')
    } else {
      next()
    }
  } else {
    if (isWhiteListed) next()
    else if (to.path.startsWith('/admin')) next('/admin/login')
    else next('/login')
  }
})

export default router
