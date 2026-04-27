<template>
  <div class="search-bar">
    <input v-model="keyword" placeholder="Search by callsign or ICAO24..." @keydown.enter="doSearch" />
    <button @click="doSearch">Search</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const keyword = ref('')
const router = useRouter()

function doSearch() {
  router.push({ name: 'Search', query: { q: keyword.value } })
}
</script>

<style scoped>
.search-bar {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 900;
  background: rgba(26, 26, 46, 0.9);
  padding: 8px 12px;
  border-radius: 8px;
  backdrop-filter: blur(8px);
}
input {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #444;
  background: #16213e;
  color: #e0e0e0;
  width: 240px;
  outline: none;
}
button {
  padding: 6px 16px;
  border-radius: 4px;
  border: none;
  background: #3b82f6;
  color: #fff;
  cursor: pointer;
}
button:hover { background: #2563eb; }
</style>
