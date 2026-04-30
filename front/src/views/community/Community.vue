<template>
  <div class="page-container">
    <NavBar />

    <div class="main-content">
      <!-- 社区头部 Banner -->
      <div class="community-banner">
        <div class="banner-content">
          <h1>学习社区</h1>
          <p>分享知识，共同进步 — 发现优质笔记，参与讨论交流</p>
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-num">{{ stats.totalNotes || 0 }}</span>
              <span class="stat-label">笔记总数</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ stats.totalComments || 0 }}</span>
              <span class="stat-label">评论互动</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ totalViews }}</span>
              <span class="stat-label">总浏览量</span>
            </div>
          </div>
        </div>
      </div>

      <div class="community-body">
        <!-- 左侧主内容 -->
        <div class="main-column">
          <!-- 热门讨论帖子 -->
          <div class="section-card">
            <div class="section-header">
              <h2><el-icon><ChatDotRound /></el-icon> 热门讨论</h2>
              <router-link to="/community/posts" class="more-link">查看更多 →</router-link>
            </div>
            <div class="post-list" v-loading="loadingPosts">
              <el-empty v-if="hotPosts.length === 0 && !loadingPosts" description="暂无讨论帖子，快来发第一帖吧！" />
              <div v-for="(post, index) in hotPosts" :key="post.id" class="post-item" @click="goToPost(post.id)">
                <div class="post-left">
                  <el-avatar :size="40" :src="post.avatar || defaultAvatar">
                    {{ (post.nickname || 'U').charAt(0) }}
                  </el-avatar>
                </div>
                <div class="post-body">
                  <div class="post-title-row">
                    <span class="post-rank" :class="{ 'post-rank-hot': index < 3 }">{{ index + 1 }}</span>
                    <el-tag v-if="post.pinned === 1" type="danger" size="small">置顶</el-tag>
                    <span class="post-title">{{ post.title }}</span>
                  </div>
                  <div class="post-snippet">{{ post.content?.replace(/<[^>]+>/g, '').substring(0, 80) }}...</div>
                  <div class="post-meta">
                    <span class="post-author">{{ post.nickname || '匿名' }}</span>
                    <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
                    <span>👍 {{ post.likeCount || 0 }}</span>
                    <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }}</span>
                    <span class="post-time">{{ formatDate(post.createTime) }}</span>
                  </div>
                </div>
                <div class="post-images-thumb" v-if="post.images">
                  <el-image :src="post.images.split(',')[0]" fit="cover" class="thumb-img" />
                </div>
              </div>
            </div>
            <div class="post-action-bar">
              <el-button type="primary" @click="$router.push('/community/post/create')">
                <el-icon><EditPen /></el-icon> 发布帖子
              </el-button>
            </div>
          </div>

          <!-- 热门笔记 -->
          <div class="section-card">
            <div class="section-header">
              <h2><el-icon><TrendCharts /></el-icon> 热门笔记</h2>
              <router-link to="/home" class="more-link">查看更多 →</router-link>
            </div>
            <div class="hot-list" v-loading="loadingHot">
              <el-empty v-if="hotNotes.length === 0 && !loadingHot" description="暂无热门笔记" />
              <div v-for="(note, index) in hotNotes" :key="note.id" class="hot-item" @click="goToDetail(note.id)">
                <span class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                <div class="hot-info">
                  <div class="hot-title">{{ note.title }}</div>
                  <div class="hot-meta">
                    <el-tag size="small" :type="getCategoryType(note.category)">{{ note.category }}</el-tag>
                    <span><el-icon><View /></el-icon> {{ note.viewCount || 0 }}</span>
                    <span><el-icon><Download /></el-icon> {{ note.downloadCount || 0 }}</span>
                    <span>👍 {{ note.likeCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 最新笔记 -->
          <div class="section-card">
            <div class="section-header">
              <h2><el-icon><Clock /></el-icon> 最新发布</h2>
              <router-link to="/home" class="more-link">查看更多 →</router-link>
            </div>
            <div class="latest-grid" v-loading="loadingLatest">
              <el-empty v-if="latestNotes.length === 0 && !loadingLatest" description="暂无最新笔记" />
              <el-card
                v-for="note in latestNotes"
                :key="note.id"
                class="latest-card"
                shadow="hover"
                @click="goToDetail(note.id)"
              >
                <!-- 封面图片 -->
                <div v-if="note.coverUrl" class="latest-cover">
                  <img :src="note.coverUrl" :alt="note.title" class="latest-cover-img" />
                </div>

                <div class="latest-card-header">
                  <el-tag :type="getCategoryType(note.category)" effect="dark" size="small">{{ note.category }}</el-tag>
                  <span class="latest-time">{{ formatDate(note.createTime) }}</span>
                </div>
                <div class="latest-title">{{ note.title }}</div>
                <div class="latest-summary">{{ note.summary || '暂无简介...' }}</div>
                <div class="latest-footer">
                  <span class="latest-author">
                    <el-avatar :size="18" style="background:#409EFF">{{ (note.createBy || 'U').charAt(0) }}</el-avatar>
                    {{ note.createBy || '匿名' }}
                  </span>
                  <div class="latest-stats">
                    <span><el-icon><View /></el-icon> {{ note.viewCount || 0 }}</span>
                    <span>👍 {{ note.likeCount || 0 }}</span>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="side-column">
          <!-- 快捷操作 -->
          <div class="section-card">
            <h3>快捷操作</h3>
            <div class="quick-actions">
              <el-button type="primary" class="action-btn" @click="$router.push('/community/post/create')">
                <el-icon><EditPen /></el-icon> 发布帖子
              </el-button>
              <el-button type="success" class="action-btn" @click="$router.push('/note/upload')">
                <el-icon><Upload /></el-icon> 上传笔记
              </el-button>
              <el-button class="action-btn" @click="$router.push('/home')">
                <el-icon><Search /></el-icon> 搜索笔记
              </el-button>
            </div>
          </div>

          <!-- 分类统计 -->
          <div class="section-card">
            <h3>分类浏览</h3>
            <div class="category-list">
              <div v-for="cat in categories" :key="cat.name" class="category-item" @click="goToCategory(cat.name)">
                <el-tag :type="getCategoryType(cat.name)" effect="plain" size="large">{{ cat.name }}</el-tag>
                <span class="category-count">{{ cat.count }} 篇</span>
              </div>
            </div>
          </div>

          <!-- 最近评论 -->
          <div class="section-card">
            <h3>最近评论</h3>
            <div class="recent-comments" v-loading="loadingComments">
              <el-empty v-if="recentComments.length === 0 && !loadingComments" description="暂无评论" :image-size="60" />
              <div v-for="item in recentComments" :key="item.id" class="comment-item" :class="{ 'admin-comment-item': item.userType === 1 }" @click="goToDetail(item.noteId)">
                <div class="comment-header">
                  <el-avatar :size="24" :style="{ background: item.userType === 1 ? '#7c3aed' : '#67C23A' }">{{ (item.nickname || 'U').charAt(0) }}</el-avatar>
                  <span class="comment-user" :class="{ 'admin-user': item.userType === 1 }">{{ item.nickname || item.username }}</span>
                  <el-tag v-if="item.userType === 1" size="small" style="background:linear-gradient(135deg,#7c3aed,#a855f7);border:none;color:#fff;font-size:9px;margin-left:4px" effect="dark">管理员</el-tag>
                </div>
                <div class="comment-text" :class="{ 'admin-comment-text': item.userType === 1 }">{{ item.content }}</div>
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
import { useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { TrendCharts, Clock, View, Download, Upload, Search, ChatDotRound, EditPen } from '@element-plus/icons-vue'
import { getHotNotes, getLatestNotes, getNoteStats } from '@/api/note'
import { getRecentComments } from '@/api/comment'
import { getHotPosts } from '@/api/post'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const stats = ref({})
const hotNotes = ref([])
const latestNotes = ref([])
const recentComments = ref([])
const hotPosts = ref([])
const loadingHot = ref(false)
const loadingLatest = ref(false)
const loadingComments = ref(false)
const loadingPosts = ref(false)

const categories = computed(() => {
  const map = {}
  const allNotes = [...hotNotes.value, ...latestNotes.value]
  allNotes.forEach(n => {
    if (n.category) {
      map[n.category] = (map[n.category] || 0) + 1
    }
  })
  const cats = ['CS', 'SE', 'Math', 'GE']
  return cats.map(name => ({ name, count: map[name] || 0 }))
})

const totalViews = computed(() => {
  const all = [...hotNotes.value, ...latestNotes.value]
  const unique = [...new Map(all.map(n => [n.id, n])).values()]
  return unique.reduce((sum, n) => sum + (n.viewCount || 0), 0)
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const day = 24 * 60 * 60 * 1000
  if (diff < day) return '今天'
  if (diff < 2 * day) return '昨天'
  if (diff < 7 * day) return Math.floor(diff / day) + '天前'
  return date.toLocaleDateString('zh-CN')
}

const getCategoryType = (cat) => {
  if (cat === 'CS') return 'primary'
  if (cat === 'SE') return 'success'
  if (cat === 'Math') return 'warning'
  return 'info'
}

const goToDetail = (id) => router.push(`/note/detail/${id}`)
const goToCategory = (cat) => router.push({ path: '/home', query: { category: cat } })
const goToPost = (id) => router.push(`/community/post/${id}`)

const loadData = async () => {
  loadingHot.value = true
  loadingLatest.value = true
  loadingComments.value = true
  loadingPosts.value = true

  try {
    const [hotRes, latestRes, statsRes, commentsRes, postsRes] = await Promise.allSettled([
      getHotNotes(8),
      getLatestNotes(6),
      getNoteStats(),
      getRecentComments(8),
      getHotPosts(6)
    ])

    if (hotRes.status === 'fulfilled' && hotRes.value?.success) {
      hotNotes.value = hotRes.value.result || []
    }
    if (latestRes.status === 'fulfilled' && latestRes.value?.success) {
      latestNotes.value = latestRes.value.result || []
    }
    if (statsRes.status === 'fulfilled' && statsRes.value?.success) {
      stats.value = statsRes.value.result || {}
    }
    if (commentsRes.status === 'fulfilled' && commentsRes.value?.success) {
      recentComments.value = commentsRes.value.result || []
    }
    if (postsRes.status === 'fulfilled' && postsRes.value?.success) {
      hotPosts.value = postsRes.value.result || []
    }
  } catch (e) {
    console.error('加载社区数据失败:', e)
  } finally {
    loadingHot.value = false
    loadingLatest.value = false
    loadingComments.value = false
    loadingPosts.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f0f2f5; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 0 20px 40px; }

/* Banner */
.community-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 30px;
  color: #fff;
}
.banner-content h1 { margin: 0 0 8px; font-size: 28px; }
.banner-content p { margin: 0 0 24px; opacity: 0.9; font-size: 15px; }
.stats-row { display: flex; gap: 40px; }
.stat-item { text-align: center; }
.stat-num { display: block; font-size: 28px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.85; }

/* Body Layout */
.community-body { display: flex; gap: 24px; }
.main-column { flex: 1; min-width: 0; }
.side-column { width: 300px; flex-shrink: 0; }

/* Section Card */
.section-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.section-card h3 { margin: 0 0 16px; font-size: 16px; color: #303133; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h2 { margin: 0; font-size: 18px; display: flex; align-items: center; gap: 6px; color: #303133; }
.more-link { color: #409EFF; text-decoration: none; font-size: 13px; }
.more-link:hover { text-decoration: underline; }

/* Hot List */
.hot-item {
  display: flex; align-items: center; gap: 14px; padding: 12px 0;
  border-bottom: 1px solid #f5f5f5; cursor: pointer; transition: background 0.2s;
}
.hot-item:last-child { border-bottom: none; }
.hot-item:hover { background: #fafafa; border-radius: 6px; padding-left: 8px; }
.rank {
  width: 28px; height: 28px; border-radius: 6px; background: #e8e8e8;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: #999; flex-shrink: 0;
}
.rank.top3 { background: linear-gradient(135deg, #ff6b35, #f7c948); color: #fff; }
.hot-info { flex: 1; min-width: 0; }
.hot-title { font-size: 15px; font-weight: 500; color: #303133; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #909399; }

/* Latest Grid */
.latest-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.latest-card { border-radius: 10px; cursor: pointer; transition: transform 0.2s; border: none; }
.latest-card:hover { transform: translateY(-3px); }
.latest-cover { width: 100%; height: 120px; overflow: hidden; border-radius: 6px; margin-bottom: 10px; }
.latest-cover-img { width: 100%; height: 100%; object-fit: cover; }
.latest-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.latest-time { font-size: 12px; color: #c0c4cc; }
.latest-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.latest-summary { font-size: 13px; color: #909399; line-height: 1.5; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; margin-bottom: 12px; }
.latest-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.latest-author { display: flex; align-items: center; gap: 4px; }
.latest-stats { display: flex; gap: 10px; }

/* Quick Actions */
.quick-actions { display: flex; flex-direction: column; gap: 10px; }
.quick-actions .action-btn { width: 100%; margin-left: 0 !important; }

/* Category List */
.category-list { display: flex; flex-direction: column; gap: 10px; }
.category-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; border-radius: 8px; cursor: pointer; transition: background 0.2s;
}
.category-item:hover { background: #f5f7fa; }
.category-count { font-size: 13px; color: #909399; }

/* Recent Comments */
.comment-item {
  padding: 10px 0; border-bottom: 1px solid #f5f5f5; cursor: pointer;
}
.comment-item:last-child { border-bottom: none; }
.comment-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.comment-user { font-size: 13px; font-weight: 500; color: #303133; }
.comment-text { font-size: 13px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Post List */
.post-item {
  display: flex; gap: 14px; padding: 16px 0;
  border-bottom: 1px solid #f2f3f5; cursor: pointer; transition: background 0.2s;
}
.post-item:last-child { border-bottom: none; }
.post-item:hover { background: #fafbfc; border-radius: 8px; padding-left: 8px; padding-right: 8px; }
.post-left { flex-shrink: 0; }
.post-body { flex: 1; min-width: 0; }
.post-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.post-rank {
  width: 22px; height: 22px; border-radius: 4px; background: #e8e8e8;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #999; flex-shrink: 0;
}
.post-rank-hot { background: linear-gradient(135deg, #f56c6c, #e6a23c); color: #fff; }
.post-title { font-size: 15px; font-weight: 600; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.post-snippet { font-size: 13px; color: #909399; line-height: 1.5; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.post-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #909399; flex-wrap: wrap; }
.post-author { font-weight: 500; color: #606266; }
.post-time { margin-left: auto; }
.post-images-thumb { flex-shrink: 0; width: 80px; height: 80px; border-radius: 8px; overflow: hidden; }
.thumb-img { width: 100%; height: 100%; object-fit: cover; }
.post-action-bar { padding-top: 16px; display: flex; justify-content: center; }

/* Admin comment styles */
.admin-comment-item { background: linear-gradient(135deg, #f5f0ff, #ede9fe); border-radius: 8px; padding: 10px; }
.admin-user { color: #7c3aed; font-weight: 600; }
.admin-comment-text { color: #4c1d95; }

/* Responsive */
@media (max-width: 768px) {
  .community-body { flex-direction: column; }
  .side-column { width: 100%; }
  .stats-row { gap: 20px; }
  .latest-grid { grid-template-columns: 1fr; }
}
</style>
