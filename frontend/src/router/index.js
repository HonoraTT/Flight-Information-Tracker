import { createRouter, createWebHistory } from 'vue-router'
import MapView from '../views/MapView.vue'
import SearchView from '../views/SearchView.vue'
import StatsView from '../views/StatsView.vue'

const routes = [
  {
    path: '/',
    name: 'Map',
    component: MapView,
  },
  {
    path: '/search',
    name: 'Search',
    component: SearchView,
  },
  {
    path: '/stats',
    name: 'Stats',
    component: StatsView,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
