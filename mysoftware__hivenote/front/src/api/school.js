import request from '../utils/request'

export const getSchoolList = (keyword) => {
  return request({
    url: '/school/list',
    method: 'get',
    params: keyword ? { keyword } : {}
  })
}
