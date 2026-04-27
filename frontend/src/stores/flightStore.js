import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useFlightStore = defineStore('flight', () => {
  const flights = ref({})          // key: icao24, value: flight object
  const loading = ref(false)
  const error = ref(null)

  function updateFlight(flight) {
    flights.value[flight.icao24] = flight
  }

  function removeFlight(icao24) {
    delete flights.value[icao24]
  }

  function clearFlights() {
    flights.value = {}
  }

  return { flights, loading, error, updateFlight, removeFlight, clearFlights }
})
