<template>
  <div id="app" :class="{ 'a11y-mode': settingsStore.settings.alerts.a11y }">
    <router-view />
    <StatusToast />
    <BgmPlayer />
  </div>
</template>

<script setup>
import BgmPlayer from './components/common/BgmPlayer.vue'
import StatusToast from './components/common/StatusToast.vue'
import { useSettingsStore } from './stores/settingsStore'

const settingsStore = useSettingsStore()
</script>
