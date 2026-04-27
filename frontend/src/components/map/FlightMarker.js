import L from 'leaflet'

const COLOR_DEFAULT = '#3b82f6'
const COLOR_SELECTED = '#f97316'
const SIZE_DEFAULT = 26
const SIZE_SELECTED = 34

/**
 * 创建飞机图标 DOM HTML
 * @param {number} heading  航向角度（度数）
 * @param {boolean} isSelected  是否选中态
 * @returns {string} HTML 字符串
 */
function buildPlaneHtml(heading = 0, isSelected = false) {
  const color = isSelected ? COLOR_SELECTED : COLOR_DEFAULT
  const size = isSelected ? SIZE_SELECTED : SIZE_DEFAULT
  const glow = isSelected
    ? `filter: drop-shadow(0 0 6px ${color}) drop-shadow(0 0 12px ${color}80);`
    : `filter: drop-shadow(0 0 4px ${color}90);`

  return `
    <div class="plane-icon-wrap" style="
      width: ${size}px;
      height: ${size}px;
      display: flex;
      align-items: center;
      justify-content: center;
      transform: rotate(${heading}deg);
      transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      ${glow}
    ">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
           width="${size}" height="${size}">
        <path fill="${color}" d="M21 16v-2l-8-5V3.5A1.5 1.5 0 0 0 11.5 2 1.5 1.5 0 0 0 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
      </svg>
    </div>
  `
}

/**
 * 创建 Leaflet DivIcon
 * @param {object} flight  航班对象
 * @param {boolean} isSelected  是否选中态
 * @returns {L.DivIcon}
 */
export function createFlightIcon(flight, isSelected = false) {
  const heading = flight.true_track ?? 0
  const size = isSelected ? SIZE_SELECTED : SIZE_DEFAULT

  return L.divIcon({
    className: '',
    html: buildPlaneHtml(heading, isSelected),
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  })
}

// ─── 全局 marker 管理 ───────────────────────────────────────────────────────

/** @type {Map<string, L.Marker>} */
const markerMap = new Map()

/**
 * 统一的 marker 更新函数，由 FlightMap.vue 调用
 * @param {L.Map} map
 * @param {object[]} flights  航班数组
 * @param {Function} onClick  点击回调，接收 (flight) 参数
 */
export function updateMarkers(map, flights, onClick) {
  if (!map) return

  const incoming = new Set(flights.map(f => f.icao24))

  // 1. 清理已消失的航班
  for (const [icao24, marker] of markerMap) {
    if (!incoming.has(icao24)) {
      map.removeLayer(marker)
      markerMap.delete(icao24)
    }
  }

  // 2. 新增 / 更新
  for (const flight of flights) {
    const { icao24, latitude, longitude } = flight
    if (latitude == null || longitude == null) continue

    if (markerMap.has(icao24)) {
      const marker = markerMap.get(icao24)
      marker.setLatLng([latitude, longitude])
      marker.setIcon(createFlightIcon(flight, false))
    } else {
      const marker = L.marker([latitude, longitude], {
        icon: createFlightIcon(flight, false),
      })
      marker.on('click', () => onClick(flight))
      marker.addTo(map)
      markerMap.set(icao24, marker)
    }
  }
}

/**
 * 更新单个 marker 的选中态图标
 * @param {L.Map} map
 * @param {string} icao24
 * @param {object} flight
 * @param {boolean} isSelected
 */
export function refreshMarkerIcon(map, icao24, flight, isSelected) {
  const marker = markerMap.get(icao24)
  if (marker) marker.setIcon(createFlightIcon(flight, isSelected))
}

/**
 * 清除所有 marker（组件卸载时调用）
 * @param {L.Map} map
 */
export function clearAllMarkers(map) {
  if (!map) return
  for (const marker of markerMap.values()) {
    map.removeLayer(marker)
  }
  markerMap.clear()
}
