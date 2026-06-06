<template>
  <div id="app">
    <Loading />
    <router-view/>
  </div>
</template>

<script>
import Loading from './components/Loading.vue'

export default {
  name: 'App',
  components: {
    Loading
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>


