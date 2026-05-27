import request from './request'

export const getCart = () => request.get('/cart')

export const addToCart = (productId, quantity = 1) =>
  request.post('/cart/items', { productId, quantity })

export const updateCartItem = (productId, quantity) =>
  request.put(`/cart/items/${productId}`, { quantity })

export const removeCartItem = (productId) =>
  request.delete(`/cart/items/${productId}`)
