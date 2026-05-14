<template>
  <div ref="mapContainer" class="flight-map"></div>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useFlightStore } from '../../stores/flightStore'
import { useFilterStore } from '../../stores/filterStore'
import { useSettingsStore } from '../../stores/settingsStore'
import { useUiStore } from '../../stores/uiStore'
import { updateMarkers, refreshMarkerIcon, clearAllMarkers } from './FlightMarker'
import { createAirportIcon } from './AirportMarker'
import { AIRPORTS } from '../../data/airports'
import { fetchWeatherByCoordinates } from '../../api/flightApi'
import { speakChinese } from '../../utils/speech'
import { translateWeather } from '../../utils/weather'

const DEFAULT_CENTER = [35.0, 105.0]
const DEFAULT_ZOOM = 5
const AIRPORT_RADIUS_DEG = 1.2

const mapContainer = ref(null)
let map = null
let airportMarkers = new Map()
let selectedAirportCode = null
let baseTileLayer = null
let weatherPulseLayer = null
const trailLayers = {}

const flightStore = useFlightStore()
const filterStore = useFilterStore()
const settingsStore = useSettingsStore()
const uiStore = useUiStore()

const visibleFlights = computed(() => {
  const filters = filterStore.filters
  let list = Object.values(flightStore.flights)

  if (uiStore.focusMode?.type === 'flight') list = list.filter(f => f.icao24 === uiStore.focusMode.icao24)
  else if (uiStore.focusMode?.type === 'airport' && uiStore.selectedAirport) {
    const airport = uiStore.selectedAirport
    list = list.filter(f => Math.abs((f.latitude ?? 0) - airport.lat) <= AIRPORT_RADIUS_DEG && Math.abs((f.longitude ?? 0) - airport.lon) <= AIRPORT_RADIUS_DEG)
  } else {
    if (filters.preset === 'airborne') list = list.filter(f => f.onGround === false)
    if (filters.preset === 'ground') list = list.filter(f => f.onGround === true)
    if (filters.callsign.trim()) {
      const keyword = filters.callsign.trim().toLowerCase()
      list = list.filter(f => (f.callsign || '').toLowerCase().includes(keyword))
    }
    if (filters.country !== '全部国家') list = list.filter(f => f.originCountry === filters.country)
    list = list.filter(f => (f.baroAltitude ?? 0) >= filters.minAltitude && (f.baroAltitude ?? 0) <= filters.maxAltitude)
    list = list.filter(f => ((f.velocity ?? 0) * 3.6) >= filters.minSpeed && ((f.velocity ?? 0) * 3.6) <= filters.maxSpeed)
    if (filters.airportNearby && uiStore.selectedAirport) {
      const airport = uiStore.selectedAirport
      list = list.filter(f => Math.abs((f.latitude ?? 0) - airport.lat) <= AIRPORT_RADIUS_DEG && Math.abs((f.longitude ?? 0) - airport.lon) <= AIRPORT_RADIUS_DEG)
    }
  }

  return list.length > 1000 && map ? list.filter(f => map.getBounds().contains([f.latitude, f.longitude])) : list
})

function updateBaseLayer() {
  if (!map) return
  if (baseTileLayer) map.removeLayer(baseTileLayer)
  const terrainUrl = 'https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}'
  const normalUrl = 'https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}'
  baseTileLayer = L.tileLayer(settingsStore.settings.layers.terrain ? terrainUrl : normalUrl, { subdomains: '1234', maxZoom: 18, minZoom: 3 }).addTo(map)
  refreshAirportIcons()
}

function refreshAirportIcons() {
  const dark = !settingsStore.settings.layers.terrain
  airportMarkers.forEach((marker, code) => marker.setIcon(createAirportIcon(code, code === selectedAirportCode, dark)))
}

function syncAirportVisibility() {
  if (!map) return
  airportMarkers.forEach((marker, code) => {
    const isFocusedAirport = uiStore.focusMode?.type === 'airport'
    const shouldShow = settingsStore.settings.layers.airports && (!isFocusedAirport || uiStore.focusMode.code === code)
    if (shouldShow) marker.addTo(map)
    else marker.remove()
  })
}

function syncLowLight() {
  if (mapContainer.value) mapContainer.value.classList.toggle('low-light-map', settingsStore.settings.layers.lowLight)
}

function getWeatherVisual(desc) {
  const text = desc || '实时天气'
  if (text.includes('雨')) return { icon: '雨', className: 'rainy', label: text }
  if (text.includes('雪')) return { icon: '雪', className: 'snowy', label: text }
  if (text.includes('雷')) return { icon: '雷', className: 'stormy', label: text }
  if (text.includes('雾') || text.includes('霾')) return { icon: '雾', className: 'foggy', label: text }
  if (text.includes('云') || text.includes('阴')) return { icon: '云', className: 'cloudy', label: text }
  if (text.includes('晴')) return { icon: '晴', className: 'sunny', label: text }
  return { icon: '天', className: 'live', label: text }
}

