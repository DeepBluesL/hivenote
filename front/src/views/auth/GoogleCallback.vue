<template>
  <div class="callback-container">
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { setToken, setUserInfo } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const message = ref('Google 登录中...')

onMounted(() => {
  const token = route.query.token
  const userInfoStr = route.query.userInfo
  const error = route.query.error

  if (error) {
    message.value = error
    ElMessage.error(error)
    setTimeout(() => router.replace('/login'), 2000)
    return
  }

  if (token && userInfoStr) {
    try {
      const userInfo = JSON.parse(decodeURIComponent(userInfoStr))
      setToken(token)
      setUserInfo(userInfo)
      ElMessage.success('Google 登录成功！')
      router.replace('/home')
    } catch (e) {
      message.value = '登录信息解析失败'
      ElMessage.error('登录信息解析失败')
      setTimeout(() => router.replace('/login'), 2000)
    }
  } else {
    message.value = '登录失败，缺少参数'
    ElMessage.error('登录失败')
    setTimeout(() => router.replace('/login'), 2000)
  }
})
</script>

<style scoped>
.callback-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #666;
  background: linear-gradient(135deg, #f5f2ae 0%, #ccdef6 100%);
}
</style>
