<template>
  <div class="navbar-container">
    <div class="content-wrapper">
      <!-- 左侧 Logo 区 -->
      <div class="logo-section" @click="$router.push('/home')">
        <img src="@/assets/nav-text-logo.png" alt="MUST Note Share" class="logo-img" />
      </div>

      <!-- 中间 菜单区 -->
      <div class="menu-section">
        <router-link to="/home" class="nav-item" active-class="active">资源大厅</router-link>
        <router-link to="/community" class="nav-item" active-class="active">学习社区</router-link>
        <router-link to="/note/upload" class="nav-item" active-class="active">上传笔记</router-link>
        <router-link to="/user/profile" class="nav-item" active-class="active">个人中心</router-link>
      </div>

      <!-- 右侧 用户信息区 -->
      <div class="user-section">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" class="avatar" :src="userAvatar">{{ displayName.charAt(0).toUpperCase() }}</el-avatar>
            <span class="username">{{ displayName }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">我的资料</el-dropdown-item>
              <el-dropdown-item divided command="logout" style="color: #F56C6C;">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserInfo as getStoredUserInfo } from '@/utils/auth'

const router = useRouter()
const userInfo = ref(null)

const displayName = computed(() => {
  if (userInfo.value) {
    return userInfo.value.nickname || userInfo.value.username || '用户'
  }
  return '用户'
})

const userAvatar = computed(() => {
  return userInfo.value?.avatar || ''
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('退出成功')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}

onMounted(() => {
  userInfo.value = getStoredUserInfo()
})
</script>

<style scoped>
.navbar-container {
  height: 64px;
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo-section {
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: opacity 0.3s;
  height: 100%;
}
.logo-section:hover { opacity: 0.8; }

.logo-img {
  height: 36px;
  width: auto;
  display: block;
}

.menu-section {
  display: flex;
  gap: 32px;
}
.nav-item {
  text-decoration: none;
  color: #606266;
  font-size: 15px;
  font-weight: 500;
  position: relative;
  padding: 21px 0;
  transition: color 0.3s;
}
.nav-item:hover { color: #409EFF; }
.nav-item.active {
  color: #409EFF;
  font-weight: 600;
}
.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #409EFF;
  border-radius: 2px 2px 0 0;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background 0.3s;
}
.user-dropdown:hover { background-color: #f5f7fa; }
.avatar { background-color: #409EFF; color: white; font-weight: bold; }
.username { margin: 0 8px; font-size: 14px; color: #333; }
</style>
