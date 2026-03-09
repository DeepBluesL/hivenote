<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧：背景大图区 -->
      <div class="login-image">
        <div class="welcome-text">
          <h2>Welcome Back!</h2>
          <p>大学数字笔记平台</p>
        </div>
      </div>

      <!-- 右侧：登录表单区 -->
      <div class="login-form">
        <!-- 平台 Logo 放在表单顶部 -->
        <div class="form-header">
          <img src="@/assets/platform-logo.png" alt="Logo" class="platform-logo" />
          <h3>账号登录</h3>
          <span class="subtitle">请输入您的学号和密码</span>
        </div>

        <el-form :model="form" :rules="rules" ref="loginRef" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="学号 / Student ID" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码 / Password" :prefix-icon="Lock"
              show-password @keyup.enter="handleLogin" />
          </el-form-item>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
            立即登录
          </el-button>

          <div class="links">
            <el-link type="info" @click="router.push('/forgot-password')">忘记密码?</el-link>
            <div class="right-links">
              <el-link type="primary" @click="router.push('/register')">注册新账号</el-link>
              <span class="divider">|</span>
              <el-link type="warning" @click="router.push('/admin/login')">管理员登录</el-link>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import { setToken, setUserInfo, isLoggedIn } from '@/utils/auth'

const router = useRouter()
const loginRef = ref(null)
const loading = ref(false)

// 如果已经登录，直接跳转到首页
onMounted(() => {
  if (isLoggedIn()) {
    router.push('/home')
  }
})

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '学号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 20, message: '密码长度在 3 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginRef.value) return

  const valid = await loginRef.value.validate()
  if (!valid) return

  const res = await login(form)

  if (res && res.success) {
    // 保存 Token 和用户信息
    if (res.result) {
      if (res.result.token) {
        setToken(res.result.token)
      }
      if (res.result.userInfo) {
        setUserInfo(res.result.userInfo)
      } else if (res.result.user) {
        // 兼容不同的返回格式
        setUserInfo(res.result.user)
      }
    }

    ElMessage.success(res.message || '登录成功！')
    // 跳转到首页
    setTimeout(() => {
      router.replace('/home')
    }, 300)

  } else {
    ElMessage.error(res.message || '登录失败，请检查学号和密码')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 浅色渐变背景 */
  background: linear-gradient(135deg, #f5f2ae 0%, #ccdef6 100%);
}

.login-card {
  width: 900px;
  height: 500px;
  background: #fff;
  border-radius: 16px;
  /* 柔和的阴影 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
}

/* 响应式：手机端隐藏左侧大图 */
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
  /* 这里放那张漂亮的图书馆/校园背景图 */
  background: linear-gradient(to bottom, rgba(28, 36, 52, 0.4), rgba(28, 36, 52, 0.8)), url('https://images.unsplash.com/photo-1541339907198-e08756dedf3f?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80');
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

/* 你的平台 Logo 样式 */
.platform-logo {
  width: 140px;
  /* 大小适中 */
  height: 140px;
  margin-bottom: 15px;
  border-radius: 8px;
  /* 如果是方形Logo，加点圆角更好看 */
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
  justify-content: space-between;
  align-items: center;
}

.right-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.divider {
  color: #dcdfe6;
  margin: 0 4px;
}
</style>