<template>
  <div id="app">
    <Loading />
    <router-view />
  </div>
</template>

<script setup>
import Loading from './components/Loading.vue'
</script>

<style>
#app {
  min-height: 100vh;
}
</style>