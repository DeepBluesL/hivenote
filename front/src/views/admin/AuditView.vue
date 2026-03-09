<template>
  <div class="audit-view">
    <el-container>
      <el-header>
        <h2>笔记审核管理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="fetchPendingNotes">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </el-header>
      
      <el-main>
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-cards">
          <el-col :span="6" v-for="stat in stats" :key="stat.label">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-icon" :style="{ color: stat.color }">
                  <el-icon :size="32"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 筛选栏 -->
        <el-card class="filter-card">
          <el-form :inline="true" :model="filterForm">
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="全部" clearable>
                <el-option label="待审核" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="filterForm.category" placeholder="全部" clearable>
                <el-option label="计算机科学" value="CS" />
                <el-option label="软件工程" value="SE" />
                <el-option label="数学" value="Math" />
                <el-option label="通识教育" value="GE" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="filterForm.keyword" placeholder="标题/作者" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchNotes">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 笔记列表 -->
        <el-card>
          <el-table 
            :data="noteList" 
            v-loading="loading"
            border
            stripe
            @row-click="handleRowClick"
            highlight-current-row
          >
            <el-table-column type="index" width="50" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createBy" label="上传者" width="120" />
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="上传时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button 
                  type="success" 
                  size="small" 
                  @click="approveNote(row.id)"
                  :disabled="row.status === 1"
                >
                  通过
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="rejectNote(row.id)"
                  :disabled="row.status === 2"
                >
                  拒绝
                </el-button>
                <el-button type="primary" size="small" @click="viewDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchNotes"
            @current-change="fetchNotes"
            class="pagination"
          />
        </el-card>
      </el-main>
    </el-container>
    
    <!-- 审核对话框 -->
    <el-dialog v-model="dialogVisible" :title="`审核 - ${currentNote?.title}`" width="500px">
      <el-form ref="formRef" :model="auditForm" :rules="rules">
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="auditForm.result">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="auditForm.result === 2" 
          label="拒绝原因" 
          prop="reason"
        >
          <el-input 
            v-model="auditForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请填写拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="submitting">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

// 统计数据
const stats = ref([
  { label: '待审核', value: 0, icon: 'Warning', color: '#e6a23c' },
  { label: '已通过', value: 0, icon: 'SuccessFilled', color: '#67c23a' },
  { label: '已拒绝', value: 0, icon: 'CircleCloseFilled', color: '#f56c6c' },
  { label: '总笔记', value: 0, icon: 'Document', color: '#409eff' }
])

// 筛选表单
const filterForm = reactive({
  status: 0,
  category: '',
  keyword: ''
})

// 列表数据
const noteList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 审核对话框
const dialogVisible = ref(false)
const currentNote = ref(null)
const submitting = ref(false)
const auditForm = reactive({
  result: 1,
  reason: ''
})

const rules = {
  reason: [{ required: true, message: '请填写拒绝原因', trigger: 'blur' }]
}

// 获取笔记列表
const fetchNotes = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...filterForm
    }
    const res = await axios.get('/note/admin/list', { params })
    if (res.data.success) {
      noteList.value = res.data.result.records
      total.value = res.data.result.total
      
      // 更新统计
      updateStats(res.data.result.records)
    }
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计
const updateStats = (records) => {
  const pending = records.filter(n => n.status === 0).length
  const approved = records.filter(n => n.status === 1).length
  const rejected = records.filter(n => n.status === 2).length
  
  stats.value[0].value = pending
  stats.value[1].value = approved
  stats.value[2].value = rejected
  stats.value[3].value = records.length
}

// 重置筛选
const resetFilter = () => {
  filterForm.status = 0
  filterForm.category = ''
  filterForm.keyword = ''
  fetchNotes()
}

// 行点击
const handleRowClick = (row) => {
  currentNote.value = row
}

// 通过笔记
const approveNote = (id) => {
  ElMessageBox.confirm('确定通过该笔记吗？', '提示', {
    type: 'success'
  }).then(async () => {
    try {
      const res = await axios.post(`/note/admin/approve/${id}`)
      if (res.data.success) {
        ElMessage.success('审核通过')
        fetchNotes()
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 拒绝笔记
const rejectNote = (id) => {
  currentNote.value = { id }
  dialogVisible.value = true
}

// 查看详情
const viewDetail = (row) => {
  currentNote.value = row
  // 可以打开详情抽屉
}

// 提交审核
const submitAudit = async () => {
  if (auditForm.result === 2 && !auditForm.reason) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  
  submitting.value = true
  try {
    if (auditForm.result === 1) {
      const res = await axios.post(`/note/admin/approve/${currentNote.value.id}`)
      if (res.data.success) {
        ElMessage.success('审核通过')
      }
    } else {
      const res = await axios.post(`/note/admin/reject/${currentNote.value.id}`, {
        reason: auditForm.reason
      })
      if (res.data.success) {
        ElMessage.success('已拒绝')
      }
    }
    dialogVisible.value = false
    fetchNotes()
    auditForm.reason = ''
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 工具函数
const getStatusType = (status) => {
  return ['warning', 'success', 'danger'][status] || 'info'
}

const getStatusText = (status) => {
  return ['待审核', '已通过', '已拒绝'][status] || '未知'
}

onMounted(() => {
  fetchNotes()
})
</script>

<style scoped>
.audit-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
