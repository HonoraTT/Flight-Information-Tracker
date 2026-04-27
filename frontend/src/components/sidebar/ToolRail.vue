<template>
  <nav class="floating-tool-rail" aria-label="地图功能选择">
    <button
      v-for="tool in tools"
      :key="tool.id"
      type="button"
      :class="{ active: activeTool === tool.id }"
      @click="$emit('change', tool.id)"
    >
      <em>{{ tool.icon }}</em>
      <span>{{ tool.name }}</span>
    </button>
  </nav>
</template>

<script setup>
defineProps({
  tools: { type: Array, default: () => [] },
  activeTool: { type: String, default: 'weather' },
})

defineEmits(['change'])
</script>
