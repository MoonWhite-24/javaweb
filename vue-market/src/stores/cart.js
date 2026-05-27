import { defineStore } from 'pinia'
import { getCart, addToCart, updateCartItem, removeCartItem } from '../api/cart'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const totalCount = computed(() => items.value.reduce((s, i) => s + (i.quantity || 0), 0))
  const totalPrice = computed(() => items.value.reduce((s, i) => s + (i.price || 0) * (i.quantity || 0), 0))

  const fetch = async () => {
    try {
      const { data } = await getCart()
      if (data.code === 200) items.value = data.data || []
    } catch (e) {}
  }

  const add = async (productId, quantity) => {
    await addToCart(productId, quantity)
    await fetch()
  }

  const updateQty = async (productId, quantity) => {
    await updateCartItem(productId, quantity)
    await fetch()
  }

  const remove = async (productId) => {
    await removeCartItem(productId)
    await fetch()
  }

  return { items, totalCount, totalPrice, fetch, add, updateQty, remove }
})
