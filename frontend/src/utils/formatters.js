export function formatSpeed(value, unit = 'km/h') {
  if (value == null) return '-'
  if (unit === 'm/s') return `${Number(value).toFixed(1)} m/s`
  return `${(Number(value) * 3.6).toFixed(0)} km/h`
}

export function formatAltitude(value, unit = 'meters') {
  if (value == null) return '-'
  if (unit === 'feet') return `${(Number(value) * 3.28084).toFixed(0)} ft`
  return `${Number(value).toFixed(0)} m`
}

export function formatTime(value, mode = 'Local time') {
  if (!value) return '-'
  const timestamp = Number(value) < 10000000000 ? Number(value) * 1000 : Number(value)
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) return '-'
  return mode === 'UTC' ? date.toISOString().replace('T', ' ').slice(0, 19) : date.toLocaleString()
}

export function isMockFlight(flight) {
  return flight?.icao24?.startsWith('mock-cn-') ?? false
}
