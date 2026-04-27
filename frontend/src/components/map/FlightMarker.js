import L from 'leaflet'

export function createFlightIcon(flight) {
  const angle = flight.true_track || 0
  return L.divIcon({
    html: `<img src="/airplane.svg" style="transform: rotate(${angle}deg); width: 24px; height: 24px;" />`,
    className: 'flight-icon',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
  })
}
