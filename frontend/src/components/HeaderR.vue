<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, House, Setting, Shop, User } from '@element-plus/icons-vue'

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
      customClass: 'market-messagebox',
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
      <span class="brand-mark" aria-hidden="true">
        <i></i>
      </span>
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
      <el-sub-menu index="shops" popper-class="market-menu-popper">
        <template #title><el-icon><Shop /></el-icon>商城分类</template>
        <el-menu-item index="/DailyShop" @click="go('/DailyShop')">日用</el-menu-item>
        <el-menu-item index="/DigitalShop" @click="go('/DigitalShop')">数码</el-menu-item>
        <el-menu-item index="/SportsShop" @click="go('/SportsShop')">体育</el-menu-item>
        <el-menu-item index="/BookShop" @click="go('/BookShop')">书刊</el-menu-item>
        <el-menu-item index="/PetShop" @click="go('/PetShop')">萌宠</el-menu-item>
      </el-sub-menu>
      <el-sub-menu index="manager" popper-class="market-menu-popper">
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
      </el-button>
    </div>
  </header>
</template>

<style scoped>
.market-header{position:sticky;top:0;z-index:20;min-height:76px;display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:18px;padding:10px max(18px,calc((100vw - 1180px)/2));border-bottom:1px solid rgba(36,48,45,.12);background:linear-gradient(90deg,rgba(248,252,250,.96),rgba(232,243,237,.92));backdrop-filter:blur(20px) saturate(1.2);box-shadow:0 14px 34px rgba(28,60,53,.12)}
.market-header::before{content:"";position:absolute;inset:0;z-index:-1;pointer-events:none;background:radial-gradient(circle at 12% 20%,rgba(47,98,88,.1),transparent 22%),radial-gradient(circle at 84% 8%,rgba(194,122,44,.12),transparent 18%),repeating-linear-gradient(135deg,rgba(33,44,41,.026) 0,rgba(33,44,41,.026) 1px,transparent 1px,transparent 22px)}
.market-header::after{content:"";position:absolute;left:0;right:0;top:0;height:3px;background:linear-gradient(90deg,var(--market-green),var(--market-gold) 50%,var(--market-red))}
.brand{display:flex;align-items:center;gap:11px;padding:0;background:transparent;color:#24302d;text-align:left;transition:transform .18s ease}
.brand:hover{transform:translateY(-1px)}
.brand-mark{position:relative;width:46px;height:46px;display:grid;place-items:center;border-radius:13px;background:linear-gradient(145deg,#2c3a36,#1c2723);box-shadow:0 12px 24px rgba(47,98,88,.28),inset 0 1px 0 rgba(255,255,255,.12)}
.brand-mark::before{content:"";position:absolute;left:13px;top:11px;width:20px;height:14px;border:3px solid #c27a2c;border-bottom:0;border-radius:14px 14px 0 0}
.brand-mark i{position:absolute;left:11px;top:23px;width:24px;height:15px;border-radius:3px;background:#fffaf1}
.brand-mark i::after{content:"";position:absolute;right:-3px;top:5px;width:8px;height:8px;border-radius:2px;background:#b34b34}
.brand strong,.brand small{display:block;white-space:nowrap}.brand strong{font-size:18px;line-height:1.1;letter-spacing:.01em}.brand small{margin-top:3px;color:#72807b;font-size:12px}
.nav-menu{min-width:0;border-bottom:0;justify-content:center;background:rgba(250,253,251,.55);border-radius:16px;box-shadow:inset 0 0 0 1px rgba(47,98,88,.1)}
.nav-menu :deep(.el-menu-item),.nav-menu :deep(.el-sub-menu__title){height:46px;border-radius:12px;font-weight:600;transition:.16s ease}
.nav-menu :deep(.el-menu-item:hover),.nav-menu :deep(.el-sub-menu__title:hover){background:rgba(47,98,88,.08)}
.nav-menu :deep(.el-menu-item.is-active),.nav-menu :deep(.el-sub-menu.is-active .el-sub-menu__title){background:rgba(47,98,88,.12);color:var(--market-green)}
.nav-menu :deep(.el-menu-item.is-active)::after{content:"";position:absolute;left:50%;bottom:6px;transform:translateX(-50%);width:18px;height:3px;border-radius:999px;background:var(--market-gold)}
.header-user{display:flex;align-items:center;gap:10px;min-width:0;padding:6px 8px 6px 12px;border:1px solid rgba(47,98,88,.16);border-radius:999px;background:rgba(250,253,251,.75);box-shadow:0 6px 16px rgba(28,60,53,.07)}
.welcome{max-width:110px;color:#40504c;font-weight:700;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.logout{height:34px;border-radius:999px;color:#40504c;background:#fffaf1;transition:.16s ease}
.logout:hover{color:#fffaf1;background:#2f6258;border-color:#2f6258}
@media(max-width:980px){.market-header{grid-template-columns:1fr;gap:8px;padding:10px 14px}.nav-menu{justify-content:flex-start;overflow-x:auto}.header-user{justify-content:flex-end;justify-self:end}}
</style>
