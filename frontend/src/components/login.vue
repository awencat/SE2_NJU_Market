<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Location, MapLocation, Message, Phone, User } from '@element-plus/icons-vue'
import { loginUser, registerUser } from '../api'
import marketStudyImage from '../assets/market/market-study.jpg'
import defaultBookImage from '../assets/market/default-book.jpg'
import defaultDigitalImage from '../assets/market/default-digital.jpg'

const router = useRouter()
const authMode = ref('login')
const loading = ref(false)
const currentUser = ref(readStoredUser())

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', email: '', phone: '', campus: '', address: '' })
const isLogin = computed(() => authMode.value === 'login')

function readStoredUser() {
  const raw = localStorage.getItem('nju-market-user')
  if (!raw) return null
  try { return JSON.parse(raw) } catch { return null }
}

function persistUser(user) {
  currentUser.value = user
  localStorage.setItem('nju-market-user', JSON.stringify(user))
}

async function submitLogin() {
  loading.value = true
  try {
    const result = await loginUser(loginForm.value)
    persistUser(result.data)
    loginForm.value.password = ''
    ElMessage.success('登录成功')
    router.replace('/FrontPage')
  } catch (error) {
    const msg = error.message || '登录失败'
    if (msg.includes('username') || msg.includes('password')) ElMessage.error('用户名或密码错误')
    else if (msg.includes('ban')) ElMessage.error('账号已被封禁，请联系管理员')
    else ElMessage.error(msg)
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
    registerForm.value.password = ''
    ElMessage.success('注册成功')
    router.replace('/FrontPage')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page" :style="{ '--hero-image': `url(${marketStudyImage})` }">
    <section class="auth-hero">
      <div class="hero-photo-stack" aria-hidden="true">
        <img class="photo-main" :src="marketStudyImage" alt="" />
        <img class="photo-small photo-book" :src="defaultBookImage" alt="" />
        <img class="photo-small photo-digital" :src="defaultDigitalImage" alt="" />
      </div>
      <div class="hero-copy">
        <p class="market-eyebrow">NJU Market</p>
        <h1>南大二手集市</h1>
        <p>用校园身份发布闲置、查看订单、联系卖家，交易信息集中在一个地方。</p>
      </div>
      <div class="hero-stats">
        <span><strong>4 类商品</strong> 日用、书刊、数码、体育</span>
        <span><strong>订单记录</strong> 订购和退订都有记录</span>
        <span><strong>评价反馈</strong> 购买后可评分和评论</span>
      </div>
    </section>

    <section class="auth-card">
      <div class="mode-tabs">
        <button :class="{ active: isLogin }" type="button" @click="authMode = 'login'">登录</button>
        <button :class="{ active: !isLogin }" type="button" @click="authMode = 'register'">注册</button>
      </div>

      <div class="card-head">
        <h2>{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>
        <p>{{ isLogin ? '输入账号后进入个人首页。' : '填写联系方式，方便后续线下沟通。' }}</p>
      </div>

      <el-form v-if="isLogin" :model="loginForm" label-position="top" class="auth-form" @keyup.enter="submitLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-button" :loading="loading" @click="submitLogin">登录</el-button>
      </el-form>

      <el-form v-else :model="registerForm" label-position="top" class="auth-form" @keyup.enter="submitRegister">
        <el-form-item label="用户名"><el-input v-model="registerForm.username" placeholder="例如 alice" :prefix-icon="User" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="registerForm.password" type="password" placeholder="至少 6 位" :prefix-icon="Lock" show-password /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="registerForm.email" placeholder="name@example.com" :prefix-icon="Message" /></el-form-item>
        <div class="form-grid">
          <el-form-item label="电话"><el-input v-model="registerForm.phone" placeholder="手机号" :prefix-icon="Phone" /></el-form-item>
          <el-form-item label="校区"><el-input v-model="registerForm.campus" placeholder="仙林 / 鼓楼" :prefix-icon="Location" /></el-form-item>
        </div>
        <el-form-item label="地址"><el-input v-model="registerForm.address" placeholder="宿舍或面交地点" :prefix-icon="MapLocation" /></el-form-item>
        <el-button type="primary" size="large" class="submit-button" :loading="loading" @click="submitRegister">注册并进入</el-button>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) 430px;
  gap: 34px;
  align-items: center;
  padding: 42px max(24px, calc((100vw - 1160px) / 2));
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

.auth-page::before {
  content: "";
  position: fixed;
  inset: 0;
  z-index: -3;
  background:
    linear-gradient(110deg, rgba(24, 38, 34, .86), rgba(24, 38, 34, .3) 45%, rgba(255, 248, 236, .84)),
    var(--hero-image) center / cover no-repeat;
  filter: saturate(.9);
}

.auth-page::after {
  content: "";
  position: fixed;
  inset: 0;
  z-index: -2;
  pointer-events: none;
  background:
    radial-gradient(circle at 18% 20%, rgba(255, 250, 241, .18), transparent 24%),
    radial-gradient(circle at 85% 78%, rgba(194, 122, 44, .22), transparent 28%),
    repeating-linear-gradient(135deg, rgba(255, 255, 255, .08) 0, rgba(255, 255, 255, .08) 1px, transparent 1px, transparent 24px);
}

.auth-hero {
  min-height: 620px;
  display: grid;
  align-content: end;
  gap: 22px;
  position: relative;
  padding: 38px;
  border: 1px solid rgba(255, 250, 241, .22);
  border-radius: 28px;
  color: #fffaf1;
  overflow: hidden;
  box-shadow: 0 28px 70px rgba(23, 32, 29, .26);
}

.auth-hero::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(to top, rgba(15, 26, 23, .82), rgba(15, 26, 23, .08) 62%),
    linear-gradient(135deg, rgba(255, 250, 241, .16), transparent 36%);
  z-index: 1;
}

.hero-photo-stack {
  position: absolute;
  inset: 0;
}

.hero-photo-stack img {
  position: absolute;
  display: block;
  object-fit: cover;
  box-shadow: 0 18px 50px rgba(13, 22, 20, .28);
}

.photo-main {
  inset: 0;
  width: 100%;
  height: 100%;
}

.photo-small {
  width: 210px;
  height: 150px;
  border: 6px solid rgba(255, 250, 241, .82);
  border-radius: 18px;
}

.photo-book {
  right: 34px;
  top: 34px;
  transform: rotate(5deg);
}

.photo-digital {
  right: 90px;
  top: 166px;
  transform: rotate(-7deg);
}

.hero-copy,
.hero-stats {
  position: relative;
  z-index: 2;
}

.hero-copy h1 {
  max-width: 620px;
  margin: 0;
  font-family: var(--market-display);
  font-size: clamp(48px, 7vw, 86px);
  line-height: 1;
  font-weight: 900;
}

.hero-copy p:not(.market-eyebrow) {
  max-width: 560px;
  margin: 18px 0 0;
  color: rgba(255, 250, 241, .78);
  font-size: 18px;
  line-height: 1.8;
}

.auth-hero .market-eyebrow {
  color: rgba(255, 250, 241, .76);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.hero-stats span {
  min-height: 96px;
  padding: 16px;
  border: 1px solid rgba(255, 250, 241, .22);
  border-radius: 18px;
  background: rgba(255, 250, 241, .14);
  color: rgba(255, 250, 241, .74);
  backdrop-filter: blur(16px);
}

.hero-stats strong {
  display: block;
  margin-bottom: 8px;
  color: #fffaf1;
  font-family: var(--market-display);
  font-size: 22px;
}

.auth-card {
  position: relative;
  padding: 28px;
  border: 1px solid rgba(255, 250, 241, .62);
  border-radius: 26px;
  background: rgba(255, 252, 245, .88);
  box-shadow: 0 26px 70px rgba(23, 32, 29, .22);
  backdrop-filter: blur(22px);
  animation: auth-rise .32s ease both;
}

.auth-card::before {
  content: "";
  position: absolute;
  inset: 14px;
  border: 1px solid rgba(47, 98, 88, .12);
  border-radius: 20px;
  pointer-events: none;
}

.mode-tabs {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 24px;
  padding: 6px;
  border-radius: 16px;
  background: rgba(35, 48, 44, .08);
}

.mode-tabs button {
  height: 42px;
  border-radius: 12px;
  background: transparent;
  color: #66736f;
  font-weight: 850;
  transition: .16s ease;
}

.mode-tabs button:hover {
  color: var(--market-green);
}

.mode-tabs button.active {
  background: var(--market-green);
  color: #fffaf1;
  box-shadow: 0 12px 26px rgba(47, 98, 88, .22);
}

.card-head,
.auth-form {
  position: relative;
  z-index: 1;
}

.card-head h2 {
  margin: 0;
  color: var(--market-ink);
  font-family: var(--market-display);
  font-size: 34px;
}

.card-head p {
  margin: 8px 0 20px;
  color: var(--market-muted);
}

.auth-form {
  display: grid;
  gap: 2px;
}

.auth-form :deep(.el-form-item__label) {
  color: var(--market-ink);
  font-weight: 800;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 14px;
  background: rgba(255, 250, 241, .78);
  box-shadow: inset 0 0 0 1px rgba(84, 67, 45, .12);
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 0 0 0 1px var(--market-green), 0 10px 24px rgba(47, 98, 88, .12);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.submit-button {
  width: 100%;
  height: 46px;
  margin-top: 8px;
  border-radius: 14px;
  font-weight: 850;
}

@keyframes auth-rise {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 980px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .auth-hero {
    min-height: 440px;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .auth-page {
    padding: 16px;
  }

  .auth-hero {
    min-height: 360px;
    padding: 24px;
    border-radius: 22px;
  }

  .photo-small {
    display: none;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .auth-card {
    padding: 20px;
    border-radius: 22px;
  }
}
</style>
