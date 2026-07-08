import request from './request'

export const getStats = () => request.get('/admin/stats')
export const getStatsTrend = (days = 7) => request.get('/admin/stats/trend', { params: { days } })
export const getStatsOrderStatus = () => request.get('/admin/stats/order-status')

export const getAdminProducts = (params) => request.get('/admin/products', { params })
export const getAdminProduct = (id) => request.get(`/admin/products/${id}`)
export const createAdminProduct = (product) => request.post('/admin/products', product)
export const updateAdminProduct = (id, product) => request.put(`/admin/products/${id}`, product)
export const updateProductStatus = (id, status) => request.put(`/admin/products/${id}/status`, { status })
export const deleteAdminProduct = (id) => request.delete(`/admin/products/${id}`)

export const getAdminOrders = (params) => request.get('/admin/orders', { params })
export const getAdminOrder = (orderNo) => request.get(`/admin/orders/${orderNo}`)
export const deleteAdminOrder = (orderNo) => request.delete(`/admin/orders/${orderNo}`)
export const updateAdminOrderStatus = (orderNo, status) => request.put(`/admin/orders/${orderNo}`, { status })

export const getAdminSeckillProducts = (params) => request.get('/admin/seckill-products', { params })
export const createAdminSeckillProduct = (p) => request.post('/admin/seckill-products', p)
export const updateAdminSeckillProduct = (id, p) => request.put(`/admin/seckill-products/${id}`, p)
export const deleteAdminSeckillProduct = (id) => request.delete(`/admin/seckill-products/${id}`)

export const getCategories = () => request.get('/categories/tree')
export const getAdminCategoryTree = () => request.get('/admin/categories/tree')
export const createAdminCategory = (category) => request.post('/admin/categories', category)
export const updateAdminCategory = (id, category) => request.put(`/admin/categories/${id}`, category)
export const deleteAdminCategory = (id) => request.delete(`/admin/categories/${id}`)

export const getAdminUsers = (params) => request.get('/admin/users', { params })
export const getAdminUser = (id) => request.get(`/admin/users/${id}`)
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })

export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
