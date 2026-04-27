<template>
  <div class="topbar">
    <div class="brand"><img src="/airplane.svg" alt="" />航班追踪</div>
    <label class="search">
      ⌕
      <input :value="modelValue" placeholder="搜索航班 / 机场 / 航司" @input="$emit('update:modelValue', $event.target.value)" />
    </label>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
})

defineEmits(['update:modelValue'])
</script>
