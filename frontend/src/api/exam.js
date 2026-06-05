import request from '../utils/request'

// 题目列表
export function getQuestionList(params) {
  return request.get('/exam/question/list', { params })
}

// 创建题目
export function createQuestion(data) {
  return request.post('/exam/question', data)
}

// 试卷列表
export function getPaperList(params) {
  return request.get('/exam/paper/list', { params })
}

// 创建试卷
export function createPaper(data) {
  return request.post('/exam/paper', data)
}

// 发布试卷
export function publishPaper(id) {
  return request.put(`/exam/paper/${id}/publish`)
}

// 可考试卷
export function getAvailablePapers() {
  return request.get('/exam/paper/available')
}

// 试卷详情
export function getPaperDetail(paperId) {
  return request.get(`/exam/paper/${paperId}`)
}

// 试卷题目(考试时)
export function getPaperQuestions(paperId) {
  return request.get(`/exam/paper/${paperId}/questions`)
}

// 开始考试
export function startExam(paperId) {
  return request.post(`/exam/paper/${paperId}/start`)
}

// 提交答案
export function submitAnswer(data) {
  return request.post('/exam/answer', data)
}

// 交卷
export function submitExam(recordId) {
  return request.post(`/exam/record/${recordId}/submit`)
}

// 我的成绩
export function getMyRecords() {
  return request.get('/exam/record/my')
}
