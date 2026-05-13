<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = ref({})
const value = ref(new Date())

function init() {
  const userData = sessionStorage.getItem('User') || localStorage.getItem('nju-market-user')
  if (userData) {
    try {
      user.value = JSON.parse(userData)
    } catch (e) {
      console.error('解析用户数据失败:', e)
      user.value = { name: '未登录', email: '-' }
    }
  } else {
    user.value = { name: '未登录', email: '-' }
  }
}

function goToUpdate() {
  router.push('/update')
}

onMounted(() => {
  init()
})
</script>

<template>
  <div class="homepage-container">
    <h1 class="welcome-title">欢迎你！{{ user.name }}</h1>

    <el-descriptions title="个人中心" :column="2" size="large" border class="user-descriptions">
      <el-descriptions-item label="用户名">
        <div class="center-text">{{ user.name }}</div>
      </el-descriptions-item>

      <el-descriptions-item label="邮箱">
        <div class="center-text">{{ user.email }}</div>
      </el-descriptions-item>

      <el-descriptions-item label="头像">
        <div class="center-text">
          <el-avatar :size="60" :src="user.avatar" />
        </div>
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

    <div class="action-buttons">
      <el-button type="primary" size="large" @click="goToUpdate">
        <el-icon><Edit /></el-icon>
        修改个人资料
      </el-button>
    </div>

    <el-calendar v-model="value" class="user-calendar" />
  </div>
</template>

<style scoped>
.homepage-container {
  text-align: center;
  background-color: #f5f7fa;
  min-height: 100%;
  padding: 40px 20px;
  margin: 0;
}

.welcome-title {
  font-size: 42px;
  color: #203040;
  margin-bottom: 30px;
  font-weight: 600;
}

.user-descriptions {
  width: 90%;
  max-width: 800px;
  margin: 0 auto 30px;
  text-align: left;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.center-text {
  text-align: center;
}

.action-buttons {
  margin: 30px 0;
}

.user-calendar {
  width: 90%;
  max-width: 1000px;
  margin: 30px auto 0;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>

