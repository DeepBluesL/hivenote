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
              <template v-if="userInfo.showStudentId !== 0">学号: {{ userInfo.username || '-' }} | </template>
              <template v-if="userInfo.showEmail !== 0">邮箱: {{ userInfo.email || '未设置' }} | </template>
              学校: {{ userInfo.school || '未设置' }}
            </p>
          </div>
        </div>
      </el-card>

      <!-- 编辑资料对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑个人资料" width="550px">
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="所属学校">
            <el-select
              v-model="editForm.school"
              placeholder="搜索并选择学校"
              style="width: 100%"
              filterable
              remote
              :remote-method="handleSchoolSearch"
              clearable
              :loading="schoolLoading"
            >
              <el-option
                v-for="s in schoolOptions"
                :key="s"
                :label="s"
                :value="s"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="头像URL" prop="avatar">
            <el-input v-model="editForm.avatar" placeholder="请输入头像URL" />
            <div class="avatar-preview" v-if="editForm.avatar">
              <el-avatar :size="60" :src="editForm.avatar" />
            </div>
          </el-form-item>
          <el-divider content-position="left">隐私设置</el-divider>
          <el-form-item label="公开学号">
            <el-switch v-model="editForm.showStudentId" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="隐藏" />
          </el-form-item>
          <el-form-item label="公开邮箱">
            <el-switch v-model="editForm.showEmail" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="隐藏" />
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

      <!-- Tabs: 我的上传 / 我的收藏 -->
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="我的上传" name="uploads">
          <div class="tab-content">
            <h3>我的上传 ({{ myUploads.length }})</h3>
            <el-table :data="myUploads" style="width: 100%" border v-loading="loading">
              <el-table-column prop="title" label="笔记标题" />
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="viewCount" label="浏览" width="80" />
              <el-table-column prop="downloadCount" label="下载" width="80" />
              <el-table-column prop="likeCount" label="点赞" width="80" />
              <el-table-column prop="createTime" label="上传时间" width="120" />
              <el-table-column prop="status" label="状态" width="90">
                <template #default="scope">
                  <el-tag type="success" v-if="scope.row.status === 1">已通过</el-tag>
                  <el-tag type="warning" v-else-if="scope.row.status === 0">待审核</el-tag>
                  <el-tag type="danger" v-else-if="scope.row.status === 2">已拒绝</el-tag>
                  <el-tag type="info" v-else>未知</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="scope">
                  <el-button type="primary" link @click="router.push(`/note/detail/${scope.row.id}`)">查看</el-button>
                  <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && myUploads.length === 0" description="还没有上传任何笔记" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="favorites">
          <div class="tab-content">
            <h3>我的收藏 ({{ myFavorites.length }})</h3>
            <el-table :data="myFavorites" style="width: 100%" border v-loading="loadingFavorites">
              <el-table-column prop="title" label="笔记标题" />
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="createBy" label="上传者" width="100" />
              <el-table-column prop="viewCount" label="浏览" width="80" />
              <el-table-column prop="likeCount" label="点赞" width="80" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="primary" link @click="router.push(`/note/detail/${scope.row.id}`)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loadingFavorites && myFavorites.length === 0" description="还没有收藏任何笔记" />
          </div>
        </el-tab-pane>
      </el-tabs>
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
import { getMyFavorites } from '@/api/favorite'
import { getSchoolList } from '@/api/school'
import request from '@/utils/request'

const router = useRouter()
const userInfo = ref({})
const myUploads = ref([])
const myFavorites = ref([])
const loading = ref(false)
const loadingFavorites = ref(false)
const activeTab = ref('uploads')

const showEditDialog = ref(false)
const showPasswordDialog = ref(false)
const editFormRef = ref(null)
const updating = ref(false)
const schoolLoading = ref(false)
const schoolOptions = ref([])
const editForm = ref({
  nickname: '',
  email: '',
  avatar: '',
  school: '',
  showEmail: 1,
  showStudentId: 1
})

const handleSchoolSearch = async (query) => {
  if (!query) {
    schoolOptions.value = []
    return
  }
  schoolLoading.value = true
  try {
    const res = await getSchoolList(query)
    if (res && res.success) {
      schoolOptions.value = res.result || []
    }
  } catch (e) {
    console.error('获取学校列表错误:', e)
  } finally {
    schoolLoading.value = false
  }
}

const editRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const passwordFormRef = ref(null)
const changingPassword = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

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
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const populateEditForm = (info) => {
  editForm.value = {
    nickname: info.nickname || '',
    email: info.email || '',
    avatar: info.avatar || '',
    school: info.school || '',
    showEmail: info.showEmail != null ? info.showEmail : 1,
    showStudentId: info.showStudentId != null ? info.showStudentId : 1
  }
  if (info.school) {
    schoolOptions.value = [info.school]
  }
}

const loadUserInfo = async () => {
  try {
    const storedInfo = getStoredUserInfo()
    if (storedInfo) {
      userInfo.value = storedInfo
      populateEditForm(storedInfo)
    }
    const res = await getUserInfo()
    if (res && res.success) {
      userInfo.value = res.result
      populateEditForm(res.result)
      setStoredUserInfo(res.result)
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
  }
}

const loadMyUploads = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/note/myList', method: 'get' })
    if (res && res.success) {
      myUploads.value = res.result || []
      myUploads.value.forEach(item => {
        if (item.createTime) item.createTime = formatDate(item.createTime)
      })
    }
  } catch (error) {
    console.error('获取上传列表错误:', error)
  } finally {
    loading.value = false
  }
}

const loadMyFavorites = async () => {
  loadingFavorites.value = true
  try {
    const res = await getMyFavorites()
    if (res && res.success) {
      myFavorites.value = res.result || []
    }
  } catch (error) {
    console.error('获取收藏列表错误:', error)
  } finally {
    loadingFavorites.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const res = await request({ url: `/note/delete/${row.id}`, method: 'post' })
    if (res && res.success) {
      ElMessage.success('删除成功')
      loadMyUploads()
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
      showPasswordDialog.value = false
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      passwordFormRef.value.resetFields()
      setTimeout(() => { localStorage.clear(); router.push('/login') }, 1500)
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
  loadMyFavorites()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
.user-card { margin-bottom: 24px; }
.user-header { display: flex; align-items: center; gap: 20px; }
.user-info h2 { margin: 0 0 10px 0; }
.role-badge { background: #E6A23C; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; display: inline-block; margin-bottom: 10px; }
.desc { color: #909399; font-size: 14px; margin: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-buttons { display: flex; gap: 10px; }
.avatar-preview { margin-top: 10px; }

.profile-tabs { background: #fff; padding: 20px; border-radius: 8px; }
.tab-content h3 { margin-top: 0; margin-bottom: 16px; border-left: 4px solid #409EFF; padding-left: 10px; }
</style>
