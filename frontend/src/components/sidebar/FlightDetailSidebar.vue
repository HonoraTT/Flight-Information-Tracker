<template>
  <transition name="slide">
    <aside v-if="uiStore.sidebarOpen" class="flight-detail-sidebar">
      <button class="close-btn" @click="uiStore.closeSidebar">✕</button>
      <div v-if="flight" class="detail-content">
        <h2>{{ flight.callsign || flight.icao24 }}</h2>
        <dl>
          <dt>ICAO24 标识</dt><dd>{{ flight.icao24 }}</dd>
          <dt>航班呼号</dt><dd>{{ flight.callsign || '-' }}</dd>
          <dt>注册国家</dt><dd>{{ flight.originCountry }}</dd>
          <dt>纬度</dt><dd>{{ flight.latitude?.toFixed(4) ?? '-' }}</dd>
          <dt>经度</dt><dd>{{ flight.longitude?.toFixed(4) ?? '-' }}</dd>
          <dt>气压高度</dt><dd>{{ flight.baroAltitude?.toFixed(0) ?? '-' }} m</dd>
          <dt>地速</dt><dd>{{ flight.velocity?.toFixed(1) ?? '-' }} m/s</dd>
          <dt>航向角</dt><dd>{{ flight.heading?.toFixed(1) ?? '-' }}°</dd>
        </dl>
      </div>
    </aside>
  </transition>
</template>

<script setup>
import { computed } from 'vue'
import { useFlightStore } from '../../stores/flightStore'
import { useUiStore } from '../../stores/uiStore'

const flightStore = useFlightStore()
const uiStore = useUiStore()

const flight = computed(() =>
  uiStore.selectedIcao ? flightStore.flights[uiStore.selectedIcao] : null
)
</script>

<style scoped>
.flight-detail-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: 300px;
  height: 100vh;
  background: #1a1a2e;
  color: #e0e0e0;
  padding: 20px;
  box-shadow: 2px 0 10px rgba(0,0,0,0.3);
  z-index: 1000;
  overflow-y: auto;
}
.close-btn {
  position: absolute;
  top: 10px;
  left: 14px;
  background: none;
  border: none;
  color: #aaa;
  font-size: 18px;
  cursor: pointer;
}

.detail-content {
  padding-top: 50px;
}
dl { display: grid; grid-template-columns: auto 1fr; gap: 8px; }
dt { color: #888; font-size: 12px; }
dd { margin: 0; font-size: 13px; }

.slide-enter-active, .slide-leave-active { transition: left 0.3s ease; }
.slide-enter-from, .slide-leave-to { left: -300px; }
</style>
