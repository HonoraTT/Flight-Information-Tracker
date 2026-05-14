<template>
  <div class="stack">
    <PanelCard title="预设高频面板" tag="Quick filters">
      <div class="chips">
        <button :class="{ picked: filters.preset === 'airborne' }" @click="filters.preset = 'airborne'">空中航班</button>
        <button :class="{ picked: filters.preset === 'ground' }" @click="filters.preset = 'ground'">地面航班</button>
        <button :class="{ picked: filters.preset === 'all' }" @click="filters.preset = 'all'">全部</button>
      </div>
    </PanelCard>

    <PanelCard title="精准过滤条件" tag="Flight data">
      <div class="form">
        <label>航班号搜索<input v-model="filters.callsign" placeholder="输入 callsign" /></label>
        <label>国家过滤<select v-model="filters.country"><option>全部国家</option><option v-for="country in countries" :key="country">{{ country }}</option></select></label>
        <label>最低高度<input v-model.number="filters.minAltitude" type="range" min="0" max="12000" /></label>
        <label>最高高度<input v-model.number="filters.maxAltitude" type="range" min="0" max="12000" /></label>
        <label>最低速度<input v-model.number="filters.minSpeed" type="range" min="0" max="1200" /></label>
        <label>最高速度<input v-model.number="filters.maxSpeed" type="range" min="0" max="1200" /></label>
      </div>
      <div class="filter-summary"><span>高度</span><b>{{ filters.minAltitude }} - {{ filters.maxAltitude }} m</b></div>
      <div class="filter-summary"><span>速度</span><b>{{ filters.minSpeed }} - {{ filters.maxSpeed }} km/h</b></div>
    </PanelCard>

    <PanelCard title="机场周边" tag="Geo">
      <ToggleRow v-model="filters.airportNearby" text="按选中机场周边过滤" subtext="选择地图机场后，显示机场附近航班" />
    </PanelCard>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PanelCard from '../common/PanelCard.vue'
import ToggleRow from '../common/ToggleRow.vue'
import { useFilterStore } from '../../stores/filterStore'
import { useFlightStore } from '../../stores/flightStore'

const filters = useFilterStore().filters
const flightStore = useFlightStore()
const countries = computed(() => [...new Set(Object.values(flightStore.flights).map(f => f.originCountry).filter(Boolean))].sort())
</script>
