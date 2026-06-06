<template>
  <transition name="loading-fade" @after-leave="onTransitionEnd">
    <div id="loading" v-if="visible">
      <div class="loading-content">
        <div class="loading-mark">
          <span>N</span>
        </div>
        <div class="loading-copy">
          <strong>NJU Market</strong>
          <span>正在加载</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'Loading',
  setup() {
    const visible = ref(true)
    const startTime = ref(Date.now())
    const minDisplayTime = 800

    const hideLoading = () => {
      const elapsedTime = Date.now() - startTime.value
      const remainingTime = Math.max(0, minDisplayTime - elapsedTime)

      setTimeout(() => {
        visible.value = false
      }, remainingTime)
    }

    const onTransitionEnd = () => {
      const loader = document.getElementById('loading')
      if (loader && !visible.value) {
        loader.remove()
      }
    }

    onMounted(() => {
      window.addEventListener('load', hideLoading)

      if (document.readyState === 'complete') {
        hideLoading()
      }
    })

    return {
      visible,
      onTransitionEnd
    }
  }
}
</script>

<style scoped>
#loading {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 30% 24%, rgba(194, 122, 44, .22), transparent 26%),
    radial-gradient(circle at 72% 74%, rgba(47, 98, 88, .2), transparent 28%),
    linear-gradient(135deg, #fff9ec 0%, #f2eadf 100%);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  padding: 30px 34px;
  border: 1px solid rgba(84, 67, 45, .18);
  border-radius: 24px;
  background: rgba(255, 252, 245, .76);
  box-shadow: 0 22px 54px rgba(50, 38, 25, .14);
  backdrop-filter: blur(18px);
}

.loading-mark {
  width: 78px;
  height: 78px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  background: #2f6258;
  color: #fffaf1;
  box-shadow: 0 16px 34px rgba(47, 98, 88, .26);
  animation: markFloat 1.6s ease-in-out infinite;
}

.loading-mark span {
  font-size: 42px;
  line-height: 1;
  font-family: Georgia, Cambria, serif;
  font-weight: 900;
}

.loading-copy {
  display: grid;
  gap: 5px;
  text-align: center;
}

.loading-copy strong {
  color: #212c29;
  font-size: 20px;
}

.loading-copy span {
  color: #65726e;
  font-size: 14px;
  letter-spacing: .08em;
}

@keyframes markFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.loading-fade-enter-active,
.loading-fade-leave-active {
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.loading-fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.loading-fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}
</style>
