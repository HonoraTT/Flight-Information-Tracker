<template>
  <div class="topbar">
    <div class="brand"><img src="/airplane.svg" alt="" />航班追踪</div>

    <div class="topbar-actions">
      <label class="search">
        ⌕
        <input :value="modelValue" placeholder="搜索航班 / 机场 / 航司" @input="$emit('update:modelValue', $event.target.value)" />
      </label>

      <button class="icon-btn login-btn" type="button" @click="$emit('login-click')">
        <span class="icon-circle">◌</span>
        <span>登录</span>
      </button>

      <button class="icon-btn more-btn" type="button" @click="$emit('more-click')" aria-label="更多">
        ⋮
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
})

defineEmits(['update:modelValue', 'login-click', 'more-click'])
</script>
