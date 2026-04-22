<template>
  <div class="page-container">
    <NavBar />

    <div class="main-content">
      <!-- 搜索与筛选区 -->
      <div class="filter-header">
        <div class="search-wrap">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索笔记标题、简介、上传者..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="filter-row">
          <!-- 分类标签 -->
          <div class="category-tabs">
            <span
              class="tab-item"
              :class="{ active: queryParams.category === '' }"
              @click="handleCategory('')"
            >全部</span>
            <span
              v-for="cat in categoryOptions"
              :key="cat"
              class="tab-item"
              :class="{ active: queryParams.category === cat }"
              @click="handleCategory(cat)"
            >
              {{ cat }}
            </span>
          </div>

          <!-- 学校筛选 -->
          <div class="school-wrap">
            <el-select
              v-model="queryParams.school"
              placeholder="按学校筛选"
              size="small"
              style="width: 180px"
              filterable
              remote
              :remote-method="handleSchoolSearch"
              clearable
              :loading="schoolLoading"
              @change="handleSearch"
              @clear="handleSearch"
            >
              <el-option
                v-for="s in schoolOptions"
                :key="s"
                :label="s"
                :value="s"
              />
            </el-select>
          </div>

          <!-- 排序选项 -->
          <div class="sort-wrap">
            <el-select v-model="queryParams.sortBy" size="small" @change="handleSearch" style="width: 130px">
              <el-option label="最新发布" value="newest" />
              <el-option label="最多浏览" value="popular" />
              <el-option label="最多下载" value="downloads" />
              <el-option label="最多点赞" value="likes" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 笔记列表展示区 -->
      <div class="note-list-wrapper" v-loading="loading">
        <el-empty v-if="noteList.length === 0 && !loading" description="暂无相关笔记" />

        <div class="grid-layout" v-else>
          <el-card
            v-for="note in noteList"
            :key="note.id"
            class="note-card"
            shadow="hover"
            :body-style="{ padding: '16px' }"
            @click="goToDetail(note.id)"
          >
            <!-- 封面图片 -->
            <div class="card-cover">
              <img v-if="note.coverUrl" :src="note.coverUrl" :alt="note.title" class="cover-img" />
              <div v-else class="cover-placeholder">
                <el-icon :size="32" color="#c0c4cc"><Document /></el-icon>
              </div>
            </div>

            <div class="card-header">
              <el-tag :type="getCategoryType(note.category)" effect="dark" size="small">
                {{ note.category }}
              </el-tag>
              <div class="card-title" :title="note.title">{{ note.title }}</div>
            </div>

            <div class="card-body">
              <p class="summary">{{ note.summary || '暂无简介...' }}</p>
            </div>

            <div class="card-footer">
              <div class="author">
                <el-avatar :size="20" style="background:#409EFF">{{ note.createBy?.charAt(0) || 'U' }}</el-avatar>
                <span class="name">{{ note.createBy || '匿名' }}</span>
                <el-tag v-if="note.school" size="small" type="info" class="school-tag">{{ note.school }}</el-tag>
              </div>
              <div class="stats">
                <span>👍 {{ note.likeCount || 0 }}</span>
                <span><el-icon><Download /></el-icon> {{ note.downloadCount || 0 }}</span>
                <span><el-icon><View /></el-icon> {{ note.viewCount || 0 }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="queryParams.size"
          :current-page="queryParams.page"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Download, View, Document } from '@element-plus/icons-vue'
import NavBar from '../../components/NavBar.vue'
import { getNoteList } from '../../api/note'
import { getSchoolList } from '../../api/school'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const noteList = ref([])
const total = ref(0)
const schoolLoading = ref(false)
const schoolOptions = ref([])

const queryParams = reactive({
  page: 1,
  size: 12,
  keyword: '',
  category: '',
  school: '',
  sortBy: 'newest'
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

const categoryOptions = ['CS', 'SE', 'Math', 'GE']

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNoteList(queryParams)
    if (res && res.success) {
      noteList.value = res.result.records || []
      total.value = res.result.total || 0
    } else {
      noteList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取笔记列表错误:', error)
    noteList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleCategory = (cat) => { queryParams.category = cat; queryParams.page = 1; fetchData() }
const handlePageChange = (val) => { queryParams.page = val; fetchData() }
const goToDetail = (id) => { router.push(`/note/detail/${id}`) }

const getCategoryType = (cat) => {
  if (cat === 'CS') return 'primary'
  if (cat === 'SE') return 'success'
  if (cat === 'Math') return 'warning'
  return 'info'
}

onMounted(() => {
  // 从路由参数读取分类（社区页跳转过来）
  if (route.query.category) {
    queryParams.category = route.query.category
  }
  fetchData()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background-color: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }
.filter-header { margin-bottom: 30px; }
.search-wrap { max-width: 600px; margin: 0 auto 20px; }

.filter-row { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.category-tabs { display: flex; gap: 10px; flex-wrap: wrap; }
.tab-item { padding: 6px 16px; background: #fff; border-radius: 20px; font-size: 14px; color: #606266; cursor: pointer; border: 1px solid #dcdfe6; transition: all 0.3s; }
.tab-item:hover, .tab-item.active { background-color: #409EFF; color: #fff; border-color: #409EFF; }
.sort-wrap { flex-shrink: 0; }

.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.note-card { border-radius: 12px; border: none; cursor: pointer; transition: transform 0.3s; }
.note-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }

.card-cover { width: 100%; height: 150px; overflow: hidden; border-radius: 8px; margin-bottom: 12px; background: #f5f7fa; }
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.note-card:hover .cover-img { transform: scale(1.05); }
.cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #e0e7ff 0%, #f5f7fa 100%); }

.card-header { display: flex; align-items: center; margin-bottom: 12px; }
.card-title { margin-left: 10px; font-weight: 600; font-size: 16px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.summary { font-size: 13px; color: #909399; line-height: 1.5; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-footer { border-top: 1px solid #f2f6fc; padding-top: 12px; display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.author { display: flex; align-items: center; gap: 6px; }
.stats { display: flex; gap: 12px; }
.school-tag { margin-left: 4px; max-width: 80px; overflow: hidden; text-overflow: ellipsis; }
.school-wrap { flex-shrink: 0; }
.pagination-wrap { margin-top: 40px; display: flex; justify-content: center; }
</style>
