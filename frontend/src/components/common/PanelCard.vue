<template>
  <div class="card">
    <div v-if="title || tag" class="card-head">
      <strong>{{ title }}</strong>
      <em v-if="tag">{{ tag }}</em>
    </div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  tag: { type: String, default: '' },
})
</script>
