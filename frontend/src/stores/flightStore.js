import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useFlightStore = defineStore('flight', () => {
  const flights = ref({})          // key: icao24, value: flight object
  const loading = ref(false)
  const error = ref(null)

  function updateFlight(flight) {
    flights.value[flight.icao24] = flight
  }

  function updateFlightList(flightList) {
    const newMap = {}
    for (const flight of flightList) {
      if (flight.icao24) newMap[flight.icao24] = flight
    }
    flights.value = newMap
  }

  function removeFlight(icao24) {
    delete flights.value[icao24]
  }

  function clearFlights() {
    flights.value = {}
  }

  return { flights, loading, error, updateFlight, updateFlightList, removeFlight, clearFlights }
})
