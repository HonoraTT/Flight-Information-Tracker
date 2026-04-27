<template>
  <div class="chart-wrapper">
    <h3>Flight Traffic Timeline</h3>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)

onMounted(() => {
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: [], smooth: true }],
  })
})
</script>

<style scoped>
.chart-wrapper { background: #1a1a2e; padding: 16px; border-radius: 8px; }
h3 { color: #e0e0e0; margin-bottom: 12px; font-size: 16px; }
.chart { width: 100%; height: 300px; }
</style>
