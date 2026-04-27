<template>
  <nav class="rail">
    <button
      v-for="tool in tools"
      :key="tool.id"
      :class="{ active: activeTool === tool.id }"
      @click="$emit('change', tool.id)"
    >
      <em>{{ tool.icon }}</em>
      {{ tool.name }}
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
