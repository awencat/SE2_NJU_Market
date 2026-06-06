<script setup>
import HeaderR from './HeaderR.vue'
</script>

<template>
  <div class="market-layout">
    <HeaderR />
    <main class="market-layout__main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.market-layout {
  min-height: 100vh;
}

.market-layout__main {
  min-height: calc(100vh - 70px);
}
</style>