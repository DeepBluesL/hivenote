<template>
  <div class="page-container">
    <NavBar />

    <div class="main-content" v-loading="loading">
      <div class="note-detail-card">
        <!-- 标题区 -->
        <div class="header-section">
          <el-tag size="large" effect="dark" class="category-tag">{{ note.category || '未分类' }}</el-tag>
          <h1 class="note-title">{{ note.title }}</h1>
        </div>

        <!-- 信息栏 -->
        <div class="meta-bar">
          <span class="meta-item"><el-icon><User /></el-icon> 上传者: {{ note.createBy || '匿名' }}</span>
          <span class="meta-item"><el-icon><Clock /></el-icon> 时间: {{ note.createTime }}</span>
          <span class="meta-item"><el-icon><View /></el-icon> 浏览: {{ note.viewCount }}</span>
          <span class="meta-item"><el-icon><Download /></el-icon> 下载: {{ note.downloadCount }}</span>
        </div>

        <!-- 简介区 -->
        <div class="content-section">
          <h3><el-icon><Document /></el-icon> 笔记简介</h3>
          <p class="description">{{ note.summary || '暂无简介' }}</p>
        </div>

        <!-- 操作区 -->
        <div class="action-section">
          <el-button type="primary" size="large" icon="Download" @click="handleDownload">
            立即下载笔记 ({{ note.price || 0 }} 积分)
          </el-button>
          <el-button size="large" icon="Star" @click="handleLike">收藏</el-button>
        </div>
      </div>

      <!-- 评论区 (对应 Interaction 需求) -->
      <div class="comment-section">
        <h3>用户评论 ({{ comments.length }})</h3>
        
        <!-- 发表评论 -->
        <div class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="写下你的看法..."
          />
          <div class="comment-btn-box">
            <el-button type="primary" @click="submitComment">发表评论</el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧~" />
          
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <el-avatar :size="40">{{ (item.nickname || item.username || 'U').charAt(0) }}</el-avatar>
            <div class="comment-content">
              <div class="comment-user">
                {{ item.nickname || item.username }} 
                <span class="comment-time">{{ item.time }}</span>
              </div>
              <div class="comment-text">{{ item.content }}</div>
              <div class="comment-actions" v-if="isMyComment(item)">
                <el-button type="danger" link size="small" @click="handleDeleteComment(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { User, Clock, View, Download, Document, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoteDetail, addDownloadCount } from '@/api/note'
import { getCommentList, addComment, deleteComment } from '@/api/comment'
import { getUserInfo as getStoredUserInfo } from '@/utils/auth'

const route = useRoute()
const loading = ref(true)
const note = ref({})
const comments = ref([])
const newComment = ref('')

// 获取详情
const loadDetail = async () => {
  const id = route.params.id // 从网址获取 ID
  loading.value = true
  
  try {
    console.log('获取笔记详情, ID:', id)
    const res = await getNoteDetail(id)
    
    if (res && res.success) {
      console.log('笔记详情:', res.result)
      note.value = res.result
      
      // 格式化时间
      if (note.value.createTime) {
        note.value.createTime = formatDate(note.value.createTime)
      }
      
      // 加载评论列表
      await loadComments()
    } else {
      ElMessage.error(res.message || '获取笔记详情失败')
    }
  } catch (error) {
    console.error('获取笔记详情错误:', error)
    ElMessage.error('获取笔记详情失败')
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

const handleDownload = async () => {
  if (!note.value.fileUrl) {
    return ElMessage.error('文件地址不存在')
  }
  
  try {
    // 调用增加下载次数接口
    await addDownloadCount(note.value.id)
    
    // 从 fileUrl 中提取文件名
    // fileUrl 格式: http://localhost:8080/zwz/file/view/20251024_uuid.pdf
    const fileName = note.value.fileUrl.split('/').pop()
    
    // 构造下载 URL（使用 download 接口而不是 view）
    const downloadUrl = note.value.fileUrl.replace('/view/', '/download/')
    
    console.log('下载文件:', fileName)
    console.log('下载URL:', downloadUrl)
    
    // 创建隐藏的 a 标签触发下载
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('开始下载...')
    
    // 更新本地下载次数显示
    note.value.downloadCount = (note.value.downloadCount || 0) + 1
  } catch (error) {
    console.error('下载错误:', error)
    ElMessage.error('下载失败，请稍后重试')
  }
}

const handleLike = () => {
  ElMessage.success('收藏成功')
}

// 加载评论列表
const loadComments = async () => {
  try {
    const res = await getCommentList({ noteId: route.params.id })
    
    if (res && res.success) {
      comments.value = res.result || []
      
      // 格式化时间
      comments.value.forEach(item => {
        if (item.createTime) {
          item.time = formatTime(item.createTime)
        }
      })
    }
  } catch (error) {
    console.error('获取评论列表错误:', error)
  }
}

// 格式化时间（相对时间）
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < 7 * day) {
    return Math.floor(diff / day) + '天前'
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 发表评论
const submitComment = async () => {
  if (!newComment.value || !newComment.value.trim()) {
    return ElMessage.warning('请输入评论内容')
  }
  
  try {
    const res = await addComment({
      noteId: route.params.id,
      content: newComment.value
    })
    
    if (res && res.success) {
      ElMessage.success('评论成功')
      newComment.value = ''
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error(res.message || '评论失败')
    }
  } catch (error) {
    console.error('发表评论错误:', error)
    ElMessage.error('评论失败，请稍后重试')
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteComment(commentId)
    
    if (res && res.success) {
      ElMessage.success('删除成功')
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论错误:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 判断是否是我的评论
const isMyComment = (comment) => {
  const userInfo = getStoredUserInfo()
  return userInfo && userInfo.username === comment.username
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 900px; margin: 30px auto; padding: 0 20px; }

.note-detail-card { background: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-section { margin-bottom: 20px; text-align: center; }
.note-title { margin: 15px 0 0; font-size: 24px; color: #303133; }

.meta-bar { display: flex; justify-content: center; gap: 30px; margin-bottom: 40px; color: #909399; font-size: 14px; }
.meta-item { display: flex; align-items: center; gap: 5px; }

.content-section { margin-bottom: 40px; padding: 20px; background: #fafafa; border-radius: 4px; }
.content-section h3 { margin-top: 0; display: flex; align-items: center; gap: 8px; font-size: 16px; }
.description { line-height: 1.8; color: #606266; font-size: 15px; }

.action-section { text-align: center; margin-bottom: 20px; }

.comment-section { margin-top: 30px; background: #fff; padding: 30px; border-radius: 8px; }
.comment-input { margin-bottom: 30px; }
.comment-btn-box { margin-top: 10px; text-align: right; }

.comment-item { display: flex; gap: 15px; margin-bottom: 20px; border-bottom: 1px solid #f0f0f0; padding-bottom: 20px; }
.comment-user { font-weight: bold; font-size: 14px; margin-bottom: 5px; }
.comment-time { font-weight: normal; color: #999; font-size: 12px; margin-left: 10px; }
.comment-text { color: #606266; line-height: 1.5; }
.comment-actions { margin-top: 5px; }
</style>