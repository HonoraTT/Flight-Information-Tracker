import L from 'leaflet'

/**
 * 在指定地图实例上为某航班绘制完整历史轨迹，
 * 返回 L.polyline 实例。
 *
 * @param {L.Map} map
 * @param {Array<{latitude: number, longitude: number}>} trackPoints
 * @param {{ color?: string; weight?: number; opacity?: number }} options
 * @returns {L.Polyline | null}
 */
export function drawTrackLine(map, trackPoints, options = {}) {
  if (!map || !trackPoints || trackPoints.length < 2) return null

  const latlngs = trackPoints.map(p => [p.latitude, p.longitude])
  return L.polyline(latlngs, {
    color: '#3b82f6',
    weight: 2,
    opacity: 0.6,
    ...options,
  }).addTo(map)
}

/**
 * 清除轨迹折线
 * @param {L.Map} map
 * @param {L.Polyline} polyline
 */
export function removeTrackLine(map, polyline) {
  if (map && polyline) map.removeLayer(polyline)
}
