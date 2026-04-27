<template>
  <main class="dashboard">
    <section class="map-area">
      <DashboardTopbar v-model="query" />
      <FlightMap />
    </section>

    <aside class="side-panel">
      <ToolRail :tools="tools" :active-tool="activeTool" @change="activeTool = $event" />
      <SidebarPanelLayout :config="currentPanelConfig">
        <component :is="currentPanelComponent" />
      </SidebarPanelLayout>
    </aside>

    <FlightDetailSidebar />
  </main>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import DashboardTopbar from '../components/sidebar/DashboardTopbar.vue'
import FlightDetailSidebar from '../components/sidebar/FlightDetailSidebar.vue'
import FlightMap from '../components/map/FlightMap.vue'
import FiltersPanel from '../components/sidebar/FiltersPanel.vue'
import MapPlaceholder from '../components/sidebar/MapPlaceholder.vue'
import PlaybackPanel from '../components/sidebar/PlaybackPanel.vue'
import SettingsPanel from '../components/sidebar/SettingsPanel.vue'
import SidebarPanelLayout from '../components/sidebar/SidebarPanelLayout.vue'
import ToolRail from '../components/sidebar/ToolRail.vue'
import WeatherPanel from '../components/sidebar/WeatherPanel.vue'
import { connectWebSocket, disconnectWebSocket } from '../api/websocket'
import { useFlightStore } from '../stores/flightStore'

const flightStore = useFlightStore()

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

const activeTool = ref('weather')
const query = ref('')
const selectedFlight = ref(null)

const tools = [
  { id: 'settings', name: 'Settings', icon: '⚙' },
  { id: 'weather', name: 'Weather', icon: '☁' },
  { id: 'filters', name: 'Filters', icon: '⏷' },
  { id: 'playback', name: 'Playback', icon: '↺' },
]

const panelConfig = {
  settings: {
    kicker: 'Control center',
    title: 'Settings',
    desc: '全局地图视图、数据展示规则、单位与偏好的总控制台。',
  },
  weather: {
    kicker: 'Aviation weather',
    title: 'Weather',
    desc: '航空气象数据与图层叠加入口，对应 METAR、机场气象、跑道运行条件。',
  },
  filters: {
    kicker: 'Flight discovery',
    title: 'Filters',
    desc: '精准过滤地图上海量航班，只展示关心的目标。',
  },
  playback: {
    kicker: 'Flight history',
    title: 'Playback',
    desc: '历史航班回溯、轨迹播放与复盘导出占位。',
  },
}

const panelComponents = {
  settings: SettingsPanel,
  weather: WeatherPanel,
  filters: FiltersPanel,
  playback: PlaybackPanel,
}

const currentPanelConfig = computed(() => panelConfig[activeTool.value])
const currentPanelComponent = computed(() => panelComponents[activeTool.value])
const filteredFlights = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  const all = Object.values(flightStore.flights)
  if (!keyword) return all
  return all.filter((flight) =>
    `${flight.callsign ?? ''} ${flight.originCountry ?? ''}`.toLowerCase().includes(keyword)
  )
})
</script>
