<template>
  <main class="dashboard">
    <section class="map-area">
      <DashboardTopbar v-model="query" />
      <MapPlaceholder :flights="filteredFlights" :selected-flight="selectedFlight" @select-flight="selectedFlight = $event" />
    </section>

    <aside class="side-panel">
      <ToolRail :tools="tools" :active-tool="activeTool" @change="activeTool = $event" />
      <SidebarPanelLayout :config="currentPanelConfig">
        <component :is="currentPanelComponent" />
      </SidebarPanelLayout>
    </aside>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import DashboardTopbar from '../components/sidebar/DashboardTopbar.vue'
import FiltersPanel from '../components/sidebar/FiltersPanel.vue'
import MapPlaceholder from '../components/sidebar/MapPlaceholder.vue'
import PlaybackPanel from '../components/sidebar/PlaybackPanel.vue'
import SettingsPanel from '../components/sidebar/SettingsPanel.vue'
import SidebarPanelLayout from '../components/sidebar/SidebarPanelLayout.vue'
import ToolRail from '../components/sidebar/ToolRail.vue'
import WeatherPanel from '../components/sidebar/WeatherPanel.vue'

const activeTool = ref('weather')
const query = ref('')

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

const flights = [
  { id: 'csn318', callsign: 'CSN318', airline: 'China Southern', aircraft: 'A350-900', from: 'CAN', to: 'PEK', speed: '872 km/h', altitude: '10,970 m', heading: 42, eta: '18:42', x: 32, y: 43, status: 'Cruising', type: 'blue' },
  { id: 'ces726', callsign: 'CES726', airline: 'China Eastern', aircraft: 'B787-9', from: 'SHA', to: 'CTU', speed: '806 km/h', altitude: '9,450 m', heading: 278, eta: '19:08', x: 57, y: 33, status: 'Airborne', type: 'blue' },
  { id: 'cca1408', callsign: 'CCA1408', airline: 'Air China', aircraft: 'B737 MAX', from: 'SZX', to: 'HGH', speed: '615 km/h', altitude: '5,850 m', heading: 16, eta: '17:55', x: 47, y: 62, status: 'Approach', type: 'green' },
  { id: 'hxa9082', callsign: 'HXA9082', airline: 'Hainan Airlines', aircraft: 'A330-300', from: 'HAK', to: 'NKG', speed: '0 km/h', altitude: 'Ground', heading: 95, eta: 'Delayed', x: 72, y: 55, status: 'Delayed', type: 'orange' },
]

const selectedFlight = ref(flights[0])

const currentPanelConfig = computed(() => panelConfig[activeTool.value])
const currentPanelComponent = computed(() => panelComponents[activeTool.value])
const filteredFlights = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  if (!keyword) return flights
  return flights.filter((flight) => `${flight.callsign} ${flight.airline} ${flight.from} ${flight.to}`.toLowerCase().includes(keyword))
})
</script>
