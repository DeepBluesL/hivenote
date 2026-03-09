<template>
  <div class="page-container">
    <NavBar />

    <div class="main-content">
      <!-- 推荐内容板块 -->
      <div class="recommend-section">
        <div class="section-header">
          <div class="section-title">
            <span class="title-icon">🔥</span>
            <h2>推荐内容</h2>
            <span class="title-desc">最新热门笔记精选</span>
          </div>
        </div>
        <div class="recommend-grid" v-loading="loadingRecommend">
          <el-empty v-if="recommendNotes.length === 0 && !loadingRecommend" description="暂无推荐内容" />
          <div
            v-for="(note, index) in recommendNotes"
            :key="note.id"
            class="recommend-card"
            @click="goToDetail(note.id)"
          >
            <div class="recommend-rank" :class="{ 'rank-gold': index === 0, 'rank-silver': index === 1, 'rank-bronze': index === 2 }">
              {{ index + 1 }}
            </div>
            <div class="recommend-body">
              <div class="recommend-header">
                <el-tag :type="getCategoryType(note.category)" effect="dark" size="small">{{ note.category }}</el-tag>
                <span class="recommend-title">{{ note.title }}</span>
              </div>
              <p class="recommend-summary">{{ note.summary || '暂无简介...' }}</p>
              <div class="recommend-footer">
                <div class="recommend-author">
                  <el-avatar :size="18" style="background:#409EFF">{{ (note.createBy || 'U').charAt(0) }}</el-avatar>
                  <span>{{ note.createBy || '匿名' }}</span>
                </div>
                <div class="recommend-stats">
                  <span>👍 {{ note.likeCount || 0 }}</span>
                  <span><el-icon><View /></el-icon> {{ note.viewCount || 0 }}</span>
                  <span><el-icon><Download /></el-icon> {{ note.downloadCount || 0 }}</span>
                </div>
              </div>
              <div class="recommend-tags" v-if="note.tags">
                <el-tag
                  v-for="tag in parseTags(note.tags)"
                  :key="tag"
                  size="small"
                  type="info"
                  effect="plain"
                  class="note-tag"
                >{{ tag }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 期末冲刺板块 -->
      <div class="exam-section">
        <div class="section-header">
          <div class="section-title">
            <span class="title-icon">📚</span>
            <h2>期末冲刺</h2>
            <span class="title-desc">备考复习精华笔记</span>
          </div>
        </div>
        <div class="exam-tags-bar">
          <span
            class="exam-tag-item"
            :class="{ active: examTag === '' }"
            @click="handleExamTag('')"
          >全部</span>
          <span
            v-for="tag in examTagOptions"
            :key="tag"
            class="exam-tag-item"
            :class="{ active: examTag === tag }"
            @click="handleExamTag(tag)"
          >{{ tag }}</span>
        </div>
        <div class="exam-grid" v-loading="loadingExam">
          <el-empty v-if="examNotes.length === 0 && !loadingExam" description="暂无期末冲刺笔记，快去上传吧~" />
          <el-card
            v-for="note in examNotes"
            :key="note.id"
            class="exam-card"
            shadow="hover"
            @click="goToDetail(note.id)"
          >
            <div class="exam-card-top">
              <el-tag :type="getCategoryType(note.category)" effect="dark" size="small">{{ note.category }}</el-tag>
              <div class="exam-card-title">{{ note.title }}</div>
            </div>
            <p class="exam-card-summary">{{ note.summary || '暂无简介...' }}</p>
            <div class="exam-card-tags" v-if="note.tags">
              <el-tag
                v-for="tag in parseTags(note.tags)"
                :key="tag"
                size="small"
                effect="plain"
                round
                class="note-tag"
              >{{ tag }}</el-tag>
            </div>
            <div class="exam-card-footer">
              <div class="exam-card-author">
                <el-avatar :size="18" style="background:#67C23A">{{ (note.createBy || 'U').charAt(0) }}</el-avatar>
                <span>{{ note.createBy || '匿名' }}</span>
              </div>
              <div class="exam-card-stats">
                <span>👍 {{ note.likeCount || 0 }}</span>
                <span><el-icon><View /></el-icon> {{ note.viewCount || 0 }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 分割线 -->
      <el-divider>
        <el-icon><Search /></el-icon> 全部资源
      </el-divider>

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
            >{{ cat }}</span>
          </div>

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

          <div class="tag-filter-wrap">
            <el-select
              v-model="queryParams.tag"
              placeholder="按标签筛选"
              size="small"
              style="width: 150px"
              clearable
              @change="handleSearch"
              @clear="handleSearch"
            >
              <el-option
                v-for="t in allTagOptions"
                :key="t"
                :label="t"
                :value="t"
              />
            </el-select>
          </div>

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
            @click="goToDetail(note.id)"
          >
            <div class="card-header">
              <el-tag :type="getCategoryType(note.category)" effect="dark" size="small">
                {{ note.category }}
              </el-tag>
              <div class="card-title" :title="note.title">{{ note.title }}</div>
            </div>

            <div class="card-body">
              <p class="summary">{{ note.summary || '暂无简介...' }}</p>
            </div>

            <div class="card-tags" v-if="note.tags">
              <el-tag
                v-for="tag in parseTags(note.tags)"
                :key="tag"
                size="small"
                type="info"
                effect="plain"
                class="note-tag"
              >{{ tag }}</el-tag>
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
import { Search, Download, View } from '@element-plus/icons-vue'
import NavBar from '../../components/NavBar.vue'
import { getNoteList, getRecommendNotes, getExamSprintNotes } from '../../api/note'
import { getSchoolList } from '../../api/school'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const noteList = ref([])
const total = ref(0)
const schoolLoading = ref(false)
const schoolOptions = ref([])

const loadingRecommend = ref(false)
const recommendNotes = ref([])
const loadingExam = ref(false)
const examNotes = ref([])
const examTag = ref('')

const examTagOptions = ['期末复习', '考试重点', '期末冲刺', '知识总结', '高分笔记', '真题解析', '考前速记']

const allTagOptions = [
  '期末复习', '考试重点', '期末冲刺', '知识总结', '高分笔记',
  '真题解析', '考前速记', '课堂笔记', '实验报告', '学习心得',
  '编程实践', '项目经验', '论文写作', '数据结构', '算法',
  '操作系统', '计算机网络', '数据库', '高等数学', '线性代数',
  '概率统计', '离散数学'
]

const queryParams = reactive({
  page: 1,
  size: 12,
  keyword: '',
  category: '',
  school: '',
  tag: '',
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

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  return tagsStr.split(',').map(t => t.trim()).filter(t => t)
}

const fetchRecommend = async () => {
  loadingRecommend.value = true
  try {
    const res = await getRecommendNotes(6)
    if (res && res.success) {
      recommendNotes.value = res.result || []
    }
  } catch (e) {
    console.error('获取推荐笔记错误:', e)
  } finally {
    loadingRecommend.value = false
  }
}

const fetchExamSprint = async () => {
  loadingExam.value = true
  try {
    const res = await getExamSprintNotes(8, examTag.value)
    if (res && res.success) {
      examNotes.value = res.result || []
    }
  } catch (e) {
    console.error('获取期末冲刺笔记错误:', e)
  } finally {
    loadingExam.value = false
  }
}

const handleExamTag = (tag) => {
  examTag.value = tag
  fetchExamSprint()
}

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
  if (route.query.category) {
    queryParams.category = route.query.category
  }
  if (route.query.tag) {
    queryParams.tag = route.query.tag
  }
  fetchRecommend()
  fetchExamSprint()
  fetchData()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background-color: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }

/* 板块通用样式 */
.section-header { margin-bottom: 20px; }
.section-title { display: flex; align-items: center; gap: 8px; }
.section-title h2 { margin: 0; font-size: 20px; color: #303133; }
.title-icon { font-size: 24px; }
.title-desc { font-size: 13px; color: #909399; margin-left: 8px; }

/* 推荐内容板块 */
.recommend-section {
  margin-bottom: 36px;
  background: linear-gradient(135deg, #667eea10, #764ba210);
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #e8ecf4;
}
.recommend-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.recommend-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}
.recommend-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: #c6e2ff;
}
.recommend-rank {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  color: #999;
  flex-shrink: 0;
  margin-top: 2px;
}
.rank-gold { background: linear-gradient(135deg, #ff6b35, #f7c948); color: #fff; }
.rank-silver { background: linear-gradient(135deg, #a8b8d8, #6b7b9e); color: #fff; }
.rank-bronze { background: linear-gradient(135deg, #d4a574, #a0784c); color: #fff; }
.recommend-body { flex: 1; min-width: 0; }
.recommend-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.recommend-title { font-weight: 600; font-size: 15px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.recommend-summary { font-size: 13px; color: #909399; line-height: 1.5; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.recommend-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.recommend-author { display: flex; align-items: center; gap: 4px; }
.recommend-stats { display: flex; gap: 12px; align-items: center; }
.recommend-tags { margin-top: 6px; display: flex; gap: 4px; flex-wrap: wrap; }

/* 期末冲刺板块 */
.exam-section {
  margin-bottom: 36px;
  background: linear-gradient(135deg, #ff9a5610, #ff614610);
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #f4e8e4;
}
.exam-tags-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.exam-tag-item {
  padding: 5px 14px;
  background: #fff;
  border-radius: 16px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}
.exam-tag-item:hover,
.exam-tag-item.active {
  background: linear-gradient(135deg, #ff6b35, #f7c948);
  color: #fff;
  border-color: transparent;
}
.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.exam-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: transform 0.3s;
}
.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}
.exam-card-top { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.exam-card-title { font-weight: 600; font-size: 15px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.exam-card-summary { font-size: 13px; color: #909399; line-height: 1.5; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; margin: 0 0 8px; }
.exam-card-tags { margin-bottom: 10px; display: flex; gap: 4px; flex-wrap: wrap; }
.exam-card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; border-top: 1px solid #f2f6fc; padding-top: 10px; }
.exam-card-author { display: flex; align-items: center; gap: 4px; }
.exam-card-stats { display: flex; gap: 10px; align-items: center; }

/* 标签通用样式 */
.note-tag { font-size: 11px; }

/* 筛选区 */
.filter-header { margin-bottom: 30px; }
.search-wrap { max-width: 600px; margin: 0 auto 20px; }
.filter-row { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.category-tabs { display: flex; gap: 10px; flex-wrap: wrap; }
.tab-item { padding: 6px 16px; background: #fff; border-radius: 20px; font-size: 14px; color: #606266; cursor: pointer; border: 1px solid #dcdfe6; transition: all 0.3s; }
.tab-item:hover, .tab-item.active { background-color: #409EFF; color: #fff; border-color: #409EFF; }
.sort-wrap { flex-shrink: 0; }
.tag-filter-wrap { flex-shrink: 0; }

/* 笔记列表 */
.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.note-card { border-radius: 12px; border: none; cursor: pointer; transition: transform 0.3s; }
.note-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
.card-header { display: flex; align-items: center; margin-bottom: 12px; }
.card-title { margin-left: 10px; font-weight: 600; font-size: 16px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.summary { font-size: 13px; color: #909399; line-height: 1.5; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-tags { margin-bottom: 10px; display: flex; gap: 4px; flex-wrap: wrap; }
.card-footer { border-top: 1px solid #f2f6fc; padding-top: 12px; display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.author { display: flex; align-items: center; gap: 6px; }
.stats { display: flex; gap: 12px; }
.school-tag { margin-left: 4px; max-width: 80px; overflow: hidden; text-overflow: ellipsis; }
.school-wrap { flex-shrink: 0; }
.pagination-wrap { margin-top: 40px; display: flex; justify-content: center; }

@media (max-width: 768px) {
  .recommend-section, .exam-section { padding: 16px; }
  .filter-row { flex-direction: column; align-items: flex-start; }
  .exam-grid { grid-template-columns: 1fr; }
}
</style>
