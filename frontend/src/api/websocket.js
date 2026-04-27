import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { useFlightStore } from '../stores/flightStore'
import { useUiStore } from '../stores/uiStore'

let stompClient = null

export function connectWebSocket() {
  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)

  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/flights', (message) => {
      const flight = JSON.parse(message.body)
      const flightStore = useFlightStore()
      flightStore.updateFlight(flight)
    })

    stompClient.subscribe('/topic/flights/removed', (message) => {
      const { icao24 } = JSON.parse(message.body)
      const flightStore = useFlightStore()
      flightStore.removeFlight(icao24)
    })
  }, (err) => {
    const uiStore = useUiStore()
    uiStore.addToast('WebSocket connection failed', 'error')
    console.error('STOMP error:', err)
  })
}

export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.disconnect()
    stompClient = null
  }
}
