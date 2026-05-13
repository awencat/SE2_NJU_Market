<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '页面功能正在补充中，当前先用于验证顶部导航和路由切换。',
  },
})
</script>

<template>
  <section class="placeholder-page">
    <p class="eyebrow">NJU MARKET</p>
    <h1>{{ title }}</h1>
    <p>{{ description }}</p>
    <el-button type="primary" @click="$router.push('/FrontPage')">返回主站</el-button>
  </section>
</template>

<style scoped>
.placeholder-page {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 16px;
  padding: 42px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(32, 48, 64, 0.08);
  box-shadow: 0 18px 48px rgba(32, 48, 64, 0.1);
}

.eyebrow {
  margin: 0;
  color: #b36b1f;
  font-weight: 800;
  letter-spacing: 0.16em;
  font-size: 13px;
}

.placeholder-page h1 {
  margin: 0;
  color: #172636;
  font-size: 34px;
  letter-spacing: 0;
}

.placeholder-page p {
  max-width: 620px;
  margin: 0;
  color: #607286;
}
</style>
