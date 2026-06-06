<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Check, Close, Refresh, ShoppingBag, Wallet } from '@element-plus/icons-vue'
import { cancelOrder, completeOrder, fetchBuyerOrders, fetchSellerOrders } from '../api'

const user = ref({})
const buyerOrders = ref([])
const sellerOrders = ref([])
const ordersLoading = ref(false)
const cancellingId = ref(null)
const completingId = ref(null)
const activeTab = ref('buyer')

const userId = computed(() => user.value.userId || user.value.id)

function init() {
  const raw = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (!raw) return
  try {
    user.value = JSON.parse(raw)
  } catch {
    user.value = {}
  }
}

async function loadOrders() {
  if (!userId.value) return
  ordersLoading.value = true
  try {
    const [buyerRes, sellerRes] = await Promise.all([
      fetchBuyerOrders(userId.value),
      fetchSellerOrders(userId.value),
    ])
    buyerOrders.value = Array.isArray(buyerRes.data) ? buyerRes.data : []
    sellerOrders.value = Array.isArray(sellerRes.data) ? sellerRes.data : []
  } catch (error) {
    ElMessage.error(error.message || '订单加载失败')
  } finally {
    ordersLoading.value = false
  }
}

async function cancelBuyerOrder(order) {
  cancellingId.value = order.orderId
  try {
    await cancelOrder(order.orderId, userId.value)
    ElMessage.success('退订成功')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '退订失败')
  } finally {
    cancellingId.value = null
  }
}

async function completeSellerOrder(order) {
  try {
    await ElMessageBox.confirm('确认已经完成线下交付吗？确认后订单会标记为已完成。', '订单交付', {
      confirmButtonText: '确认交付',
      cancelButtonText: '取消',
      type: 'warning',
    })
    completingId.value = order.orderId
    await completeOrder(order.orderId, userId.value)
    ElMessage.success('订单已完成')
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '订单交付失败')
    }
  } finally {
    completingId.value = null
  }
}

function canCancel(order) {
  return order.status !== 'CANCELLED' && order.status !== 'COMPLETED'
}

function canComplete(order) {
  return order.status !== 'CANCELLED' && order.status !== 'COMPLETED'
}

function formatOrderQuantity(order) {
  const items = Array.isArray(order.items) ? order.items : []
  return items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
}

function formatGoods(order) {
  const items = Array.isArray(order.items) ? order.items : []
  if (items.length === 0) return '商品信息待补充'
  return items.map((item) => `商品 ${item.goodId} x${item.quantity || 0}`).join('、')
}

