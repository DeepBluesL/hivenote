import request from '../utils/request'

/**
 * 获取某篇笔记的评论列表
 * @param {Object} params - { noteId: 'xxx', pageNumber: 1, pageSize: 10 }
 */
export const getCommentList = (params) => {
  return request({
    url: '/comment/getAllByPage',
    method: 'get',
    params
  })
}

/**
 * 发表评论
 * @param {Object} data - { noteId: 'xxx', content: '很有用的笔记！', rating: 5 }
 */
export const addComment = (data) => {
  return request({
    url: '/comment/save',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 * @param {String} id 
 */
export const deleteComment = (id) => {
  return request({
    url: `/comment/del/${id}`,
    method: 'delete'
  })
}

/**
 * 获取最近评论（社区页面用）
 */
export const getRecentComments = (limit = 10) => {
  return request({
    url: '/comment/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 管理员获取所有评论列表
 */
export const getAdminCommentList = (params) => {
  return request({
    url: '/comment/admin/list',
    method: 'get',
    params,
    silent: true
  })
}