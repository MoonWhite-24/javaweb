import request from './request'

export const createOrder = (shippingAddressId) =>
  request.post('/orders', { shippingAddressId })

export const getOrders = (params) =>
  request.get('/orders', { params })

export const getOrderDetail = (orderNo) =>
  request.get(`/orders/${orderNo}`)

export const payOrder = (orderNo, amount, tradeNo) =>
  request.post(`/orders/${orderNo}/pay`, { amount, tradeNo })