async function setWeatherPulse(airport) {
  if (!map) return
  if (weatherPulseLayer) {
    map.removeLayer(weatherPulseLayer)
    weatherPulseLayer = null
  }
  if (!airport) return
  let visual = getWeatherVisual('实时天气')
  try {
    const data = await fetchWeatherByCoordinates(airport.lat, airport.lon)
    const desc = translateWeather(data?.weather?.[0]?.description || data?.weatherDescription || data?.description)
    const temp = data?.main?.temp ?? data?.temperature
    visual = getWeatherVisual(desc)
    visual.label = temp != null ? `${desc} · ${Math.round(Number(temp))}°C` : desc
  } catch {
    visual = getWeatherVisual('天气加载中')
  }
  weatherPulseLayer = L.marker([airport.lat, airport.lon], {
    interactive: false,
    icon: L.divIcon({
      className: '',
      iconSize: [240, 240],
      iconAnchor: [120, 120],
      html: `<div class="weather-zone ${visual.className}"><span class="ring r1"></span><span class="ring r2"></span><span class="weather-badge"><b>${visual.icon}</b><em>${visual.label}</em></span><i class="particle p1"></i><i class="particle p2"></i><i class="particle p3"></i></div>`,
    }),
  }).addTo(map)
}

async function speakAirportWeather(airport) {
  if (!settingsStore.settings.alerts.voice || !airport) return
  try {
    const data = await fetchWeatherByCoordinates(airport.lat, airport.lon)
    const desc = translateWeather(data?.weather?.[0]?.description || data?.weatherDescription || data?.description)
    const temp = data?.main?.temp ?? data?.temperature ?? '未知'
    const wind = data?.wind?.speed ?? data?.windSpeed ?? '未知'
    const visibility = data?.visibility != null ? (Number(data.visibility) / 1000).toFixed(1) : '未知'
    speakChinese(`${airport.name}天气，${desc}，温度${temp}度，风速${wind}米每秒，能见度${visibility}公里。`)
  } catch {
    speakChinese(`${airport.name}天气数据暂时不可用。`)
  }
}

function focusAirportOnMap(airport) {
  if (!map || !airport) return
  selectedAirportCode = airport.code
  refreshAirportIcons()
  syncAirportVisibility()
  setWeatherPulse(airport)
  speakAirportWeather(airport)
  map.setView([airport.lat, airport.lon], Math.max(map.getZoom(), 9), { animate: true })
}

function focusFlightOnMap(icao24) {
  const flight = flightStore.flights[icao24]
  if (!map || !flight?.latitude || !flight?.longitude) return
  setWeatherPulse(null)
  syncAirportVisibility()
  map.setView([flight.latitude, flight.longitude], Math.max(map.getZoom(), 8), { animate: true })
}

function zoomIn() { if (map) map.zoomIn() }
function zoomOut() { if (map) map.zoomOut() }
function resetView() {
  uiStore.clearFocus()
  setWeatherPulse(null)
  if (map) map.fitBounds([[18, 73], [54, 135]], { padding: [20, 20] })
}
function locateDefaultView() {
  uiStore.clearFocus()
  setWeatherPulse(null)
  if (map) map.setView(DEFAULT_CENTER, DEFAULT_ZOOM, { animate: true })
}
defineExpose({ zoomIn, zoomOut, resetView, locateDefaultView, focusAirportOnMap, focusFlightOnMap })

onMounted(() => {
  map = L.map(mapContainer.value, { center: DEFAULT_CENTER, zoom: DEFAULT_ZOOM, zoomControl: false, attributionControl: false })
  updateBaseLayer()

  AIRPORTS.forEach((airport) => {
    const marker = L.marker([airport.lat, airport.lon], { icon: createAirportIcon(airport.code, false, !settingsStore.settings.layers.terrain), interactive: true })
      .on('click', () => uiStore.focusAirport(airport))
      .addTo(map)
    airportMarkers.set(airport.code, marker)
  })
  syncAirportVisibility()
  syncLowLight()

  map.on('click', (e) => {
    const target = e.originalEvent?.target
    const clickedOnMarker = target?.closest('.leaflet-marker-icon')
    if (!clickedOnMarker) {
      selectedAirportCode = null
      uiStore.clearFocus()
      setWeatherPulse(null)
      refreshAirportIcons()
      syncAirportVisibility()
      uiStore.closeSidebar()
    }
  })
})

onUnmounted(() => {
  clearAllMarkers(map)
  if (map) {
    airportMarkers.forEach((m) => map.removeLayer(m))
    airportMarkers.clear()
    if (weatherPulseLayer) map.removeLayer(weatherPulseLayer)
    map.remove()
    map = null
  }
})

function syncTrail(flight) {
  if (!map) return
  const points = flightStore.getCachedTrack(flight.icao24).filter(p => p.latitude != null && p.longitude != null).map(p => [p.latitude, p.longitude])
  if (points.length < 2) return
  if (!trailLayers[flight.icao24]) trailLayers[flight.icao24] = { trail: null }
  const layer = trailLayers[flight.icao24]
  if (layer.trail) layer.trail.setLatLngs(points)
  else layer.trail = L.polyline(points, { color: '#3b82f6', weight: 2, opacity: 0.55 }).addTo(map)
}

