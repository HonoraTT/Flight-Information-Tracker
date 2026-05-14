<template>
  <div class="stack">
    <PanelCard title="单航班全轨迹回放" tag="Track">
      <div class="form">
        <label>航班 ICAO24 / Callsign<input v-model="keyword" placeholder="输入航班标识" @keydown.enter="loadTrack" /></label>
      </div>
      <div class="chips"><button class="picked" @click="loadTrack">加载轨迹</button></div>
      <div class="chips playback-buttons"><button @click="step(-1)">快退</button><button class="picked" :disabled="trackPoints.length === 0" @click="playing = !playing">{{ playing ? '暂停' : '播放' }}</button><button @click="step(1)">快进</button></div>
      <div class="chips"><button v-for="rate in rates" :key="rate" :class="{ picked: speedRate === rate }" @click="speedRate = rate">{{ rate }}x</button></div>
      <div class="timeline-card"><span>{{ trackPoints.length ? progress + 1 : 0 }}</span><div class="progress"><i :style="{ width: progressPercent + '%' }"></i></div><span>{{ trackPoints.length }}</span></div>
      <input v-model.number="progress" type="range" min="0" :max="Math.max(trackPoints.length - 1, 0)" />
      <div v-if="currentPoint" class="track-stats"><p><span>当前高度</span><b>{{ formatAltitude(currentPoint.baroAltitude) }}</b></p><p><span>当前速度</span><b>{{ formatSpeed(currentPoint.velocity) }}</b></p><p><span>航向</span><b>{{ currentPoint.heading?.toFixed(0) ?? '-' }}°</b></p></div>
      <div class="track-stats"><p><span>最大高度</span><b>{{ formatAltitude(stats.maxAltitude) }}</b></p><p><span>最大速度</span><b>{{ formatSpeed(stats.maxSpeed) }}</b></p><p><span>航点</span><b>{{ trackPoints.length }}</b></p></div>
      <p v-if="statusText" class="hint">{{ statusText }}</p>
      <p class="hint">说明：Playback 依赖后端 `/api/tracks/{icao24}` 历史轨迹；如果后端没有入库，会退回使用本页面打开后收到的实时位置缓存。</p>
    </PanelCard>

    <PanelCard title="复盘备注" tag="Local">
      <textarea v-model="notes" class="review-note" placeholder="复盘备注会保存在本地浏览器中"></textarea>
    </PanelCard>
  </div>
</template>

<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { fetchTracks } from '../../api/flightApi'
import PanelCard from '../common/PanelCard.vue'
import { useFlightStore } from '../../stores/flightStore'
import { useUiStore } from '../../stores/uiStore'
import { formatAltitude, formatSpeed } from '../../utils/formatters'

const flightStore = useFlightStore()
const uiStore = useUiStore()
const keyword = ref('')
const trackPoints = ref([])
const playing = ref(false)
const progress = ref(0)
const speedRate = ref(1)
const rates = [1, 2, 4]
const statusText = ref('输入当前在线航班的 ICAO24 或 callsign 后加载轨迹。')
const notes = ref(localStorage.getItem('flight-playback-notes') || '')
let timer = null

const selectedFlight = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return null
  return Object.values(flightStore.flights).find(f => f.icao24.toLowerCase() === key || (f.callsign || '').toLowerCase().includes(key)) || { icao24: key }
})
const currentPoint = computed(() => trackPoints.value[progress.value] || null)
const stats = computed(() => ({ maxAltitude: Math.max(...trackPoints.value.map(p => p.baroAltitude ?? p.altitude ?? 0), 0), maxSpeed: Math.max(...trackPoints.value.map(p => p.velocity ?? 0), 0) }))
const progressPercent = computed(() => trackPoints.value.length ? ((progress.value + 1) / trackPoints.value.length) * 100 : 0)

function step(delta) { progress.value = Math.min(Math.max(progress.value + delta, 0), Math.max(trackPoints.value.length - 1, 0)) }
async function loadTrack() {
  if (!selectedFlight.value?.icao24) return
  playing.value = false
  statusText.value = '正在加载轨迹...'
  try {
    const data = await fetchTracks(selectedFlight.value.icao24, 86400)
    const apiPoints = Array.isArray(data) ? data : []
    const cachedPoints = flightStore.getCachedTrack(selectedFlight.value.icao24)
    trackPoints.value = apiPoints.length ? apiPoints : cachedPoints
    progress.value = 0
    statusText.value = apiPoints.length ? '' : `后端历史轨迹为空，已使用本次页面运行期间缓存的 ${cachedPoints.length} 个实时轨迹点。`
    if (trackPoints.value.length < 2) statusText.value += ' 至少 2 个点才能形成完整回放。'
  } catch {
    const cachedPoints = flightStore.getCachedTrack(selectedFlight.value.icao24)
    trackPoints.value = cachedPoints
    progress.value = 0
    statusText.value = `轨迹接口暂时不可用，已使用本地实时缓存 ${cachedPoints.length} 个点。`
  }
}
watch(() => uiStore.selectedIcao, (icao24) => {
  if (!icao24) return
  keyword.value = icao24
  loadTrack()
}, { immediate: true })
watch(playing, enabled => { clearInterval(timer); if (enabled) timer = setInterval(() => { if (progress.value >= trackPoints.value.length - 1) playing.value = false; else step(1) }, 1000 / speedRate.value) })
watch(speedRate, () => { if (playing.value) { playing.value = false; playing.value = true } })
watch(notes, value => localStorage.setItem('flight-playback-notes', value))
onUnmounted(() => clearInterval(timer))
</script>
