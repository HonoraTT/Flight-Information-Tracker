<template>
  <div class="stack">
    <PanelCard title="时间点回溯" tag="Time machine">
      <input v-model="playbackTime" class="date" type="datetime-local" />
      <div class="chips playback-buttons"><button>◀ 15m</button><button class="picked">Play</button><button>15m ▶</button></div>
      <div class="timeline-card"><span>00:00</span><div class="progress"><i style="width: 54%"></i></div><span>24:00</span></div>
      <p class="hint">后续可按时间点请求历史快照，并把地图状态恢复到指定时刻。</p>
    </PanelCard>

    <PanelCard title="单航班全轨迹回放" tag="CSN318">
      <div class="track-head"><b>CAN → PEK</b><span>A350-900 · 2h 47m</span></div>
      <div class="mini-route"><i></i><i></i><i></i><i></i><i></i></div>
      <div class="track-stats"><p><span>最大高度</span><b>11,280 m</b></p><p><span>最大速度</span><b>913 km/h</b></p><p><span>航点</span><b>86</b></p></div>
    </PanelCard>

    <PanelCard title="历史数据导出与复盘" tag="Export">
      <div class="form two-col">
        <label>范围<select v-model="exportConfig.range"><option>最近 24 小时</option><option>最近 7 天</option><option>自定义</option></select></label>
        <label>格式<select v-model="exportConfig.format"><option>CSV</option><option>JSON</option><option>GeoJSON</option></select></label>
      </div>
      <div class="chips"><button class="picked">生成复盘</button><button>导出文件</button></div>
    </PanelCard>

    <PanelCard title="复盘备注" tag="Notes">
      <div class="review-note">延误原因、绕飞点、天气影响与运行恢复情况将在这里形成复盘摘要。</div>
    </PanelCard>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import PanelCard from '../common/PanelCard.vue'

const playbackTime = ref('2026-04-27T15:30')
const exportConfig = reactive({ range: '最近 7 天', format: 'CSV' })
</script>
