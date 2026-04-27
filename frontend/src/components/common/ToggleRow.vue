<template>
  <label class="toggle">
    <span>
      {{ text }}
      <small v-if="subtext">{{ subtext }}</small>
    </span>
    <input type="checkbox" :checked="modelValue" @change="$emit('update:modelValue', $event.target.checked)" />
  </label>
</template>

<script setup>
defineProps({
  text: { type: String, default: '' },
  subtext: { type: String, default: '' },
  modelValue: { type: Boolean, default: false },
})

defineEmits(['update:modelValue'])
</script>
