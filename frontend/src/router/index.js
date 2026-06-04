import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/login.vue'
import Index from '../components/Index.vue'
import FrontPage from '../components/FrontPage.vue'
import PlaceholderPage from '../components/PlaceholderPage.vue'
import CrudDashboard from '../Admins/CrudDashboard.vue'
import HomePage from '../components/Homepage.vue'
import UpdatePage from '../components/update.vue'
import DailyShop from '../shops/DailyShop.vue'
import BookShop from '../shops/BookShop.vue'
import SportsShop from '../shops/SportsShop.vue'
import DigitalShop from '../shops/DigitalShop.vue'
import GoodsManager from "../components/GoodsManager.vue";
import GoodDetail from "../shops/GoodDetail.vue";
import AdminLogin from '../Admins/AdminLogin.vue'
import AdminDashboard from '../Admins/AdminDashboard.vue'
import OrderManager from  '../components/OrderManager.vue'


const placeholderRoutes = [

  {
    path: '/DigitalShop',
    menuIndex: '/DigitalShop',
    title: '游戏商城',
    description: '游戏数码分类页还未实现，当前先保留路由入口。',
  },
  {
    path: '/SportsShop',
    menuIndex: '/SportsShop',
    title: '音乐商城',
    description: '音乐影音分类页还未实现，当前先保留路由入口。',
  },
  {
    path: '/BookShop',
    menuIndex: '/BookShop',
    title: '书刊商城',
    description: '教材书刊分类页还未实现，当前先保留路由入口。',
  },
  {
    path: '/PetShop',
    menuIndex: '/PetShop',
    title: '萌宠商城',
    description: '萌宠用品分类页还未实现，当前先保留路由入口。',
  },

  {
    path: '/UserManager',
    menuIndex: '/UserManager',
    title: '用户管理',
    description: '用户管理页还未实现，后续可以提供用户列表、封禁和权限管理。',
  },
]

const routes = [
  {
    path: '/',
    name: 'login',
    component: Login,
    meta: {
      title: '登录',
    },
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: {
      title: '管理员登录',
    },
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: {
      title: '后台管理系统',
    },
  },
  {
    path: '/admin/crud-dashboard',
    name: 'AdminCrudDashboard',
    component: CrudDashboard,
    meta: {
      title: '数据控制台',
    },
  },
  {
    path: '/FrontPage',
    component: Index,
    children: [
      {
        path: '',
        name: 'FrontPage',
        component: FrontPage,
        meta: {
          title: '南京大学校园二手交易平台',
          menuIndex: '/FrontPage',
        }
      },
      {
        path: '/HomePage',
        name: 'HomePage',
        component: HomePage,
        meta: {
          title: '个人主页',
          menuIndex: '/FrontPage',
        },
      },
      {
        path: '/DailyShop',
        name: 'DailyShop',
        component: DailyShop,
        meta: {
          title: '日用商城',
          menuIndex: '/FrontPage',
        },

      },
      {
        path: '/BookShop',
        name: 'BookShop',
        component: BookShop,
        meta: {
          title: '日用商城',
          menuIndex: '/FrontPage',
        },

      },
      {
        path: '/SportsShop',
        name: 'SportsShop',
        component: SportsShop,
        meta: {
          title: '日用商城',
          menuIndex: '/FrontPage',
        },

      },
      {
        path: '/DigitalShop',
        name: 'DigitalShop',
        component: DigitalShop,
        meta: {
          title: '日用商城',
          menuIndex: '/FrontPage',
        },

      },

      {
        path: '/GoodsManager',
        name: 'GoodsManager',
        component: GoodsManager,
        meta: {
          title: '商品管理',
          menuIndex: '/FrontPage',
        },
      },
      {
        path: '/OrderManager',
        name: 'OrderManager',
        component: OrderManager,
        meta: {
          title: '商品管理',
          menuIndex: '/FrontPage',
        },
      },
      {
        path: '/goods/:id',
        name: 'GoodDetail',
        component: GoodDetail,
        meta: {
          title: '商品详情',
          menuIndex: '/FrontPage',
        },
      }
    ],
  },

  {
    path: '/update',
    name: 'UpdatePage',
    component: UpdatePage,
    meta: {
      title: '修改资料',
      menuIndex: '/update',
    }
  },
  ...placeholderRoutes.map((item) => ({
    path: item.path,
    component: Index,
    children: [
      {
        path: '',
        name: item.title,
        component: PlaceholderPage,
        props: {
          title: item.title,
          description: item.description,
        },
        meta: {
          title: item.title,
          menuIndex: item.menuIndex,
        },
      },
    ],
  })),

]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router
