<template>
  <div class="page-container">
    <NavBar />
    <div class="main-content">
      <div class="page-header">
        <div class="header-left">
          <el-button link @click="$router.push('/community')"><el-icon><ArrowLeft /></el-icon> 返回社区</el-button>
          <h1>讨论广场</h1>
        </div>
        <el-button type="primary" @click="$router.push('/community/post/create')">
          <el-icon><EditPen /></el-icon> 发布帖子
        </el-button>
      </div>

      <!-- 搜索 -->
      <el-card class="search-card">
        <el-input
          v-model="keyword"
          placeholder="搜索帖子标题或内容..."
          clearable
          @keyup.enter="doSearch"
          style="max-width: 400px"
        >
          <template #append>
            <el-button @click="doSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </el-card>

      <!-- 帖子列表 -->
      <div v-loading="loading">
        <el-empty v-if="posts.length === 0 && !loading" description="暂无帖子">
          <el-button type="primary" @click="$router.push('/community/post/create')">发第一帖</el-button>
        </el-empty>

        <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover" @click="goToPost(post.id)">
          <div class="post-row">
            <el-avatar :size="44" :src="post.avatar || defaultAvatar">
              {{ (post.nickname || 'U').charAt(0) }}
            </el-avatar>
            <div class="post-info">
              <div class="post-title">{{ post.title }}</div>
              <div class="post-snippet">{{ post.content?.substring(0, 120) }}...</div>
              <div class="post-meta">
                <span class="post-author">{{ post.nickname || '匿名' }}</span>
                <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
                <span>👍 {{ post.likeCount || 0 }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-thumb" v-if="post.images">
              <el-image :src="post.images.split(',')[0]" fit="cover" class="thumb-img" />
            </div>
          </div>
        </el-card>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadPosts"
          @current-change="loadPosts"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { ArrowLeft, EditPen, Search, View, ChatDotRound } from '@element-plus/icons-vue'
import { getPostList } from '@/api/post'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const loading = ref(false)
const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const goToPost = (id) => router.push(`/community/post/${id}`)

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

const doSearch = () => {
  page.value = 1
  loadPosts()
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getPostList({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined
    })
    if (res && res.success) {
      posts.value = res.result?.records || []
      total.value = res.result?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPosts())
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f0f2f5; }
.main-content { max-width: 900px; margin: 0 auto; padding: 20px 20px 40px; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left h1 { margin: 0; font-size: 24px; color: #1d2129; }

.search-card { margin-bottom: 20px; border-radius: 10px; }

.post-card { margin-bottom: 12px; border-radius: 10px; cursor: pointer; transition: transform 0.2s; }
.post-card:hover { transform: translateY(-2px); }

.post-row { display: flex; gap: 14px; align-items: flex-start; }
.post-info { flex: 1; min-width: 0; }
.post-title { font-size: 16px; font-weight: 600; color: #1d2129; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.post-snippet { font-size: 13px; color: #86909c; line-height: 1.5; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.post-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #c0c4cc; flex-wrap: wrap; }
.post-author { color: #606266; font-weight: 500; }
.post-time { margin-left: auto; }

.post-thumb { flex-shrink: 0; width: 90px; height: 90px; border-radius: 8px; overflow: hidden; }
.thumb-img { width: 100%; height: 100%; object-fit: cover; }

.pagination { margin-top: 24px; display: flex; justify-content: center; }
</style>
