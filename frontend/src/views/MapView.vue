<template>
  <main class="dashboard">
    <section class="map-area">
      <FlightMap ref="flightMapRef" />

      <div class="map-overlay-layer">
        <DashboardTopbar
          v-model="query"
          @login-click="showLoginHint = !showLoginHint"
          @more-click="showMoreMenu = !showMoreMenu"
        />

        <ToolRail :tools="tools" :active-tool="activeTool" @change="activeTool = $event" />

        <div class="controls" aria-label="地图控制">
          <button type="button" aria-label="放大地图" @click="flightMapRef?.zoomIn()">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg>
          </button>
          <button type="button" aria-label="缩小地图" @click="flightMapRef?.zoomOut()">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14" /></svg>
          </button>
          <button type="button" aria-label="还原视图" @click="flightMapRef?.resetView()">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 4H4v4M16 4h4v4M20 16v4h-4M4 16v4h4" /></svg>
          </button>
          <button type="button" aria-label="定位到默认视角" @click="flightMapRef?.locateDefaultView()">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="5" /><path d="M12 2v3M12 19v3M2 12h3M19 12h3" /></svg>
          </button>
        </div>

        <div v-if="showLoginHint" class="top-popover login-popover glass">
          <strong>登录入口占位</strong>
          <span>后续可接入账号体系、偏好同步与收藏航班。</span>
        </div>

        <div v-if="showMoreMenu" class="top-popover more-popover glass">
          <button type="button">帮助中心</button>
          <button type="button">快捷键说明</button>
          <button type="button">关于项目</button>
        </div>
      </div>
    </section>

    <aside class="side-panel">
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
import PlaybackPanel from '../components/sidebar/PlaybackPanel.vue'
import SettingsPanel from '../components/sidebar/SettingsPanel.vue'
import SidebarPanelLayout from '../components/sidebar/SidebarPanelLayout.vue'
import ToolRail from '../components/sidebar/ToolRail.vue'
import WeatherPanel from '../components/sidebar/WeatherPanel.vue'
import { connectWebSocket, disconnectWebSocket } from '../api/websocket'

const flightMapRef = ref(null)

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

const activeTool = ref('weather')
const query = ref('')
const showLoginHint = ref(false)
const showMoreMenu = ref(false)

const tools = [
  { id: 'weather', name: 'Weather', icon: '☁' },
  { id: 'settings', name: 'Settings', icon: '⚙' },
  { id: 'filters', name: 'Filters', icon: '⏷' },
  { id: 'playback', name: 'Playback', icon: '↺' },
]

const panelConfig = {
  weather: {
    kicker: 'Aviation weather',
    title: 'Weather',
    desc: '航空气象数据与图层叠加入口，对应 METAR、机场气象、跑道运行条件。',
  },
  settings: {
    kicker: 'Control center',
    title: 'Settings',
    desc: '全局地图视图、数据展示规则、单位与偏好的总控制台。',
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
  weather: WeatherPanel,
  settings: SettingsPanel,
  filters: FiltersPanel,
  playback: PlaybackPanel,
}

const currentPanelConfig = computed(() => panelConfig[activeTool.value])
const currentPanelComponent = computed(() => panelComponents[activeTool.value])
</script>
