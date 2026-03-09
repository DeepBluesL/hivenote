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

        <!-- 标签展示 -->
        <div class="tags-bar" v-if="note.tags">
          <el-tag
            v-for="tag in parseTags(note.tags)"
            :key="tag"
            size="small"
            effect="plain"
            round
            class="detail-tag"
          >{{ tag }}</el-tag>
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
            立即下载笔记
          </el-button>
          <el-button
            size="large"
            :type="isLiked ? 'danger' : 'default'"
            @click="handleToggleLike"
          >
            <el-icon><Pointer /></el-icon>
            {{ isLiked ? '已点赞' : '点赞' }} ({{ note.likeCount || 0 }})
          </el-button>
          <el-button
            size="large"
            :type="isFavorited ? 'warning' : 'default'"
            @click="handleToggleFavorite"
          >
            <el-icon><Star /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }} ({{ note.favoriteCount || 0 }})
          </el-button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <h3>用户评论 ({{ commentCount }})</h3>

        <!-- 发表评论 -->
        <div class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="写下你的看法..."
          />
          <div class="comment-btn-box">
            <el-button type="primary" @click="submitComment()">发表评论</el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧~" />

          <div v-for="item in comments" :key="item.id" class="comment-item" :class="{ 'admin-comment': item.userType === 1 }">
            <div class="avatar-wrapper">
              <el-avatar :size="40" :style="{ background: item.userType === 1 ? '#7c3aed' : '#409EFF' }">
                {{ (item.nickname || item.username || 'U').charAt(0) }}
              </el-avatar>
              <span v-if="item.userType === 1" class="admin-badge">管理</span>
            </div>
            <div class="comment-content">
              <div class="comment-user">
                <span :class="{ 'admin-name': item.userType === 1 }">{{ item.nickname || item.username }}</span>
                <el-tag v-if="item.userType === 1" size="small" class="admin-tag" effect="dark">管理员</el-tag>
                <span class="comment-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="comment-text" :class="{ 'admin-text': item.userType === 1 }">{{ item.content }}</div>
              <div class="comment-actions-bar">
                <el-button type="primary" link size="small" @click="startReply(item)">
                  <el-icon><ChatDotRound /></el-icon> 回复
                </el-button>
                <el-button v-if="canDeleteComment(item)" type="danger" link size="small" @click="handleDeleteComment(item.id)">
                  删除
                </el-button>
              </div>

              <!-- 回复输入框 -->
              <div v-if="replyTarget?.id === item.id" class="reply-input-box">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  :placeholder="'回复 ' + (item.nickname || item.username) + '...'"
                  size="small"
                />
                <div class="reply-btn-box">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button type="primary" size="small" @click="submitComment(item)">回复</el-button>
                </div>
              </div>

              <!-- 子回复列表 -->
              <div v-if="item.replies && item.replies.length > 0" class="replies-list">
                <div v-for="reply in item.replies" :key="reply.id" class="reply-item" :class="{ 'admin-reply': reply.userType === 1 }">
                  <div class="avatar-wrapper reply-avatar-wrapper">
                    <el-avatar :size="28" :style="{ background: reply.userType === 1 ? '#7c3aed' : '#67C23A' }">
                      {{ (reply.nickname || 'U').charAt(0) }}
                    </el-avatar>
                    <span v-if="reply.userType === 1" class="admin-badge small">管理</span>
                  </div>
                  <div class="reply-content">
                    <div class="reply-user">
                      <span :class="{ 'admin-name': reply.userType === 1 }">{{ reply.nickname || reply.username }}</span>
                      <el-tag v-if="reply.userType === 1" size="small" class="admin-tag" effect="dark">管理员</el-tag>
                      <span v-if="reply.replyTo" class="reply-to">回复 {{ reply.replyTo }}</span>
                      <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-text" :class="{ 'admin-text': reply.userType === 1 }">{{ reply.content }}</div>
                    <div class="comment-actions-bar">
                      <el-button type="primary" link size="small" @click="startReplyToReply(item, reply)">
                        <el-icon><ChatDotRound /></el-icon> 回复
                      </el-button>
                      <el-button v-if="canDeleteComment(reply)" type="danger" link size="small" @click="handleDeleteComment(reply.id)">
                        删除
                      </el-button>
                    </div>
                    <!-- 回复子回复的输入框 -->
                    <div v-if="replyTarget?.id === reply.id" class="reply-input-box">
                      <el-input
                        v-model="replyContent"
                        type="textarea"
                        :rows="2"
                        :placeholder="'回复 ' + (reply.nickname || reply.username) + '...'"
                        size="small"
                      />
                      <div class="reply-btn-box">
                        <el-button size="small" @click="cancelReply">取消</el-button>
                        <el-button type="primary" size="small" @click="submitComment(item, reply)">回复</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { User, Clock, View, Download, Document, Star, Pointer, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoteDetail, addDownloadCount } from '@/api/note'
