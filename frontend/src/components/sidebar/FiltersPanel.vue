<template>
  <div class="stack">
    <PanelCard title="预设高频面板" tag="Quick filters">
      <div class="chips">
        <button v-for="preset in presets" :key="preset" :class="{ picked: activePreset === preset }" @click="activePreset = preset">{{ preset }}</button>
      </div>
      <p class="hint">这些预设用于快速定位高频场景：空中、进港、延误、货运、宽体机等。</p>
    </PanelCard>

    <PanelCard title="精准过滤条件" tag="Flight rules">
      <div class="form">
        <label>航司<select v-model="filters.airline"><option>全部航司</option><option>Air China</option><option>China Southern</option><option>China Eastern</option><option>Hainan Airlines</option></select></label>
        <label>机型<select v-model="filters.aircraft"><option>全部机型</option><option>A320 family</option><option>Boeing 737</option><option>Wide-body</option><option>Cargo aircraft</option></select></label>
        <label>航班状态<select v-model="filters.status"><option>全部状态</option><option>巡航</option><option>进近</option><option>延误</option><option>地面等待</option></select></label>
        <label>高度范围<input v-model="filters.altitude" type="range" min="0" max="12000" /></label>
      </div>
      <div class="filter-summary"><span>当前高度上限</span><b>{{ filters.altitude }} m</b></div>
    </PanelCard>

    <PanelCard title="航线与区域" tag="Geo">
      <div class="chips"><button class="picked">国内干线</button><button>华东区域</button><button>机场周边</button><button>跨境航班</button></div>
      <ToggleRow v-model="geo.onlyInAir" text="只显示空中航班" subtext="隐藏地面滑行、停场与取消航班" />
      <ToggleRow v-model="geo.followViewport" text="仅当前视野内过滤" subtext="提高地图响应速度，适合大流量航班" />
    </PanelCard>

    <PanelCard title="布局与自定义" tag="Saved">
      <div class="saved-layout"><span>当前模板</span><h3>Domestic trunk routes</h3><p class="hint">后续可接入用户保存的过滤模板、面板布局与分享链接。</p></div>
    </PanelCard>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import PanelCard from '../common/PanelCard.vue'
import ToggleRow from '../common/ToggleRow.vue'

const presets = ['空中航班', '延误', '进港', '离港', '货运', '宽体机']
const activePreset = ref('空中航班')
const filters = reactive({ airline: '全部航司', aircraft: '全部机型', status: '全部状态', altitude: 9000 })
const geo = reactive({ onlyInAir: true, followViewport: true })
</script>
