<template>
  <section class="panel">
    <p class="kicker">{{ config.kicker }}</p>
    <h2>{{ config.title }}</h2>
    <p class="desc">{{ config.desc }}</p>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  config: {
    type: Object,
    required: true,
  },
})
</script>
