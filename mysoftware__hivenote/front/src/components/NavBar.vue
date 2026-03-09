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
        <!-- 通知铃铛 -->
        <el-popover placement="bottom" :width="360" trigger="click" @show="loadNotifications">
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
              <el-icon :size="20" class="bell-icon"><Bell /></el-icon>
            </el-badge>
          </template>
          <div class="notification-panel">
            <div class="notification-header">
              <span class="notification-title">系统通知</span>
              <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="handleMarkAllRead">全部已读</el-button>
            </div>
            <div class="notification-list" v-if="notifications.length > 0">
              <div
                v-for="item in notifications"
                :key="item.id"
                class="notification-item"
                :class="{ unread: item.isRead === 0 }"
                @click="handleNotificationClick(item)"
              >
                <div class="notif-dot" v-if="item.isRead === 0"></div>
                <div class="notif-content">
                  <div class="notif-title">{{ item.title }}</div>
                  <div class="notif-text">{{ item.content }}</div>
                  <div class="notif-time">{{ formatNotifTime(item.createTime) }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无通知" :image-size="60" />
          </div>
        </el-popover>

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserInfo as getStoredUserInfo } from '@/utils/auth'
import { getNotifications, getUnreadNotificationCount, markNotificationRead, markAllNotificationsRead } from '@/api/user'

const router = useRouter()
const userInfo = ref(null)
const notifications = ref([])
const unreadCount = ref(0)
let pollTimer = null

const displayName = computed(() => {
  if (userInfo.value) {
    return userInfo.value.nickname || userInfo.value.username || '用户'
  }
  return '用户'
})

const userAvatar = computed(() => {
  return userInfo.value?.avatar || ''
})

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadNotificationCount()
    if (res && res.success) {
      unreadCount.value = res.result || 0
    }
  } catch (e) { /* silent */ }
}

const loadNotifications = async () => {
  try {
    const res = await getNotifications()
    if (res && res.success) {
      notifications.value = res.result || []
    }
  } catch (e) { /* silent */ }
}

const handleNotificationClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await markNotificationRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) { /* silent */ }
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (e) { /* silent */ }
}

const formatNotifTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}

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
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
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

.user-section {
  display: flex;
  align-items: center;
  gap: 4px;
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

.notification-badge { margin-right: 16px; cursor: pointer; }
.bell-icon { color: #606266; transition: color 0.3s; vertical-align: middle; }
.bell-icon:hover { color: #409EFF; }

.notification-panel { max-height: 400px; }
.notification-header {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 10px; border-bottom: 1px solid #ebeef5; margin-bottom: 8px;
}
.notification-title { font-weight: 600; font-size: 14px; }
.notification-list { max-height: 340px; overflow-y: auto; }
.notification-item {
  display: flex; align-items: flex-start; gap: 8px;
  padding: 10px 4px; border-bottom: 1px solid #f2f6fc; cursor: pointer;
  transition: background 0.2s;
}
.notification-item:hover { background: #f5f7fa; }
.notification-item.unread { background: #ecf5ff; }
.notif-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #409EFF; flex-shrink: 0; margin-top: 6px;
}
.notif-content { flex: 1; min-width: 0; }
.notif-title { font-weight: 600; font-size: 13px; color: #303133; margin-bottom: 4px; }
.notif-text { font-size: 12px; color: #606266; line-height: 1.5; word-break: break-all; }
.notif-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
</style>
