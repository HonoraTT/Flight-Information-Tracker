import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(false)
  const selectedIcao = ref(null)
  const selectedAirport = ref(null)
  const focusMode = ref(null)
  const toasts = ref([])

  function openSidebar(icao24) {
    selectedIcao.value = icao24
    selectedAirport.value = null
    sidebarOpen.value = true
  }

  function openAirportSidebar(airport) {
    selectedAirport.value = airport
    selectedIcao.value = null
    sidebarOpen.value = true
  }

  function focusFlight(icao24) {
    focusMode.value = { type: 'flight', icao24 }
    openSidebar(icao24)
  }

  function focusAirport(airport) {
    focusMode.value = { type: 'airport', code: airport.code }
    openAirportSidebar(airport)
  }

  function clearFocus() {
    focusMode.value = null
  }

  function closeSidebar() {
    sidebarOpen.value = false
    selectedIcao.value = null
    selectedAirport.value = null
  }

  let toastId = 0
  function addToast(message, type = 'error') {
    const id = ++toastId
    toasts.value.push({ id, message, type })
    setTimeout(() => removeToast(id), 5000)
  }

  function removeToast(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  return { sidebarOpen, selectedIcao, selectedAirport, focusMode, toasts, openSidebar, openAirportSidebar, focusFlight, focusAirport, clearFocus, closeSidebar, addToast, removeToast }
})
