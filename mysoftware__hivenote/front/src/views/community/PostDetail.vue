<template>
  <div class="page-container">
    <NavBar />
    <div class="main-content" v-loading="loading">
      <template v-if="post">
        <!-- 帖子主体 -->
        <el-card class="post-card">
          <div class="post-header">
            <el-avatar :size="48" :src="post.avatar || defaultAvatar">
              {{ (post.nickname || 'U').charAt(0) }}
            </el-avatar>
            <div class="post-header-info">
              <div class="post-author-name">{{ post.nickname || '匿名用户' }}</div>
              <div class="post-date">{{ formatFullDate(post.createTime) }}</div>
            </div>
            <div class="post-header-actions">
              <el-button v-if="canDelete" link type="danger" @click="handleDelete">删除帖子</el-button>
            </div>
          </div>

          <h1 class="post-title">{{ post.title }}</h1>
          <div class="post-content">{{ post.content }}</div>

          <!-- 图片 -->
          <div class="post-images" v-if="postImages.length > 0">
            <el-image
              v-for="(img, i) in postImages"
              :key="i"
              :src="img"
              fit="cover"
              class="post-img"
              :preview-src-list="postImages"
              :initial-index="i"
            />
          </div>

          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }} 浏览</span>
            <span class="like-btn" :class="{ liked: hasLiked }" @click="handleLike">
              👍 {{ post.likeCount || 0 }} 点赞
            </span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }} 回复</span>
          </div>
        </el-card>

        <!-- 回复区域 -->
        <el-card class="reply-card">
          <div class="reply-header">
            <h3>全部回复 ({{ replies.length }})</h3>
          </div>

          <!-- 回复输入 -->
          <div class="reply-input-area">
            <el-input
              v-model="replyContent"
              type="textarea"
              placeholder="写下你的回复..."
              :autosize="{ minRows: 2, maxRows: 6 }"
              maxlength="1000"
            />
            <el-button type="primary" @click="handleReply" :loading="replying" style="margin-top: 10px">
              发表回复
            </el-button>
          </div>

          <!-- 回复列表 -->
          <div class="reply-list">
            <el-empty v-if="replies.length === 0" description="暂无回复，快来抢沙发！" :image-size="60" />
            <div v-for="reply in replies" :key="reply.id" class="reply-item" :class="{ 'admin-reply': reply.userType === 1 }">
              <el-avatar :size="36" :src="reply.avatar || defaultAvatar" :style="{ background: reply.userType === 1 ? '#7c3aed' : '#409EFF' }">
                {{ (reply.nickname || 'U').charAt(0) }}
              </el-avatar>
              <div class="reply-body">
                <div class="reply-meta">
                  <span class="reply-author" :class="{ 'admin-author': reply.userType === 1 }">
                    {{ reply.nickname || '匿名' }}
                  </span>
                  <el-tag v-if="reply.userType === 1" size="small" style="background:linear-gradient(135deg,#7c3aed,#a855f7);border:none;color:#fff;font-size:9px;margin-left:4px" effect="dark">管理员</el-tag>
                  <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                  <el-button
                    v-if="canDeleteReply(reply)"
                    link type="danger" size="small"
                    style="margin-left: auto"
                    @click="handleDeleteReply(reply)"
                  >删除</el-button>
                </div>
                <div class="reply-text" :class="{ 'admin-text': reply.userType === 1 }">{{ reply.content }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </template>

      <el-empty v-if="!loading && !post" description="帖子不存在或已被删除">
        <el-button type="primary" @click="$router.push('/community')">返回社区</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { View, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, likePost, deletePost, getPostReplies, addPostReply, deletePostReply } from '@/api/post'
import { getUserInfo as getStoredUserInfo } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const loading = ref(true)
const post = ref(null)
const replies = ref([])
const replyContent = ref('')
const replying = ref(false)
const hasLiked = ref(false)

const currentUser = computed(() => getStoredUserInfo() || {})

const postImages = computed(() => {
  if (!post.value?.images) return []
  return post.value.images.split(',').filter(s => s.trim())
})

const canDelete = computed(() => {
  if (!currentUser.value?.id || !post.value) return false
  const isAuthor = String(post.value.userId) === String(currentUser.value.id)
  const isAdmin = currentUser.value.type === 1
  return isAuthor || isAdmin
})

const canDeleteReply = (reply) => {
  if (!currentUser.value?.id) return false
  const isAuthor = String(reply.userId) === String(currentUser.value.id)
  const isAdmin = currentUser.value.type === 1
  return isAuthor || isAdmin
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}

const formatFullDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadPost = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(route.params.id)
    if (res && res.success) {
      post.value = res.result
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadReplies = async () => {
  try {
    const res = await getPostReplies(route.params.id)
    if (res && res.success) {
      replies.value = res.result || []
    }
  } catch (e) { /* silent */ }
}

const handleLike = async () => {
  if (hasLiked.value) return
  try {
    const res = await likePost(post.value.id)
    if (res && res.success) {
      post.value.likeCount = res.result
      hasLiked.value = true
    }
  } catch (e) {
    ElMessage.error('请先登录')
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', { type: 'warning' })
    const res = await deletePost(post.value.id)
    if (res && res.success) {
      ElMessage.success('删除成功')
      router.push('/community')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  replying.value = true
  try {
    const res = await addPostReply({
      postId: post.value.id,
      content: replyContent.value
    })
    if (res && res.success) {
      ElMessage.success('回复成功')
      replyContent.value = ''
      post.value.replyCount = (post.value.replyCount || 0) + 1
      await loadReplies()
    }
  } catch (e) {
    ElMessage.error('回复失败，请先登录')
  } finally {
    replying.value = false
  }
}

const handleDeleteReply = async (reply) => {
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '提示', { type: 'warning' })
    const res = await deletePostReply(reply.id)
    if (res && res.success) {
      ElMessage.success('删除成功')
      post.value.replyCount = Math.max(0, (post.value.replyCount || 0) - 1)
      await loadReplies()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadPost()
  loadReplies()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f0f2f5; }
.main-content { max-width: 800px; margin: 30px auto; padding: 0 20px 40px; }

.post-card { border-radius: 12px; margin-bottom: 20px; }
.post-header { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
.post-header-info { flex: 1; }
.post-author-name { font-weight: 600; font-size: 15px; color: #303133; }
.post-date { font-size: 12px; color: #c0c4cc; margin-top: 2px; }
.post-header-actions { flex-shrink: 0; }

.post-title { font-size: 22px; font-weight: 700; color: #1d2129; margin: 0 0 16px; line-height: 1.4; }
.post-content { font-size: 15px; color: #4e5969; line-height: 1.8; white-space: pre-wrap; word-break: break-all; margin-bottom: 20px; }

.post-images { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.post-img { width: 200px; height: 200px; border-radius: 8px; cursor: pointer; object-fit: cover; }

.post-stats {
  display: flex; gap: 24px; padding-top: 16px;
  border-top: 1px solid #f2f3f5; font-size: 14px; color: #86909c;
}
.like-btn { cursor: pointer; transition: color 0.2s; user-select: none; }
.like-btn:hover { color: #409EFF; }
.like-btn.liked { color: #409EFF; font-weight: 600; }

/* Reply Card */
.reply-card { border-radius: 12px; }
.reply-header h3 { margin: 0 0 16px; font-size: 17px; color: #1d2129; }

.reply-input-area { margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid #f2f3f5; }

.reply-list { display: flex; flex-direction: column; }
.reply-item {
  display: flex; gap: 12px; padding: 16px 0;
  border-bottom: 1px solid #f7f8fa;
}
.reply-item:last-child { border-bottom: none; }
.reply-body { flex: 1; min-width: 0; }
.reply-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; font-size: 13px; }
.reply-author { font-weight: 600; color: #303133; }
.reply-time { color: #c0c4cc; font-size: 12px; }
.reply-text { font-size: 14px; color: #4e5969; line-height: 1.6; word-break: break-all; }

/* Admin styling */
.admin-reply { background: linear-gradient(135deg, #f5f0ff, #ede9fe); border-radius: 10px; padding: 16px 12px; }
.admin-author { color: #7c3aed; }
.admin-text { color: #4c1d95; }
</style>
