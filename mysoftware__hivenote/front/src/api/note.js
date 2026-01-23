import request from '../utils/request'

/**
 * 1. 获取笔记列表 (支持分页、搜索、分类筛选)
 * @param {Object} params - { page: 1, size: 10, title: 'Java', category: 'CS' }
 */
export const getNoteList = (params) => {
  return request({
    url: '/note/list', // 对应后端的 @GetMapping("/note/list")
    method: 'get',
    params
  })
}

/**
 * 2. 获取笔记详情
 * @param {String|Number} id - 笔记ID
 */
export const getNoteDetail = (id) => {
  return request({
    url: `/note/detail/${id}`, // 对应后端的 @GetMapping("/note/detail/{id}")
    method: 'get'
  })
}

/**
 * 3. 上传/发布笔记
 * @param {Object} data - { title: '', summary: '', fileUrl: '', category: '' }
 */
export const addNote = (data) => {
  return request({
    url: '/note/add', // 对应后端的 @PostMapping("/note/add")
    method: 'post',
    data
  })
}

/**
 * 4. 增加下载次数 (统计用)
 */
export const addDownloadCount = (id) => {
  return request({
    url: `/note/download/${id}`,
    method: 'post'
  })
}

/**
 * 5. 上传文件
 */
export const uploadFile = (formData) => {
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 6. 管理员获取笔记列表（包含所有状态）
 * @param {Object} params - { page, size, status, category, title }
 */
export const getAdminNoteList = (params) => {
  return request({
    url: '/note/admin/list',
    method: 'get',
    params
  })
}

/**
 * 7. 审核通过笔记
 * @param {String} id - 笔记ID
 */
export const approveNote = (id) => {
  return request({
    url: `/note/admin/approve/${id}`,
    method: 'post'
  })
}

/**
 * 8. 拒绝笔记
 * @param {String} id - 笔记ID
 */
export const rejectNote = (id) => {
  return request({
    url: `/note/admin/reject/${id}`,
    method: 'post'
  })
}