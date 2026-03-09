<template>
  <div class="ai-audit-sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
    <!-- 折叠/展开触发器 -->
    <div class="sidebar-trigger" @click="toggleSidebar">
      <el-icon v-if="isCollapsed"><ChatLineRound /></el-icon>
      <span v-else>🤖 AI审核</span>
    </div>

    <!-- 侧边栏内容 -->
    <div class="sidebar-content" v-show="!isCollapsed">
      <div class="sidebar-header">
        <h3><el-icon><ChatLineRound /></el-icon> AI智能审核</h3>
        <p class="subtitle">从下方列表选择要审核的笔记</p>
      </div>

      <!-- 从页面提取的笔记列表 -->
      <div class="page-notes-list" v-if="pageNotes.length > 0">
        <el-scrollbar max-height="250px">
          <div class="note-items">
            <div 
              v-for="note in pageNotes" 
              :key="note.id"
              class="note-item"
              :class="{ 'selected': selectedNoteId === note.id }"
              @click="handleNoteSelect(note)"
            >
              <div class="note-title">{{ note.title }}</div>
              <div class="note-meta">
                <span>{{ note.author }}</span>
                <el-tag size="small" :type="getStatusType(note.status)" effect="plain">
                  {{ getStatusText(note.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 没有笔记时的提示 -->
      <div v-else class="no-notes">
        <el-empty description="当前页面没有笔记数据" />
        <el-button type="primary" @click="extractNotesFromPage" size="small">
          重新提取
        </el-button>
      </div>

      <!-- 当前选中的笔记详情 -->
      <div class="selected-note-detail" v-if="currentNote.id">
        <el-divider />
        <div class="detail-header">
          <span class="label">当前选中：</span>
          <span class="value">{{ currentNote.title }}</span>
        </div>
      </div>

      <!-- 控制区 -->
      <div class="control-section" v-if="currentNote.id">
        <el-button 
          type="primary" 
          :loading="isAnalyzing" 
          @click="startAnalysis"
          class="analyze-btn"
          size="large"
        >
          <template #icon>
            <el-icon><Search /></el-icon>
          </template>
          {{ isAnalyzing ? 'AI分析中...' : '开始AI深度分析' }}
        </el-button>
      </div>

      <!-- 审核报告 -->
      <div class="audit-report" v-if="auditResult">
        <!-- 评分卡片 -->
        <div class="score-card" :class="auditResult.riskLevel">
          <div class="score-main">
            <div class="score-number">{{ auditResult.score }}</div>
            <div class="score-info">
              <div class="score-label">综合评分</div>
              <div class="risk-tag">{{ auditResult.riskLevelText }}</div>
            </div>
          </div>
          <div class="score-desc">{{ auditResult.summary }}</div>
        </div>

        <!-- 详细分析维度 -->
        <div class="dimensions-section" v-if="auditResult.dimensions">
          <h4>详细分析维度</h4>
          <div class="dimension-list">
            <div v-for="dim in auditResult.dimensions" :key="dim.name" class="dimension-item">
              <div class="dimension-header">
                <span class="dimension-name">{{ dim.name }}</span>
                <span class="dimension-score" :style="{ color: getScoreColor(dim.score) }">
                  {{ dim.score }}分
                </span>
              </div>
              <el-progress 
                :percentage="dim.score" 
                :color="getScoreColor(dim.score)"
                :stroke-width="6"
                :show-text="false"
              />
              <div class="dimension-desc">{{ dim.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 问题分析 -->
        <div class="analysis-section" v-if="auditResult.issues?.length">
          <h4>⚠️ 问题分析</h4>
          <ul class="issue-list">
            <li v-for="(issue, idx) in auditResult.issues" :key="idx">
              <span class="issue-icon">•</span>
              <span class="issue-text">{{ issue }}</span>
            </li>
          </ul>
        </div>

        <!-- 改进建议 -->
        <div class="analysis-section" v-if="auditResult.suggestions?.length">
          <h4>💡 改进建议</h4>
          <ul class="suggestion-list">
            <li v-for="(suggestion, idx) in auditResult.suggestions" :key="idx">
              <span class="suggestion-icon">✓</span>
              <span class="suggestion-text">{{ suggestion }}</span>
            </li>
          </ul>
        </div>

        <!-- AI详细评语 -->
        <div class="comment-section" v-if="auditResult.detailedComment">
          <h4>📝 AI详细评语</h4>
          <div class="comment-content">
            {{ auditResult.detailedComment }}
          </div>
        </div>

        <!-- 关键词检测 -->
        <div class="keywords-section" v-if="auditResult.keywords?.length">
          <h4>🔍 检测到关键词</h4>
          <div class="keyword-tags">
            <el-tag 
              v-for="kw in auditResult.keywords" 
              :key="kw.word"
              :type="kw.type"
              size="small"
              effect="plain"
              class="keyword-tag"
            >
              {{ kw.word }}
            </el-tag>
          </div>
        </div>

        <!-- 审核时间 -->
        <div class="audit-footer">
          <span class="audit-time">审核时间：{{ auditResult.auditTime }}</span>
          <span class="audit-source">来源：{{ auditResult.source }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, ChatLineRound } from '@element-plus/icons-vue'

// 响应式数据
const isCollapsed = ref(false)
const isAnalyzing = ref(false)
const pageNotes = ref([])
const selectedNoteId = ref('')
const currentNote = ref({})
const auditResult = ref(null)

// 切换折叠
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 从页面提取笔记数据
const extractNotesFromPage = () => {
  const notes = []
  const rows = document.querySelectorAll('.el-table__row')
  
  rows.forEach((row, index) => {
    try {
      const cells = row.querySelectorAll('.el-table__cell')
      if (cells.length >= 7) {
        const statusText = cells[6]?.textContent?.trim() || ''
        
        notes.push({
          id: cells[0]?.textContent?.trim() || `row-${index}`,
          title: cells[1]?.textContent?.trim() || '无标题',
          category: cells[2]?.textContent?.trim() || '',
          summary: cells[3]?.textContent?.trim() || '',
          author: cells[4]?.textContent?.trim() || '匿名',
          uploadTime: cells[5]?.textContent?.trim() || '',
          status: getStatusFromText(statusText)
        })
      }
    } catch (error) {
      console.error('提取行数据失败:', error)
    }
  })
  
  pageNotes.value = notes
}

// 从文本获取状态值
const getStatusFromText = (text) => {
  if (text?.includes('待审核')) return 0
  if (text?.includes('已通过')) return 1
  if (text?.includes('已拒绝')) return 2
  return 0
}

// 获取状态类型
const getStatusType = (status) => {
  return ['warning', 'success', 'danger'][status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return ['待审核', '已通过', '已拒绝'][status] || '未知'
}

// 处理笔记选择
const handleNoteSelect = (note) => {
  selectedNoteId.value = note.id
  currentNote.value = note
  auditResult.value = null
}

// 获取评分颜色
const getScoreColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  if (score >= 40) return '#f56c6c'
  return '#909399'
}

// 生成多样化的审核结果
const generateAuditResult = (note) => {
  const title = note.title?.toLowerCase() || ''
  const summary = note.summary?.toLowerCase() || ''
  const content = title + ' ' + summary
  
  // 风险关键词库
  const riskKeywords = {
    high: [
      { word: '答案', reason: '包含考试答案，可能涉及学术不端', type: 'danger' },
      { word: '作弊', reason: '涉及作弊相关内容', type: 'danger' },
      { word: '代考', reason: '涉及代考服务，严重违规', type: 'danger' },
      { word: '替考', reason: '涉及替考行为', type: 'danger' },
      { word: '买卖', reason: '涉及交易行为', type: 'danger' },
      { word: '微信', reason: '包含个人联系方式', type: 'danger' },
      { word: 'qq', reason: '包含个人联系方式', type: 'danger' },
      { word: '政治', reason: '涉及政治敏感内容', type: 'danger' },
      { word: '色情', reason: '可能包含低俗内容', type: 'danger' },
      { word: '赌博', reason: '涉及赌博内容', type: 'danger' }
    ],
    medium: [
      { word: '兼职', reason: '包含兼职信息，可能涉及广告', type: 'warning' },
      { word: '赚钱', reason: '包含赚钱相关内容', type: 'warning' },
      { word: '广告', reason: '包含广告内容', type: 'warning' },
      { word: '推广', reason: '包含推广信息', type: 'warning' },
      { word: '优惠', reason: '包含优惠信息', type: 'warning' },
      { word: '促销', reason: '包含促销内容', type: 'warning' },
      { word: '免费', reason: '包含免费领取等诱导性内容', type: 'warning' }
    ],
    low: [
      { word: '笔记', reason: '正常学习笔记', type: '' },
      { word: '总结', reason: '学习总结', type: '' },
      { word: '复习', reason: '复习资料', type: '' },
      { word: '重点', reason: '重点整理', type: '' }
    ]
  }
  
  // 随机种子，基于标题生成不同的结果
  const seed = title.length + (note.author?.length || 0) + (note.summary?.length || 0)
  const random = (min, max) => {
    const fakeRandom = ((seed * 9301 + 49297) % 233280) / 233280
    return min + fakeRandom * (max - min)
  }
  
  let detectedKeywords = []
  let issues = []
  let suggestions = []
  let riskLevel = 'low'
  let riskLevelText = '低风险'
  let baseScore = Math.floor(random(70, 95))
  
  // 检测高风险关键词
  for (const kw of riskKeywords.high) {
    if (content.includes(kw.word)) {
      detectedKeywords.push({ word: kw.word, type: 'danger' })
      issues.push(`检测到高风险内容：${kw.reason}`)
      riskLevel = 'high'
      riskLevelText = '高风险'
      baseScore = Math.max(10, baseScore - random(30, 50))
    }
  }
  
  // 检测中风险关键词
  if (riskLevel !== 'high') {
    for (const kw of riskKeywords.medium) {
      if (content.includes(kw.word)) {
        detectedKeywords.push({ word: kw.word, type: 'warning' })
        issues.push(`检测到中风险内容：${kw.reason}`)
        riskLevel = 'medium'
        riskLevelText = '中风险'
        baseScore = Math.max(30, baseScore - random(15, 30))
      }
    }
  }
  
  // 检测低风险关键词（正常内容）
  for (const kw of riskKeywords.low) {
    if (content.includes(kw.word)) {
      detectedKeywords.push({ word: kw.word, type: 'success' })
    }
  }
  
  // 根据标题长度生成问题 - 稍微增大占比
  if (note.title.length < 5) {
    issues.push('标题过短，无法准确反映内容')
    suggestions.push('建议使用更具描述性的标题，长度在10-30字之间')
    baseScore -= random(8, 15) // 原来是5-10，增大到8-15
  } else if (note.title.length > 50) {
    issues.push('标题过长，可能影响阅读体验')
    suggestions.push('建议精简标题，突出核心内容')
    baseScore -= random(5, 12) // 原来是3-8，增大到5-12
  } else if (note.title.length >= 10 && note.title.length <= 30) {
    // 标题长度合适，加分
    baseScore += random(2, 5)
  }
  
  // 根据简介生成问题 - 稍微增大占比
  if (!note.summary || note.summary.length < 10) {
    issues.push('简介过短，无法提供足够的信息')
    suggestions.push('建议补充100字左右的简介，说明笔记的主要内容和价值')
    baseScore -= random(10, 18) // 原来是8-15，增大到10-18
  } else if (note.summary.length < 50) {
    issues.push('简介内容较少，建议丰富')
    suggestions.push('简介可以再丰富一些，让读者更好地了解内容')
    baseScore -= random(4, 10) // 原来是2-5，增大到4-10
  } else if (note.summary.length > 500) {
    issues.push('简介过长，建议精简')
    suggestions.push('简介过长，建议精简到300字以内')
    baseScore -= random(4, 10) // 原来是2-5，增大到4-10
  } else if (note.summary.length >= 100 && note.summary.length <= 300) {
    // 简介长度合适，加分
    baseScore += random(2, 5)
  }
  
  // 根据分类生成建议
  if (note.category) {
    const category = note.category.toLowerCase()
    if (category.includes('cs') || category.includes('计算机')) {
      if (!content.includes('代码') && !content.includes('程序')) {
        suggestions.push('计算机科学类笔记建议包含代码示例')
      }
      if (!content.includes('算法')) {
        suggestions.push('可以补充算法说明，提高笔记价值')
      }
    } else if (category.includes('math') || category.includes('数学')) {
      if (!content.includes('公式')) {
        suggestions.push('数学类笔记建议包含公式推导')
      }
      if (!content.includes('例题')) {
        suggestions.push('添加例题解析会更有帮助')
      }
    } else if (category.includes('ge') || category.includes('通识')) {
      if (!content.includes('案例')) {
        suggestions.push('通识类笔记可以结合实际案例分析')
      }
    }
  }
  
  // 如果没有发现问题，添加正面评价
  if (issues.length === 0) {
    const positiveComments = [
      '内容完整，结构清晰',
      '表达准确，逻辑性强',
      '重点突出，易于理解',
      '格式规范，可读性强',
      '内容详实，覆盖全面'
    ]
    issues.push(`✅ ${positiveComments[Math.floor(random(0, positiveComments.length))]}`)
  }
  
  // 确保至少有一条建议
  if (suggestions.length === 0) {
    const defaultSuggestions = [
      '可以考虑添加更多实例说明',
      '建议补充参考文献或资料来源',
      '可以适当增加图表辅助理解',
      '建议添加关键词标签方便检索'
    ]
    suggestions.push(defaultSuggestions[Math.floor(random(0, defaultSuggestions.length))])
  }
  
  // 计算最终分数（1-100），确保不超过100
  let finalScore = Math.max(1, Math.min(99, Math.floor(baseScore)))
  
  // 生成详细评语
  const detailedComments = {
    high: [
      `经AI深度分析，该笔记存在明显的违规风险。检测到多个敏感关键词（${detectedKeywords.filter(k => k.type === 'danger').map(k => k.word).join('、')}），内容可能违反平台规定。建议拒绝通过，并通知用户修改。`,
      `内容审核未通过。笔记中包含${detectedKeywords.filter(k => k.type === 'danger').map(k => k.word).join('、')}等违规内容，存在法律风险。建议立即拒绝并记录。`,
      `高风险内容检测：笔记涉及敏感话题，可能引发争议。建议人工复核并谨慎处理。`
    ],
    medium: [
      `内容基本合格，但存在一些潜在风险。检测到疑似广告或营销内容（${detectedKeywords.filter(k => k.type === 'warning').map(k => k.word).join('、')}），建议人工重点复核。`,
      `中等风险笔记。内容质量尚可，但部分表述可能引起误解。建议联系用户澄清后决定是否通过。`,
      `笔记需要进一步优化。${issues.filter(i => !i.includes('✅')).join('、')}。建议修改后重新提交。`
    ],
    low: [
      `优质内容！笔记结构完整，表达清晰，对学习者很有帮助。各项指标均表现良好，推荐通过并给予曝光。`,
      `内容质量优秀。${issues.filter(i => i.includes('✅')).join('、')}。符合平台要求，建议通过。`,
      `笔记专业详实，分类准确，可读性强。${suggestions[0] ? '建议' + suggestions[0] : '可直接通过'}。`
    ]
  }
  
  const commentList = detailedComments[riskLevel] || detailedComments.low
  const detailedComment = commentList[Math.floor(random(0, commentList.length))]
  
  // 生成各维度评分 - 确保总分和相关度
  const dimensions = [
    {
      name: '内容完整性',
      score: Math.floor(random(65, 98)),
      desc: getDimensionDesc('完整性', finalScore)
    },
    {
      name: '专业准确性',
      score: Math.floor(random(60, 95)),
      desc: getDimensionDesc('专业性', finalScore)
    },
    {
      name: '合规安全性',
      score: riskLevel === 'high' ? Math.floor(random(20, 45)) : (riskLevel === 'medium' ? Math.floor(random(45, 70)) : Math.floor(random(80, 100))),
      desc: getDimensionDesc('合规性', finalScore, riskLevel)
    },
    {
      name: '可读性',
      score: Math.floor(random(70, 95)),
      desc: getDimensionDesc('可读性', finalScore)
    },
    {
      name: '分类匹配度',
      score: Math.floor(random(65, 92)),
      desc: getDimensionDesc('分类', finalScore)
    }
  ]
  
  return {
    score: finalScore,
    riskLevel: riskLevel,
    riskLevelText: riskLevelText,
    summary: getSummaryByRisk(riskLevel, finalScore),
    issues: [...new Set(issues)], // 去重
    suggestions: [...new Set(suggestions)], // 去重
    detailedComment: detailedComment,
    dimensions: dimensions,
    keywords: detectedKeywords.slice(0, 8), // 最多显示8个
    auditTime: new Date().toLocaleString('zh-CN'),
    source: 'AI深度分析引擎 v2.0'
  }
}

// 获取摘要
const getSummaryByRisk = (riskLevel, score) => {
  const summaries = {
    high: [
      `存在严重违规风险，建议拒绝（综合评分${score}分）`,
      `内容不合规，需立即处理（评分${score}分）`,
      `高风险内容，不予通过（评分${score}分）`
    ],
    medium: [
      `需要人工复核，存在潜在风险（评分${score}分）`,
      `内容基本合格，建议重点审核（评分${score}分）`,
      `中等风险，建议联系用户确认（评分${score}分）`
    ],
    low: [
      `内容优质，推荐通过（评分${score}分）`,
      `符合平台要求，建议直接通过（评分${score}分）`,
      `质量良好，可发布（评分${score}分）`
    ]
  }
  const list = summaries[riskLevel] || summaries.low
  return list[Math.floor(Math.random() * list.length)]
}

// 获取维度描述
const getDimensionDesc = (type, score, riskLevel) => {
  if (type === '合规性' && riskLevel === 'high') {
    return '检测到违规内容，需要处理'
  }
  if (score >= 85) return '表现优秀，继续保持'
  if (score >= 70) return '良好，略有提升空间'
  if (score >= 60) return '合格，可以优化'
  return '需要改进'
}

// 开始AI分析
const startAnalysis = async () => {
  if (!currentNote.value.id) {
    ElMessage.warning('请先选择一篇笔记')
    return
  }
  
  isAnalyzing.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟分析时间
    auditResult.value = generateAuditResult(currentNote.value)
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    isAnalyzing.value = false
  }
}

// 监听表格变化
onMounted(() => {
  setTimeout(extractNotesFromPage, 500)
  
  const observer = new MutationObserver(extractNotesFromPage)
  const tableWrapper = document.querySelector('.el-table__body-wrapper')
  if (tableWrapper) {
    observer.observe(tableWrapper, {
      childList: true,
      subtree: true
    })
  }
})
</script>

<style scoped>
.ai-audit-sidebar {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 9999;
  display: flex;
  transition: all 0.3s ease;
  max-height: 90vh;
}

.sidebar-trigger {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 8px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  cursor: pointer;
  border-radius: 8px 0 0 8px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  box-shadow: -2px 0 10px rgba(0,0,0,0.1);
}

.sidebar-content {
  width: 400px;
  background: white;
  border: 1px solid #e4e7ed;
  border-right: none;
  border-radius: 12px 0 0 12px;
  box-shadow: -4px 0 20px rgba(0,0,0,0.08);
  overflow-y: auto;
  max-height: 90vh;
  padding: 20px;
}

.sidebar-collapsed .sidebar-content {
  display: none;
}

.sidebar-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f2f5;
}

.sidebar-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.4rem;
}

.subtitle {
  color: #7f8c8d;
  margin: 8px 0 0 0;
  font-size: 0.9rem;
}

.page-notes-list {
  margin-bottom: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px;
}

.note-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.note-item {
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.note-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.note-item.selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.note-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.selected-note-detail {
  margin: 15px 0;
}

.detail-header {
  padding: 8px 12px;
  background: #f0f9eb;
  border-radius: 6px;
  color: #67c23a;
}

.control-section {
  margin: 20px 0;
}

.analyze-btn {
  width: 100%;
}

.audit-report {
  margin-top: 20px;
}

.score-card {
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;
}

.score-card.high {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.score-card.medium {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.score-card.low {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.score-main {
  display: flex;
  align-items: flex-end;
  gap: 15px;
  margin-bottom: 10px;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
}

.score-info {
  flex: 1;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.risk-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255,255,255,0.2);
  border-radius: 16px;
  font-size: 12px;
}

.score-desc {
  font-size: 14px;
  opacity: 0.95;
  padding-top: 10px;
  border-top: 1px solid rgba(255,255,255,0.2);
}

.dimensions-section {
  margin-bottom: 20px;
}

.dimensions-section h4,
.analysis-section h4,
.comment-section h4,
.keywords-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.dimension-item {
  margin-bottom: 15px;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 13px;
}

.dimension-name {
  color: #606266;
}

.dimension-score {
  font-weight: 500;
}

.dimension-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.analysis-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.issue-list,
.suggestion-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.issue-list li,
.suggestion-list li {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  line-height: 1.5;
}

.issue-icon {
  color: #f56c6c;
  font-weight: bold;
}

.suggestion-icon {
  color: #67c23a;
  font-weight: bold;
}

.issue-text,
.suggestion-text {
  flex: 1;
  color: #303133;
}

.comment-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f0f7ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}

.keywords-section {
  margin-bottom: 20px;
}

.keyword-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  margin-right: 0;
}

.audit-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

.no-notes {
  text-align: center;
  padding: 20px 0;
}

/* 滚动条样式 */
.sidebar-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
</style>
