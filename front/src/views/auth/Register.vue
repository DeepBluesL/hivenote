<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-image">
        <div class="welcome-text">
          <h2>Join Us!</h2>
          <p>开启您的数字笔记共享之旅</p>
          <p class="sub-text">分享知识 · 共同进步</p>
        </div>
      </div>
      
      <div class="register-form">
        <div class="form-header">
          <img src="@/assets/platform-logo.png" alt="Logo" class="platform-logo" />
          <h3>注册新账号</h3>
          <span class="subtitle">请填写以下信息完成注册</span>
        </div>
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="registerRef" 
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

          <el-form-item prop="nickname">
            <el-input 
              v-model="form.nickname" 
              placeholder="请输入昵称 / Nickname" 
              :prefix-icon="Postcard" 
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="请输入常用邮箱 / Email" 
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
              placeholder="请输入邮箱验证码 (模拟: 666666)" 
              :prefix-icon="Key" 
              maxlength="6"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="设置密码 / Password" 
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="确认密码 / Confirm Password" 
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
            立即注册
          </el-button>
          
          <div class="links">
            <span>已有账号？</span>
            <el-link type="primary" @click="router.push('/login')">去登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Postcard, Message, Key } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const registerRef = ref(null)
const loading = ref(false)
const isSending = ref(false)
const countdown = ref(0)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  code: '',       // 新增验证码字段
  password: '',
  confirmPassword: ''
})

// 密码一致性校验
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  // 新增验证码校验
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码通常为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 新增：发送验证码逻辑
const sendCode = () => {
  // 先简单校验一下是否填了邮箱
  if (!form.email) {
    return ElMessage.warning('请先输入邮箱地址')
  }
  // 简单的正则校验
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    return ElMessage.warning('邮箱格式不正确')
  }

  isSending.value = true
  
  // 模拟发送请求
  setTimeout(() => {
    ElMessage.success(`验证码已发送至 ${form.email} (测试码: 666666)`)
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        isSending.value = false
      }
    }, 1000)
    
  }, 500) // 模拟网络延迟
}

const handleRegister = async () => {
  if (!registerRef.value) return
  
  try {
    const valid = await registerRef.value.validate()
    if (!valid) return
    
    // 模拟验证码校验（实际项目中应该由后端验证）
    if (form.code !== '666666') {
      return ElMessage.error('验证码错误 (请使用测试码: 666666)')
    }

    loading.value = true
    
    // 准备注册数据（不包含 confirmPassword 和 code）
    const registerData = {
      username: form.username,
      nickname: form.nickname,
      email: form.email,
      password: form.password
    }
    
    const res = await register(registerData)
    
    if (res && res.success) {
      ElMessage.success(res.message || '注册成功！请登录')
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/login')
      }, 1000)
    } else {
      ElMessage.error(res.message || '注册失败，请稍后重试')
    }
  } catch (error) {
    console.error('注册错误:', error)
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
}

.register-card {
  width: 900px;
  /* 表单项变多了，高度需要增加，或者改为 auto 让内容撑开 */
  height: 640px; 
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  overflow: hidden;
}

@media (max-width: 768px) {
  .register-card {
    width: 90%;
    max-width: 400px;
    height: auto;
    flex-direction: column;
  }
  .register-image { display: none; }
  .register-form { padding: 40px 30px; }
}

.register-image {
  flex: 1.1;
  background: linear-gradient(to bottom, rgba(38, 166, 154, 0.4), rgba(0, 105, 92, 0.8)), url('https://images.unsplash.com/photo-1517048676732-d65bc937f952?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80');
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
.welcome-text p { margin: 10px 0 0; font-size: 18px; font-weight: 500; }
.sub-text { font-size: 14px !important; opacity: 0.8; margin-top: 5px !important; }

.register-form {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 20px;
  text-align: center;
}

.platform-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 10px;
}

.form-header h3 { font-size: 24px; color: #333; margin: 0 0 5px 0; }
.subtitle { color: #999; font-size: 13px; }

.submit-btn { width: 100%; font-weight: bold; letter-spacing: 1px; padding: 20px 0; margin-top: 10px;}

.links {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
  display: flex;
  justify-content: center;
  gap: 5px;
}
</style>