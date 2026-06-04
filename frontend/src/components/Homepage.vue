<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { fetchBuyerOrders, fetchSellerOrders } from '../api'

const router = useRouter()
const user = ref({})
const buyerOrders = ref([])
const sellerOrders = ref([])
const ordersLoading = ref(false)

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
