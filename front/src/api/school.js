import request from '@/utils/request'

// 获取学校列表
export function getSchoolList() {
  return request({
    url: '/school/list',
    method: 'get'
  })
}

// 搜索学校
export function searchSchools(keyword) {
  return request({
    url: '/school/search',
    method: 'get',
    params: { keyword }
  })
}

// 添加学校
export function addSchool(data) {
  return request({
    url: '/school/add',
    method: 'post',
    data
  })
}
