<template>
  <div class="stack">
    <PanelCard title="地图与图层基础设置" tag="View">
      <ToggleRow v-model="layers.routes" text="航路与管制区边界" subtext="显示主要航路、FIR 与管制区边界" />
      <ToggleRow v-model="layers.airports" text="机场 IATA / ICAO 标签" subtext="在地图缩放到合适级别时显示机场标识" />
      <ToggleRow v-model="layers.lowLight" text="低亮度地图模式" subtext="适合夜间监控与长时间值守" />
      <ToggleRow v-model="layers.terrain" text="地形与海岸线强调" subtext="增强海陆边界与地形层识别" />
    </PanelCard>

    <PanelCard title="数据与刷新规则" tag="Mock API">
      <div class="chips">
        <button v-for="item in refreshOptions" :key="item" :class="{ picked: refreshRate === item }" @click="refreshRate = item">{{ item }}</button>
      </div>
      <div class="setting-row"><span>航班标签密度</span><b>{{ labelDensity }}</b></div>
      <input v-model="densityValue" type="range" min="1" max="3" />
      <p class="hint">当前只模拟前端刷新策略，后续可替换为真实接口、WebSocket 或轮询。</p>
    </PanelCard>

    <PanelCard title="单位与国际化配置" tag="Locale">
      <div class="form two-col">
        <label>速度单位<select v-model="units.speed"><option>km/h</option><option>kt</option><option>mph</option></select></label>
        <label>高度单位<select v-model="units.altitude"><option>meters</option><option>feet</option></select></label>
        <label>时间格式<select v-model="units.time"><option>Local time</option><option>UTC</option></select></label>
        <label>语言<select v-model="units.language"><option>中文 / English</option><option>English</option></select></label>
      </div>
    </PanelCard>

    <PanelCard title="告警与辅助设置" tag="Safety">
      <ToggleRow v-model="alerts.delay" text="延误航班高亮" subtext="在地图中突出展示延误或取消状态" />
      <ToggleRow v-model="alerts.weather" text="气象风险提示" subtext="低能见度、强侧风、雷暴等机场运行风险" />
      <ToggleRow v-model="alerts.a11y" text="辅助阅读模式" subtext="提高文本对比度并减少装饰元素" />
    </PanelCard>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import PanelCard from '../common/PanelCard.vue'
import ToggleRow from '../common/ToggleRow.vue'

const refreshOptions = ['5s', '15s', '30s', '60s']
const refreshRate = ref('15s')
const densityValue = ref(2)
const layers = reactive({ routes: true, airports: true, lowLight: false, terrain: true })
const units = reactive({ speed: 'km/h', altitude: 'meters', time: 'Local time', language: '中文 / English' })
const alerts = reactive({ delay: true, weather: true, a11y: false })
const densityLabels = { 1: '低密度', 2: '标准', 3: '高密度' }
const labelDensity = computed(() => densityLabels[densityValue.value])
</script>
