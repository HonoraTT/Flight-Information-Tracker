import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(false)
  const selectedIcao = ref(null)   // currently selected flight icao24
  const selectedAirport = ref(null) // currently selected airport { code, lat, lon }
  const toasts = ref([])           // { id, message, type }

  function openSidebar(icao24) {
    selectedIcao.value = icao24
    sidebarOpen.value = true
  }

  function openAirportSidebar(airport) {
    selectedAirport.value = airport
    sidebarOpen.value = true
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

  return { sidebarOpen, selectedIcao, selectedAirport, toasts, openSidebar, openAirportSidebar, closeSidebar, addToast, removeToast }
})