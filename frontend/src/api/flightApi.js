import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 10000 })

api.interceptors.response.use(
  res => res.data,
  err => {
    console.error('API error:', err)
    throw err
  }
)

export function fetchFlights() { return api.get('/flights') }
export function fetchFlightByIcao(icao24) { return api.get(`/flights/${icao24}`) }
export function fetchTracks(icao24, seconds = 86400) { return api.get(`/tracks/${icao24}`, { params: { seconds } }) }
export function fetchWeatherByCoordinates(lat, lon) { return api.get('/weather/coordinates', { params: { lat, lon } }) }
export function fetchStats() { return api.get('/stats') }
export default api
