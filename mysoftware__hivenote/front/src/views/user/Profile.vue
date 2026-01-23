<template>
  <div class="page-container">
    <NavBar />
    
    <div class="main-content">
      <!-- 个人信息卡片 -->
      <el-card class="user-card">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
            <div class="header-buttons">
              <el-button type="primary" @click="showEditDialog = true">编辑资料</el-button>
              <el-button type="warning" @click="showPasswordDialog = true">修改密码</el-button>
            </div>
          </div>
        </template>
        
        <div class="user-header">
          <el-avatar :size="80" :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          <div class="user-info">
            <h2>{{ userInfo.nickname || userInfo.username || '用户' }}</h2>
            <p class="role-badge">{{ userInfo.type === 1 ? '管理员' : '普通用户' }}</p>
            <p class="desc">
              学号: {{ userInfo.username || '-' }} | 
              邮箱: {{ userInfo.email || '未设置' }}
            </p>
          </div>
        </div>
      </el-card>
      
      <!-- 编辑资料对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px">
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="头像URL" prop="avatar">
            <el-input v-model="editForm.avatar" placeholder="请输入头像URL" />
            <div class="avatar-preview" v-if="editForm.avatar">
              <el-avatar :size="60" :src="editForm.avatar" />
            </div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存</el-button>
        </template>
      </el-dialog>
      
      <!-- 修改密码对话框 -->
      <el-dialog v-model="showPasswordDialog" title="修改密码" width="500px">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">确定修改</el-button>
        </template>
      </el-dialog>

      <!-- 我的资源列表 -->
      <div class="my-resources">
        <h3>我的上传 ({{ myUploads.length }})</h3>
        <el-table :data="myUploads" style="width: 100%" border v-loading="loading">
          <el-table-column prop="title" label="笔记标题" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="viewCount" label="浏览量" width="100" />
          <el-table-column prop="downloadCount" label="下载量" width="100" />
          <el-table-column prop="createTime" label="上传时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag type="success" v-if="scope.row.status === 1">已通过</el-tag>
              <el-tag type="warning" v-else-if="scope.row.status === 0">待审核</el-tag>
              <el-tag type="danger" v-else-if="scope.row.status === 2">已拒绝</el-tag>
              <el-tag type="info" v-else>未知</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" link @click="router.push(`/note/detail/${scope.row.id}`)">查看</el-button>
              <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="!loading && myUploads.length === 0" description="还没有上传任何笔记" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, updateUserProfile, changePassword } from '@/api/user'
import { getUserInfo as getStoredUserInfo, setUserInfo as setStoredUserInfo } from '@/utils/auth'
import request from '@/utils/request'

const router = useRouter()
const userInfo = ref({})
const myUploads = ref([])
const loading = ref(false)

// 编辑资料相关
const showEditDialog = ref(false)
const showPasswordDialog = ref(false)
const editFormRef = ref(null)
const updating = ref(false)
const editForm = ref({
  nickname: '',
  email: '',
  avatar: ''
})

const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 修改密码相关
const passwordFormRef = ref(null)
const changingPassword = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 获取用户信息
const loadUserInfo = async () => {
  try {
    // 先从本地获取
    const storedInfo = getStoredUserInfo()
    if (storedInfo) {
      userInfo.value = storedInfo
      // 初始化编辑表单
      editForm.value = {
        nickname: storedInfo.nickname || '',
        email: storedInfo.email || '',
        avatar: storedInfo.avatar || ''
      }
    }
    
    // 再从服务器获取最新信息
    const res = await getUserInfo()
    if (res && res.success) {
      userInfo.value = res.result
      // 更新编辑表单
      editForm.value = {
        nickname: res.result.nickname || '',
        email: res.result.email || '',
        avatar: res.result.avatar || ''
      }
      // 更新本地存储
      setStoredUserInfo(res.result)
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
  }
}

// 获取我的上传列表
const loadMyUploads = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/note/myList',
      method: 'get'
    })
    
    if (res && res.success) {
      myUploads.value = res.result || []
      
      // 格式化时间
      myUploads.value.forEach(item => {
        if (item.createTime) {
          item.createTime = formatDate(item.createTime)
        }
      })
    }
  } catch (error) {
    console.error('获取上传列表错误:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 删除笔记
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request({
      url: `/note/delete/${row.id}`,
      method: 'post'
    })
    
    if (res && res.success) {
      ElMessage.success('删除成功')
      loadMyUploads() // 刷新列表
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除错误:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 更新个人资料
const handleUpdateProfile = async () => {
  if (!editFormRef.value) return
  
  try {
    const valid = await editFormRef.value.validate()
    if (!valid) return
    
    updating.value = true
    
    const res = await updateUserProfile(editForm.value)
    
    if (res && res.success) {
      ElMessage.success(res.message || '资料更新成功')
      showEditDialog.value = false
      // 重新加载用户信息
      await loadUserInfo()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新资料错误:', error)
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    updating.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return
    
    changingPassword.value = true
    
    const res = await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    if (res && res.success) {
      ElMessage.success(res.message || '密码修改成功，请重新登录')
      
      // 关闭对话框
      showPasswordDialog.value = false
      
      // 清空表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      passwordFormRef.value.resetFields()
      
      // 延迟跳转到登录页
      setTimeout(() => {
        localStorage.clear()
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改密码错误:', error)
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadMyUploads()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
.user-card { margin-bottom: 30px; }
.user-header { display: flex; align-items: center; gap: 20px; }
.user-info h2 { margin: 0 0 10px 0; }
.role-badge { background: #E6A23C; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; display: inline-block; margin-bottom: 10px; }
.desc { color: #909399; font-size: 14px; margin: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-buttons { display: flex; gap: 10px; }
.avatar-preview { margin-top: 10px; }
.my-resources { background: #fff; padding: 20px; border-radius: 8px; }
.my-resources h3 { margin-top: 0; margin-bottom: 20px; border-left: 4px solid #409EFF; padding-left: 10px; }
</style>