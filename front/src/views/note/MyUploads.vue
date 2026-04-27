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
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">查看</el-button>
            <el-button link type="danger" @click="deleteNote(row)">删除</el-button>
            <el-button
              v-if="row.status === 2"
              link
              type="warning"
              @click="showRejectReason(row.id)"
            >查看原因</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 拒绝原因弹窗 -->
      <el-dialog v-model="reasonDialogVisible" title="拒绝原因" width="500px">
        <div style="white-space: pre-wrap; padding: 10px;">{{ rejectReasonText }}</div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getNoteReviewLog } from '../../api/note'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const myNotes = ref([])
const reasonDialogVisible = ref(false)
const rejectReasonText = ref('')

const fetchMyNotes = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('http://localhost:8080/zwz/note/myList', {
      headers: { accessToken: token }
    })
    const data = res.data
    if (data && data.success) {
      myNotes.value = data.result || []
    } else {
      ElMessage.error(data.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载我的上传列表失败')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/note/detail/${id}`)

const deleteNote = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该笔记？', '提示', { type: 'warning' })
    const token = localStorage.getItem('token')
    await axios.post(`http://localhost:8080/zwz/note/delete/${row.id}`, {}, {
      headers: { accessToken: token }
    })
    ElMessage.success('删除成功')
    fetchMyNotes()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const showRejectReason = async (noteId) => {
  try {
    const res = await getNoteReviewLog(noteId)
    if (res && res.success) {
      const rejectLog = (res.result || []).find(item => item.reviewType === 'MANUAL_REJECT')
      rejectReasonText.value = rejectLog ? rejectLog.rejectReason : '暂无详细原因'
      reasonDialogVisible.value = true
    } else {
      ElMessage.error('获取拒绝原因失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  }
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
</style>
