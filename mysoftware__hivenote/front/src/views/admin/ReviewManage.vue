<template>
  <div class="review-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-content">
        <div class="logo-section">
          <img src="@/assets/nav-text-logo.png" alt="HiveNote" class="logo" />
          <span class="title">笔记审核管理</span>
        </div>
        <div class="user-section">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              <span class="username">{{ userInfo.nickname || userInfo.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ pendingCount }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon approved">
              <el-icon :size="32"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ approvedCount }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rejected">
              <el-icon :size="32"><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ rejectedCount }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 筛选和操作栏 -->
      <el-card class="filter-card">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="审核状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" @change="loadNoteList" clearable style="width: 150px">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryForm.category" placeholder="全部分类" @change="loadNoteList" clearable style="width: 150px">
              <el-option label="计算机科学" value="CS" />
              <el-option label="软件工程" value="SE" />
              <el-option label="数学" value="Math" />
              <el-option label="通识课" value="GE" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="queryForm.title" placeholder="搜索标题" @keyup.enter="loadNoteList" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadNoteList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 笔记列表 -->
      <el-card class="table-card">
        <el-table :data="noteList" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="180" />
          <el-table-column prop="title" label="标题" min-width="200" />
          <el-table-column prop="category" label="分类" width="100">
            <template #default="{ row }">
              <el-tag :type="getCategoryType(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="summary" label="简介" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createBy" label="上传者" width="120" />
          <el-table-column prop="createTime" label="上传时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
              <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
              <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
              <el-button v-if="row.status === 0" link type="success" @click="handleApprove(row)">通过</el-button>
              <el-button v-if="row.status === 0" link type="danger" @click="handleReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="queryForm.page"
            v-model:page-size="queryForm.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadNoteList"
            @current-change="loadNoteList"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="笔记详情" width="700px">
      <el-descriptions :column="1" border v-if="currentNote">
        <el-descriptions-item label="标题">{{ currentNote.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryLabel(currentNote.category) }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ currentNote.summary }}</el-descriptions-item>
        <el-descriptions-item label="文件地址">
          <el-link :href="currentNote.fileUrl" target="_blank" type="primary">{{ currentNote.fileUrl }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="上传者">{{ currentNote.createBy }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ formatDate(currentNote.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentNote.viewCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="下载量">{{ currentNote.downloadCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentNote.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentNote.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="currentNote.status === 2" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentNote && currentNote.status === 0" type="success" @click="handleApprove(currentNote)">通过</el-button>
        <el-button v-if="currentNote && currentNote.status === 0" type="danger" @click="handleReject(currentNote)">拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Select, Close } from '@element-plus/icons-vue'
import { getAdminNoteList, approveNote, rejectNote } from '@/api/note'
import { getUserInfo as getUserInfoStorage, clearAuth } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const noteList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentNote = ref(null)
const userInfo = ref(getUserInfoStorage() || {})

const queryForm = reactive({
  page: 1,
  size: 10,
  status: null,
  category: '',
  title: ''
})

// 统计数据
const pendingCount = computed(() => noteList.value.filter(n => n.status === 0).length)
const approvedCount = computed(() => noteList.value.filter(n => n.status === 1).length)
const rejectedCount = computed(() => noteList.value.filter(n => n.status === 2).length)

onMounted(() => {
  loadNoteList()
})

const loadNoteList = async () => {
  loading.value = true
  try {
    const res = await getAdminNoteList(queryForm)
    if (res && res.success) {
      noteList.value = res.result.records || []
      total.value = res.result.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.page = 1
  queryForm.status = null
  queryForm.category = ''
  queryForm.title = ''
  loadNoteList()
}

const viewDetail = (row) => {
  currentNote.value = row
  detailVisible.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该笔记的审核？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    const res = await approveNote(row.id)
    if (res && res.success) {
      ElMessage.success('审核通过')
      detailVisible.value = false
      loadNoteList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该笔记？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await rejectNote(row.id)
    if (res && res.success) {
      ElMessage.success('已拒绝')
      detailVisible.value = false
      loadNoteList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    clearAuth()
    ElMessage.success('已退出登录')
    router.push('/admin/login')
  }
}

const getCategoryLabel = (category) => {
  const map = {
    'CS': '计算机科学',
    'SE': '软件工程',
    'Math': '数学',
    'GE': '通识课'
  }
  return map[category] || category
}

const getCategoryType = (category) => {
  const map = {
    'CS': 'primary',
    'SE': 'success',
    'Math': 'warning',
    'GE': 'info'
  }
  return map[category] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.review-container {
  min-height: 100vh;
  background: #f2f3f5;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo {
  height: 40px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.approved {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.rejected {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
