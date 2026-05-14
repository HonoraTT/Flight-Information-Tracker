import L from 'leaflet'
import { formatAltitude, formatSpeed } from '../../utils/formatters'

const SIZE_DEFAULT = 26
const SIZE_SELECTED = 34

function buildPlaneHtml(flight, isSelected = false) {
  const heading = flight.heading ?? 0
  const size = isSelected ? SIZE_SELECTED : SIZE_DEFAULT
  const fill = flight.onGround ? '#8a97a6' : '#2f8ee5'
  return `
    <div class="plane-icon-wrap" style="
      width: ${size}px;
      height: ${size}px;
      display: flex;
      align-items: center;
      justify-content: center;
      transform: rotate(${heading}deg);
      transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    ">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"
           width="${size}" height="${size}"
           stroke="#ffffff" stroke-width="26" stroke-linejoin="round">
        <path fill="${fill}" d="M995.68 658.82c-8.94-1.49-23.85-2.98-44.72-4.47-73.04-8.94-207.18-26.83-295.13-38.75l-19.38-5.96h-2.98l-44.72-13.41c-4.47 90.92-10.43 166.94-17.89 225.07v10.43l22.36 10.43 70.06 32.79c2.98 1.49 4.47 4.47 4.47 7.45l1.49 55.15v4.47c0 2.98-1.49 5.96-4.47 4.47l-114.77-19.38c-8.94 22.36-17.89 34.28-31.3 35.77h-1.49c-13.41 0-23.85-11.92-31.3-35.77l-114.77 17.89c-2.98 0-4.47-1.49-4.47-4.47v-4.47l1.49-55.15c0-2.98 1.49-5.96 4.47-7.45l70.06-32.79 22.36-10.43v-25.88c-5.96-58.13-10.43-134.15-14.91-225.07l-46.21 13.41h-4.47l-19.38 4.47c-87.94 10.43-222.09 26.83-295.13 35.77-20.87 2.98-35.77 4.47-44.72 4.47-23.85 2.98-25.34-26.83-7.45-37.26 2.98-1.49 56.64-29.81 123.71-67.07v-38.75c0-14.91 11.92-26.83 26.83-26.83s26.83 11.92 26.83 26.83v8.94c40.24-20.87 81.98-43.23 119.24-64.09v-44.72c0-14.91 11.92-26.83 26.83-26.83s26.83 11.92 26.83 26.83v14.91c23.85-11.92 43.23-23.85 56.64-31.3-1.49-174.39 11.92-308.54 61.11-332.39 2.98-1.49 4.47-1.49 7.45-2.98 1.49 0 4.47 0 5.96-1.49h5.96c1.49 0 4.47 0 5.96 1.49 2.98 0 4.47 1.49 7.45 2.98 49.19 23.85 62.6 158 58.13 332.39 13.41 7.45 32.79 17.89 55.15 29.81v-14.91c0-14.91 11.92-26.83 26.83-26.83s26.83 11.92 26.83 26.83v44.72c37.26 20.87 79 43.23 119.24 65.58v-14.91c0-14.91 11.92-26.83 26.83-26.83s26.83 11.92 26.83 26.83v44.72c68.56 37.26 122.22 67.07 123.71 68.56 25.34 10.43 23.85 41.74-1.49 38.75z"/>
      </svg>
    </div>
  `
}

function tooltipHtml(flight) {
  const callsign = flight.callsign?.trim() || '未知呼号'
  return `
    <div class="flight-tooltip">
      <strong>${callsign}</strong>
      <span>高度：${formatAltitude(flight.baroAltitude)}</span>
      <span>速度：${formatSpeed(flight.velocity)}</span>
      <span>国家：${flight.originCountry || '-'}</span>
    </div>
  `
}

export function createFlightIcon(flight, isSelected = false) {
  const size = isSelected ? SIZE_SELECTED : SIZE_DEFAULT
  return L.divIcon({
    className: '',
    html: buildPlaneHtml(flight, isSelected),
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  })
}

const markerMap = new Map()

export function updateMarkers(map, flights, onClick) {
  if (!map) return
  const incoming = new Set(flights.map(f => f.icao24))

  for (const [icao24, marker] of markerMap) {
    if (!incoming.has(icao24)) {
      map.removeLayer(marker)
      markerMap.delete(icao24)
    }
  }

  for (const flight of flights) {
    const { icao24, latitude, longitude } = flight
    if (latitude == null || longitude == null) continue

    if (markerMap.has(icao24)) {
      const marker = markerMap.get(icao24)
      marker.setLatLng([latitude, longitude])
      marker.setIcon(createFlightIcon(flight, false))
      marker.setTooltipContent(tooltipHtml(flight))
    } else {
      const marker = L.marker([latitude, longitude], { icon: createFlightIcon(flight, false) })
      marker.bindTooltip(tooltipHtml(flight), { direction: 'top', offset: [0, -12], opacity: 0.95 })
      marker.on('click', () => onClick(flight))
      marker.addTo(map)
      markerMap.set(icao24, marker)
    }
  }
}

export function refreshMarkerIcon(map, icao24, flight, isSelected) {
  const marker = markerMap.get(icao24)
  if (marker) marker.setIcon(createFlightIcon(flight, isSelected))
}

export function clearAllMarkers(map) {
  if (!map) return
  for (const marker of markerMap.values()) map.removeLayer(marker)
  markerMap.clear()
}
