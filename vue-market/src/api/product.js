import request from './request'
export const getProducts = (p) => request.get('/products', { params: p })
export const getProductDetail = (id) => request.get(`/products/${id}`)
export const getCategories = () => request.get('/categories/tree')