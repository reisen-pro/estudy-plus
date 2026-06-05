import request from '../utils/request'

// 课程列表
export function getCourseList(params) {
  return request.get('/course/list', { params })
}

// 课程详情
export function getCourseDetail(id) {
  return request.get(`/course/${id}`)
}

// 创建课程
export function createCourse(data) {
  return request.post('/course', data)
}

// 更新课程
export function updateCourse(id, data) {
  return request.put(`/course/${id}`, data)
}

// 删除课程
export function deleteCourse(id) {
  return request.delete(`/course/${id}`)
}

// 分类树
export function getCategoryTree() {
  return request.get('/course/category/tree')
}

// 记录学习进度
export function saveProgress(data) {
  return request.post('/course/progress', data)
}

// 我的课程
export function getMyCourses() {
  return request.get('/course/my')
}
