import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/login.vue'
import CrudDashboard from '../components/CrudDashboard.vue'

const routes = [
    {
        path: '/',
        name: 'login',
        component: Login,
        meta: {
            title: '登录'
        }
    },
    {
        path: '/CrudDashboard',
        name: 'CrudDashboard',
        component: CrudDashboard,
        meta: {
            title: '数据控制台'
        }
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 全局前置守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
    if (to.meta.title) {
        document.title = to.meta.title
    }
    next()
})

export default router

