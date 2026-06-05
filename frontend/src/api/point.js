import request from '../utils/request'

// 积分账户
export function getPointAccount() {
  return request.get('/point/account')
}

// 年度统计
export function getPointStats(year) {
  return request.get('/point/stats', { params: { year } })
}

// 积分明细
export function getPointLogs(year) {
  return request.get('/point/logs', { params: { year } })
}

// 打赏
export function tipCourse(data) {
  return request.post('/point/tip', data)
}

// 积分商品
export function getPointGoods() {
  return request.get('/point/goods')
}

// 兑换
export function redeemGoods(data) {
  return request.post('/point/redeem', data)
}

// 兑换记录
export function getMyExchanges() {
  return request.get('/point/exchange/my')
}
