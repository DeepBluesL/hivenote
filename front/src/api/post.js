import request from '../utils/request'

export const getHotPosts = (limit = 10) => {
  return request({
    url: '/post/hot',
    method: 'get',
    params: { limit },
    silent: true
  })
}

export const getPostList = (params) => {
  return request({
    url: '/post/list',
    method: 'get',
    params,
    silent: true
  })
}

export const getPostDetail = (id) => {
  return request({
    url: `/post/detail/${id}`,
    method: 'get'
  })
}

export const addPost = (data) => {
  return request({
    url: '/post/add',
    method: 'post',
    data
  })
}

export const getMyPosts = () => {
  return request({
    url: '/post/myList',
    method: 'get'
  })
}

export const editPost = (data) => {
  return request({
    url: '/post/edit',
    method: 'post',
    data
  })
}

export const deletePost = (id) => {
  return request({
    url: `/post/delete/${id}`,
    method: 'post'
  })
}

export const likePost = (id) => {
  return request({
    url: `/post/like/${id}`,
    method: 'post'
  })
}

export const getPostLiked = (id) => {
  return request({
    url: `/post/liked/${id}`,
    method: 'get',
    silent: true
  })
}

export const pinPost = (id, pinned) => {
  return request({
    url: `/post/admin/pin/${id}`,
    method: 'post',
    params: { pinned }
  })
}

export const getPostReplies = (postId) => {
  return request({
    url: `/post/reply/list/${postId}`,
    method: 'get'
  })
}

export const addPostReply = (data) => {
  return request({
    url: '/post/reply/add',
    method: 'post',
    data
  })
}

export const deletePostReply = (id) => {
  return request({
    url: `/post/reply/delete/${id}`,
    method: 'post'
  })
}

export const uploadPostImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/uploadPostImage',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}
