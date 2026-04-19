import request from './request'

// 获取所有股票列表
export function getStockList() {
  return request({
    url: '/list',
    method: 'get'
  })
}

// 添加股票
export function addStock(data) {
  return request({
    url: '/add',
    method: 'post',
    data
  })
}

// 删除股票
export function deleteStock(code) {
  return request({
    url: `/delete/${code}`,
    method: 'delete'
  })
}

// 更新股票
export function updateStock(code, data) {
  return request({
    url: `/${code}`,
    method: 'put',
    data
  })
}

// 获取实时行情（包含盈亏）
export function getMarketDataWithProfit() {
  return request({
    url: '/market-data/all',
    method: 'get'
  })
}

// 获取盈亏概览
export function getProfitOverview() {
  return request({
    url: '/profit-overview',
    method: 'get'
  })
}

// 手动触发定时任务
export function executeManualTask() {
  return request({
    url: '/task/execute',
    method: 'post'
  })
}
