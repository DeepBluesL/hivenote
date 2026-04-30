import request from '../utils/request'

export const getNoteList = (params) => request({ url: '/note/list', method: 'get', params })
export const getNoteDetail = (id) => request({ url: `/note/detail/${id}`, method: 'get' })
export const getMyNoteDetail = (id) => request({ url: `/note/my/detail/${id}`, method: 'get' })
export const addNote = (data, submit = true) => request({ url: '/note/add', method: 'post', data, params: { submit } })
export const updateNote = (id, data, submit = true) => request({ url: `/note/update/${id}`, method: 'post', data, params: { submit } })
export const deleteNote = (id) => request({ url: `/note/delete/${id}`, method: 'post' })
export const addDownloadCount = (id) => request({ url: `/note/download/${id}`, method: 'post' })
export const uploadFile = (formData) => request({ url: '/file/upload', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
export const getAdminNoteList = (params) => request({ url: '/note/admin/list', method: 'get', params })
export const approveNote = (id) => request({ url: `/note/admin/approve/${id}`, method: 'post' })

/**
 * 拒绝笔记（需要提供拒绝原因）
 */
export const rejectNote = (id, reason) => request({
  url: `/note/admin/reject/${id}`,
  method: 'post',
  params: { reason }
})

export const getHotNotes = (limit = 10) => request({ url: '/note/hot', method: 'get', params: { limit } })
export const getLatestNotes = (limit = 10) => request({ url: '/note/latest', method: 'get', params: { limit } })
export const getNoteStats = () => request({ url: '/note/stats', method: 'get' })
export const getRecommendNotes = (limit = 6) => request({ url: '/note/recommend', method: 'get', params: { limit }, silent: true })
export const getExamSprintNotes = (limit = 8, tag = '') => request({ url: '/note/examSprint', method: 'get', params: { limit, tag }, silent: true })
export const setNoteFeatured = (id, featured) => request({ url: `/note/admin/setFeatured/${id}`, method: 'post', params: { featured } })
export const setNoteExamFeatured = (id, examFeatured) => request({ url: `/note/admin/setExamFeatured/${id}`, method: 'post', params: { examFeatured } })

/**
 * AI 辅助审核建议
 */
export const aiReviewNote = (noteId) => request({
  url: `/note/admin/aiReview/${noteId}`,
  method: 'post'
})

/**
 * 获取某条笔记的审核记录（拒绝原因等）
 */
export const getNoteReviewLog = (noteId) => request({
  url: `/note/reviewLog/${noteId}`,
  method: 'get'
})
