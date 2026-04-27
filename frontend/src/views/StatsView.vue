<template>
  <div class="stats-view">
    <CountryChart />
    <AltitudeChart />
    <TimelineChart />
  </div>
</template>

<script setup>
import CountryChart from '../components/stats/CountryChart.vue'
import AltitudeChart from '../components/stats/AltitudeChart.vue'
import TimelineChart from '../components/stats/TimelineChart.vue'
</script>
