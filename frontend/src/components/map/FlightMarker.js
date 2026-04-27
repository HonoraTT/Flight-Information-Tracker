import L from 'leaflet'

const DEFAULT_COLOR = '#3b82f6'
const SELECTED_COLOR = '#f97316'

export function createFlightIcon(flight, isSelected = false) {
  const angle = flight.true_track ?? 0
  const color = isSelected ? SELECTED_COLOR : DEFAULT_COLOR
  const size = isSelected ? 32 : 24
  const half = size / 2

  return L.divIcon({
    html: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
           width="${size}" height="${size}"
           style="transform: rotate(${angle}deg); filter: drop-shadow(0 0 4px ${color});">
        <path fill="${color}" d="M21 16v-2l-8-5V3.5A1.5 1.5 0 0 0 11.5 2 1.5 1.5 0 0 0 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
      </svg>
    `,
    className: 'flight-icon',
    iconSize: [size, size],
    iconAnchor: [half, half],
  })
}
