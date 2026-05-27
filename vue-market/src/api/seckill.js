import request from './request'

export const getSeckillProducts = () => request.get('/seckill/products')

export const executeSeckill = (productId) =>
  request.post(`/seckill/execute/${productId}`)

export const getSeckillResult = (productId) =>
  request.get(`/seckill/result/${productId}`)
