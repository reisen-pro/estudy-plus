import request from '../utils/request'

// 我的任务列表
export function getMyTasks() {
  return request.get('/task/my')
}

// 任务详情
export function getTaskDetail(taskId) {
  return request.get(`/task/${taskId}`)
}

// 创建任务
export function createTask(data) {
  return request.post('/task', data)
}

// 完成学习步骤
export function completeLearn(taskId) {
  return request.post(`/task/${taskId}/complete-learn`)
}

// 检查是否可以开始考试
export function canStartExam(taskId) {
  return request.get(`/task/${taskId}/can-exam`)
}

// 获取任务关联的试卷ID
export function getTaskPaperId(taskId) {
  return request.get(`/task/${taskId}/paper`)
}
