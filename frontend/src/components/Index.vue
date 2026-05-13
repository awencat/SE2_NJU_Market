<script setup>
import HeaderR from './HeaderR.vue'
</script>

<template>
  <el-container class="market-layout">
    <el-header class="market-layout__header">
      <HeaderR />
    </el-header>

    <el-main class="market-layout__main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.market-layout {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(245, 248, 252, 0.96), rgba(235, 241, 247, 0.96)),
    url('/src/assets/hero.png') center top / cover fixed;
}

.market-layout__header {
  height: auto;
  min-height: 64px;
  padding: 0;
  box-shadow: 0 8px 30px rgba(20, 36, 54, 0.14);
  z-index: 2;
}

.market-layout__main {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  padding: 28px 0 48px;
}
</style>