import { getCommentList, addComment, deleteComment } from '@/api/comment'
import { toggleFavorite, checkFavorite } from '@/api/favorite'
import { toggleLike, checkLike } from '@/api/like'
import { getUserInfo as getStoredUserInfo } from '@/utils/auth'

const route = useRoute()
const loading = ref(true)
const note = ref({})
const comments = ref([])
const newComment = ref('')
const isLiked = ref(false)
const isFavorited = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  return tagsStr.split(',').map(t => t.trim()).filter(t => t)
}

const commentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(c => {
    if (c.replies) count += c.replies.length
  })
  return count
})

const isAdmin = computed(() => {
  const userInfo = getStoredUserInfo()
  return userInfo && userInfo.type === 1
})

const canDeleteComment = (comment) => {
  const userInfo = getStoredUserInfo()
  if (!userInfo) return false
  if (userInfo.type === 1) return true
  return userInfo.username === comment.username
}

const loadDetail = async () => {
  const id = route.params.id
  loading.value = true

  try {
    const res = await getNoteDetail(id)
    if (res && res.success) {
      note.value = res.result
      if (note.value.createTime) {
        note.value.createTime = formatDate(note.value.createTime)
      }
      await Promise.allSettled([
        loadComments(),
        loadInteractionStatus()
      ])
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

const loadInteractionStatus = async () => {
  const id = route.params.id
  try {
    const [likeRes, favRes] = await Promise.allSettled([
      checkLike(id),
      checkFavorite(id)
    ])
    if (likeRes.status === 'fulfilled' && likeRes.value?.success) {
      isLiked.value = likeRes.value.result.liked
    }
    if (favRes.status === 'fulfilled' && favRes.value?.success) {
      isFavorited.value = favRes.value.result.favorited
    }
  } catch (e) {
    // not logged in - ignore
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < 7 * day) return Math.floor(diff / day) + '天前'
  return date.toLocaleDateString('zh-CN')
}

const handleDownload = async () => {
  if (!note.value.fileUrl) {
    return ElMessage.error('文件地址不存在')
  }
  try {
    await addDownloadCount(note.value.id)
    const downloadUrl = note.value.fileUrl.replace('/view/', '/download/')
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = note.value.fileUrl.split('/').pop()
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('开始下载...')
    note.value.downloadCount = (note.value.downloadCount || 0) + 1
  } catch (error) {
    console.error('下载错误:', error)
    ElMessage.error('下载失败，请稍后重试')
  }
}

const handleToggleLike = async () => {
  try {
    const res = await toggleLike(route.params.id)
    if (res && res.success) {
      isLiked.value = res.result.liked
      const delta = res.result.liked ? 1 : -1
      note.value.likeCount = (note.value.likeCount || 0) + delta
      ElMessage.success(res.result.message)
    }
  } catch (error) {
    ElMessage.error('操作失败，请先登录')
  }
}

const handleToggleFavorite = async () => {
  try {
    const res = await toggleFavorite(route.params.id)
    if (res && res.success) {
      isFavorited.value = res.result.favorited
      const delta = res.result.favorited ? 1 : -1
      note.value.favoriteCount = (note.value.favoriteCount || 0) + delta
      ElMessage.success(res.result.message)
    }
  } catch (error) {
    ElMessage.error('操作失败，请先登录')
  }
}

const loadComments = async () => {
  try {
    const res = await getCommentList({ noteId: route.params.id })
    if (res && res.success) {
      comments.value = res.result || []
    }
  } catch (error) {
    console.error('获取评论列表错误:', error)
  }
}

const startReply = (comment) => {
  replyTarget.value = comment
  replyContent.value = ''
}

const startReplyToReply = (parentComment, reply) => {
  replyTarget.value = reply
  replyTarget.value._parentId = parentComment.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const submitComment = async (parentComment, replyToComment) => {
  const isReply = !!parentComment
  const content = isReply ? replyContent.value : newComment.value

  if (!content || !content.trim()) {
    return ElMessage.warning('请输入评论内容')
  }

  try {
    const data = {
      noteId: route.params.id,
      content: content
    }

    if (isReply) {
      data.parentId = parentComment.id
      if (replyToComment) {
        data.replyTo = replyToComment.nickname || replyToComment.username
      } else {
        data.replyTo = parentComment.nickname || parentComment.username
      }
    }

    const res = await addComment(data)

    if (res && res.success) {
      ElMessage.success(isReply ? '回复成功' : '评论成功')
      if (isReply) {
        replyContent.value = ''
        replyTarget.value = null
      } else {
        newComment.value = ''
      }
      await loadComments()
    } else {
      ElMessage.error(res.message || '评论失败')
    }
  } catch (error) {
    console.error('发表评论错误:', error)
    ElMessage.error('评论失败，请稍后重试')
  }
}

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

onMounted(() => loadDetail())
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 900px; margin: 30px auto; padding: 0 20px; }

.note-detail-card { background: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-section { margin-bottom: 20px; text-align: center; }
.note-title { margin: 15px 0 0; font-size: 24px; color: #303133; }

.tags-bar { display: flex; justify-content: center; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.detail-tag { font-size: 12px; }
.meta-bar { display: flex; justify-content: center; gap: 30px; margin-bottom: 40px; color: #909399; font-size: 14px; }
.meta-item { display: flex; align-items: center; gap: 5px; }

.content-section { margin-bottom: 40px; padding: 20px; background: #fafafa; border-radius: 4px; }
.content-section h3 { margin-top: 0; display: flex; align-items: center; gap: 8px; font-size: 16px; }
.description { line-height: 1.8; color: #606266; font-size: 15px; }

.action-section { text-align: center; margin-bottom: 20px; display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; }

.comment-section { margin-top: 30px; background: #fff; padding: 30px; border-radius: 8px; }
.comment-input { margin-bottom: 30px; }
.comment-btn-box { margin-top: 10px; text-align: right; }

/* 评论项 */
.comment-item { display: flex; gap: 15px; margin-bottom: 20px; border-bottom: 1px solid #f0f0f0; padding-bottom: 20px; }
.comment-content { flex: 1; min-width: 0; }
.comment-user { font-weight: bold; font-size: 14px; margin-bottom: 5px; display: flex; align-items: center; gap: 6px; }
.comment-time { font-weight: normal; color: #999; font-size: 12px; margin-left: 6px; }
.comment-text { color: #606266; line-height: 1.5; margin-bottom: 6px; }
.comment-actions-bar { display: flex; gap: 8px; }

/* 管理员头像和徽章 */
.avatar-wrapper { position: relative; flex-shrink: 0; }
.admin-badge {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  color: #fff;
  font-size: 9px;
  padding: 1px 4px;
  border-radius: 6px;
  white-space: nowrap;
  line-height: 1.2;
  font-weight: 600;
}
.admin-badge.small {
  font-size: 8px;
  padding: 0px 3px;
  bottom: -3px;
}

/* 管理员评论样式 */
.admin-comment {
  background: linear-gradient(135deg, #f5f0ff, #ede9fe);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #ddd6fe;
  border-bottom: 1px solid #ddd6fe !important;
}
.admin-name { color: #7c3aed; font-weight: 700; }
.admin-tag {
  background: linear-gradient(135deg, #7c3aed, #a855f7) !important;
  border: none !important;
  color: #fff !important;
  font-size: 10px;
}
.admin-text { color: #4c1d95; }

/* 管理员回复样式 */
.admin-reply {
  background: linear-gradient(135deg, #f5f0ff, #ede9fe);
  border-radius: 8px;
  padding: 10px;
  margin: 4px 0;
}
.reply-avatar-wrapper { position: relative; flex-shrink: 0; }

.reply-input-box { margin-top: 10px; padding: 10px; background: #f9f9f9; border-radius: 6px; }
.reply-btn-box { margin-top: 8px; text-align: right; display: flex; justify-content: flex-end; gap: 8px; }

.replies-list { margin-top: 12px; padding-left: 8px; border-left: 2px solid #e8e8e8; }
.reply-item { display: flex; gap: 10px; padding: 10px 0; }
.reply-item:not(:last-child) { border-bottom: 1px solid #f5f5f5; }
.reply-content { flex: 1; min-width: 0; }
.reply-user { font-size: 13px; font-weight: 500; margin-bottom: 4px; display: flex; align-items: center; gap: 4px; }
.reply-to { color: #409EFF; font-weight: normal; margin: 0 4px; }
.reply-text { font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 4px; }
</style>
