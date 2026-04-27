<template>
  <transition-group tag="div" name="toast" class="toast-container">
    <div
      v-for="toast in uiStore.toasts"
      :key="toast.id"
      :class="['toast', `toast--${toast.type}`]"
      @click="uiStore.removeToast(toast.id)"
    >
      {{ toast.message }}
    </div>
  </transition-group>
</template>

<script setup>
import { useUiStore } from '../../stores/uiStore'
const uiStore = useUiStore()
</script>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2000;
}
.toast {
  padding: 10px 18px;
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  min-width: 220px;
}
.toast--error { background: #ef4444; }
.toast--success { background: #22c55e; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(20px); }
</style>
