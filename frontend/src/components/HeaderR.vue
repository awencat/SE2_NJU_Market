<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight, Box, House, Setting, Shop, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const user = computed(() => {
  const raw = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (!raw) return { username: '游客' }
  try {
    return JSON.parse(raw)
  } catch {
    return { username: '游客' }
  }
})

const activeMenu = computed(() => route.meta.menuIndex || route.path)

function go(path) {
  router.push(path)
}

function displayName() {
  return user.value.username || user.value.name || '游客'
}

function avatarUrl() {
  const image = user.value.image || user.value.avatar || user.value.avatarUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8095/heads/${image}`
}

async function logout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
    })
    localStorage.removeItem('nju-market-user')
    sessionStorage.clear()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    ElMessage.info('已取消退出')
  }
}
</script>

<template>
  <header class="market-header">
    <button class="brand" type="button" @click="go('/FrontPage')">
      <span class="brand-mark">N</span>
      <span>
        <strong>NJU Market</strong>
        <small>校园二手交易</small>
      </span>
    </button>

    <el-menu
      :default-active="activeMenu"
      class="nav-menu"
      mode="horizontal"
      :ellipsis="false"
      background-color="transparent"
      text-color="#40504c"
      active-text-color="#2f6258"
    >
      <el-menu-item index="/FrontPage" @click="go('/FrontPage')"><el-icon><House /></el-icon>首页</el-menu-item>
      <el-sub-menu index="shops">
        <template #title><el-icon><Shop /></el-icon>商城分类</template>
        <el-menu-item index="/DailyShop" @click="go('/DailyShop')">日用</el-menu-item>
        <el-menu-item index="/DigitalShop" @click="go('/DigitalShop')">数码</el-menu-item>
        <el-menu-item index="/SportsShop" @click="go('/SportsShop')">体育</el-menu-item>
        <el-menu-item index="/BookShop" @click="go('/BookShop')">书刊</el-menu-item>
        <el-menu-item index="/PetShop" @click="go('/PetShop')">萌宠</el-menu-item>
      </el-sub-menu>
      <el-sub-menu index="manager">
        <template #title><el-icon><Box /></el-icon>管理</template>
        <el-menu-item index="/GoodsManager" @click="go('/GoodsManager')">商品管理</el-menu-item>
        <el-menu-item index="/OrderManager" @click="go('/OrderManager')">订单管理</el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/HomePage" @click="go('/HomePage')"><el-icon><User /></el-icon>个人主页</el-menu-item>
      <el-menu-item index="/admin-login" @click="go('/admin-login')"><el-icon><Setting /></el-icon>管理员</el-menu-item>
    </el-menu>

    <div class="header-user">
      <span class="welcome">{{ displayName() }}</span>
      <el-avatar v-if="avatarUrl()" :size="36" :src="avatarUrl()" />
      <el-avatar v-else :size="36">{{ displayName().slice(0, 1).toUpperCase() }}</el-avatar>
      <el-button class="logout" plain @click="logout">
        退出
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>
  </header>
</template>

<style scoped>
.market-header{position:sticky;top:0;z-index:20;min-height:72px;display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:18px;padding:10px max(18px,calc((100vw - 1180px)/2));border-bottom:1px solid rgba(36,48,45,.1);background:rgba(255,250,241,.9);backdrop-filter:blur(18px)}.brand{display:flex;align-items:center;gap:10px;padding:0;background:transparent;color:#24302d;text-align:left}.brand-mark{width:42px;height:42px;display:grid;place-items:center;border-radius:9px;background:#2f6258;color:#fffaf1;font-weight:900;font-size:22px;box-shadow:0 10px 22px rgba(47,98,88,.22)}.brand strong,.brand small{display:block;white-space:nowrap}.brand strong{font-size:17px;line-height:1.1}.brand small{margin-top:3px;color:#72807b;font-size:12px}.nav-menu{min-width:0;border-bottom:0;justify-content:center}.header-user{display:flex;align-items:center;gap:10px;min-width:0}.welcome{max-width:110px;color:#40504c;font-weight:700;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.logout{height:34px;border-radius:8px;color:#40504c;background:#fffaf1}@media(max-width:980px){.market-header{grid-template-columns:1fr;gap:8px;padding:10px 14px}.nav-menu{justify-content:flex-start;overflow-x:auto}.header-user{justify-content:flex-end}}
</style>