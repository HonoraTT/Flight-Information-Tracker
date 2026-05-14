const WEATHER_CN = {
  'clear sky': '晴',
  'few clouds': '少云',
  'scattered clouds': '少云',
  'broken clouds': '多云',
  'overcast clouds': '阴',
  'light rain': '小雨',
  'moderate rain': '中雨',
  'heavy intensity rain': '大雨',
  'shower rain': '阵雨',
  thunderstorm: '雷暴',
  snow: '雪',
  mist: '薄雾',
  fog: '雾',
  haze: '霾',
}

const WEATHER_ALIASES = {
  散云: '少云',
  云: '多云',
}

export function translateWeather(text) {
  if (!text) return '天气数据暂不可用'
  const key = String(text).toLowerCase().trim()
  const translated = WEATHER_CN[key] || String(text).trim()
  return WEATHER_ALIASES[translated] || translated
}
