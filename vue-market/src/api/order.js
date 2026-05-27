import request from './request'
export const createOrder = (addrId) => request.post('/orders', { shippingAddressId: addrId })
export const getOrders = (p) => request.get('/orders', { params: p })
export const getOrderDetail = (no) => request.get(`/orders/${no}`)
export const payOrder = (no, amt, tn) => request.post(`/orders/${no}/pay`, { amount: amt, tradeNo: tn })