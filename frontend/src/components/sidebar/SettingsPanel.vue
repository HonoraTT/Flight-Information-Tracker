<template>
  <div class="stack">
    <PanelCard title="地图与图层基础设置" tag="View">
      <ToggleRow v-model="settings.layers.airports" text="机场标签" subtext="控制地图上的机场标记显示" />
      <ToggleRow v-model="settings.layers.lowLight" text="低亮度地图模式" subtext="切换地图区域低亮度显示" />
      <ToggleRow v-model="settings.layers.terrain" text="地形底图" subtext="开启为地形底图，关闭为普通底图" />
    </PanelCard>

    <PanelCard title="数据与刷新规则" tag="WebSocket">
      <div class="chips">
        <button v-for="item in refreshOptions" :key="item" :class="{ picked: settings.refreshRate === item }" @click="settings.refreshRate = item">{{ item }}</button>
      </div>
      <p class="hint">这里控制 WebSocket 心跳/重连检测频率；后端推送频率仍由后端决定。</p>
    </PanelCard>

    <PanelCard title="单位配置" tag="Units">
      <div class="form two-col">
        <label>速度单位<select v-model="settings.units.speed"><option>m/s</option><option>km/h</option></select></label>
        <label>高度单位<select v-model="settings.units.altitude"><option value="meters">米</option><option value="feet">英尺</option></select></label>
        <label>时间格式<select v-model="settings.units.time"><option>Local time</option><option>UTC</option></select></label>
      </div>
    </PanelCard>

    <PanelCard title="BGM 设置" tag="Audio">
      <ToggleRow v-model="settings.bgm.enabled" text="自动循环播放 BGM" subtext="受浏览器限制时，会在首次点击页面后播放；开启语音播报时会自动暂停 BGM" />
      <div class="setting-row"><span>音量</span><b>{{ Math.round(settings.bgm.volume * 100) }}%</b></div>
      <input v-model.number="settings.bgm.volume" type="range" min="0" max="1" step="0.01" />
    </PanelCard>

    <PanelCard title="辅助设置" tag="A11y">
      <ToggleRow v-model="settings.alerts.a11y" text="辅助阅读模式" subtext="提高整体对比度并减少背景透明感" />
      <ToggleRow v-model="settings.alerts.voice" text="语音播报" subtext="开启后播报机场天气和航班详情；语音播报与 BGM 不能同时播放，开启后 BGM 会暂停" />
    </PanelCard>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import PanelCard from '../common/PanelCard.vue'
import ToggleRow from '../common/ToggleRow.vue'
import { useSettingsStore } from '../../stores/settingsStore'

const refreshOptions = ['5s', '15s', '30s', '60s']
const settings = useSettingsStore().settings

watch(() => settings.alerts.voice, (enabled) => {
  if (enabled) settings.bgm.enabled = false
})
watch(() => settings.bgm.enabled, (enabled) => {
  if (enabled) settings.alerts.voice = false
})
</script>
