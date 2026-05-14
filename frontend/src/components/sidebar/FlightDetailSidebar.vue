<template>
  <transition name="slide">
    <aside v-if="uiStore.sidebarOpen && flight" class="flight-detail-sidebar panel-like-sidebar">
      <button class="close-btn" @click="uiStore.closeSidebar">✕</button>
      <div class="detail-content">
        <p class="kicker">Flight detail</p>
        <h2>{{ callsign }}</h2>
        <dl>
          <dt>注册国家</dt><dd>{{ flight.originCountry || '-' }}</dd>
          <dt>纬度 / 经度</dt><dd>{{ positionText }}</dd>
          <dt>高度</dt><dd>{{ formatAltitude(flight.baroAltitude, settingsStore.settings.units.altitude) }}</dd>
          <dt>速度</dt><dd>{{ formatSpeed(flight.velocity, settingsStore.settings.units.speed) }}</dd>
          <dt>航向</dt><dd>{{ flight.heading?.toFixed(1) ?? '-' }}°</dd>
          <dt>是否在地面</dt><dd>{{ flight.onGround ? '是' : '否' }}</dd>
          <dt>位置更新时间</dt><dd>{{ formatTime(flight.timePosition, settingsStore.settings.units.time) }}</dd>
          <dt>最后通信时间</dt><dd>{{ formatTime(flight.lastContact, settingsStore.settings.units.time) }}</dd>
        </dl>
      </div>
    </aside>
  </transition>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useFlightStore } from '../../stores/flightStore'
import { useSettingsStore } from '../../stores/settingsStore'
import { useUiStore } from '../../stores/uiStore'
import { formatAltitude, formatSpeed, formatTime } from '../../utils/formatters'
import { speakChinese } from '../../utils/speech'

const flightStore = useFlightStore()
const uiStore = useUiStore()
const settingsStore = useSettingsStore()

const flight = computed(() => uiStore.selectedIcao ? flightStore.flights[uiStore.selectedIcao] : null)
const callsign = computed(() => flight.value?.callsign?.trim() || flight.value?.icao24 || '未知呼号')
const positionText = computed(() => flight.value?.latitude != null && flight.value?.longitude != null ? `${flight.value.latitude.toFixed(4)}, ${flight.value.longitude.toFixed(4)}` : '-')

watch(() => uiStore.selectedIcao, (icao24) => {
  if (!settingsStore.settings.alerts.voice || !icao24) return
  const f = flightStore.flights[icao24]
  if (f) speakChinese(`航班${f.callsign?.trim() || f.icao24}，注册国家${f.originCountry || '未知'}，${f.onGround ? '当前在地面' : '当前在空中'}，高度${Math.round(f.baroAltitude || 0)}米，速度${Math.round((f.velocity || 0) * 3.6)}公里每小时。`)
}, { immediate: true })
</script>

<style scoped>
.flight-detail-sidebar {
  position: fixed;
  left: 24px;
  top: 96px;
  width: 340px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  z-index: 2000;
}
.panel-like-sidebar {
  border: 1px solid rgba(122, 161, 195, 0.26);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 50px rgba(76, 116, 150, 0.18);
  backdrop-filter: blur(18px);
  border-radius: 24px;
  color: #17324d;
  padding: 20px;
}
.close-btn { position: absolute; top: 14px; right: 14px; border: 0; background: #eef6fd; color: #34526e; width: 30px; height: 30px; border-radius: 10px; cursor: pointer; }
.detail-content { display: grid; gap: 16px; }
h2 { margin: 0; font-size: 28px; }
dl { display: grid; grid-template-columns: 100px 1fr; gap: 10px; }
dt { color: #7a8fa3; font-size: 12px; }
dd { margin: 0; font-size: 13px; font-weight: 700; }
.slide-enter-active, .slide-leave-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateX(-16px); }
</style>
