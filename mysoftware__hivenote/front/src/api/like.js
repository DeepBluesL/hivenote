import request from '../utils/request'

export const toggleLike = (noteId) => {
  return request({
    url: '/like/toggle',
    method: 'post',
    data: { noteId }
  })
}

export const checkLike = (noteId) => {
  return request({
    url: '/like/check',
    method: 'get',
    params: { noteId }
  })
}
