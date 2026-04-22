<template>
  <el-card 
    class="note-card" 
    shadow="hover" 
    :body-style="{ padding: '0px' }"
    @click="handleClick"
  >
    <div class="card-content">
      <!-- 封面图片 -->
      <div class="card-cover">
        <img v-if="note.coverUrl" :src="note.coverUrl" :alt="note.title" class="cover-img" />
        <div v-else class="cover-placeholder">
          <el-icon :size="32" color="#c0c4cc"><Document /></el-icon>
        </div>
      </div>

      <!-- 头部：分类与标题 -->
      <div class="card-header">
        <el-tag 
          :type="categoryType" 
          effect="light" 
          size="small" 
          class="category-tag"
        >
          {{ note.category || '未分类' }}
        </el-tag>
        <h3 class="title" :title="note.title">{{ note.title }}</h3>
      </div>

      <!-- 中间：简介 -->
      <p class="summary">
        {{ note.summary || note.description || '暂无简介...' }}
      </p>

      <!-- 标签 -->
      <div class="card-tags" v-if="note.tags">
        <el-tag
          v-for="tag in parseTags(note.tags)"
          :key="tag"
          size="small"
          type="info"
          effect="plain"
          class="card-tag-item"
        >{{ tag }}</el-tag>
      </div>

      <!-- 底部：作者与数据 -->
      <div class="card-footer">
        <div class="author-info">
          <el-avatar :size="20" class="mini-avatar">
            {{ note.uploaderName ? note.uploaderName.charAt(0).toUpperCase() : 'U' }}
          </el-avatar>
          <span class="author-name">{{ note.uploaderName || '匿名用户' }}</span>
        </div>
        
        <div class="stats-info">
          <span class="stat-item" title="下载量">
            <el-icon><Download /></el-icon> {{ note.downloadCount || 0 }}
          </span>
          <span class="stat-item" title="浏览量">
            <el-icon><View /></el-icon> {{ note.viewCount || 0 }}
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Download, View, Collection, ArrowDown, Document } from '@element-plus/icons-vue'

const router = useRouter()

// 定义 Props：接收父组件传来的笔记数据
const props = defineProps({
  note: {
    type: Object,
    required: true,
    default: () => ({
      id: '',
      title: '默认标题',
      category: '',
      summary: '',
      uploaderName: '',
      downloadCount: 0
    })
  }
})

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  return tagsStr.split(',').map(t => t.trim()).filter(t => t)
}

const categoryType = computed(() => {
  const cat = props.note.category
  if (!cat) return 'info'
  if (cat.includes('CS') || cat.includes('计算机')) return 'primary'
  if (cat.includes('SE') || cat.includes('软件')) return 'success'
  if (cat.includes('Math') || cat.includes('数')) return 'warning'
  if (cat.includes('GE') || cat.includes('通识')) return 'danger'
  return 'info'
})

// 卡片点击跳转详情页
const handleClick = () => {
  if (props.note.id) {
    router.push(`/note/detail/${props.note.id}`)
  }
}
</script>

<style scoped>
.note-card {
  cursor: pointer;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  height: 100%; /* 让卡片在 Grid 布局中高度拉伸 */
  display: flex;
  flex-direction: column;
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: #c6e2ff;
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
}

.card-cover {
  width: 100%;
  height: 140px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #f5f7fa;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.note-card:hover .cover-img {
  transform: scale(1.05);
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.category-tag {
  margin-right: 8px;
  flex-shrink: 0; /* 防止标签被压缩 */
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  /* 标题超出省略 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.card-tag-item {
  font-size: 11px;
}

.summary {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0 0 16px 0;
  /* 多行省略 (最多显示2行) */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 42px; /* 强制高度对齐 */
}

.card-footer {
  margin-top: auto; /* 自动推到底部 */
  padding-top: 12px;
  border-top: 1px solid #f2f6fc;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 6px;
}
.mini-avatar {
  background-color: #a0cfff; 
  font-size: 10px;
}
.author-name {
  font-size: 12px;
  color: #606266;
}

.stats-info {
  display: flex;
  gap: 12px;
}
.stat-item {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>