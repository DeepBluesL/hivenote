import request from '../utils/request'

export const addReport = (data) => {
  return request({
    url: '/report/add',
    method: 'post',
    data
  })
}

export const getAdminReportList = (params) => {
  return request({
    url: '/report/admin/list',
    method: 'get',
    params,
    silent: true
  })
}

export const handleReport = (id, data) => {
  return request({
    url: `/report/admin/handle/${id}`,
    method: 'post',
    data
  })
}
