import request from './request'

export const getStats = () => request.get('/admin/stats')

export const getAdminProducts = (params) => request.get('/admin/products', { params })
export const getAdminProduct = (id) => request.get(`/admin/products/${id}`)
export const createAdminProduct = (product) => request.post('/admin/products', product)
export const updateAdminProduct = (id, product) => request.put(`/admin/products/${id}`, product)
export const updateProductStatus = (id, status) => request.put(`/admin/products/${id}/status`, { status })

export const getAdminOrders = (params) => request.get('/admin/orders', { params })
export const getAdminOrder = (orderNo) => request.get(`/admin/orders/${orderNo}`)

export const getAdminSeckillProducts = (params) => request.get('/admin/seckill-products', { params })
export const createAdminSeckillProduct = (p) => request.post('/admin/seckill-products', p)
export const updateAdminSeckillProduct = (id, p) => request.put(`/admin/seckill-products/${id}`, p)
export const deleteAdminSeckillProduct = (id) => request.delete(`/admin/seckill-products/${id}`)

export const getAdminUsers = () => request.get('/admin/users')
