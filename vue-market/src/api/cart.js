import request from './request'
export const getCart = () => request.get('/cart')
export const addToCart = (pid, qty = 1) => request.post('/cart/items', { productId: pid, quantity: qty })
export const updateCartItem = (pid, qty) => request.put(`/cart/items/${pid}`, { quantity: qty })
export const removeCartItem = (pid) => request.delete(`/cart/items/${pid}`)