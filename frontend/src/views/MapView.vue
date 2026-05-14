<template>
  <main class="dashboard">
    <section class="map-area">
      <FlightMap ref="flightMapRef" />

      <div class="map-overlay-layer">
        <DashboardTopbar
          v-model="query"
          @search="openSearchResults"
          @login-click="showLoginHint = !showLoginHint"
          @more-click="showMoreMenu = !showMoreMenu"
        />

        <ToolRail :tools="tools" :active-tool="activeTool" @change="activeTool = $event" />

        <div class="controls" aria-label="地图控制">
          <button type="button" aria-label="放大地图" @click="flightMapRef?.zoomIn()"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg></button>
          <button type="button" aria-label="缩小地图" @click="flightMapRef?.zoomOut()"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14" /></svg></button>
          <button type="button" aria-label="还原视图" @click="flightMapRef?.resetView()"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 4H4v4M16 4h4v4M20 16v4h-4M4 16v4h4" /></svg></button>
          <button type="button" aria-label="定位到默认视角" @click="flightMapRef?.locateDefaultView()"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="5" /><path d="M12 2v3M12 19v3M2 12h3M19 12h3" /></svg></button>
        </div>

        <div v-if="showLoginHint" class="top-popover login-popover glass"><strong>登录入口占位</strong><span>后续可接入账号体系、偏好同步与收藏航班。</span></div>
        <div v-if="showMoreMenu" class="top-popover more-popover glass"><button type="button">帮助中心</button><button type="button">快捷键说明</button><button type="button">关于项目</button></div>

        <div v-if="searchOpen" class="search-backdrop" @click="searchOpen = false"></div>
        <div v-if="searchOpen" class="search-modal glass" @click.stop>
          <button type="button" class="modal-close" @click="searchOpen = false">✕</button>
          <h3>搜索结果</h3>
          <div v-if="searchResults.length" class="search-results">
            <button v-for="item in searchResults" :key="item.id" type="button" @click="selectSearchResult(item)">
              <b>{{ item.title }}</b><span>{{ item.subtitle }}</span>
            </button>
          </div>
          <p v-else class="hint">只能搜索当前航班和机场，未找到匹配结果。</p>
        </div>
      </div>
    </section>

    <aside class="side-panel">
      <SidebarPanelLayout :config="currentPanelConfig"><component :is="currentPanelComponent" /></SidebarPanelLayout>
    </aside>

    <FlightDetailSidebar />
  </main>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import DashboardTopbar from '../components/sidebar/DashboardTopbar.vue'
import FlightDetailSidebar from '../components/sidebar/FlightDetailSidebar.vue'
import FlightMap from '../components/map/FlightMap.vue'
import FiltersPanel from '../components/sidebar/FiltersPanel.vue'
import SettingsPanel from '../components/sidebar/SettingsPanel.vue'
import SidebarPanelLayout from '../components/sidebar/SidebarPanelLayout.vue'
import ToolRail from '../components/sidebar/ToolRail.vue'
import WeatherPanel from '../components/sidebar/WeatherPanel.vue'
import { connectWebSocket, disconnectWebSocket } from '../api/websocket'
import { AIRPORTS } from '../data/airports'
import { useFlightStore } from '../stores/flightStore'
import { useSettingsStore } from '../stores/settingsStore'
import { useUiStore } from '../stores/uiStore'

const flightMapRef = ref(null)
const flightStore = useFlightStore()
const settingsStore = useSettingsStore()
const uiStore = useUiStore()

onMounted(() => { connectWebSocket() })
onUnmounted(() => { disconnectWebSocket() })
watch(() => settingsStore.settings.refreshRate, () => {
  disconnectWebSocket()
  connectWebSocket()
})

const activeTool = ref('weather')
const query = ref('')
const showLoginHint = ref(false)
const showMoreMenu = ref(false)
const searchOpen = ref(false)
const searchResults = ref([])

const tools = [
  { id: 'weather', name: 'Weather', icon: '☁' },
  { id: 'settings', name: 'Settings', icon: '⚙' },
  { id: 'filters', name: 'Filters', icon: '⏷' },
]

const panelConfig = {
  weather: { kicker: 'Weather', title: 'Weather', desc: '实时天气与机场天气概览。' },
  settings: { kicker: 'Control center', title: 'Settings', desc: '地图、数据、单位、BGM 与辅助设置。' },
  filters: { kicker: 'Flight discovery', title: 'Filters', desc: '基于后端可用字段过滤航班。' },
}
const panelComponents = { weather: WeatherPanel, settings: SettingsPanel, filters: FiltersPanel }
const currentPanelConfig = computed(() => panelConfig[activeTool.value])
const currentPanelComponent = computed(() => panelComponents[activeTool.value])

function openSearchResults() {
  const keyword = query.value.trim().toLowerCase()
  if (!keyword) return
  const flights = Object.values(flightStore.flights)
    .filter(f => `${f.callsign || ''} ${f.icao24}`.toLowerCase().includes(keyword))
    .slice(0, 8)
    .map(f => ({ id: `flight-${f.icao24}`, type: 'flight', payload: f, title: f.callsign?.trim() || f.icao24, subtitle: `${f.originCountry || '-'} · ${f.onGround ? '地面' : '空中'}` }))
  const airports = AIRPORTS
    .filter(a => `${a.code} ${a.name}`.toLowerCase().includes(keyword))
    .slice(0, 8)
    .map(a => ({ id: `airport-${a.code}`, type: 'airport', payload: a, title: `${a.code} · ${a.name}`, subtitle: `${a.lat.toFixed(4)}, ${a.lon.toFixed(4)}` }))
  searchResults.value = [...flights, ...airports]
  searchOpen.value = true
}

function selectSearchResult(item) {
  searchOpen.value = false
  if (item.type === 'flight') uiStore.focusFlight(item.payload.icao24)
  else uiStore.focusAirport(item.payload)
}
</script>
