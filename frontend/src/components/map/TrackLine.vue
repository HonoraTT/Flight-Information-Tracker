import L from 'leaflet'

export function drawTrackLine(map, icao24, trackPoints) {
  if (!trackPoints || trackPoints.length < 2) return null

  const latlngs = trackPoints.map(p => [p.latitude, p.longitude])
  const polyline = L.polyline(latlngs, {
    color: '#3b82f6',
    weight: 2,
    opacity: 0.7,
  }).addTo(map)

  return polyline
}
