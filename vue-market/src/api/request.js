import axios from 'axios'
import router from '../router'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

request.interceptors.request.use(c => {
  const t = localStorage.getItem('accessToken')
  if (t) c.headers.Authorization = `Bearer ${t}`
  return c
})

request.interceptors.response.use(
  r => r,
  async e => {
    if (e.response?.status === 401) {
      const rt = localStorage.getItem('refreshToken')
      if (rt && !e.config._retry) {
        e.config._retry = true
        try {
          const { data } = await axios.post('/api/auth/refresh', { refreshToken: rt })
          if (data.code === 200) {
            localStorage.setItem('accessToken', data.data.accessToken)
            localStorage.setItem('refreshToken', data.data.refreshToken)
            e.config.headers.Authorization = `Bearer ${data.data.accessToken}`
            return request(e.config)
          }
        } catch (_) {}
      }
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
    return Promise.reject(e)
  }
)

export default request