<template>
  <div class="stack">
    <div class="weather-hero">
      <div><span>ZBAA · Beijing Capital</span><h3>18°C · VFR</h3><p>METAR 0820Z · Wind 040° / 9 kt · Visibility 10 km</p></div>
      <strong>Low</strong>
    </div>

    <div class="weather-grid">
      <p><span>QNH</span><b>1016 hPa</b></p><p><span>Cloud</span><b>SCT 1200</b></p>
      <p><span>Runway</span><b>36R Open</b></p><p><span>Crosswind</span><b>7 kt</b></p>
    </div>

    <PanelCard title="机场实时气象面板" tag="METAR / TAF">
      <div class="metar-card">
        <code>ZBAA 270820Z 04009KT 9999 SCT012 18/09 Q1016 NOSIG</code>
        <p class="hint">后续可对接机场 METAR、TAF、能见度、云底高、气压与气温数据。</p>
      </div>
      <div class="weather-list"><p><span>首都机场</span><b>适航</b></p><p><span>大兴机场</span><b>侧风关注</b></p><p><span>上海虹桥</span><b>小雨</b></p></div>
    </PanelCard>

    <PanelCard title="气象图层叠加" tag="Layers">
      <ToggleRow v-model="layers.radar" text="降水雷达云图" subtext="显示降水强度、回波移动趋势" />
      <ToggleRow v-model="layers.wind" text="风场与高空急流" subtext="叠加不同高度层风向风速" />
      <ToggleRow v-model="layers.visibility" text="低能见度机场提醒" subtext="突出 RVR / VIS 低于阈值的机场" />
      <ToggleRow v-model="layers.storm" text="雷暴与积冰风险" subtext="用于航路绕飞与延误预警" />
    </PanelCard>

    <PanelCard title="起降相关气象" tag="Next 3h">
      <div class="runway-row"><span>18:00</span><i style="width: 68%"></i><b>稳定</b></div>
      <div class="runway-row"><span>19:00</span><i style="width: 42%"></i><b>侧风下降</b></div>
      <div class="runway-row"><span>20:00</span><i style="width: 78%"></i><b>窗口良好</b></div>
    </PanelCard>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import PanelCard from '../common/PanelCard.vue'
import ToggleRow from '../common/ToggleRow.vue'

const layers = reactive({ radar: true, wind: true, visibility: false, storm: false })
</script>
