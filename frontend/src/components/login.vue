<script setup>
import { computed, ref } from 'vue'
import { loginUser, registerUser } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()

const authMode = ref('login')
const loading = ref(false)
const message = ref('请先登录或注册，然后再进入数据控制台。')
const currentUser = ref(readStoredUser())

const loginForm = ref({
  username: '',
  password: '',
})

const registerForm = ref({
  username: '',
  password: '',
  email: '',
  phone: '',
  campus: '',
  address: '',
})

const isLoggedIn = computed(() => Boolean(currentUser.value))

function readStoredUser() {
  const raw = localStorage.getItem('nju-market-user')
  return raw ? JSON.parse(raw) : null
}

function persistUser(user) {
  currentUser.value = user
  localStorage.setItem('nju-market-user', JSON.stringify(user))
}

function logout() {
  currentUser.value = null
  localStorage.removeItem('nju-market-user')
  message.value = '你已退出登录。'
}

async function submitLogin() {
  loading.value = true
  try {
    const result = await loginUser(loginForm.value)
    persistUser(result.data)
    message.value = `欢迎回来，${result.data.username}`
    loginForm.value.password = ''
    router.replace('/CrudDashboard')
  } catch (error) {
    message.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  loading.value = true
  try {
    const payload = {
      ...registerForm.value,
      username: registerForm.value.username.trim(),
      email: registerForm.value.email.trim(),
    }
    const result = await registerUser(payload)
    persistUser(result.data)
    message.value = `注册成功，欢迎你，${result.data.username}`
    registerForm.value.password = ''
  } catch (error) {
    message.value = error.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="loginBody">
    <div class="loginDiv">
      <h2 class="login-title">{{ authMode === 'login' ? '用户登录' : '用户注册' }}</h2>

      <!-- 登录表单 -->
      <el-form
          v-if="authMode === 'login'"
          :model="loginForm"
          label-width="80px"
          class="login-content"
      >
        <el-form-item label="用户名">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="submitLogin"
              :loading="loading"
              style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
              type="text"
              @click="authMode = 'register'"
          >
            没有账号？去注册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册表单 -->
      <el-form
          v-else
          :model="registerForm"
          label-width="80px"
          class="login-content"
      >
        <el-form-item label="用户名">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="校区">
          <el-input
              v-model="registerForm.campus"
              placeholder="请输入校区"
              prefix-icon="Location"
          />
        </el-form-item>

        <el-form-item label="地址">
          <el-input
              v-model="registerForm.address"
              placeholder="请输入地址"
              prefix-icon="MapLocation"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="submitRegister"
              :loading="loading"
              style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
              type="text"
              @click="authMode = 'login'"
          >
            已有账号？去登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 消息提示 -->
      <div v-if="message" class="message-tip">
        {{ message }}
      </div>
    </div>

    <!-- 登录后显示控制台 -->
  </div>
</template>

<style scoped>
.loginBody {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.loginDiv {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 450px;
  min-height: 400px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 30px;
}

.login-title {
  margin: 0 0 30px 0;
  text-align: center;
  color: #333;
  font-size: 24px;
}

.login-content {
  width: 100%;
}

.message-tip {
  margin-top: 20px;
  padding: 10px;
  text-align: center;
  color: #666;
  font-size: 14px;
  background-color: #f5f5f5;
  border-radius: 4px;
}
</style>
