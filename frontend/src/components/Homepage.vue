<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Refresh, Close, Bell } from '@element-plus/icons-vue'
import { cancelOrder, fetchBuyerOrders, fetchSellerOrders } from '../api'

const router = useRouter()
const user = ref({})
const buyerOrders = ref([])
const sellerOrders = ref([])
const ordersLoading = ref(false)
const cancellingId = ref(null)

const userId = computed(() => user.value.userId || user.value.id)
const displayName = computed(() => user.value.username || user.value.name || '未登录')

function init() {
  const userData = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (userData) {
    try {
      user.value = JSON.parse(userData)
    } catch (error) {
      console.error('用户数据解析失败:', error)
      user.value = {}
    }
  }
}

function avatarUrl() {
  const image = user.value.image || user.value.avatar || user.value.avatarUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8095/heads/${image}`
}

function goToUpdate() {
  router.push('/update')
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

function canCancel(order) {
  return order.status !== 'CANCELLED' && order.status !== 'COMPLETED'
}

function formatMoney(value) {
  return `￥${Number(value || 0).toFixed(2)}`
}

onMounted(async () => {
  init()
  await loadOrders()
})
</script>

<template>
  <div class="homepage-container">
    <section class="profile-panel">
      <div class="profile-main">
        <el-avatar :size="72" :src="avatarUrl()">{{ displayName.charAt(0) }}</el-avatar>
        <div>
          <h1 class="welcome-title">{{ displayName }}</h1>
          <p class="profile-subtitle">校园市场个人中心</p>
        </div>
      </div>
      <el-button type="primary" @click="goToUpdate">
        <el-icon><Edit /></el-icon>
        修改资料
      </el-button>
    </section>

    <el-descriptions title="个人资料" :column="2" size="large" border class="user-descriptions">
      <el-descriptions-item label="用户名">
        <div class="center-text">{{ displayName }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="邮箱">
        <div class="center-text">{{ user.email || '-' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="手机号">
        <div class="center-text">{{ user.phone || '未设置' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="校区">
        <div class="center-text">{{ user.campus || '未设置' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="地址">
        <div class="center-text">{{ user.address || '未设置' }}</div>
      </el-descriptions-item>
    </el-descriptions>

    <section class="orders-section" v-loading="ordersLoading">
      <div class="section-head">
        <div>
          <h2>交易订单</h2>
          <p>买家可以退订订单，卖家可以查看被订购提醒。</p>
        </div>
        <el-button @click="loadOrders">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-tabs class="orders-tabs">
        <el-tab-pane label="我的订购">
          <el-table :data="buyerOrders" empty-text="暂无订购记录" border>
            <el-table-column prop="orderNumber" label="订单号" min-width="180" />
            <el-table-column prop="sellerId" label="卖家ID" width="90" />
            <el-table-column label="金额" width="110">
              <template #default="scope">{{ formatMoney(scope.row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                    size="small"
                    type="danger"
                    :disabled="!canCancel(scope.row)"
                    :loading="cancellingId === scope.row.orderId"
                    @click="cancelBuyerOrder(scope.row)"
                >
                  <el-icon><Close /></el-icon>
                  退订
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="卖家通知">
          <el-table :data="sellerOrders" empty-text="暂无买家订购你的商品" border>
            <el-table-column prop="orderNumber" label="订单号" min-width="180" />
            <el-table-column prop="buyerId" label="买家ID" width="90" />
            <el-table-column label="金额" width="110">
              <template #default="scope">{{ formatMoney(scope.row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="提醒" min-width="160">
              <template #default>
                <span class="notify-text">
                  <el-icon><Bell /></el-icon>
                  有买家订购了你的商品
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<style scoped>
.homepage-container {
  min-height: 100%;
  padding: 32px 20px;
  background: #f5f7fa;
}

.profile-panel,
.orders-section,
.user-descriptions {
  width: min(960px, 100%);
  margin: 0 auto 24px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(32, 48, 64, 0.08);
}

.profile-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
}

.profile-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-title {
  margin: 0;
  color: #203040;
  font-size: 30px;
  font-weight: 700;
}

.profile-subtitle {
  margin: 6px 0 0;
  color: #64748b;
}

.user-descriptions {
  overflow: hidden;
}

.center-text {
  text-align: center;
}

.orders-section {
  padding: 22px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-head h2,
.section-head p {
  margin: 0;
}

.section-head h2 {
  color: #203040;
  font-size: 22px;
}

.section-head p {
  margin-top: 6px;
  color: #64748b;
}

.orders-tabs :deep(.el-tabs__content) {
  padding-top: 8px;
}

.notify-text {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #2563eb;
}

@media (max-width: 720px) {
  .profile-panel,
  .section-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
