<template>
  <div class="stack">
    <div class="weather-hero">
      <div>
        <span>{{ locationName }}</span>
        <h3>{{ normalized.temperature }}°C · {{ normalized.description }}</h3>
        <p>体感 {{ normalized.feelsLike }}°C · 湿度 {{ normalized.humidity }}% · 云量 {{ normalized.clouds }}%</p>
      </div>
      <strong>{{ weatherStatus }}</strong>
    </div>

    <div class="weather-grid compact-weather-grid">
      <p><span>风速</span><b>{{ normalized.windSpeed }} m/s</b></p>
      <p><span>能见度</span><b>{{ normalized.visibility }} km</b></p>
      <p><span>气压</span><b>{{ normalized.pressure }} hPa</b></p>
      <p><span>降水</span><b>{{ normalized.precipitation }} mm</b></p>
    </div>

    <PanelCard title="热门机场天气情况" tag="Weather">
      <div class="popular-weather-grid">
        <article v-for="item in airportWeather" :key="item.code" class="popular-weather-card">
          <div><strong>{{ item.code }}</strong><span>{{ item.name }}</span></div>
          <b>{{ item.status }}</b>
          <p>{{ item.temperature }}°C · 风 {{ item.windSpeed }} m/s</p>
        </article>
      </div>
      <p v-if="weatherError" class="hint">{{ weatherError }}</p>
    </PanelCard>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { fetchWeatherByCoordinates } from '../../api/flightApi'
import { AIRPORTS } from '../../data/airports'
import { useUiStore } from '../../stores/uiStore'
import { translateWeather } from '../../utils/weather'
import PanelCard from '../common/PanelCard.vue'

const uiStore = useUiStore()
const weather = ref(null)
const airportWeather = ref([])
const weatherError = ref('')
const beijing = { code: 'PEK', name: '北京首都国际机场', lat: 40.078538, lon: 116.587095 }
const majorAirports = ['PEK', 'PKX', 'SHA', 'PVG', 'CAN', 'SZX'].map(code => AIRPORTS.find(a => a.code === code)).filter(Boolean)

const selectedLocation = computed(() => uiStore.selectedAirport || beijing)
const locationName = computed(() => selectedLocation.value.name || selectedLocation.value.code || '北京')
const normalized = computed(() => normalizeWeather(weather.value))
const weatherStatus = computed(() => normalized.value.description)

function normalizeWeather(data) {
  const description = data?.weatherDescription ?? data?.weather?.[0]?.description ?? data?.description
  const clouds = typeof data?.clouds === 'number' ? data.clouds : (data?.clouds?.all ?? data?.clouds?.coverage ?? '-')
  return {
    temperature: data?.temperature ?? data?.main?.temp ?? '-',
    feelsLike: data?.feelsLike ?? data?.main?.feels_like ?? data?.main?.feelsLike ?? '-',
    humidity: data?.humidity ?? data?.main?.humidity ?? '-',
    description: translateWeather(description),
    weatherIcon: data?.weatherIcon ?? data?.weather?.[0]?.icon ?? '01d',
    windSpeed: data?.windSpeed ?? data?.wind?.speed ?? '-',
    windDeg: data?.windDeg ?? data?.wind?.deg ?? '-',
    windGust: data?.windGust ?? data?.wind?.gust ?? '-',
    visibility: data?.visibility != null ? (Number(data.visibility) / 1000).toFixed(1) : '-',
    pressure: data?.pressure ?? data?.main?.pressure ?? '-',
    clouds,
    precipitation: data?.precipitation ?? data?.rain?.['1h'] ?? data?.snow?.['1h'] ?? 0,
  }
}

async function loadWeather() {
  const loc = selectedLocation.value
  weatherError.value = ''
  try {
    weather.value = await fetchWeatherByCoordinates(loc.lat, loc.lon)
  } catch {
    weather.value = null
    weatherError.value = '天气接口暂时不可用，请检查后端天气配置。'
  }
}

async function loadAirportWeather() {
  airportWeather.value = await Promise.all(majorAirports.map(async airport => {
    try {
      const data = normalizeWeather(await fetchWeatherByCoordinates(airport.lat, airport.lon))
      return { code: airport.code, name: airport.name, status: data.description, temperature: data.temperature, windSpeed: data.windSpeed }
    } catch {
      return { code: airport.code, name: airport.name, status: '暂不可用', temperature: '-', windSpeed: '-' }
    }
  }))
}

watch(selectedLocation, loadWeather, { immediate: true })
onMounted(loadAirportWeather)
</script>
