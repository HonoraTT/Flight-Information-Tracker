import L from 'leaflet'
import { AIRPORTS } from '../../data/airports'

const SIZE_DEFAULT = 24
const SIZE_SELECTED = 32

// Build lookup maps once
const AIRPORT_NAME_MAP = {}
const AIRPORT_COLOR_MAP = {}
for (const a of AIRPORTS) {
  AIRPORT_NAME_MAP[a.code] = a.name
  AIRPORT_COLOR_MAP[a.code] = a.markerColor || '#22c55e'
}

/**
 * @param {string} code IATA/ICAO 代码
 * @param {boolean} selected 是否选中
 * @param {string|boolean} [colorOrDark] 覆盖颜色字符串，或 dark 模式布尔值（兼容旧调用）
 * @returns {L.DivIcon}
 */
export function createAirportIcon(code, selected = false, colorOrDark) {
  const name = AIRPORT_NAME_MAP[code] || code
  const size = selected ? SIZE_SELECTED : SIZE_DEFAULT
  let markerColor = AIRPORT_COLOR_MAP[code] || '#22c55e'
  if (typeof colorOrDark === 'string') {
    markerColor = colorOrDark
  } else if (colorOrDark === true) {
    markerColor = '#2f5f75'
  }
  const sizePx = `${size}px`

  return L.divIcon({
    className: '',
    html: `
      <div style="
        width: ${sizePx};
        height: ${sizePx};
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <svg xmlns="http://www.w3.org/2000/svg"
             viewBox="0 0 1024 1024"
             width="${size}"
             height="${size}"
             class="airport-marker"
             data-code="${code}"
        >
          <!-- 外层底座 -->
          <path class="marker-bg"
                fill="${markerColor}"
                d="M792.629688 546.001972h0.378273L515.979769 942.336885 239.075642 546.001972h0.35273c-27.245365-46.694176-43.118222-100.710544-43.118222-158.546129 0-175.075794 143.135469-317.043604 319.707325-317.043604 176.620509 0 319.708541 141.967811 319.708541 317.043604-0.001216 57.835584-15.752443 111.851952-43.096328 158.546129z"/>
          <!-- 飞机 -->
          <path class="plane-icon" fill="#FFFFFF"
                d="M685.568785 397.658259l-133.580131-76.384325a6.628895 6.628895 0 0 1-3.346071-6.13264l3.627039-65.527534a31.399066 31.399066 0 0 0-8.448496-23.609812 31.032957 31.032957 0 0 0-22.862996-9.642913h-4.493053c-8.845013 0-16.966321 3.439727-22.911648 9.687917-5.919785 6.272515-8.891233 14.626139-8.399844 23.496694l3.62704 65.594432a6.486586 6.486586 0 0 1-3.322962 6.109529l-133.602024 76.384326a9.337619 9.337619 0 0 0-4.704691 8.144418v27.917985a9.338835 9.338835 0 0 0 12.310283 8.893665l128.033753-42.311808c1.215095 0.053518 2.396133 0.433007 3.417834 1.098329a6.793096 6.793096 0 0 1 2.970231 5.056204l4.7278 76.80517a6.454962 6.454962 0 0 1-3.886113 6.364955l-40.346249 18.956205a9.365594 9.365594 0 0 0-5.38096 8.472822v12.284741a9.432491 9.432491 0 0 0 3.510273 7.324624 9.50547 9.50547 0 0 0 7.88656 1.826899l57.8964-12.661797 58.808633 12.731127a9.292615 9.292615 0 0 0 7.864666-1.848793 9.297481 9.297481 0 0 0 3.487164-7.301514v-12.355287a9.365594 9.365594 0 0 0-5.383392-8.472822l-40.39247-18.907553a6.59727 6.59727 0 0 1-3.837461-6.481721l4.729017-76.712729a6.637409 6.637409 0 0 1 2.948338-5.008769c0.655592-0.469496 1.824466-0.959669 5.781126-0.772357l125.644918 41.912858a9.381406 9.381406 0 0 0 12.333392-8.892449v-27.896091a9.321807 9.321807 0 0 0-4.705906-8.141986z"/>
        </svg>
      </div>
    `,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    title: name,
  })
}

/**
 * 动态更新已渲染的机场标记颜色（无需重建 Icon）。
 * @param {L.Map} map
 * @param {string} code
 * @param {string} color hex 色值
 */
export function updateAirportMarkerColor(map, code, color) {
  AIRPORT_COLOR_MAP[code] = color
  if (!map) return
  const el = document.querySelector(
    `.airport-marker[data-code="${code}"] .marker-bg`
  )
  if (el) el.setAttribute('fill', color)
}
