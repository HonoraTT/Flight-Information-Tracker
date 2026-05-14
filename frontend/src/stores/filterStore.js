import { defineStore } from 'pinia'
import { reactive } from 'vue'

export const useFilterStore = defineStore('filters', () => {
  const filters = reactive({
    preset: 'all',
    callsign: '',
    country: '全部国家',
    minAltitude: 0,
    maxAltitude: 12000,
    minSpeed: 0,
    maxSpeed: 1200,
    airportNearby: false,
  })

  return { filters }
})
