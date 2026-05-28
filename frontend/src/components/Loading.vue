<template>
  <transition name="loading-fade" @after-leave="onTransitionEnd">
    <div id="loading" v-if="visible">
      <div class="loading-content">
        <img src="/loading-logo.png" alt="Loading" class="loading-logo" />
        <div class="loading-text">加载中...</div>
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
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #fff9f0 0%, #ffe4cc 100%);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.loading-logo {
  width: 150px;
  height: 150px;
  object-fit: contain;
  animation: logoSpin 2s linear infinite, logoPulse 1.5s ease-in-out infinite;
  filter: drop-shadow(0 4px 12px rgba(255, 154, 60, 0.3));
}

.loading-text {
  font-size: 20px;
  color: #ff8c2e;
  font-weight: 600;
  letter-spacing: 3px;
  animation: textPulse 1.5s ease-in-out infinite;
}

@keyframes logoSpin {
  0% {
    transform: rotateY(0deg);
  }
  100% {
    transform: rotateY(360deg);
  }
}

@keyframes logoPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes textPulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
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
