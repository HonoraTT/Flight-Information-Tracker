<template>
  <audio ref="audioRef" src="/Andrew Prahlow - Outer Wilds.mp3" loop preload="auto" />
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useSettingsStore } from '../../stores/settingsStore'

const audioRef = ref(null)
const settingsStore = useSettingsStore()

async function tryPlay() {
  const audio = audioRef.value
  if (!audio || !settingsStore.settings.bgm.enabled || settingsStore.settings.alerts.voice) return
  audio.volume = settingsStore.settings.bgm.volume
  try {
    audio.muted = true
    await audio.play()
    audio.muted = false
  } catch {
    const resume = async () => {
      try {
        if (!settingsStore.settings.bgm.enabled || settingsStore.settings.alerts.voice) return
        audio.muted = false
        audio.volume = settingsStore.settings.bgm.volume
        await audio.play()
      } finally {
        window.removeEventListener('click', resume)
      }
    }
    window.addEventListener('click', resume, { once: true })
  }
}

onMounted(tryPlay)

watch(() => settingsStore.settings.bgm.enabled, (enabled) => {
  if (!audioRef.value) return
  if (enabled && !settingsStore.settings.alerts.voice) tryPlay()
  else audioRef.value.pause()
})

watch(() => settingsStore.settings.alerts.voice, (enabled) => {
  if (!audioRef.value) return
  if (enabled) audioRef.value.pause()
  else if (settingsStore.settings.bgm.enabled) tryPlay()
})

watch(() => settingsStore.settings.bgm.volume, (volume) => {
  if (audioRef.value) audioRef.value.volume = volume
})
</script>
