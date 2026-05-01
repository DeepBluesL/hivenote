<template>
  <div class="review-container">
    <div class="header">
      <div class="header-content">
        <div class="logo-section">
          <img src="@/assets/nav-text-logo.png" alt="HiveNote" class="logo" />
          <span class="title">管理后台</span>
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

    <div class="main-content">
      <el-tabs v-model="activeTab" type="border-card" class="admin-tabs">
        <el-tab-pane label="笔记审核" name="notes">
          <div class="stats-cards">
            <el-card class="stat-card" @click="filterNoteStatus(0)">
              <div class="stat-content">
                <div class="stat-icon pending"><el-icon :size="32"><Clock /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ pendingCount }}</div>
                  <div class="stat-label">待审核</div>
                </div>
              </div>
            </el-card>
            <el-card class="stat-card" @click="filterNoteStatus(1)">
              <div class="stat-content">
                <div class="stat-icon approved"><el-icon :size="32"><Select /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ approvedCount }}</div>
                  <div class="stat-label">已通过</div>
                </div>
              </div>
            </el-card>
            <el-card class="stat-card" @click="filterNoteStatus(2)">
              <div class="stat-content">
                <div class="stat-icon rejected"><el-icon :size="32"><Close /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ rejectedCount }}</div>
                  <div class="stat-label">已拒绝</div>
                </div>
              </div>
            </el-card>
          </div>

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

          <el-card class="table-card">
            <el-table :data="noteList" v-loading="loading" stripe>
              <el-table-column prop="id" label="ID" width="180" />
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="category" label="分类" width="100">
                <template #default="{ row }">
                  <el-tag :type="getCategoryType(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createBy" label="上传者" width="120" />
              <el-table-column prop="createTime" label="上传时间" width="170">
                <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
                  <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                  <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="推荐" width="170">
                <template #default="{ row }">
                  <el-tag v-if="row.featured === 1" type="warning" effect="dark" size="small" class="mr-4">推荐</el-tag>
                  <el-tag v-if="row.examFeatured === 1" type="danger" effect="dark" size="small">期末</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="400" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
                  <el-button v-if="row.status === 0" link type="warning" :loading="aiLoadingMap[row.id]" @click="handleAiReview(row)">AI 建议</el-button>
                  <el-button v-if="row.status === 0" link type="success" @click="handleApprove(row)">通过</el-button>
                  <el-button v-if="row.status === 0" link type="danger" @click="openRejectDialog(row)">拒绝</el-button>
                  <el-button v-if="row.status === 1" link :type="row.featured === 1 ? 'warning' : 'default'" @click="toggleFeatured(row)">
                    {{ row.featured === 1 ? '取消推荐' : '设为推荐' }}
                  </el-button>
                  <el-button v-if="row.status === 1" link :type="row.examFeatured === 1 ? 'danger' : 'default'" @click="toggleExamFeatured(row)">
                    {{ row.examFeatured === 1 ? '取消期末' : '期末冲刺' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

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
        </el-tab-pane>

        <!-- 评论管理和用户管理保持不变，此处省略，实际项目里需完整保留，我们只增补功能 -->
        <el-tab-pane label="评论管理" name="comments">
          <el-card class="filter-card">
            <el-form :inline="true" :model="commentQuery">
              <el-form-item label="关键词">
                <el-input v-model="commentQuery.keyword" placeholder="搜索评论内容/用户" @keyup.enter="loadCommentList" clearable style="width: 250px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadCommentList">查询</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          <el-card class="table-card">
            <el-table :data="commentList" v-loading="commentLoading" stripe>
              <el-table-column prop="id" label="ID" width="180" />
              <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
              <el-table-column label="评论人" width="140">
                <template #default="{ row }">
                  <div style="display:flex; align-items:center; gap:4px;">
                    <span>{{ row.nickname || row.username }}</span>
                    <el-tag v-if="row.userType === 1" size="small" type="primary" effect="dark" style="font-size:10px">管理</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="noteId" label="笔记ID" width="180" />
              <el-table-column prop="createTime" label="评论时间" width="170">
                <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="danger" @click="handleDeleteComment(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination">
              <el-pagination
                v-model:current-page="commentQuery.page"
                v-model:page-size="commentQuery.size"
                :page-sizes="[20, 50, 100]"
                :total="commentTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="loadCommentList"
                @current-change="loadCommentList"
              />
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="用户管理" name="users">
          <el-card class="filter-card">
            <el-form :inline="true" :model="userQuery">
              <el-form-item label="关键词">
                <el-input v-model="userQuery.keyword" placeholder="搜索学号/昵称/邮箱" @keyup.enter="loadUserList" clearable style="width: 250px" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="userQuery.status" placeholder="全部" @change="loadUserList" clearable style="width: 130px">
                  <el-option label="正常" :value="0" />
                  <el-option label="已封禁" :value="-1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadUserList">查询</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          <el-card class="table-card">
            <el-table :data="userList" v-loading="userLoading" stripe>
              <el-table-column prop="id" label="ID" width="180" />
              <el-table-column label="用户" min-width="180">
                <template #default="{ row }">
                  <div style="display:flex; align-items:center; gap:8px;">
                    <el-avatar :size="32" :src="row.avatar" />
                    <div>
                      <div style="font-weight:600">{{ row.nickname || '未设置' }}</div>
                      <div style="font-size:12px; color:#999">{{ row.username }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
              <el-table-column prop="school" label="学校" width="150" show-overflow-tooltip />
              <el-table-column prop="createTime" label="注册时间" width="170">
                <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0" type="success">正常</el-tag>
                  <el-tag v-else-if="row.status === -1" type="danger">已封禁</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.status === 0" link type="danger" @click="handleBanUser(row)">封禁</el-button>
                  <el-button v-if="row.status === -1" link type="success" @click="handleUnbanUser(row)">解封</el-button>
                  <el-button
                    link
                    type="warning"
                    @click="handleBanAvatar(row)"
                    :disabled="!row.avatar || row.avatar === defaultAvatarUrl"
                  >封禁头像</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination">
              <el-pagination
                v-model:current-page="userQuery.page"
                v-model:page-size="userQuery.size"
                :page-sizes="[20, 50, 100]"
                :total="userTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="loadUserList"
                @current-change="loadUserList"
              />
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="举报处理" name="reports">
          <el-card class="filter-card">
            <el-form :inline="true" :model="reportQuery">
              <el-form-item label="状态">
                <el-select v-model="reportQuery.status" placeholder="全部" @change="loadReportList" clearable style="width: 130px">
                  <el-option label="待处理" :value="0" />
                  <el-option label="已通过" :value="1" />
                  <el-option label="已驳回" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadReportList">查询</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          <el-card class="table-card">
            <el-table :data="reportList" v-loading="reportLoading" stripe>
              <el-table-column prop="id" label="ID" width="180" />
              <el-table-column prop="targetType" label="类型" width="110" />
              <el-table-column prop="targetId" label="对象ID" width="180" show-overflow-tooltip />
              <el-table-column prop="reporterName" label="举报人" width="120" />
              <el-table-column prop="reason" label="举报原因" min-width="220" show-overflow-tooltip />
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0" type="warning">待处理</el-tag>
                  <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                  <el-tag v-else type="info">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="handleResult" label="处理说明" min-width="180" show-overflow-tooltip />
              <el-table-column label="举报时间" width="170">
                <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.status === 0" link type="success" @click="openReportDialog(row, true)">通过</el-button>
                  <el-button v-if="row.status === 0" link type="danger" @click="openReportDialog(row, false)">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination">
              <el-pagination
                v-model:current-page="reportQuery.page"
                v-model:page-size="reportQuery.size"
                :page-sizes="[20, 50, 100]"
                :total="reportTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="loadReportList"
                @current-change="loadReportList"
              />
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 笔记详情对话框（原有） -->
    <el-dialog v-model="detailVisible" title="笔记详情" width="700px">
      <el-descriptions :column="1" border v-if="currentNote">
        <el-descriptions-item label="标题">{{ currentNote.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryLabel(currentNote.category) }}</el-descriptions-item>
        <el-descriptions-item label="标签">{{ currentNote.tags || '无' }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ currentNote.summary }}</el-descriptions-item>
        <el-descriptions-item label="文件地址">
          <el-link :href="currentNote.fileUrl" target="_blank" type="primary">{{ currentNote.fileUrl }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="上传者">{{ currentNote.createBy }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ formatDate(currentNote.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentNote.viewCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="下载量">{{ currentNote.downloadCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="推荐状态">
          <el-tag v-if="currentNote.featured === 1" type="warning" effect="dark" size="small">热门推荐</el-tag>
          <el-tag v-if="currentNote.examFeatured === 1" type="danger" effect="dark" size="small" style="margin-left:4px">期末冲刺</el-tag>
          <span v-if="currentNote.featured !== 1 && currentNote.examFeatured !== 1" style="color:#999">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="currentNote.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentNote.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="currentNote.status === 2" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentNote && currentNote.status === 0" type="warning" @click="handleAiReview(currentNote); detailVisible = false">AI 建议</el-button>
        <el-button v-if="currentNote && currentNote.status === 0" type="success" @click="handleApprove(currentNote)">通过</el-button>
        <el-button v-if="currentNote && currentNote.status === 0" type="danger" @click="openRejectDialog(currentNote); detailVisible = false">拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="填写拒绝原因" width="500px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请详细填写拒绝原因，该原因会发送给上传用户"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectSubmitting">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- AI 建议弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="AI 审核建议" width="600px">
      <div v-if="aiSuggestion" style="white-space: pre-wrap; padding: 10px;">{{ aiSuggestion }}</div>
      <el-empty v-else description="暂无建议" />
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" :title="reportAccepted ? '通过举报' : '驳回举报'" width="500px">
      <el-input
        v-model="reportHandleResult"
        type="textarea"
        :rows="4"
        placeholder="填写处理说明"
      />
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button :type="reportAccepted ? 'success' : 'danger'" @click="confirmHandleReport" :loading="reportSubmitting">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Select, Close } from '@element-plus/icons-vue'
import {
  getAdminNoteList, approveNote, rejectNote, setNoteFeatured, setNoteExamFeatured,
  aiReviewNote, getNoteReviewLog
} from '@/api/note'
import { getAdminCommentList, deleteComment } from '@/api/comment'
import { getAdminUserList, banUser, unbanUser, banAvatar } from '@/api/user'
import { getAdminReportList, handleReport as handleReportApi } from '@/api/report'
import { getUserInfo as getUserInfoStorage, clearAuth } from '@/utils/auth'

const router = useRouter()
const activeTab = ref('notes')
const userInfo = ref(getUserInfoStorage() || {})

// 笔记审核
const loading = ref(false)
const noteList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentNote = ref(null)

// 拒绝相关
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectSubmitting = ref(false)
const pendingRejectNoteId = ref(null)

// AI 建议
const aiDialogVisible = ref(false)
const aiSuggestion = ref('')
const aiLoadingMap = reactive({})

const queryForm = reactive({
  page: 1, size: 10, status: null, category: '', title: ''
})

const pendingCount = computed(() => noteList.value.filter(n => n.status === 0).length)
const approvedCount = computed(() => noteList.value.filter(n => n.status === 1).length)
const rejectedCount = computed(() => noteList.value.filter(n => n.status === 2).length)

const filterNoteStatus = (status) => {
  queryForm.status = status
  queryForm.page = 1
  loadNoteList()
}

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
    await ElMessageBox.confirm('确认通过该笔记的审核？', '提示', { type: 'success' })
    const res = await approveNote(row.id)
    if (res && res.success) {
      ElMessage.success('审核通过')
      detailVisible.value = false
      loadNoteList()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// 打开拒绝对话框
const openRejectDialog = (row) => {
  pendingRejectNoteId.value = row.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  rejectSubmitting.value = true
  try {
    const res = await rejectNote(pendingRejectNoteId.value, rejectReason.value.trim())
    if (res && res.success) {
      ElMessage.success('已拒绝笔记并生成报告')
      rejectDialogVisible.value = false
      loadNoteList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    rejectSubmitting.value = false
  }
}

// AI 辅助审核
const handleAiReview = async (row) => {
  aiLoadingMap[row.id] = true
  try {
    const res = await aiReviewNote(row.id)
    if (res && res.success) {
      aiSuggestion.value = res.result || res.message || 'AI 未返回建议'
      aiDialogVisible.value = true
    } else {
      ElMessage.error(res?.message || 'AI 调用失败')
    }
  } catch (error) {
    ElMessage.error('AI 服务异常')
  } finally {
    aiLoadingMap[row.id] = false
  }
}

const toggleFeatured = async (row) => {
  const newVal = row.featured === 1 ? 0 : 1
  try {
    const res = await setNoteFeatured(row.id, newVal)
    if (res && res.success) {
      ElMessage.success(res.result || (newVal === 1 ? '已设为推荐' : '已取消推荐'))
      row.featured = newVal
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleExamFeatured = async (row) => {
  const newVal = row.examFeatured === 1 ? 0 : 1
  try {
    const res = await setNoteExamFeatured(row.id, newVal)
    if (res && res.success) {
      ElMessage.success(res.result || (newVal === 1 ? '已设为期末冲刺推荐' : '已取消期末冲刺推荐'))
      row.examFeatured = newVal
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 评论管理
const commentLoading = ref(false)
const commentList = ref([])
const commentTotal = ref(0)
const commentQuery = reactive({ page: 1, size: 20, keyword: '' })
const loadCommentList = async () => {
  commentLoading.value = true
  try {
    const res = await getAdminCommentList(commentQuery)
    if (res && res.success) {
      commentList.value = res.result.records || []
      commentTotal.value = res.result.total || 0
    }
  } catch (error) {
    ElMessage.error('加载评论列表失败')
  } finally {
    commentLoading.value = false
  }
}
const handleDeleteComment = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该评论吗？\n内容: "${row.content?.substring(0, 50)}..."`, '删除评论', { type: 'warning' })
    const res = await deleteComment(row.id)
    if (res && res.success) {
      ElMessage.success('删除成功')
      loadCommentList()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

// 用户管理
const userLoading = ref(false)
const userList = ref([])
const userTotal = ref(0)
const userQuery = reactive({ page: 1, size: 20, keyword: '', status: null })
const defaultAvatarUrl = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const loadUserList = async () => {
  userLoading.value = true
  try {
    const res = await getAdminUserList(userQuery)
    if (res && res.success) {
      userList.value = res.result.records || []
      userTotal.value = res.result.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    userLoading.value = false
  }
}
const handleBanUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${row.nickname || row.username}" 吗？封禁后该用户将无法登录。`, '封禁用户', { type: 'warning' })
    const res = await banUser(row.id)
    if (res && res.success) {
      ElMessage.success('用户已封禁')
      loadUserList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}
const handleUnbanUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解封用户 "${row.nickname || row.username}" 吗？`, '解封用户', { type: 'success' })
    const res = await unbanUser(row.id)
    if (res && res.success) {
      ElMessage.success('用户已解封')
      loadUserList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}
const handleBanAvatar = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${row.nickname || row.username}" 的头像吗？头像将被重置为默认头像并向用户发送通知。`, '封禁头像', { type: 'warning' })
    const res = await banAvatar(row.id)
    if (res && res.success) {
      ElMessage.success('头像已封禁')
      loadUserList()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// 举报处理
const reportLoading = ref(false)
const reportList = ref([])
const reportTotal = ref(0)
const reportQuery = reactive({ page: 1, size: 20, status: 0 })
const reportDialogVisible = ref(false)
const reportAccepted = ref(false)
const reportSubmitting = ref(false)
const currentReport = ref(null)
const reportHandleResult = ref('')

const loadReportList = async () => {
  reportLoading.value = true
  try {
    const res = await getAdminReportList(reportQuery)
    if (res && res.success) {
      reportList.value = res.result.records || []
      reportTotal.value = res.result.total || 0
    }
  } catch (error) {
    ElMessage.error('加载举报列表失败')
  } finally {
    reportLoading.value = false
  }
}

const openReportDialog = (row, accepted) => {
  currentReport.value = row
  reportAccepted.value = accepted
  reportHandleResult.value = accepted ? '举报属实，相关内容已处理' : '举报依据不足，暂不处理'
  reportDialogVisible.value = true
}

const confirmHandleReport = async () => {
  if (!currentReport.value) return
  reportSubmitting.value = true
  try {
    const res = await handleReportApi(currentReport.value.id, {
      accepted: reportAccepted.value,
      handleResult: reportHandleResult.value
    })
    if (res && res.success) {
      ElMessage.success('处理成功')
      reportDialogVisible.value = false
      loadReportList()
    } else {
      ElMessage.error(res?.message || '处理失败')
    }
  } catch (error) {
    ElMessage.error('处理失败')
  } finally {
    reportSubmitting.value = false
  }
}

// 通用
const handleCommand = (command) => {
  if (command === 'logout') {
    clearAuth()
    ElMessage.success('已退出登录')
    router.push('/admin/login')
  }
}
const getCategoryLabel = (category) => {
  const map = { 'CS': '计算机科学', 'SE': '软件工程', 'Math': '数学', 'GE': '通识课' }
  return map[category] || category
}
const getCategoryType = (category) => {
  const map = { 'CS': 'primary', 'SE': 'success', 'Math': 'warning', 'GE': 'info' }
  return map[category] || ''
}
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

watch(activeTab, (val) => {
  if (val === 'comments' && commentList.value.length === 0) loadCommentList()
  if (val === 'users' && userList.value.length === 0) loadUserList()
  if (val === 'reports' && reportList.value.length === 0) loadReportList()
})

onMounted(() => {
  loadNoteList()
})
</script>

<style scoped>
/* 保持原有样式不变 */
.review-container { min-height: 100vh; background: #f2f3f5; }
.header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1); position: sticky; top:0; z-index:100; }
.header-content { max-width:1400px; margin:0 auto; padding:0 20px; height:60px; display:flex; justify-content:space-between; align-items:center; }
.logo-section { display:flex; align-items:center; gap:15px; }
.logo { height:40px; }
.title { font-size:18px; font-weight:600; color:#333; }
.user-section { display:flex; align-items:center; }
.user-info { display:flex; align-items:center; gap:10px; cursor:pointer; }
.username { font-size:14px; color:#333; }
.main-content { max-width:1400px; margin:20px auto; padding:0 20px; }
.admin-tabs { border-radius:8px; }
.stats-cards { display:grid; grid-template-columns: repeat(auto-fit, minmax(250px,1fr)); gap:20px; margin-bottom:20px; }
.stat-card { cursor:pointer; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-5px); }
.stat-content { display:flex; align-items:center; gap:20px; }
.stat-icon { width:60px; height:60px; border-radius:12px; display:flex; align-items:center; justify-content:center; color:#fff; }
.stat-icon.pending { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.approved { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.rejected { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-info { flex:1; }
.stat-value { font-size:28px; font-weight:bold; color:#333; }
.stat-label { font-size:14px; color:#999; margin-top:5px; }
.filter-card { margin-bottom:20px; }
.table-card { margin-bottom:20px; }
.pagination { margin-top:20px; display:flex; justify-content:flex-end; }
.mr-4 { margin-right:4px; }
</style>
