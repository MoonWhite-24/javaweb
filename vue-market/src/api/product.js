import request from './request'

export const getProducts = (params) =>
  request.get('/products', { params })

export const getProductDetail = (id) =>
  request.get(`/products/${id}`)

export const getCategories = () =>
  request.get('/categories/tree')
