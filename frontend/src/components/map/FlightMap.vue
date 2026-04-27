<template>
  <div ref="mapContainer" class="flight-map"></div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useFlightStore } from '../../stores/flightStore'
import { useUiStore } from '../../stores/uiStore'
import { createFlightIcon } from './FlightMarker'

const mapContainer = ref(null)
let map = null

// icao24 -> { marker, trail: L.polyline, trailPoints: [] }
const flightLayers = {}
const flightStore = useFlightStore()
const uiStore = useUiStore()

onMounted(() => {
  map = L.map(mapContainer.value, {
    center: [35.0, 105.0],
    zoom: 5,
    zoomControl: true,
    attributionControl: true,
  })

  // 深色底图（无国家名称标注）
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

  // 点击地图空白区域，关闭侧边栏
  map.on('click', () => {
    uiStore.closeSidebar()
  })
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})

// --- marker / trail helpers ---

function syncFlight(flight) {
  if (!map) return
  const { icao24, latitude, longitude } = flight
  const isSelected = uiStore.selectedIcao === icao24

  if (!flightLayers[icao24]) {
    const marker = L.marker([latitude, longitude], {
      icon: createFlightIcon(flight, isSelected),
    })
    marker.on('click', (e) => {
      L.DomEvent.stopPropagation(e)
      uiStore.openSidebar(icao24)
    })
    marker.addTo(map)

    flightLayers[icao24] = {
      marker,
      trail: null,
      trailPoints: [],
    }
  } else {
    const layer = flightLayers[icao24]

    // 1. 移动标记
    layer.marker.setLatLng([latitude, longitude])
    layer.marker.setIcon(createFlightIcon(flight, isSelected))

    // 2. 追加轨迹点
    layer.trailPoints.push([latitude, longitude])

    // 3. 绘制/更新轨迹折线
    if (layer.trail) {
      layer.trail.setLatLngs(layer.trailPoints)
    } else if (layer.trailPoints.length >= 2) {
      layer.trail = L.polyline(layer.trailPoints, {
        color: '#3b82f6',
        weight: 2,
        opacity: 0.6,
      }).addTo(map)
    }
  }
}

function removeFlight(icao24) {
  const layer = flightLayers[icao24]
  if (!layer) return

  if (layer.marker) map.removeLayer(layer.marker)
  if (layer.trail)  map.removeLayer(layer.trail)
  delete flightLayers[icao24]
}

// --- reactive watchers ---

// 监听航班数据变化
watch(
  () => flightStore.flights,
  (newFlights) => {
    const currentKeys = new Set(Object.keys(newFlights))

    // 清理已消失的航班
    for (const key of Object.keys(flightLayers)) {
      if (!currentKeys.has(key)) removeFlight(key)
    }

    // 新增 / 更新
    for (const [icao24, flight] of Object.entries(newFlights)) {
      syncFlight(flight)
    }
  },
  { deep: true }
)

// 监听选中态变化，更新图标
watch(
  () => uiStore.selectedIcao,
  (newIcao, oldIcao) => {
    const update = (icao24) => {
      const layer = flightLayers[icao24]
      if (!layer) return
      const flight = flightStore.flights[icao24]
      if (!flight) return
      layer.marker.setIcon(createFlightIcon(flight, true))
    }

    if (oldIcao && flightLayers[oldIcao]) {
      const flight = flightStore.flights[oldIcao]
      if (flight) flightLayers[oldIcao].marker.setIcon(createFlightIcon(flight, false))
    }
    if (newIcao) update(newIcao)
  }
)

// 监听 sidebar 关闭，清除所有轨迹
watch(
  () => uiStore.sidebarOpen,
  (open) => {
    if (open) return
    for (const layer of Object.values(flightLayers)) {
      if (layer.trail) {
        map.removeLayer(layer.trail)
        layer.trail = null
        layer.trailPoints = []
      }
    }
  }
)
</script>

<style scoped>
.flight-map {
  width: 100%;
  height: 100vh;
}
</style>
