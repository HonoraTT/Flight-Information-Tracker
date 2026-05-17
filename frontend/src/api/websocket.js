import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { useSettingsStore } from '../stores/settingsStore'
import { useUiStore } from '../stores/uiStore'

let stompClient = null
let reconnectTimer = null
let heartbeatTimer = null

function scheduleReconnect() {
  clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => connectWebSocket(), 3000)
}

function startHeartbeat() {
  clearInterval(heartbeatTimer)
  const settingsStore = useSettingsStore()
  heartbeatTimer = setInterval(() => {
    if (!stompClient?.connected) scheduleReconnect()
  }, settingsStore.refreshMs)
}

export function connectWebSocket() {
  if (stompClient?.connected) return
  const socket = new SockJS('/ws-flights')
  stompClient = Stomp.over(socket)
  stompClient.debug = () => {}

  const settingsStore = useSettingsStore()
  stompClient.heartbeat.outgoing = settingsStore.refreshMs
  stompClient.heartbeat.incoming = settingsStore.refreshMs

  stompClient.connect({}, () => {
    startHeartbeat()
  }, () => {
    useUiStore().addToast('实时航班连接异常，正在尝试重连', 'error')
    scheduleReconnect()
  })
}

export function disconnectWebSocket() {
  clearTimeout(reconnectTimer)
  clearInterval(heartbeatTimer)
  if (stompClient) {
    stompClient.disconnect()
    stompClient = null
  }
}
