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
          <div class="avatar-upload-wrapper" @click="triggerAvatarUpload">
            <el-avatar :size="80" :src="userInfo.avatar || defaultAvatar" />
            <div class="avatar-overlay">
              <el-icon :size="20"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              style="display: none"
              @change="handleAvatarChange"
            />
          </div>
          <div class="user-info">
            <h2>{{ userInfo.nickname || userInfo.username || '用户' }}</h2>
            <p class="role-badge">{{ userInfo.type === 1 ? '管理员' : '普通用户' }}</p>
            <p class="desc">
              <template v-if="userInfo.showStudentId !== 0">学号: {{ userInfo.username || '-' }} | </template>
              <template v-if="userInfo.showEmail !== 0">邮箱: {{ userInfo.email || '未设置' }} | </template>
              学校: {{ userInfo.school || '未设置' }}
            </p>
            <p v-if="avatarUploading" class="avatar-tip">头像上传中...</p>
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
              <el-option v-for="s in schoolOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
          <el-form-item label="头像">
            <div class="dialog-avatar-upload">
              <div class="dialog-avatar-preview">
                <el-avatar :size="80" :src="editForm.avatar || defaultAvatar" />
              </div>
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif,image/webp"
                @change="handleDialogAvatarChange"
              >
                <el-button size="small" type="primary">选择图片</el-button>
              </el-upload>
              <p class="upload-hint">支持 JPG/PNG/GIF/WEBP，不超过 5MB</p>
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

      <!-- Tabs: 我的上传 / 我的收藏 / 我的帖子 -->
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
                  <el-tag type="info" v-else-if="scope.row.status === 3">草稿</el-tag>
                  <el-tag type="info" v-else>未知</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260">
                <template #default="scope">
                  <el-button v-if="scope.row.status === 1" type="primary" link @click="router.push(`/note/detail/${scope.row.id}`)">查看</el-button>
                  <el-button v-if="[2,3].includes(scope.row.status)" type="primary" link @click="router.push(`/note/upload?id=${scope.row.id}`)">
                    {{ scope.row.status === 2 ? '修改重提' : '编辑草稿' }}
                  </el-button>
                  <el-button type="info" link @click="showReviewHistory(scope.row.id)">审核记录</el-button>
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

        <el-tab-pane label="我的帖子" name="posts">
          <div class="tab-content">
            <div class="tab-header">
              <h3>我的帖子 ({{ myPosts.length }})</h3>
              <el-button type="primary" size="small" @click="router.push('/community/post/create')">发布新帖</el-button>
            </div>
            <el-table :data="myPosts" style="width: 100%" border v-loading="loadingPosts">
              <el-table-column prop="title" label="帖子标题" min-width="180" show-overflow-tooltip />
              <el-table-column label="浏览" width="80" align="center">
                <template #default="scope">{{ scope.row.viewCount || 0 }}</template>
              </el-table-column>
              <el-table-column label="点赞" width="80" align="center">
                <template #default="scope">{{ scope.row.likeCount || 0 }}</template>
              </el-table-column>
              <el-table-column label="回复" width="80" align="center">
                <template #default="scope">{{ scope.row.replyCount || 0 }}</template>
              </el-table-column>
              <el-table-column label="发布时间" width="120">
                <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template #default="scope">
                  <el-tag type="success" v-if="scope.row.status === 1">正常</el-tag>
                  <el-tag type="info" v-else>已删除</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center">
                <template #default="scope">
                  <el-button type="primary" link @click="router.push(`/community/post/${scope.row.id}`)">查看</el-button>
                  <el-button type="warning" link @click="router.push(`/community/post/edit/${scope.row.id}`)" v-if="scope.row.status === 1">编辑</el-button>
                  <el-button type="danger" link @click="handleDeletePost(scope.row)" v-if="scope.row.status === 1">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loadingPosts && myPosts.length === 0" description="还没有发布任何帖子" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的评论" name="comments">
          <div class="tab-content">
            <h3>我的评论 ({{ myComments.length }})</h3>
            <el-table :data="myComments" style="width: 100%" border v-loading="loadingComments">
              <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
              <el-table-column prop="noteId" label="笔记ID" width="180" show-overflow-tooltip />
              <el-table-column label="评论时间" width="150">
                <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button type="primary" link @click="router.push(`/note/detail/${scope.row.noteId}`)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loadingComments && myComments.length === 0" description="还没有发表任何评论" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="通知中心" name="notifications">
          <div class="tab-content">
            <div class="tab-header">
              <h3>通知中心 ({{ notifications.length }})</h3>
              <el-button size="small" @click="handleReadAll" :disabled="notifications.length === 0">全部已读</el-button>
            </div>
            <el-table :data="notifications" style="width: 100%" border v-loading="loadingNotifications">
              <el-table-column label="状态" width="80" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.isRead === 0" type="danger" size="small">未读</el-tag>
                  <el-tag v-else type="info" size="small">已读</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" width="160" show-overflow-tooltip />
              <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
              <el-table-column label="时间" width="150">
                <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button v-if="scope.row.isRead === 0" type="primary" link @click="handleRead(scope.row)">标记已读</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loadingNotifications && notifications.length === 0" description="暂无通知" />
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-dialog v-model="rejectDialogVisible" title="审核记录" width="620px">
        <el-timeline v-if="reviewHistory.length">
          <el-timeline-item
            v-for="item in reviewHistory"
            :key="item.id"
            :timestamp="formatDate(item.createTime)"
          >
            <div class="history-title">{{ getReviewTypeLabel(item.reviewType) }}</div>
            <div v-if="item.rejectReason" class="history-body">{{ item.rejectReason }}</div>
            <div v-if="item.aiSuggestion" class="history-body">{{ item.aiSuggestion }}</div>
            <div class="history-meta">处理人：{{ item.reviewer || '-' }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审核记录" />
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { getUserInfo, updateUserProfile, changePassword, uploadAvatar, setAvatar, getNotifications, markNotificationRead, markAllNotificationsRead } from '@/api/user'
import { getUserInfo as getStoredUserInfo, setUserInfo as setStoredUserInfo } from '@/utils/auth'
import { getMyFavorites } from '@/api/favorite'
import { getMyPosts, deletePost } from '@/api/post'
import { getMyComments } from '@/api/comment'
import { getSchoolList } from '@/api/school'
import { getNoteReviewLog } from '@/api/note'
import request from '@/utils/request'

const router = useRouter()
const userInfo = ref({})
const myUploads = ref([])
const myFavorites = ref([])
const loading = ref(false)
const loadingFavorites = ref(false)
const activeTab = ref('uploads')
const avatarInputRef = ref(null)
const avatarUploading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const myPosts = ref([])
const loadingPosts = ref(false)
const myComments = ref([])
const loadingComments = ref(false)
const notifications = ref([])
const loadingNotifications = ref(false)

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

// 拒绝原因相关
const rejectDialogVisible = ref(false)
const reviewHistory = ref([])

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

const loadMyPosts = async () => {
  loadingPosts.value = true
  try {
    const res = await getMyPosts()
    if (res && res.success) {
      myPosts.value = res.result || []
    }
  } catch (error) {
    console.error('获取帖子列表错误:', error)
  } finally {
    loadingPosts.value = false
  }
}

const loadMyComments = async () => {
  loadingComments.value = true
  try {
    const res = await getMyComments()
    if (res && res.success) {
      myComments.value = res.result || []
    }
  } catch (error) {
    console.error('获取评论列表错误:', error)
  } finally {
    loadingComments.value = false
  }
}

const loadNotifications = async () => {
  loadingNotifications.value = true
  try {
    const res = await getNotifications()
    if (res && res.success) {
      notifications.value = res.result || []
    }
  } catch (error) {
    console.error('获取通知列表错误:', error)
  } finally {
    loadingNotifications.value = false
  }
}

const handleRead = async (row) => {
  const res = await markNotificationRead(row.id)
  if (res && res.success) {
    row.isRead = 1
  }
}

const handleReadAll = async () => {
  const res = await markAllNotificationsRead()
  if (res && res.success) {
    notifications.value.forEach(item => { item.isRead = 1 })
    ElMessage.success('已全部标记为已读')
  }
}

const handleDeletePost = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const res = await deletePost(row.id)
    if (res && res.success) {
      ElMessage.success('删除成功')
      loadMyPosts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子错误:', error)
      ElMessage.error('删除失败')
    }
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
      if (res.result) {
        userInfo.value = res.result
        populateEditForm(res.result)
        setStoredUserInfo(res.result)
      } else {
        await loadUserInfo()
      }
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

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const doAvatarUpload = async (file) => {
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式的图片')
    return
  }

  avatarUploading.value = true
  try {
    const uploadRes = await uploadAvatar(file)
    if (uploadRes && uploadRes.success) {
      const avatarUrl = uploadRes.result.url
      const setRes = await setAvatar(avatarUrl)
      if (setRes && setRes.success) {
        ElMessage.success('头像更新成功')
        userInfo.value.avatar = avatarUrl
        editForm.value.avatar = avatarUrl
        const stored = getStoredUserInfo()
        if (stored) {
          stored.avatar = avatarUrl
          stored.avatarBanned = 0
          setStoredUserInfo(stored)
        }
      }
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
  }
}

const handleAvatarChange = (e) => {
  const file = e.target.files[0]
  doAvatarUpload(file)
  e.target.value = ''
}

const handleDialogAvatarChange = (uploadFile) => {
  doAvatarUpload(uploadFile.raw)
}

const getReviewTypeLabel = (type) => {
  const map = {
    SUBMIT: '提交审核',
    RESUBMIT: '重新提交',
    MANUAL_APPROVE: '审核通过',
    MANUAL_REJECT: '审核拒绝',
    AI_SUGGESTION: 'AI 建议'
  }
  return map[type] || type || '审核记录'
}

const showReviewHistory = async (noteId) => {
  try {
    const res = await getNoteReviewLog(noteId)
    if (res && res.success) {
      reviewHistory.value = res.result || []
      rejectDialogVisible.value = true
    } else {
      ElMessage.error('获取审核记录失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  }
}

onMounted(() => {
  loadUserInfo()
  loadMyUploads()
  loadMyFavorites()
  loadMyPosts()
  loadMyComments()
  loadNotifications()
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
.tab-header { display: flex; justify-content: space-between; align-items: center; }
.header-buttons { display: flex; gap: 10px; }
.avatar-tip { color: #409EFF; font-size: 12px; margin: 4px 0 0; }

.avatar-upload-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}
.avatar-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-upload-wrapper:hover .avatar-overlay { opacity: 1; }

.dialog-avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}
.dialog-avatar-preview {
  border: 2px dashed #dcdfe6;
  border-radius: 50%;
  padding: 2px;
}
.upload-hint { color: #909399; font-size: 12px; margin: 0; }

.profile-tabs { background: #fff; padding: 20px; border-radius: 8px; }
.tab-content h3 { margin-top: 0; margin-bottom: 16px; border-left: 4px solid #409EFF; padding-left: 10px; }
.history-title { font-weight: 600; color: #303133; }
.history-body { white-space: pre-wrap; margin-top: 6px; color: #606266; }
.history-meta { margin-top: 6px; font-size: 12px; color: #909399; }
</style>
