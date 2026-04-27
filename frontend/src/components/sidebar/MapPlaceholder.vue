<template>
  <div class="map-canvas">
    <div class="land land-a"></div>
    <div class="land land-b"></div>
    <div class="route r1"></div>
    <div class="route r2"></div>
    <div class="route r3"></div>

    <button
      v-for="flight in flights"
      :key="flight.id"
      class="marker"
      :class="{ active: selectedFlight.id === flight.id }"
      :style="{ left: flight.x + '%', top: flight.y + '%' }"
      @click="$emit('select-flight', flight)"
    >
      <i :style="{ transform: `rotate(${flight.heading}deg)` }">✈</i>
      <span>{{ flight.callsign }}</span>
    </button>

    <article class="flight-card glass">
      <p class="eyebrow">Selected flight</p>
      <div class="title-row">
        <h1>{{ selectedFlight.callsign }}</h1>
        <b :class="selectedFlight.type">{{ selectedFlight.status }}</b>
      </div>
      <small>{{ selectedFlight.airline }} · {{ selectedFlight.aircraft }}</small>
      <div class="od"><span>{{ selectedFlight.from }}</span><strong>→</strong><span>{{ selectedFlight.to }}</span></div>
      <dl>
        <div><dt>速度</dt><dd>{{ selectedFlight.speed }}</dd></div>
        <div><dt>高度</dt><dd>{{ selectedFlight.altitude }}</dd></div>
        <div><dt>航向</dt><dd>{{ selectedFlight.heading }}°</dd></div>
        <div><dt>ETA</dt><dd>{{ selectedFlight.eta }}</dd></div>
      </dl>
    </article>

    <div class="controls"><button>＋</button><button>－</button><button>⤢</button><button>⌖</button></div>
    <div class="legend glass"><span><i class="blue-dot"></i>巡航</span><span><i class="green-dot"></i>进近</span><span><i class="orange-dot"></i>延误</span></div>
  </div>
</template>

<script setup>
defineProps({
  flights: { type: Array, default: () => [] },
  selectedFlight: { type: Object, required: true },
})

defineEmits(['select-flight'])
</script>
