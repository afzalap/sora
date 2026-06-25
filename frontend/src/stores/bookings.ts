import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '@/api/client'
import type { BookingResponse } from '@/types'

export const useBookingsStore = defineStore('bookings', () => {
  const bookings = ref<BookingResponse[]>([])
  const loading = ref(false)
  const cancellingId = ref<number | null>(null)   // which booking shows confirm-cancel step
  const cancelConfirmId = ref<number | null>(null) // which booking is awaiting final confirm

  const confirmedCount = computed(
    () => bookings.value.filter((b) => b.status === 'CONFIRMED').length,
  )

  async function fetch() {
    loading.value = true
    try {
      const { data } = await apiClient.get<BookingResponse[]>('/api/bookings')
      // Sort newest first
      bookings.value = data.sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      )
    } catch {
      // silently ignore — user might not be logged in yet
    } finally {
      loading.value = false
    }
  }

  function startCancel(id: number) {
    cancelConfirmId.value = id
  }

  function abortCancel() {
    cancelConfirmId.value = null
  }

  async function confirmCancel(id: number) {
    try {
      const { data } = await apiClient.patch<BookingResponse>(`/api/bookings/${id}/cancel`)
      const idx = bookings.value.findIndex((b) => b.id === id)
      if (idx !== -1) bookings.value[idx] = data
      cancelConfirmId.value = null
    } catch {
      // error could be shown inline; keep it simple for Phase 1
      cancelConfirmId.value = null
    }
  }

  return {
    bookings,
    loading,
    cancellingId,
    cancelConfirmId,
    confirmedCount,
    fetch,
    startCancel,
    abortCancel,
    confirmCancel,
  }
})
