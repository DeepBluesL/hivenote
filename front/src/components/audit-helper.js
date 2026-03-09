import { ElMessage } from 'element-plus'

export default class AuditHelper {

  /**
   * 从页面中提取当前选中的笔记信息
   */
  static extractNoteInfo() {
    // 尝试从表格中获取选中的笔记
    const selectedRow = document.querySelector('.el-table__row.current-row')
    if (selectedRow) {
      const cells = selectedRow.querySelectorAll('.el-table__cell')
      return {
        title: cells[1]?.textContent?.trim() || '无标题',
        content: cells[2]?.textContent?.trim() || '无内容',
        author: cells[3]?.textContent?.trim() || '匿名',
        uploadTime: cells[4]?.textContent?.trim() || '未知时间'
      }
    }

    // 从详情面板获取
    const detailPanel = document.querySelector('.note-detail, .detail-panel, .el-drawer__body')
    if (detailPanel) {
      const titleEl = detailPanel.querySelector('.title, h3, h4')
      const contentEl = detailPanel.querySelector('.content, .description, .el-form-item__content')
      return {
        title: titleEl?.textContent?.trim() || '无标题',
        content: contentEl?.textContent?.trim() || '无内容预览',
        author: detailPanel.querySelector('.author, .username')?.textContent?.trim() || '匿名',
        uploadTime: detailPanel.querySelector('.time, .upload-time')?.textContent?.trim() || '未知时间'
      }
    }

    // 取第一行笔记
    const firstNoteRow = document.querySelector('.el-table__row')
    if (firstNoteRow) {
      const cells = firstNoteRow.querySelectorAll('.el-table__cell')
      return {
        title: cells[1]?.textContent?.trim() || '高等数学期末考试重点',
        content: cells[2]?.textContent?.trim() || '这是高等数学的期末考试重点内容，包括微积分、线性代数等章节的总结。',
        author: cells[3]?.textContent?.trim() || '张三',
        uploadTime: cells[4]?.textContent?.trim() || '2024-03-09'
      }
    }

    // 兜底方案 - 返回模拟数据
    const mockNotes = [
      {
        title: '高等数学期末考试重点',
        content: '第一章：函数与极限\n第二章：导数与微分\n第三章：不定积分\n第四章：定积分及其应用',
        author: '张三',
        uploadTime: '2024-03-09'
      },
      {
        title: '大学英语四级真题解析',
        content: '2023年12月四级真题完整解析，包含听力原文、阅读理解和作文范文。',
        author: '李四',
        uploadTime: '2024-03-08'
      },
      {
        title: '数据结构与算法笔记',
        content: '二叉树遍历、排序算法、动态规划、图论等核心知识点总结。',
        author: '王五',
        uploadTime: '2024-03-07'
      },
      {
        title: '考研政治必背知识点',
        content: '马克思主义基本原理、毛泽东思想和中国特色社会主义理论体系概要。',
        author: '赵六',
        uploadTime: '2024-03-06'
      },
      {
        title: 'Python从入门到精通',
        content: '基础语法、函数、面向对象、异常处理、文件操作、网络编程。',
        author: '钱七',
        uploadTime: '2024-03-05'
      }
    ]
    
    const randomIndex = Math.floor(Math.random() * mockNotes.length)
    return mockNotes[randomIndex]
  }

  /**
   * 生成模拟的AI审核报告（不调用真实API）
   */
  static simulateAudit(noteInfo) {
    // 根据标题内容生成不同的审核结果
    const title = noteInfo.title.toLowerCase()
    const content = noteInfo.content.toLowerCase()
    
    // 高风险关键词
    const highRiskKeywords = ['答案', '作弊', '代考', '替考', '买卖', '微信', 'qq', '政治', '领导人']
    // 中风险关键词
    const mediumRiskKeywords = ['兼职', '赚钱', '广告', '推广', '优惠', '促销']
    
    let score = 8
    let riskLevel = 'low'
    let issues = []
    let suggestions = []
    
    // 检查高风险内容
    for (const keyword of highRiskKeywords) {
      if (title.includes(keyword) || content.includes(keyword)) {
        riskLevel = 'high'
        score = Math.min(score, 4)
        issues.push(`包含敏感词: "${keyword}"`)
      }
    }
    
    // 检查中风险内容
    for (const keyword of mediumRiskKeywords) {
      if (title.includes(keyword) || content.includes(keyword)) {
        if (riskLevel !== 'high') riskLevel = 'medium'
        score = Math.min(score, 6)
        issues.push(`包含疑似广告词: "${keyword}"`)
      }
    }
    
    // 根据分类添加特定检查
    if (title.includes('数学') || title.includes('高数')) {
      suggestions.push('建议补充更多例题解析')
      suggestions.push('可以添加公式推导过程')
    } else if (title.includes('英语') || title.includes('四级')) {
      suggestions.push('建议附上听力音频链接')
      suggestions.push('可以补充翻译技巧')
    } else if (title.includes('政治')) {
      if (riskLevel === 'low') {
        riskLevel = 'medium'
        score = Math.min(score, 7)
      }
      suggestions.push('请确保内容符合主流价值观')
      suggestions.push('建议引用权威来源')
    }
    
    // 通用检查
    if (noteInfo.content.length < 50) {
      issues.push('内容篇幅较短，可能不够详细')
      suggestions.push('建议补充更多详细内容')
      score = Math.max(1, score - 1)
    }
    
    if (noteInfo.content.split('\n').length < 3) {
      suggestions.push('建议使用分段提高可读性')
    }
    
    // 确保有默认的问题和建议
    if (issues.length === 0) {
      if (riskLevel === 'low') {
        issues.push('未发现明显问题')
      } else {
        issues.push('建议人工复核')
      }
    }
    
    if (suggestions.length === 0) {
      suggestions.push('内容完整，可以直接通过')
      suggestions.push('可以添加标签提高检索效率')
    }
    
    // 生成AI评语
    const aiComments = {
      low: [
        '这份笔记内容完整、条理清晰，质量较高，建议通过审核。',
        '笔记内容专业详实，对学习者很有帮助，推荐通过。',
        '内容质量优秀，格式规范，可以直接发布。'
      ],
      medium: [
        '内容基本合格，但有一些可以改进的地方，建议修改后通过。',
        '部分内容需要优化，建议参考改进建议进行修改。',
        '整体不错，但需要注意一些细节问题。'
      ],
      high: [
        '内容存在违规风险，建议拒绝或要求用户修改后重新提交。',
        '检测到敏感内容，不建议通过审核。',
        '需要用户提供更多信息或证明，暂时无法通过。'
      ]
    }
    
    const commentList = aiComments[riskLevel] || aiComments.medium
    const aiComment = commentList[Math.floor(Math.random() * commentList.length)]
    
    return {
      score: score,
      riskLevel: riskLevel,
      summary: `AI审核完成，综合评分${score}/10`,
      issues: issues,
      suggestions: suggestions,
      aiComment: aiComment,
      details: {
        contentQuality: Math.floor(Math.random() * 30) + 70,
        complianceScore: riskLevel === 'high' ? 40 : (riskLevel === 'medium' ? 70 : 95),
        readabilityScore: Math.floor(Math.random() * 20) + 75,
        categoryMatch: Math.floor(Math.random() * 15) + 80
      }
    }
  }

  /**
   * 生成针对特定笔记的模拟审核报告
   */
  static generateMockReport(noteInfo) {
    return this.simulateAudit(noteInfo)
  }
}
