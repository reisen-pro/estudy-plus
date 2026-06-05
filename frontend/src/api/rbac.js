import request from '../utils/request'

// 获取当前用户权限列表
export function getMyPermissions() {
  return request.get('/rbac/my-permissions')
}

// 获取当前用户角色列表
export function getMyRoles() {
  return request.get('/rbac/my-roles')
}

// 检查是否有某权限
export function checkPermission(code) {
  return request.get('/rbac/check', { params: { code } })
}

// 提交权限申请
export function applyPermission(data) {
  return request.post('/rbac/apply', data)
}

// 我的申请列表
export function getMyApplies() {
  return request.get('/rbac/apply/my')
}

// 待审批列表
export function getPendingApplies() {
  return request.get('/rbac/apply/pending')
}

// 所有申请列表
export function getAllApplies() {
  return request.get('/rbac/apply/all')
}

// 审批
export function reviewApply(data) {
  return request.post('/rbac/apply/review', data)
}

// 所有角色
export function getRoles() {
  return request.get('/rbac/roles')
}

// 所有权限
export function getPermissions() {
  return request.get('/rbac/permissions')
}

// 分配角色
export function assignRole(data) {
  return request.post('/rbac/assign-role', data)
}
