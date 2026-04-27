<template>
  <div class="map-view">
    <SearchBar />
    <FlightMap />
    <FlightDetailSidebar />
    <StatusToast />
  </div>
</template>

<script setup>
import FlightMap from '../components/map/FlightMap.vue'
import SearchBar from '../components/common/SearchBar.vue'
import FlightDetailSidebar from '../components/sidebar/FlightDetailSidebar.vue'
import StatusToast from '../components/common/StatusToast.vue'
import { connectWebSocket } from '../api/websocket'

connectWebSocket()
</script>
