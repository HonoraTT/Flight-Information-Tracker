<template>
  <div ref="mapContainer" class="flight-map"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useFlightStore } from '../../stores/flightStore'
import { useUiStore } from '../../stores/uiStore'
import { createFlightIcon } from './FlightMarker'

const mapContainer = ref(null)
let map = null
const markers = {}

const flightStore = useFlightStore()
const uiStore = useUiStore()

onMounted(() => {
  map = L.map(mapContainer.value).setView([30, 118], 4)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
  }).addTo(map)
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})

function updateMarker(flight) {
  if (!map) return
  const { icao24, latitude, longitude } = flight
  if (markers[icao24]) {
    markers[icao24].setLatLng([latitude, longitude])
  } else {
    const marker = L.marker([latitude, longitude], { icon: createFlightIcon(flight) })
    marker.on('click', () => uiStore.openSidebar(icao24))
    marker.addTo(map)
    markers[icao24] = marker
  }
}

function removeMarker(icao24) {
  if (markers[icao24]) {
    map.removeLayer(markers[icao24])
    delete markers[icao24]
  }
}

// Watch flightStore.flights and sync markers...
</script>

<style scoped>
.flight-map {
  width: 100%;
  height: 100vh;
}
</style>
