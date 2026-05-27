<template>
  <el-pagination
    v-model:current-page="currentPage"
    :page-size="pageSize"
    :total="total"
    layout="prev, pager, next, total"
    @current-change="handleChange"
  />
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ total: Number, pageSize: { type: Number, default: 20 }, page: Number })
const emit = defineEmits(['change'])

const currentPage = ref(props.page || 1)
watch(() => props.page, (v) => { currentPage.value = v || 1 })

const handleChange = (p) => emit('change', p)
</script>
