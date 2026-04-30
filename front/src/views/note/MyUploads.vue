<template>
  <div class="page-container">
    <NavBar />
    <div class="main-content">
      <div class="section-header">
        <h2>📄 我的上传</h2>
        <el-button type="primary" @click="$router.push('/note/upload')">上传新笔记</el-button>
      </div>

      <el-table :data="myNotes" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">审核中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="row.status === 3" type="info">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="primary" @click="goDetail(row.id)">查看</el-button>
            <el-button v-if="[2,3].includes(row.status)" link type="primary" @click="editNote(row.id)">
              {{ row.status === 2 ? '修改重提' : '编辑草稿' }}
            </el-button>
            <el-button link type="info" @click="showReviewHistory(row.id)">审核记录</el-button>
            <el-button link type="danger" @click="deleteNote(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="historyDialogVisible" title="审核记录" width="620px">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { deleteNote as deleteNoteApi, getNoteReviewLog } from '../../api/note'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const myNotes = ref([])
const historyDialogVisible = ref(false)
const reviewHistory = ref([])

const fetchMyNotes = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/note/myList', method: 'get' })
    if (res && res.success) {
      myNotes.value = res.result || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载我的上传列表失败')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/note/detail/${id}`)
const editNote = (id) => router.push(`/note/upload?id=${id}`)

const deleteNote = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该笔记？', '提示', { type: 'warning' })
    await deleteNoteApi(row.id)
    ElMessage.success('删除成功')
    fetchMyNotes()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const showReviewHistory = async (noteId) => {
  try {
    const res = await getNoteReviewLog(noteId)
    if (res && res.success) {
      reviewHistory.value = res.result || []
      historyDialogVisible.value = true
    } else {
      ElMessage.error('获取审核记录失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  }
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

const getCategoryLabel = (cat) => {
  const map = { 'CS': '计算机科学', 'SE': '软件工程', 'Math': '数学', 'GE': '通识课' }
  return map[cat] || cat
}
const getCategoryType = (cat) => {
  if (cat === 'CS') return 'primary'
  if (cat === 'SE') return 'success'
  if (cat === 'Math') return 'warning'
  return 'info'
}
const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

onMounted(fetchMyNotes)
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h2 { margin: 0; }
.history-title { font-weight: 600; color: #303133; }
.history-body { white-space: pre-wrap; margin-top: 6px; color: #606266; }
.history-meta { margin-top: 6px; font-size: 12px; color: #909399; }
</style>
