<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Edit, Refresh, ShoppingBag, User } from '@element-plus/icons-vue'
import { fetchBuyerOrders, fetchSellerOrders } from '../api'

const router = useRouter()
const user = ref({})
const buyerOrders = ref([])
const sellerOrders = ref([])
const ordersLoading = ref(false)
const userId = computed(() => user.value.userId || user.value.id)
const displayName = computed(() => user.value.username || user.value.name || '未登录')
const orderTotal = computed(() => buyerOrders.value.length + sellerOrders.value.length)

function init() {
  const raw = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (!raw) return
  try { user.value = JSON.parse(raw) } catch { user.value = {} }
}

function avatarUrl() {
  const image = user.value.image || user.value.avatar || user.value.avatarUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8095/heads/${image}`
}

async function loadOrders() {
  if (!userId.value) return
  ordersLoading.value = true
  try {
    const [buyerRes, sellerRes] = await Promise.all([fetchBuyerOrders(userId.value), fetchSellerOrders(userId.value)])
    buyerOrders.value = Array.isArray(buyerRes.data) ? buyerRes.data : []
    sellerOrders.value = Array.isArray(sellerRes.data) ? sellerRes.data : []
  } catch (error) {
    ElMessage.error(error.message || '订单加载失败')
  } finally {
    ordersLoading.value = false
  }
}

function formatMoney(value) { return `¥${Number(value || 0).toFixed(2)}` }
function formatTime(value) { return value ? String(value).replace('T', ' ').slice(0, 16) : '-' }
function statusName(status) {
  const map = { PENDING: '待交付', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status || '未知'
}

onMounted(async () => { init(); await loadOrders() })
</script>

<template>
  <div class="profile-page market-page">
    <section class="profile-hero">
      <div class="profile-main">
        <el-avatar :size="86" :src="avatarUrl()" class="avatar">
          {{ displayName.charAt(0).toUpperCase() }}
        </el-avatar>
        <div>
          <p class="market-eyebrow">Profile</p>
          <h1>{{ displayName }}</h1>
          <span>{{ user.email || '未填写邮箱' }}</span>
        </div>
      </div>
      <div class="hero-actions">
        <div class="mini-stat">
          <strong>{{ orderTotal }}</strong>
          <span>相关订单</span>
        </div>
        <el-button type="primary" @click="router.push('/update')">
          <el-icon><Edit /></el-icon>编辑资料
        </el-button>
      </div>
    </section>

    <section class="profile-layout">
      <aside class="identity-card">
        <el-icon><User /></el-icon>
        <strong>{{ displayName }}</strong>
        <p>ID {{ userId || '-' }}</p>
        <div><span>电话</span><b>{{ user.phone || '未设置' }}</b></div>
        <div><span>校区</span><b>{{ user.campus || '未设置' }}</b></div>
        <div><span>地址</span><b>{{ user.address || '未设置' }}</b></div>
      </aside>

      <section class="orders-panel" v-loading="ordersLoading">
        <div class="panel-head">
          <div>
            <p class="market-eyebrow">Orders</p>
            <h2>交易动态</h2>
          </div>
          <el-button @click="loadOrders"><el-icon><Refresh /></el-icon>刷新</el-button>
        </div>
        <div class="quick-cards">
          <button type="button" @click="router.push('/OrderManager')">
            <el-icon><ShoppingBag /></el-icon>
            <span>我的订购</span>
            <strong>{{ buyerOrders.length }}</strong>
          </button>
          <button type="button" @click="router.push('/OrderManager')">
            <el-icon><Bell /></el-icon>
            <span>卖家订单</span>
            <strong>{{ sellerOrders.length }}</strong>
          </button>
        </div>
        <el-tabs>
          <el-tab-pane :label="`我的订购 ${buyerOrders.length}`">
            <div class="order-list">
              <el-empty v-if="buyerOrders.length === 0" description="暂无订购记录" />
              <article v-for="order in buyerOrders" :key="order.orderId" class="order-card">
                <div><strong>{{ order.orderNumber }}</strong><span>{{ formatTime(order.createdAt) }}</span></div>
                <p>卖家 ID：{{ order.sellerId }} · {{ statusName(order.status) }}</p>
                <b>{{ formatMoney(order.totalAmount) }}</b>
              </article>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`卖家订单 ${sellerOrders.length}`">
            <div class="order-list">
              <el-empty v-if="sellerOrders.length === 0" description="暂无买家订购你的商品" />
              <article v-for="order in sellerOrders" :key="order.orderId" class="order-card notice">
                <div><strong>{{ order.orderNumber }}</strong><span><el-icon><Bell /></el-icon>{{ statusName(order.status) }}</span></div>
                <p>买家 ID：{{ order.buyerId }}</p>
                <b>{{ formatMoney(order.totalAmount) }}</b>
              </article>
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>
    </section>
  </div>
</template>

<style scoped>
.profile-page { display: grid; gap: 20px; }
.profile-hero { position: relative; overflow: hidden; display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 30px; border: 1px solid var(--market-line); border-radius: 26px; background: linear-gradient(135deg, rgba(255,252,245,.92), rgba(228,238,233,.74)); box-shadow: var(--market-shadow); }
.profile-hero::after { content: ""; position: absolute; right: -80px; top: -90px; width: 250px; height: 250px; border-radius: 50%; background: rgba(65,106,143,.14); }
.profile-main, .hero-actions { position: relative; z-index: 1; display: flex; align-items: center; gap: 18px; }
.avatar { border: 4px solid rgba(255,250,241,.86); box-shadow: 0 12px 26px rgba(50,38,25,.16); }
.profile-main h1 { margin: 0; color: var(--market-ink); font-family: var(--market-display); font-size: clamp(36px,5vw,58px); line-height: 1; }
.profile-main span { color: var(--market-muted); }
.mini-stat { display: grid; min-width: 96px; padding: 12px 14px; border-radius: 16px; background: rgba(36,48,45,.86); color: #fffaf1; }
.mini-stat strong { font-size: 28px; line-height: 1; }
.mini-stat span { color: rgba(255,250,241,.72); font-size: 13px; }
.profile-layout { display: grid; grid-template-columns: 280px minmax(0,1fr); gap: 18px; align-items: start; }
.identity-card { display: grid; gap: 12px; padding: 22px; border: 1px solid var(--market-line); border-radius: 22px; background: rgba(255,252,245,.78); box-shadow: 0 14px 34px rgba(50,38,25,.09); backdrop-filter: blur(14px); }
.identity-card > .el-icon { width: 48px; height: 48px; display: grid; place-items: center; border-radius: 15px; background: var(--market-green); color: #fff; font-size: 24px; }
.identity-card strong { color: var(--market-ink); font-size: 22px; }
.identity-card p { margin: -6px 0 8px; color: var(--market-muted); }
.identity-card div { display: grid; gap: 4px; padding-top: 12px; border-top: 1px solid var(--market-line); }
.identity-card span { color: var(--market-muted); font-size: 13px; }
.identity-card b { color: var(--market-ink); overflow-wrap: anywhere; }
.orders-panel { padding: 24px; border: 1px solid var(--market-line); border-radius: 22px; background: rgba(255,252,245,.76); box-shadow: var(--market-shadow); backdrop-filter: blur(14px); }
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 14px; margin-bottom: 16px; }
.panel-head h2 { margin: 0; color: var(--market-ink); font-family: var(--market-display); font-size: 34px; }
.quick-cards { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 12px; margin-bottom: 16px; }
.quick-cards button { display: grid; grid-template-columns: auto 1fr auto; align-items: center; gap: 12px; min-height: 76px; padding: 16px; border-radius: 18px; background: rgba(255,250,241,.92); color: var(--market-ink); text-align: left; box-shadow: inset 0 0 0 1px var(--market-line); transition: .16s ease; }
.quick-cards button:hover { transform: translateY(-2px); box-shadow: inset 0 0 0 1px var(--market-green), 0 14px 28px rgba(50,38,25,.1); }
.quick-cards .el-icon { width: 38px; height: 38px; display: grid; place-items: center; border-radius: 12px; background: var(--market-green); color: #fff; font-size: 20px; }
.quick-cards strong { font-size: 28px; }
.order-list { display: grid; gap: 12px; }
.order-card { display: grid; gap: 8px; padding: 15px; border: 1px solid var(--market-line); border-radius: 16px; background: #fffaf1; }
.order-card div { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.order-card strong { color: var(--market-ink); word-break: break-all; }
.order-card span, .order-card p { margin: 0; color: var(--market-muted); }
.order-card b { color: var(--market-red); font-size: 20px; }
.notice { border-color: rgba(65,106,143,.26); }
@media (max-width: 860px) { .profile-hero, .profile-main, .hero-actions { align-items: flex-start; flex-direction: column; } .profile-layout, .quick-cards { grid-template-columns: 1fr; } }
</style>
