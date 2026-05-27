import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister } from '../api/auth'
import { ref, computed } from 'vue'
import router from '../router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1 || userInfo.value?.role === '1')

  const login = async (username, password) => {
    const { data } = await apiLogin(username, password)
    if (data.code === 200) {
      token.value = data.data.accessToken
      refreshToken.value = data.data.refreshToken
      userInfo.value = { username: data.data.username, role: parseInt(data.data.role) || 0 }
      localStorage.setItem('accessToken', token.value)
      localStorage.setItem('refreshToken', refreshToken.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      router.push('/')
    }
    return data
  }

  const register = async (username, password) => {
    const { data } = await apiRegister(username, password, '', '')
    if (data.code === 200) router.push('/login')
    return data
  }

  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return { token, refreshToken, userInfo, isLoggedIn, isAdmin, login, register, logout }
})