function removeTrail(icao24) {
  const layer = trailLayers[icao24]
  if (!layer) return
  if (layer.trail) map.removeLayer(layer.trail)
  delete trailLayers[icao24]
}

watch(visibleFlights, (flightList) => {
  updateMarkers(map, flightList, (flight) => uiStore.focusFlight(flight.icao24))
  const currentKeys = new Set(flightList.map(f => f.icao24))
  for (const key of Object.keys(trailLayers)) if (!currentKeys.has(key)) removeTrail(key)
  for (const flight of flightList) syncTrail(flight)
}, { deep: true })

watch(() => uiStore.selectedIcao, (newIcao, oldIcao) => {
  if (oldIcao) {
    const flight = flightStore.flights[oldIcao]
    if (flight) refreshMarkerIcon(map, oldIcao, flight, false)
  }
  if (newIcao) {
    const flight = flightStore.flights[newIcao]
    if (flight) refreshMarkerIcon(map, newIcao, flight, true)
  }
})

watch(() => uiStore.focusMode, (focus) => {
  if (focus?.type === 'airport') focusAirportOnMap(uiStore.selectedAirport)
  if (focus?.type === 'flight') focusFlightOnMap(focus.icao24)
}, { deep: true })
watch(() => settingsStore.settings.layers.terrain, updateBaseLayer)
watch(() => settingsStore.settings.layers.airports, syncAirportVisibility)
watch(() => settingsStore.settings.layers.lowLight, syncLowLight, { immediate: true })
watch(() => uiStore.sidebarOpen, (open) => { if (!open && uiStore.focusMode?.type !== 'airport') setWeatherPulse(null) })
</script>

<style scoped>
.flight-map { width: 100%; height: 100vh; }
:deep(.plane-icon-wrap) { transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1); }
:deep(.weather-zone) { position: relative; width: 240px; height: 240px; pointer-events: none; }
:deep(.weather-zone .ring) { position: absolute; inset: 22px; border-radius: 50%; border: 2px solid rgba(79, 180, 255, 0.62); background: radial-gradient(circle, rgba(79, 180, 255, 0.22), rgba(79, 180, 255, 0.04) 58%, transparent 72%); animation: weatherZonePulse 2.6s ease-out infinite; }
:deep(.weather-zone .r2) { animation-delay: 1.15s; }
:deep(.weather-badge) { position: absolute; left: 50%; top: 50%; display: grid; place-items: center; gap: 4px; min-width: 104px; padding: 10px 14px; border-radius: 999px; transform: translate(-50%, -50%); color: #fff; background: linear-gradient(135deg, rgba(23,108,192,0.92), rgba(79,180,255,0.88)); box-shadow: 0 12px 28px rgba(23, 108, 192, 0.32); font-style: normal; text-align: center; }
:deep(.weather-badge b) { font-size: 18px; line-height: 1; }
:deep(.weather-badge em) { font-size: 12px; font-style: normal; white-space: nowrap; }
:deep(.weather-zone .particle) { position: absolute; width: 9px; height: 9px; border-radius: 50%; background: rgba(79,180,255,0.82); animation: weatherParticle 1.8s ease-in-out infinite; }
:deep(.weather-zone .p1) { left: 54px; top: 86px; }
:deep(.weather-zone .p2) { right: 60px; top: 78px; animation-delay: 0.4s; }
:deep(.weather-zone .p3) { left: 118px; bottom: 48px; animation-delay: 0.8s; }
:deep(.weather-zone.sunny .ring) { border-color: rgba(245, 178, 46, 0.72); background: radial-gradient(circle, rgba(245, 178, 46, 0.26), rgba(245, 178, 46, 0.06) 58%, transparent 72%); }
:deep(.weather-zone.sunny .weather-badge) { background: linear-gradient(135deg, #f59e0b, #facc15); }
:deep(.weather-zone.cloudy .ring), :deep(.weather-zone.foggy .ring) { border-color: rgba(125, 158, 184, 0.72); background: radial-gradient(circle, rgba(125, 158, 184, 0.22), rgba(125, 158, 184, 0.06) 58%, transparent 72%); }
:deep(.weather-zone.rainy .particle) { width: 3px; height: 16px; border-radius: 999px; animation-name: weatherRainDrop; }
@keyframes weatherZonePulse { 0% { transform: scale(0.36); opacity: 0.9; } 100% { transform: scale(1.12); opacity: 0; } }
@keyframes weatherParticle { 0%, 100% { transform: translateY(0); opacity: 0.35; } 50% { transform: translateY(-16px); opacity: 1; } }
@keyframes weatherRainDrop { 0% { transform: translateY(-12px); opacity: 0; } 50% { opacity: 1; } 100% { transform: translateY(22px); opacity: 0; } }
</style>
