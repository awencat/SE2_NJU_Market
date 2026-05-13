<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch {
    ElMessage.info('已取消退出')
  }
}
</script>

<template>
  <el-menu
    :default-active="activeMenu"
    class="market-header"
    mode="horizontal"
    background-color="#203040"
    text-color="#f8fbff"
    active-text-color="#f5b94c"
  >
    <el-menu-item index="/FrontPage" @click="go('/FrontPage')">主站</el-menu-item>

    <el-sub-menu index="shops">
      <template #title>商城精选</template>
      <el-menu-item index="/DailyShop" @click="go('/DailyShop')">日用</el-menu-item>
      <el-menu-item index="/VideoShop" @click="go('/VideoShop')">游戏</el-menu-item>
      <el-menu-item index="/MusicShop" @click="go('/MusicShop')">音乐</el-menu-item>
      <el-menu-item index="/BookShop" @click="go('/BookShop')">书刊</el-menu-item>
      <el-menu-item index="/PetShop" @click="go('/PetShop')">萌宠</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="user-center">
      <template #title>个人中心</template>
      <el-menu-item index="/HomePage" @click="go('/HomePage')">个人主页</el-menu-item>
      <el-menu-item index="/update" @click="go('/update')">修改资料</el-menu-item>
      <el-menu-item index="logout" @click="logout">退出登录</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/GoodsManager" @click="go('/GoodsManager')">商品管理</el-menu-item>
    <el-menu-item index="/UserManager" @click="go('/UserManager')">用户管理</el-menu-item>
    <el-menu-item index="/CrudDashboard" @click="go('/CrudDashboard')">数据控制台</el-menu-item>

    <div class="header-user">
      <span>欢迎，{{ displayName() }}</span>
      <el-avatar v-if="avatarUrl()" :size="38" :src="avatarUrl()" />
      <el-avatar v-else :size="38">{{ displayName().slice(0, 1).toUpperCase() }}</el-avatar>
    </div>
  </el-menu>
</template>

<style scoped>
.market-header {
  position: relative;
  width: 100%;
  height: 64px;
  padding-right: 240px;
  border-bottom: 0;
}

.header-user {
  position: absolute;
  top: 0;
  right: 24px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #f8fbff;
  font-size: 14px;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .market-header {
    height: auto;
    min-height: 64px;
    padding-right: 0;
  }

  .header-user {
    position: static;
    padding: 0 16px;
    margin-left: auto;
  }
}
</style>
