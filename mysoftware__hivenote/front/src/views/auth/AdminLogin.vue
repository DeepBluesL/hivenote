<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧：背景大图区 -->
      <div class="login-image">
        <div class="welcome-text">
          <h2>管理员登录</h2>
          <p>HiveNote 后台管理系统</p>
        </div>
      </div>

      <!-- 右侧：登录表单区 -->
      <div class="login-form">
        <div class="form-header">
          <img src="@/assets/platform-logo.png" alt="Logo" class="platform-logo" />
          <h3>管理员登录</h3>
          <span class="subtitle">请输入管理员账号和密码</span>
        </div>

        <el-form :model="form" :rules="rules" ref="loginRef" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="管理员账号" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock"
              show-password @keyup.enter="handleLogin" />
          </el-form-item>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
            立即登录
          </el-button>

          <div class="links">
            <el-link type="primary" @click="router.push('/login')">返回用户登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/user'
import { setToken, setUserInfo } from '@/utils/auth'

const router = useRouter()
const loginRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginRef.value) return

  const valid = await loginRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await adminLogin(form)

    if (res && res.success) {
      // 保存 Token 和用户信息
      if (res.result) {
        if (res.result.token) {
          setToken(res.result.token)
        }
        if (res.result.userInfo) {
          setUserInfo(res.result.userInfo)
          console.log('管理员登录成功，用户信息：', res.result.userInfo)
        }
      }

      ElMessage.success(res.message || '登录成功！')
      
      // 使用 replace 而不是 push，避免路由守卫问题
      setTimeout(() => {
        router.replace('/admin/review')
      }, 300)
    } else {
      ElMessage.error(res.message || '登录失败，请检查账号和密码')
    }
  } catch (error) {
    console.error('登录错误：', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 900px;
  height: 500px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  overflow: hidden;
}

@media (max-width: 768px) {
  .login-card {
    width: 90%;
    max-width: 400px;
    height: auto;
    flex-direction: column;
  }

  .login-image {
    display: none;
  }

  .login-form {
    padding: 40px 30px;
  }
}

.login-image {
  flex: 1.2;
  background: linear-gradient(to bottom, rgba(102, 126, 234, 0.8), rgba(118, 75, 162, 0.9)), url('https://images.unsplash.com/photo-1497633762265-9d179a990aa6?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 40px;
}

.welcome-text {
  position: relative;
  z-index: 1;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.welcome-text h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
}

.welcome-text p {
  margin: 10px 0 0;
  font-size: 16px;
  opacity: 0.9;
}

.login-form {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 30px;
  text-align: center;
}

.platform-logo {
  width: 140px;
  height: 140px;
  margin-bottom: 15px;
  border-radius: 8px;
}

.form-header h3 {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #999;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  font-weight: bold;
  letter-spacing: 1px;
  padding: 20px 0;
}

.links {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
