<template>
  <div class="forgot-container">
    <div class="forgot-card">
      <div class="forgot-image">
        <div class="welcome-text">
          <h2>Secure Reset</h2>
          <p>找回您的账号密码</p>
          <p class="sub-text">安全 · 快捷 · 可靠</p>
        </div>
      </div>

      <div class="forgot-form">
        <div class="form-header">
          <img src="@/assets/platform-logo.png" alt="Logo" class="platform-logo" />
          <h3>重置密码</h3>
          <span class="subtitle">验证身份并设置新密码</span>
        </div>

        <el-form 
          :model="form" 
          :rules="rules" 
          ref="forgotRef" 
          size="large"
          status-icon
        >
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入学号 / Student ID" 
              :prefix-icon="User" 
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="注册邮箱 / Email Address" 
              :prefix-icon="Message"
            >
              <template #append>
                <el-button 
                  @click="sendCode" 
                  :disabled="isSending || countdown > 0"
                >
                  {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code">
            <el-input 
              v-model="form.code" 
              placeholder="请输入验证码 (模拟: 123456)" 
              :prefix-icon="Key" 
              maxlength="6"
            />
          </el-form-item>

          <el-form-item prop="newPassword">
            <el-input 
              v-model="form.newPassword" 
              type="password" 
              placeholder="新密码 / New Password" 
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleReset">
            确认重置
          </el-button>

          <div class="links">
            <el-link type="info" @click="$router.push('/login')">想起密码了？返回登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Message, Key, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const forgotRef = ref(null)
const loading = ref(false)
const isSending = ref(false)
const countdown = ref(0)

const form = reactive({
  username: '',
  email: '',
  code: '',
  newPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 模拟发送验证码
const sendCode = () => {
  if (!form.email) return ElMessage.warning('请先输入邮箱')
  
  isSending.value = true
  ElMessage.success(`验证码已发送至 ${form.email} (测试码: 123456)`)
  
  // 倒计时逻辑
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      isSending.value = false
    }
  }, 1000)
}

// 处理重置
const handleReset = () => {
  forgotRef.value.validate(async (valid) => {
    if (valid) {
      if (form.code !== '123456') {
        return ElMessage.error('验证码错误 (请使用测试码: 123456)')
      }

      loading.value = true
      // --- 模拟后端请求 ---
      setTimeout(() => {
        loading.value = false
        ElMessage.success('密码重置成功！请使用新密码登录')
        router.push('/login')
      }, 1500)
    }
  })
}
</script>

<style scoped>
/* 样式复用并微调背景色，使用紫色系渐变区分 */
.forgot-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
}

.forgot-card {
  width: 900px;
  height: 550px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  overflow: hidden;
}

@media (max-width: 768px) {
  .forgot-card {
    width: 90%;
    max-width: 400px;
    height: auto;
    flex-direction: column;
  }
  .forgot-image { display: none; }
  .forgot-form { padding: 40px 30px; }
}

.forgot-image {
  flex: 1.1;
  /* 换一张“锁/安全”主题的图片 */
  background: linear-gradient(to bottom, rgba(103, 58, 183, 0.4), rgba(81, 45, 168, 0.8)), url('https://images.unsplash.com/photo-1614064641938-3bcee529cfc4?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80');
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
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}
.welcome-text h2 { margin: 0; font-size: 32px; font-weight: 700; }
.welcome-text p { margin: 10px 0 0; font-size: 18px; }
.sub-text { font-size: 14px !important; opacity: 0.8; margin-top: 5px !important; }

.forgot-form {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header { margin-bottom: 25px; text-align: center; }
.platform-logo { width: 70px; height: 70px; margin-bottom: 10px; }
.form-header h3 { font-size: 24px; color: #333; margin: 0; }
.subtitle { color: #999; font-size: 13px; }

.submit-btn { width: 100%; padding: 20px 0; font-weight: bold; letter-spacing: 1px; margin-top: 10px; }

.links { margin-top: 20px; text-align: center; }
</style>