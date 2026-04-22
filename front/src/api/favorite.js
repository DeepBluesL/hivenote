import request from '../utils/request'

export const toggleFavorite = (noteId) => {
  return request({
    url: '/favorite/toggle',
    method: 'post',
    data: { noteId }
  })
}

export const checkFavorite = (noteId) => {
  return request({
    url: '/favorite/check',
    method: 'get',
    params: { noteId }
  })
}

export const getMyFavorites = () => {
  return request({
    url: '/favorite/myList',
    method: 'get'
  })
}
