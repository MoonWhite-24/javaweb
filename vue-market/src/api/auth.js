import request from './request'
export const login = (u, p) => request.post('/auth/login', { username: u, password: p })
export const register = (u, p) => request.post('/auth/register', { username: u, password: p, phone: '', captchaCode: '' })
export const refresh = (rt) => request.post('/auth/refresh', { refreshToken: rt })