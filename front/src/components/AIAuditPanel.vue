<template>
  <div class="ai-audit-panel">
    <el-card shadow="hover" :class="['audit-card', riskLevelClass]">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :class="riskLevelIconClass">
              <component :is="riskLevelIcon" />
            </el-icon>
            <span class="title">AI审核建议</span>
            <el-tag :type="riskLevelTagType" size="small">
              {{ riskLevelText }}
            </el-tag>
          </div>
          <div class="header-right">
            <el-tooltip content="置信度" placement="top">
              <span class="confidence">
                可信度 {{ (confidence * 100).toFixed(0) }}%
              </span>
            </el-tooltip>
            <el-button 
              type="primary" 
              link 
              :icon="Refresh"
              @click="refreshAudit"
              :loading="loading"
            >
              重新审核
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 风险原因 -->
      <div class="risk-reason" v-if="riskReason">
        <el-alert
          :title="riskReason"
          :type="riskLevelAlertType"
          :closable="false"
          show-icon
        />
      </div>
      
      <!-- 详细评分 -->
      <div class="score-details" v-if="details">
        <div class="score-item" v-for="(score, key) in details" :key="key">
          <span class="score-label">{{ getScoreLabel(key) }}</span>
          <el-progress 
            :percentage="score" 
            :color="getScoreColor(score)"
            :stroke-width="8"
            :show-text="false"
          />
          <span class="score-value">{{ score }}分</span>
        </div>
      </div>
      
      <!-- 关键词 -->
      <div class="keywords" v-if="keywords && keywords.length">
        <span class="keywords-label">敏感词：</span>
        <el-tag
          v-for="(keyword, index) in keywords"
          :key="index"
          size="small"
          type="danger"
          effect="plain"
          class="keyword-tag"
        >
          {{ keyword }}
        </el-tag>
      </div>
      
      <!-- 操作按钮 -->
      <div class="audit-actions">
        <el-button 
          type="success" 
          :icon="Check" 
          :disabled="!canApprove"
          @click="handleApprove"
        >
          采纳建议通过
        </el-button>
        <el-button 
          type="danger" 
          :icon="Close" 
          :disabled="!canReject"
          @click="handleReject"
        >
          采纳建议拒绝
        </el-button>
        <el-button 
          type="info" 
          :icon="View" 
          @click="showDetail"
        >
          查看详细
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, View, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  noteId: {
    type: String,
    required: true
  },
  auditResult: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['approve', 'reject', 'refresh', 'view-detail'])

const loading = ref(false)
const localResult = ref(props.auditResult)

// 风险等级映射
const riskLevelMap = {
  'HIGH': { text: '高风险', tag: 'danger', icon: 'WarningFilled', class: 'high-risk', alert: 'error' },
  'MEDIUM': { text: '中风险', tag: 'warning', icon: 'Warning', class: 'medium-risk', alert: 'warning' },
  'LOW': { text: '低风险', tag: 'success', icon: 'SuccessFilled', class: 'low-risk', alert: 'success' },
  'ERROR': { text: '审核失败', tag: 'info', icon: 'CircleCloseFilled', class: 'error-risk', alert: 'info' }
}

const riskLevel = computed(() => localResult.value?.riskLevel || 'MEDIUM')
const riskLevelText = computed(() => riskLevelMap[riskLevel.value]?.text || '未知')
const riskLevelTagType = computed(() => riskLevelMap[riskLevel.value]?.tag || 'info')
const riskLevelIcon = computed(() => riskLevelMap[riskLevel.value]?.icon || 'Warning')
const riskLevelIconClass = computed(() => riskLevelMap[riskLevel.value]?.class || '')
const riskLevelAlertType = computed(() => riskLevelMap[riskLevel.value]?.alert || 'info')
const riskLevelClass = computed(() => riskLevelMap[riskLevel.value]?.class || '')

const confidence = computed(() => localResult.value?.confidence || 0)
const riskReason = computed(() => localResult.value?.riskReason || '')
const details = computed(() => localResult.value?.details || null)
const keywords = computed(() => localResult.value?.keywords || [])
const suggestion = computed(() => localResult.value?.suggestion || 'REVIEW')

const canApprove = computed(() => {
  return suggestion.value === 'PASS' || riskLevel.value === 'LOW'
})

const canReject = computed(() => {
  return suggestion.value === 'REJECT' || riskLevel.value === 'HIGH'
})

// 刷新AI审核
const refreshAudit = async () => {
  loading.value = true
  try {
    emit('refresh')
  } finally {
    loading.value = false
  }
}

// 采纳建议通过
const handleApprove = () => {
  ElMessageBox.confirm('确定采纳AI建议，通过该笔记吗？', '提示', {
    type: 'success'
  }).then(() => {
    emit('approve', { 
      noteId: props.noteId, 
      aiResult: localResult.value 
    })
  }).catch(() => {})
}

// 采纳建议拒绝
const handleReject = () => {
  ElMessageBox.confirm('确定采纳AI建议，拒绝该笔记吗？', '提示', {
    type: 'warning'
  }).then(() => {
    emit('reject', { 
      noteId: props.noteId, 
      aiResult: localResult.value 
    })
  }).catch(() => {})
}

// 查看详情
const showDetail = () => {
  emit('view-detail', localResult.value)
}

// 获取评分标签
const getScoreLabel = (key) => {
  const map = {
    pornScore: '色情内容',
    politicalScore: '政治敏感',
    adScore: '广告营销',
    violenceScore: '暴力恐怖',
    qualityScore: '内容质量'
  }
  return map[key] || key
}

// 获取评分颜色
const getScoreColor = (score) => {
  if (score >= 70) return '#f56c6c'
  if (score >= 40) return '#e6a23c'
  return '#67c23a'
}
</script>

<style scoped>
.ai-audit-panel {
  margin: 20px 0;
}

.audit-card {
  border-left: 4px solid #909399;
}

.audit-card.high-risk {
  border-left-color: #f56c6c;
}

.audit-card.medium-risk {
  border-left-color: #e6a23c;
}

.audit-card.low-risk {
  border-left-color: #67c23a;
}

.audit-card.error-risk {
  border-left-color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left .title {
  font-weight: bold;
  font-size: 16px;
}

.confidence {
  font-size: 12px;
  color: #909399;
  margin-right: 12px;
}

.risk-reason {
  margin-bottom: 16px;
}

.score-details {
  margin: 16px 0;
}

.score-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.score-label {
  width: 70px;
  font-size: 13px;
  color: #606266;
}

.score-value {
  width: 45px;
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.el-progress {
  flex: 1;
  margin: 0 8px;
}

.keywords {
  margin: 16px 0;
  padding: 12px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.keywords-label {
  font-size: 13px;
  color: #f56c6c;
  margin-right: 8px;
}

.keyword-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.audit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
