<template>
  <div ref="mapContainer" class="flight-map"></div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useFlightStore } from '../../stores/flightStore'
import { useUiStore } from '../../stores/uiStore'
import { updateMarkers, refreshMarkerIcon, clearAllMarkers } from './FlightMarker'
import { createAirportIcon } from './AirportMarker'
import { AIRPORTS } from '../../data/airports'

const DEFAULT_CENTER = [35.0, 105.0]
const DEFAULT_ZOOM = 5

const mapContainer = ref(null)
let map = null
let airportMarkers = new Map() // code → marker
let selectedAirportCode = null  // 当前选中机场代码，用于标记选中态

const trailLayers = {}

const flightStore = useFlightStore()
const uiStore = useUiStore()

function zoomIn() {
  if (map) map.zoomIn()
}

function zoomOut() {
  if (map) map.zoomOut()
}

function resetView() {
  if (map) map.fitBounds([[18, 73], [54, 135]], { padding: [20, 20] })
}

function locateDefaultView() {
  if (map) map.setView(DEFAULT_CENTER, DEFAULT_ZOOM, { animate: true })
}

defineExpose({ zoomIn, zoomOut, resetView, locateDefaultView })

onMounted(() => {
  map = L.map(mapContainer.value, {
    center: DEFAULT_CENTER,
    zoom: DEFAULT_ZOOM,
    zoomControl: false,
    attributionControl: false,
  })

  // 高德影像地图（稳定、加载快、无白块）
  L.tileLayer(
    'https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}',
    {
      subdomains: '1234',
      maxZoom: 18,
      minZoom: 3,
    }
  ).addTo(map)

  // 机场标记：只创建一次，不响应 watch 频繁重建
  AIRPORTS.forEach((airport) => {
    const marker = L.marker([airport.lat, airport.lon], {
      icon: createAirportIcon(airport.code, false),
      interactive: true,
    })
      .on('click', () => {
        // 切换选中态
        const prev = selectedAirportCode
        if (prev) {
          const prevMarker = airportMarkers.get(prev)
          if (prevMarker) prevMarker.setIcon(createAirportIcon(prev, false))
        }
        selectedAirportCode = airport.code
        marker.setIcon(createAirportIcon(airport.code, true))
        map.panTo([airport.lat, airport.lon], { animate: true })
        uiStore.openAirportSidebar(airport)
      })
      .addTo(map)
    airportMarkers.set(airport.code, marker)
  })

  // 点击地图空白处：取消机场选中 + 关闭侧边栏
  map.on('click', (e) => {
    // 如果点击的是机场标记，不处理（机场自己处理）
    const target = e.originalEvent?.target
    const clickedOnMarker = target?.closest('.leaflet-marker-icon')
    if (!clickedOnMarker) {
      if (selectedAirportCode) {
        const marker = airportMarkers.get(selectedAirportCode)
        if (marker) marker.setIcon(createAirportIcon(selectedAirportCode, false))
        selectedAirportCode = null
      }
      uiStore.closeSidebar()
    }
  })
})

onUnmounted(() => {
  clearAllMarkers(map)
  if (map) {
    airportMarkers.forEach((m) => map.removeLayer(m))
    airportMarkers.clear()
    map.remove()
    map = null
  }
})

function syncTrail(flight) {
  if (!map) return
  const { icao24, latitude, longitude } = flight

  if (!trailLayers[icao24]) {
    trailLayers[icao24] = { trail: null, trailPoints: [] }
  }
  const layer = trailLayers[icao24]
  layer.trailPoints.push([latitude, longitude])

  if (layer.trail) {
    layer.trail.setLatLngs(layer.trailPoints)
  } else if (layer.trailPoints.length >= 2) {
    layer.trail = L.polyline(layer.trailPoints, {
      color: '#3b82f6',
      weight: 2,
      opacity: 0.55,
    }).addTo(map)
  }
}

function removeTrail(icao24) {
  const layer = trailLayers[icao24]
  if (!layer) return
  if (layer.trail) map.removeLayer(layer.trail)
  delete trailLayers[icao24]
}

watch(
  () => flightStore.flights,
  (flights) => {
    const flightList = Object.values(flights)

    updateMarkers(map, flightList, (flight) => {
      uiStore.openSidebar(flight.icao24)
    })

    const currentKeys = new Set(Object.keys(flights))
    for (const key of Object.keys(trailLayers)) {
      if (!currentKeys.has(key)) removeTrail(key)
    }
    for (const flight of flightList) {
      syncTrail(flight)
    }
  },
  { deep: true }
)

watch(
  () => uiStore.selectedIcao,
  (newIcao, oldIcao) => {
    if (oldIcao) {
      const flight = flightStore.flights[oldIcao]
      if (flight) refreshMarkerIcon(map, oldIcao, flight, false)
    }
    if (newIcao) {
      const flight = flightStore.flights[newIcao]
      if (flight) refreshMarkerIcon(map, newIcao, flight, true)
    }
  }
)

watch(
  () => uiStore.selectedAirport,
  (newAirport) => {
    // 选中态由点击处理，此处仅处理关闭时取消选中
    if (!newAirport && selectedAirportCode) {
      const marker = airportMarkers.get(selectedAirportCode)
      if (marker) marker.setIcon(createAirportIcon(selectedAirportCode, false))
      selectedAirportCode = null
    }
  }
)

watch(
  () => uiStore.sidebarOpen,
  (open) => {
    if (open) return
    for (const layer of Object.values(trailLayers)) {
      if (layer.trail) {
        map.removeLayer(layer.trail)
        layer.trail = null
      }
      layer.trailPoints = []
    }
  }
)
</script>

<style scoped>
.flight-map {
  width: 100%;
  height: 100vh;
}

:deep(.plane-icon-wrap) {
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
