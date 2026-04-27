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

const mapContainer = ref(null)
let map = null

// icao24 -> { trail: L.polyline, trailPoints: [] }
const trailLayers = {}

const flightStore = useFlightStore()
const uiStore = useUiStore()

onMounted(() => {
  map = L.map(mapContainer.value, {
    center: [35.0, 105.0],
    zoom: 5,
    zoomControl: true,
    attributionControl: true,
  })

  L.tileLayer(
    'https://tiles.stadiamaps.com/tiles/stamen_terrain/{z}/{x}/{y}{r}.png',
    {
      attribution:
        '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors | Data: OpenSky Network',
      subdomains: 'abcd',
      maxZoom: 14,
      minZoom: 3,
    }
  ).addTo(map)

  map.on('click', () => uiStore.closeSidebar())
})

onUnmounted(() => {
  clearAllMarkers(map)
  if (map) { map.remove(); map = null }
})

// ─── 轨迹 helpers ───────────────────────────────────────────────────────────

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

// ─── reactive watchers ──────────────────────────────────────────────────────

watch(
  () => flightStore.flights,
  (flights) => {
    const flightList = Object.values(flights)

    // 统一更新 marker（包含旋转动画）
    updateMarkers(map, flightList, (flight) => {
      uiStore.openSidebar(flight.icao24)
    })

    // 更新轨迹 + 清理消失航班
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
  () => uiStore.sidebarOpen,
  (open) => {
    if (open) return
    for (const layer of Object.values(trailLayers)) {
      if (layer.trail) { map.removeLayer(layer.trail); layer.trail = null }
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
