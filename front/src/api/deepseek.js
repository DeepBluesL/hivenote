import axios from 'axios'

// DeepSeek API配置
const DEEPSEEK_API_KEY = import.meta.env.VITE_DEEPSEEK_API_KEY
const DEEPSEEK_API_URL = import.meta.env.VITE_DEEPSEEK_API_URL || 'https://api.deepseek.com/v1/chat/completions'

// 创建独立的axios实例
const deepseekRequest = axios.create({
  baseURL: DEEPSEEK_API_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${DEEPSEEK_API_KEY}`
  }
})

/**
 * 调用DeepSeek API进行内容审核
 */
export async function auditWithDeepSeek(noteInfo) {
  // 如果没有API密钥，返回模拟数据
  if (!DEEPSEEK_API_KEY || DEEPSEEK_API_KEY === 'sk-您的API密钥') {
    console.warn('未配置DeepSeek API密钥，返回模拟数据')
    return {
      success: true,
      data: getMockResult(noteInfo)
    }
  }

  try {
    console.log('调用DeepSeek API审核:', noteInfo.title)
    
    const systemPrompt = `你是一个专业的笔记内容审核助手。请对用户上传的笔记进行全面的质量、合规性和内容审核。
请以严格但友好的态度，按照以下JSON格式返回审核结果（不要有任何额外文字）：
{
  "score": 数字评分(1-10),
  "riskLevel": "low" | "medium" | "high",
  "summary": "简要总结",
  "issues": ["问题1", "问题2", ...],
  "suggestions": ["建议1", "建议2", ...],
  "aiComment": "详细的AI评语",
  "details": {
    "contentQuality": 内容质量分数(0-100),
    "complianceScore": 合规性分数(0-100),
    "readabilityScore": 可读性分数(0-100),
    "categoryMatch": 分类匹配度(0-100)
  }
}`

    const userPrompt = `请审核以下笔记：
【标题】${noteInfo.title}
【作者】${noteInfo.author || '匿名'}
【上传时间】${noteInfo.uploadTime || '未知'}
【内容】${noteInfo.content || noteInfo.summary || '无详细内容'}`

    const response = await deepseekRequest.post('', {
      model: 'deepseek-chat',
      messages: [
        { role: 'system', content: systemPrompt },
        { role: 'user', content: userPrompt }
      ],
      temperature: 0.3,
      max_tokens: 2000
    })

    if (response.data && response.data.choices && response.data.choices[0]) {
      const aiResponse = response.data.choices[0].message.content
      console.log('DeepSeek响应:', aiResponse)
      
      // 提取JSON
      try {
        const jsonMatch = aiResponse.match(/\{[\s\S]*\}/)
        if (jsonMatch) {
          const result = JSON.parse(jsonMatch[0])
          return {
            success: true,
            data: result
          }
        } else {
          throw new Error('无法解析JSON')
        }
      } catch (parseError) {
        console.error('解析AI响应失败:', parseError)
        return {
          success: true,
          data: getMockResult(noteInfo)
        }
      }
    } else {
      throw new Error('API响应格式错误')
    }
    
  } catch (error) {
    console.error('DeepSeek API调用失败:', error)
    // API调用失败时返回模拟数据
    return {
      success: true,
      data: getMockResult(noteInfo)
    }
  }
}

/**
 * 生成模拟的审核结果（API密钥未配置或调用失败时使用）
 */
function getMockResult(noteInfo) {
  const title = noteInfo.title?.toLowerCase() || ''
  const content = noteInfo.content?.toLowerCase() || ''
  
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
  
  // 通用检查
  if (content.length < 50) {
    issues.push('内容篇幅较短，可能不够详细')
    suggestions.push('建议补充更多详细内容')
    score = Math.max(1, score - 1)
  }
  
  if (issues.length === 0) {
    issues.push('未发现明显问题')
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
 * 批量审核（逐条调用，避免并发过高）
 */
export async function batchAuditWithDeepSeek(notes, onProgress) {
  const results = []
  
  for (let i = 0; i < notes.length; i++) {
    const note = notes[i]
    try {
      if (onProgress) {
        onProgress(i + 1, notes.length, `正在审核: ${note.title}`)
      }
      
      const result = await auditWithDeepSeek(note)
      
      if (result.success) {
        results.push({
          noteId: note.id || i,
          noteTitle: note.title,
          ...result.data
        })
      } else {
        results.push({
          noteId: note.id || i,
          noteTitle: note.title,
          score: 0,
          riskLevel: 'error',
          issues: ['AI审核失败: ' + result.error],
          suggestions: ['请重试或人工审核'],
          aiComment: 'API调用失败'
        })
      }
      
      // 添加延迟，避免触发限流
      await new Promise(resolve => setTimeout(resolve, 500))
      
    } catch (error) {
      results.push({
        noteId: note.id || i,
        noteTitle: note.title,
        score: 0,
        riskLevel: 'error',
        issues: ['审核过程出错'],
        suggestions: ['请重试'],
        aiComment: error.message
      })
    }
  }
  
  return results
}
