import request from './request'

export const login = (username, password) =>
  request.post('/auth/login', { username, password })

export const register = (username, password, phone, captchaCode) =>
  request.post('/auth/register', { username, password, phone, captchaCode })

export const refresh = (refreshToken) =>
  request.post('/auth/refresh', { refreshToken })
