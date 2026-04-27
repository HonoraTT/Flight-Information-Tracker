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

const DEFAULT_CENTER = [35.0, 105.0]
const DEFAULT_ZOOM = 5

const mapContainer = ref(null)
let map = null

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
  if (map) {
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
