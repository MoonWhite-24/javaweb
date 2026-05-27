import request from './request'
export const getSeckillProducts = () => request.get('/seckill/products')
export const executeSeckill = (pid) => request.post(`/seckill/execute/${pid}`)
export const getSeckillResult = (pid) => request.get(`/seckill/result/${pid}`)