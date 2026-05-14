<template>
  <transition name="float-up">
    <div v-if="airport" class="airport-card">
      <button class="close-btn" @click="uiStore.closeSidebar">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>

      <div class="card-header">
        <svg class="tower-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="28" height="28">
          <path fill="#e3f99c"
            d="M687 898.3h-54.3l-35.4-565.9h38.8V275H596l-0.3-11.3
               c0.5-3.9 0.8-7.7 0.8-11.7V125.9
               c0-49.1-36.4-89.6-83.6-96.2v-6.9
               c0-7.6-6.1-13.7-13.7-13.7s-13.7 6.1-13.7 13.7v6.9
               c-47.3 6.7-83.6 47.2-83.6 96.2V252c0 4 0.3 7.9 0.8 11.8l-0.2 11.2h-40.1v57.4h38.8
               l-26 566h-36.3v58.3h-90v58.3h526.3v-58.3H687v-58.4z
               M507 156c0-18.9 15.3-34.2 34.2-34.2 18.9 0 34.2 15.3 34.2 34.2v45H507v-45z
               m-85.4 413.7l4.5-88.7h144.8l5.9 88.7H421.6z
               m0-123.2l4.5-88.7h144.8l5.9 88.7H421.6z"/>
        </svg>
        <div>
          <div class="airport-name">{{ airportName }}</div>
          <div class="airport-code">{{ airport.code }}</div>
        </div>
      </div>

      <div class="card-divider"></div>

      <div class="card-body">
        <div class="info-row">
          <span class="info-label">机场名称</span>
          <span class="info-value">{{ airportName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">ICAO 代码</span>
          <span class="info-value code">{{ airport.code }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">纬度</span>
          <span class="info-value">{{ airport.lat.toFixed(4) }}°</span>
        </div>
        <div class="info-row">
          <span class="info-label">经度</span>
          <span class="info-value">{{ airport.lon.toFixed(4) }}°</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed } from 'vue'
import { useUiStore } from '../../stores/uiStore'
import { AIRPORTS } from '../../data/airports'

const AIRPORT_NAME_MAP = {}
for (const a of AIRPORTS) AIRPORT_NAME_MAP[a.code] = a.name

const uiStore = useUiStore()
const airport = computed(() => uiStore.selectedAirport)
const airportName = computed(() =>
  airport.value ? (AIRPORT_NAME_MAP[airport.value.code] || airport.value.code) : ''
)
</script>

<style scoped>
.airport-card {
  position: fixed;
  left: 24px;
  top: 24px;
  width: 280px;
  background: rgba(20, 22, 40, 0.92);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(227, 249, 156, 0.18);
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4), 0 0 0 1px rgba(255,255,255,0.04);
  z-index: 2000;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 6px;
  color: #aaa;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.close-btn:hover {
  background: rgba(255,255,255,0.12);
  color: #fff;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.tower-icon {
  flex-shrink: 0;
  filter: drop-shadow(0 0 6px #e3f99c60);
}

.airport-name {
  font-size: 16px;
  font-weight: 600;
  color: #e3f99c;
  line-height: 1.2;
}

.airport-code {
  font-size: 11px;
  color: #666;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
  margin-top: 2px;
}

.card-divider {
  height: 1px;
  background: rgba(255,255,255,0.08);
  margin-bottom: 12px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.info-label {
  font-size: 11px;
  color: #555;
  min-width: 60px;
  flex-shrink: 0;
}

.info-value {
  font-size: 12px;
  color: #c0c0c0;
}

.info-value.code {
  font-family: 'Courier New', monospace;
  color: #e3f99c;
  background: rgba(227, 249, 156, 0.1);
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 11px;
}

.float-up-enter-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.float-up-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.float-up-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.float-up-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