function formatMoney(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function statusName(status) {
  const map = {
    PENDING: '待交付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] || status || '未知'
}

function statusType(status) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'warning'
}

function stepClass(order, step) {
  if (order.status === step) return 'active'
  if (order.status === 'COMPLETED' && step === 'PENDING') return 'done'
  return ''
}

onMounted(async () => {
  init()
  await loadOrders()
})
</script>

<template>
  <div class="order-manager market-page">
    <section class="order-hero">
      <div class="hero-copy">
        <p class="market-eyebrow">Orders</p>
        <h1>订单管理</h1>
        <p>查看订购记录和卖家收到的订单，线下交付完成后可以标记订单状态。</p>
      </div>
      <button class="refresh-card" type="button" @click="loadOrders">
        <el-icon><Refresh /></el-icon>
        <span>刷新订单</span>
      </button>
    </section>

    <section class="summary-grid">
      <button class="summary-card" :class="{ active: activeTab === 'buyer' }" type="button" @click="activeTab = 'buyer'">
        <el-icon><ShoppingBag /></el-icon>
        <span>我的订购</span>
        <strong>{{ buyerOrders.length }}</strong>
      </button>
      <button class="summary-card seller" :class="{ active: activeTab === 'seller' }" type="button" @click="activeTab = 'seller'">
        <el-icon><Bell /></el-icon>
        <span>卖家订单</span>
        <strong>{{ sellerOrders.length }}</strong>
      </button>
    </section>

    <section v-loading="ordersLoading" class="orders-board">
      <div v-if="activeTab === 'buyer'" class="order-list">
        <el-empty v-if="buyerOrders.length === 0" description="暂无订购记录" />
        <article v-for="order in buyerOrders" :key="order.orderId" class="order-card">
          <div class="card-top">
            <span class="order-no">{{ order.orderNumber }}</span>
            <el-tag :type="statusType(order.status)" effect="plain">{{ statusName(order.status) }}</el-tag>
          </div>
          <div class="order-meta">
            <span>卖家 ID {{ order.sellerId }}</span>
            <span>数量 {{ formatOrderQuantity(order) }}</span>
          </div>
          <div class="order-timeline" :class="{ cancelled: order.status === 'CANCELLED' }">
            <span :class="stepClass(order, 'PENDING')">待交付</span>
            <i></i>
            <span :class="stepClass(order, 'COMPLETED')">已完成</span>
            <i></i>
            <span :class="stepClass(order, 'CANCELLED')">已取消</span>
          </div>
          <p class="goods-line">{{ formatGoods(order) }}</p>
          <div class="card-bottom">
            <strong><el-icon><Wallet /></el-icon>{{ formatMoney(order.totalAmount) }}</strong>
            <el-button
              size="small"
              type="danger"
              plain
              :disabled="!canCancel(order)"
              :loading="cancellingId === order.orderId"
              @click="cancelBuyerOrder(order)"
            >
              <el-icon><Close /></el-icon>退订
            </el-button>
          </div>
        </article>
      </div>

      <div v-else class="order-list">
        <el-empty v-if="sellerOrders.length === 0" description="暂无买家订购你的商品" />
        <article v-for="order in sellerOrders" :key="order.orderId" class="order-card seller-order">
          <div class="card-top">
            <span class="order-no">{{ order.orderNumber }}</span>
            <el-tag :type="statusType(order.status)" effect="plain">{{ statusName(order.status) }}</el-tag>
          </div>
          <div class="order-meta">
            <span>买家 ID {{ order.buyerId }}</span>
            <span>数量 {{ formatOrderQuantity(order) }}</span>
          </div>
          <div class="order-timeline" :class="{ cancelled: order.status === 'CANCELLED' }">
            <span :class="stepClass(order, 'PENDING')">待交付</span>
            <i></i>
            <span :class="stepClass(order, 'COMPLETED')">已完成</span>
            <i></i>
            <span :class="stepClass(order, 'CANCELLED')">已取消</span>
          </div>
          <p class="goods-line">{{ formatGoods(order) }}</p>
          <div class="card-bottom">
            <strong><el-icon><Wallet /></el-icon>{{ formatMoney(order.totalAmount) }}</strong>
            <el-button
              size="small"
              type="primary"
              plain
              :disabled="!canComplete(order)"
              :loading="completingId === order.orderId"
              @click="completeSellerOrder(order)"
            >
              <el-icon><Check /></el-icon>订单交付
            </el-button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.order-manager { display: grid; gap: 20px; }
.order-hero { display: grid; grid-template-columns: minmax(0,1fr) 190px; gap: 18px; align-items: stretch; }
.hero-copy { position: relative; overflow: hidden; padding: 32px; border-radius: 24px; border: 1px solid var(--market-line); background: linear-gradient(135deg,rgba(255,252,245,.9),rgba(228,238,233,.72)); box-shadow: var(--market-shadow); }
.hero-copy::after { content: ""; position: absolute; right: -70px; top: -90px; width: 220px; height: 220px; border-radius: 50%; background: rgba(65,106,143,.14); }
.hero-copy h1 { margin: 0; font-size: clamp(34px,5vw,60px); line-height: 1; color: var(--market-ink); }
.hero-copy p:not(.market-eyebrow) { margin: 14px 0 0; color: var(--market-muted); font-size: 17px; line-height: 1.7; }
.refresh-card { display: flex; flex-direction: column; align-items: flex-start; justify-content: flex-end; gap: 12px; padding: 22px; border-radius: 22px; background: #24302d; color: #fffaf1; box-shadow: var(--market-shadow); }
.refresh-card .el-icon { font-size: 28px; }
.refresh-card span { font-weight: 800; }
.summary-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 14px; }
.summary-card { display: grid; grid-template-columns: auto minmax(0,1fr) auto; align-items: center; gap: 14px; min-height: 94px; padding: 20px; border-radius: 20px; border: 1px solid var(--market-line); background: rgba(255,252,245,.72); box-shadow: 0 12px 28px rgba(50,38,25,.08); color: var(--market-ink); text-align: left; transition: .18s ease; }
.summary-card:hover { transform: translateY(-2px); }
.summary-card .el-icon { width: 44px; height: 44px; display: grid; place-items: center; border-radius: 14px; background: var(--market-green); color: #fff; font-size: 22px; }
.summary-card.seller .el-icon { background: var(--market-blue); }
.summary-card span { font-weight: 800; }
.summary-card strong { font-size: 34px; line-height: 1; }
.summary-card.active { outline: 3px solid rgba(47,98,88,.16); background: rgba(255,252,245,.96); }
.orders-board { min-height: 260px; padding: 6px; }
.order-list { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 14px; }
.order-card { position: relative; overflow: hidden; display: grid; gap: 14px; padding: 18px 18px 18px 22px; border-radius: 20px; border: 1px solid var(--market-line); background: rgba(255,252,245,.88); backdrop-filter: blur(12px); box-shadow: 0 14px 32px rgba(50,38,25,.09); transition: .16s ease; }
.order-card:hover { transform: translateY(-3px); box-shadow: 0 18px 40px rgba(50,38,25,.13); }
.order-card::before { content: ""; position: absolute; inset: 0 auto 0 0; width: 6px; background: var(--market-green); }
.seller-order::before { background: var(--market-blue); }
.card-top, .card-bottom, .order-meta { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.order-no { font-weight: 900; color: var(--market-ink); word-break: break-all; }
.order-meta { margin: 0; color: var(--market-muted); font-size: 14px; justify-content: flex-start; flex-wrap: wrap; }
.order-meta span { padding: 5px 9px; border-radius: 999px; background: rgba(255,250,241,.9); box-shadow: inset 0 0 0 1px rgba(84,67,45,.12); }
.order-timeline { display: grid; grid-template-columns: auto 1fr auto 1fr auto; align-items: center; gap: 8px; padding: 10px 12px; border-radius: 14px; background: rgba(255,250,241,.78); box-shadow: inset 0 0 0 1px rgba(84,67,45,.12); }
.order-timeline span { display: inline-flex; align-items: center; justify-content: center; min-height: 26px; padding: 0 9px; border-radius: 999px; color: var(--market-muted); font-size: 12px; font-weight: 850; background: rgba(255,255,255,.5); white-space: nowrap; }
.order-timeline span::before { content: ""; width: 7px; height: 7px; margin-right: 6px; border-radius: 50%; background: currentColor; opacity: .5; }
.order-timeline i { height: 2px; border-radius: 999px; background: rgba(84,67,45,.18); }
.order-timeline span.done { color: var(--market-green); background: rgba(47,98,88,.1); }
.order-timeline span.active { color: #fffaf1; background: var(--market-green); }
.order-timeline.cancelled span.active { background: #8b928f; }
.goods-line { min-height: 24px; margin: 0; padding: 12px 14px; border-radius: 14px; background: rgba(255,250,241,.74); color: var(--market-ink); font-size: 14px; line-height: 1.55; }
.card-bottom strong { display: flex; align-items: center; gap: 6px; color: var(--market-red); font-size: 22px; }
@media (max-width:780px) {
  .order-hero, .summary-grid, .order-list { grid-template-columns: 1fr; }
  .refresh-card { min-height: 120px; }
  .card-top, .card-bottom { align-items: flex-start; flex-direction: column; }
  .order-timeline { grid-template-columns: 1fr; align-items: stretch; }
  .order-timeline i { display: none; }
}
</style>
