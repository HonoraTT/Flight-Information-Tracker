import { defineStore } from 'pinia'
import { ref } from 'vue'

const CACHE_TTL = 10 * 60 * 1000
const TRACK_LIMIT = 600

export const useFlightStore = defineStore('flight', () => {
  const flights = ref({})
  const trackCache = ref({})
  const loading = ref(false)
  const error = ref(null)
  const lastUpdatedAt = ref(0)

  function normalizeFlight(flight) {
    return {
      ...flight,
      originCountry: flight.originCountry ?? flight.origin_country ?? '-',
      baroAltitude: flight.baroAltitude ?? flight.baro_altitude,
      timePosition: flight.timePosition ?? flight.time_position,
      lastContact: flight.lastContact ?? flight.last_contact,
      heading: flight.heading ?? flight.true_track,
    }
  }

  function pushTrackPoint(flight) {
    if (flight.latitude == null || flight.longitude == null) return
    const list = trackCache.value[flight.icao24] ? [...trackCache.value[flight.icao24]] : []
    const last = list[list.length - 1]
    if (last && last.latitude === flight.latitude && last.longitude === flight.longitude && last.timePosition === flight.timePosition) return
    list.push({ ...flight, recordTime: new Date().toISOString() })
    trackCache.value[flight.icao24] = list.slice(-TRACK_LIMIT)
  }

  function updateFlight(flight) {
    if (!flight?.icao24) return
    const normalized = normalizeFlight(flight)
    flights.value[normalized.icao24] = normalized
    pushTrackPoint(normalized)
    lastUpdatedAt.value = Date.now()
  }

  function updateFlightList(flightList) {
    const now = Date.now()
    const next = { ...flights.value }
    for (const flight of flightList) {
      if (!flight?.icao24) continue
      const normalized = normalizeFlight(flight)
      next[normalized.icao24] = normalized
      pushTrackPoint(normalized)
    }
    for (const [icao24, flight] of Object.entries(next)) {
      const updatedAt = (flight.lastContact || flight.timePosition || 0) * 1000
      if (updatedAt && now - updatedAt > CACHE_TTL) delete next[icao24]
    }
    flights.value = next
    lastUpdatedAt.value = now
  }

  function getCachedTrack(icao24) {
    return trackCache.value[icao24] || []
  }

  function removeFlight(icao24) { delete flights.value[icao24] }
  function clearFlights() { flights.value = {} }

  return { flights, trackCache, loading, error, lastUpdatedAt, updateFlight, updateFlightList, getCachedTrack, removeFlight, clearFlights }
})
