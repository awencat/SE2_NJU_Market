<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import { banUser, fetchList, fetchPage, unbanUser } from '../api'

const router = useRouter()
const admin = ref(readAdmin())
const loadingGoods = ref(false)
const loadingUsers = ref(false)
const goods = ref([])
const users = ref([])
const totalGoods = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchTitle = ref('')
const searchCategory = ref('')

const categoryOptions = [
  { value: 'daily', label: '日用' },
  { value: 'digital', label: '数码' },
  { value: 'pet', label: '萌宠' },
  { value: 'book', label: '书刊' },
  { value: 'sports', label: '体育' },
]

const adminName = computed(() => admin.value?.username || '管理员')

function readAdmin() {
  const raw = localStorage.getItem('nju-market-admin')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

function requireAdmin() {
  if (admin.value) return true
  ElMessage.warning('请先登录管理员后台')
  router.replace('/admin-login')
  return false
}

async function loadGoods() {
  if (!requireAdmin()) return
  loadingGoods.value = true
  try {
    const result = await fetchPage({
      pageSize: pageSize.value,
      pageNum: pageNum.value,
      param: {
        title: searchTitle.value,
        category: searchCategory.value,
      },
    })
    goods.value = result.data?.records || []
    totalGoods.value = result.data?.total || 0
  } catch (error) {
    ElMessage.error(error.message || '商品列表加载失败')
  } finally {
    loadingGoods.value = false
  }
}

async function loadUsers() {
  if (!requireAdmin()) return
  loadingUsers.value = true
  try {
    const result = await fetchList('users')
    users.value = result.data || []
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败')
  } finally {
    loadingUsers.value = false
  }
}

function resetGoodsFilter() {
  searchTitle.value = ''
  searchCategory.value = ''
  pageNum.value = 1
  loadGoods()
}

function statusText(status) {
  return status === 'BANNED' ? '已封禁' : '正常'
}

function statusTag(status) {
  return status === 'BANNED' ? 'danger' : 'success'
}

async function toggleBan(user) {
  const banned = user.status === 'BANNED'
  const actionText = banned ? '解封' : '封禁'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户 ${user.username} 吗？`, '用户管理', {
      confirmButtonText: actionText,
      cancelButtonText: '取消',
      type: banned ? 'info' : 'warning',
    })
    if (banned) {
      await unbanUser(user.userId)
    } else {
      await banUser(user.userId)
    }
    ElMessage.success(`${actionText}成功`)
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${actionText}失败`)
    }
  }
}

function logout() {
  localStorage.removeItem('nju-market-admin')
  router.replace('/admin-login')
}

onMounted(async () => {
  if (!requireAdmin()) return
  await Promise.all([loadGoods(), loadUsers()])
})
</script>

<template>
  <main class="admin-dashboard">
    <header class="admin-header">
      <div>
        <h1>后台管理系统</h1>
        <p>当前管理员：{{ adminName }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="router.push('/admin/crud-dashboard')">数据控制台</el-button>
        <el-button :icon="SwitchButton" @click="logout">退出</el-button>
      </div>
    </header>

    <section class="section-block">
      <div class="section-title">
        <h2>全部商品</h2>
        <div class="toolbar">
          <el-input
            v-model="searchTitle"
            class="title-search"
            clearable
            placeholder="商品标题"
            @keyup.enter="loadGoods"
          />
          <el-select v-model="searchCategory" class="category-search" clearable placeholder="分类">
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadGoods">查询</el-button>
          <el-button :icon="Refresh" @click="resetGoodsFilter">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loadingGoods" :data="goods" border stripe>
        <el-table-column prop="goodId" label="ID" width="80" />
        <el-table-column prop="title" label="商品标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column prop="price" label="价格" width="110" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30]"
          :total="totalGoods"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadGoods"
          @current-change="loadGoods"
        />
      </div>
    </section>

    <section class="section-block">
      <div class="section-title">
        <h2>用户封禁</h2>
        <el-button :icon="Refresh" @click="loadUsers">刷新</el-button>
      </div>

      <el-table v-loading="loadingUsers" :data="users" border stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="campus" label="校区" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.status === 'BANNED' ? 'success' : 'danger'"
              @click="toggleBan(row)"
            >
              {{ row.status === 'BANNED' ? '解封' : '封禁' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </main>
</template>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  padding: 24px;
  background: #eef3f8;
  color: #1f2d3d;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  max-width: 1180px;
  margin: 0 auto 24px;
}

.admin-header h1 {
  margin: 0 0 6px;
  font-size: 28px;
}

.admin-header p {
  margin: 0;
  color: #66788a;
}

.section-block {
  max-width: 1180px;
  margin: 0 auto 24px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(22, 38, 56, 0.08);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.title-search {
  width: 220px;
}

.category-search {
  width: 140px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  padding-top: 16px;
}

@media (max-width: 760px) {
  .admin-header,
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .toolbar,
  .title-search,
  .category-search {
    width: 100%;
  }
}
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  max-width: 1180px;
  margin: 0 auto 24px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
