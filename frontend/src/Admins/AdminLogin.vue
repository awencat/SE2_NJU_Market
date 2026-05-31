<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { loginAdmin } from '../api'

const router = useRouter()
const loading = ref(false)
const form = ref({
  username: '',
  password: '',
})

async function submitLogin() {
  if (!form.value.username.trim() || !form.value.password) {
    ElMessage.warning('请输入管理员用户名和密码')
    return
  }

  loading.value = true
  try {
    const result = await loginAdmin({
      username: form.value.username.trim(),
      password: form.value.password,
    })
    localStorage.setItem('nju-market-admin', JSON.stringify(result.data))
    ElMessage.success('管理员登录成功')
    router.replace('/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '管理员登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="admin-login">
    <section class="login-panel">
      <h1>后台管理员登录</h1>
      <el-form :model="form" label-position="top" @keyup.enter="submitLogin">
        <el-form-item label="管理员用户名">
          <el-input v-model="form.username" :prefix-icon="User" placeholder="请输入管理员用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="login-button" @click="submitLogin">
          登录后台
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: #eef3f8;
}

.login-panel {
  width: min(420px, 100%);
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 18px 48px rgba(22, 38, 56, 0.16);
}

h1 {
  margin: 0 0 28px;
  text-align: center;
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 700;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}
</style>
