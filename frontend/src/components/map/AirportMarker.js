import L from 'leaflet'
import { AIRPORTS } from '../../data/airports'

const SIZE_DEFAULT = 20
const SIZE_SELECTED = 28

// Build lookup map once
const AIRPORT_NAME_MAP = {}
for (const a of AIRPORTS) AIRPORT_NAME_MAP[a.code] = a.name

function buildTowerHtml(size, dark = false) {
  const fill = dark ? '#2f5f75' : '#e3f99c'
  return `
    <div style="
      width: ${size}px;
      height: ${size}px;
      display: flex;
      align-items: center;
      justify-content: center;
    ">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"
           width="${size}" height="${size}">
        <path fill="${fill}"
          d="M687 898.3h-54.3l-35.4-565.9h38.8V275H596l-0.3-11.3
             c0.5-3.9 0.8-7.7 0.8-11.7V125.9
             c0-49.1-36.4-89.6-83.6-96.2v-6.9
             c0-7.6-6.1-13.7-13.7-13.7s-13.7 6.1-13.7 13.7v6.9
             c-47.3 6.7-83.6 47.2-83.6 96.2V252c0 4 0.3 7.9 0.8 11.8l-0.2 11.2h-40.1v57.4h38.8
             l-26 566h-36.3v58.3h-90v58.3h526.3v-58.3H687v-58.4z
             M507 156c0-18.9 15.3-34.2 34.2-34.2 18.9 0 34.2 15.3 34.2 34.2v45H507v-45z
             m-85.4 413.7l4.5-88.7h144.8l5.9 88.7H421.6z
             m0-123.2l4.5-88.7h144.8l5.9 88.7H421.6z"/>
      </svg>
    </div>
  `
}

/**
 * @param {string} code IATA/ICAO 代码
 * @param {boolean} selected 是否选中
 * @returns {L.DivIcon}
 */
export function createAirportIcon(code, selected = false, dark = false) {
  const name = AIRPORT_NAME_MAP[code] || code
  const size = selected ? SIZE_SELECTED : SIZE_DEFAULT
  return L.divIcon({
    className: '',
    html: buildTowerHtml(size, dark),
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    title: name,
  })
}
