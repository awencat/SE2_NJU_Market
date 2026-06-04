<script setup lang="ts">
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

function formatOrderQuantity(order) {
  const items = Array.isArray(order.items) ? order.items : []
  if (items.length === 0) return 0
  return items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
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
  <div>
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
            <el-table-column label="数量" width="80">
              <template #default="scope">{{ formatOrderQuantity(scope.row) }}</template>
            </el-table-column>
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
            <el-table-column label="数量" width="80">
              <template #default="scope">{{ formatOrderQuantity(scope.row) }}</template>
            </el-table-column>
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

</style>

