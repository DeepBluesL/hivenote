<template>
  <div class="navbar-container">
    <div class="content-wrapper">
      <!-- 左侧 Logo 区 (已修改为图片) -->
      <div class="logo-section" @click="$router.push('/home')">
        <!-- 确保图片 nav-text-logo.png 已经放入 src/assets/ 目录下 -->
        <img src="@/assets/nav-text-logo.png" alt="MUST Note Share" class="logo-img" />
      </div>

      <!-- 中间 菜单区 -->
      <div class="menu-section">
        <router-link to="/home" class="nav-item" active-class="active">资源大厅</router-link>
        <router-link to="/note/upload" class="nav-item" active-class="active">上传笔记</router-link>
        <router-link to="/user/profile" class="nav-item" active-class="active">个人中心</router-link>
      </div>

      <!-- 右侧 用户信息区 -->
      <div class="user-section">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" class="avatar">{{ username.charAt(0).toUpperCase() }}</el-avatar>
            <span class="username">{{ username }}</span>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
// 暂时写死，实际开发中可以从 localStorage 或 Pinia/Vuex 中获取
const username = ref('Admin')

// 处理下拉菜单点击
const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    ElMessage.success('退出成功')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
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

/* Logo 样式修改 */
.logo-section {
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: opacity 0.3s;
  height: 100%; /* 确保高度充满 */
}
.logo-section:hover { opacity: 0.8; }

/* 新增图片样式，限制高度，防止撑破导航栏 */
.logo-img {
  height: 36px; 
  width: auto; /* 宽度自适应 */
  display: block;
}

/* 菜单样式 */
.menu-section {
  display: flex;
  gap: 40px;
}
.nav-item {
  text-decoration: none;
  color: #606266;
  font-size: 16px;
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

/* 用户下拉区样式 */
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