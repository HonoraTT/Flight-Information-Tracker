import { defineStore } from 'pinia'
import { computed, reactive, watch } from 'vue'

const STORAGE_KEY = 'flight-tracker-settings'
const defaultSettings = {
  layers: { airports: true, lowLight: false, terrain: true },
  refreshRate: '15s',
  units: { speed: 'km/h', altitude: 'meters', time: 'Local time' },
  alerts: { a11y: false, voice: false },
  bgm: { enabled: true, volume: 0.3 },
}

function deepMerge(base, saved) {
  return {
    layers: { ...base.layers, ...(saved.layers || {}) },
    units: { ...base.units, ...(saved.units || {}) },
    alerts: { ...base.alerts, ...(saved.alerts || {}) },
    bgm: { ...base.bgm, ...(saved.bgm || {}) },
    refreshRate: saved.refreshRate || base.refreshRate,
  }
}

function loadSettings() {
  try { return deepMerge(defaultSettings, JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')) }
  catch { return defaultSettings }
}

export const useSettingsStore = defineStore('settings', () => {
  const settings = reactive(loadSettings())
  const refreshMs = computed(() => Number.parseInt(settings.refreshRate, 10) * 1000)
  watch(settings, () => localStorage.setItem(STORAGE_KEY, JSON.stringify(settings)), { deep: true })
  return { settings, refreshMs }
})
