<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Box, Collection, Goods, Reading, Search, ShoppingBag } from '@element-plus/icons-vue'
import { fetchPage } from '../api'
import marketStudyImage from '../assets/market/market-study.jpg'
import defaultBookImage from '../assets/market/default-book.jpg'
import defaultDigitalImage from '../assets/market/default-digital.jpg'
import defaultSportsImage from '../assets/market/default-sports.jpg'

const router = useRouter()
const goods = ref([])
const loading = ref(false)

const categories = [
  { title: '日用百货', desc: '宿舍用品、小家电、收纳清洁', path: '/DailyShop', icon: ShoppingBag, color: '#2f6258' },
  { title: '教材书刊', desc: '教材、资料、课外读物', path: '/BookShop', icon: Reading, color: '#8b5e2f' },
  { title: '数码设备', desc: '键盘、耳机、配件和外设', path: '/DigitalShop', icon: Box, color: '#416a8f' },
  { title: '体育用品', desc: '球类、护具、训练装备', path: '/SportsShop', icon: Goods, color: '#b34b34' },
]

const notices = [
  '交易前先确认商品数量、成色和取货地点',
  '贵重物品建议线下面交验货',
  '遇到异常交易可以在商品详情页举报',
]

function getImageUrl(item) {
  const image = item.image || item.imageUrl || item.coverUrl || item.images?.[0]?.imageUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8080/uploads/goods/${image}`
}

function getFallbackImage(item = {}) {
  const category = String(item.category || '').toLowerCase()
  if (category.includes('book') || category.includes('书')) return defaultBookImage
  if (category.includes('digital') || category.includes('数码')) return defaultDigitalImage
  if (category.includes('sport') || category.includes('体育')) return defaultSportsImage
  return marketStudyImage
}

async function loadLatestGoods() {
  loading.value = true
  try {
    const result = await fetchPage({ pageSize: 4, pageNum: 1, param: {} })
    const records = Array.isArray(result.data) ? result.data : (result.data?.records || [])
    goods.value = records.map((item) => ({ ...item, imageUrl: getImageUrl(item) }))
  } catch {
    goods.value = []
  } finally {
    loading.value = false
  }
}

function openGoodDetail(item) {
  const id = item.goodId || item.id
  if (id) router.push(`/goods/${id}`)
}

onMounted(loadLatestGoods)
</script>

<template>
  <div class="front-page market-page">
    <div class="page-art page-art-a"></div>
    <div class="page-art page-art-b"></div>
    <section class="hero-layout">
      <div class="hero-copy">
        <div class="paper-pin pin-left"></div>
        <div class="paper-pin pin-right"></div>
        <div class="floating-goods" aria-hidden="true">
          <span class="float-item book"><el-icon><Reading /></el-icon></span>
          <span class="float-item lamp"><i></i></span>
          <span class="float-item ball"><i></i></span>
          <span class="float-item keys"><el-icon><Box /></el-icon></span>
        </div>
        <p class="market-eyebrow">NJU Market</p>
        <h1>南大二手集市</h1>
        <p>教材、台灯、键盘、球拍，都可以在这里继续流转。</p>
        <div class="hero-tags">
          <span>线下面交</span>
          <span>校园闲置</span>
          <span>评论评分</span>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/DailyShop')">
            <el-icon><Search /></el-icon>逛商品
          </el-button>
          <el-button size="large" @click="router.push('/GoodsManager')">
            <el-icon><Collection /></el-icon>发布商品
          </el-button>
        </div>
        <div class="hero-metrics">
          <button type="button" @click="router.push('/BookShop')">
            <strong>书刊</strong>
            <span>教材、资料、考试用书</span>
          </button>
          <button type="button" @click="router.push('/DigitalShop')">
            <strong>数码</strong>
            <span>键盘、耳机、配件</span>
          </button>
          <button type="button" @click="router.push('/OrderManager')">
            <strong>订单</strong>
            <span>查看订购和交付</span>
          </button>
        </div>
      </div>

      <div class="hero-side">
        <div class="hero-photo">
          <img :src="marketStudyImage" alt="南大二手集市" />
          <div class="photo-note">
            <span>最近上新</span>
            <strong>{{ goods.length }}</strong>
            <p>件商品</p>
          </div>
        </div>
        <div class="notice-board">
          <strong>交易提醒</strong>
          <p v-for="notice in notices" :key="notice">{{ notice }}</p>
        </div>
      </div>
    </section>

    <section class="category-ribbon">
      <button
        v-for="category in categories"
        :key="category.title"
        type="button"
        class="category-pill"
        :style="{ '--accent': category.color }"
        @click="router.push(category.path)"
      >
        <el-icon><component :is="category.icon" /></el-icon>
        <span>
          <strong>{{ category.title }}</strong>
          <small>{{ category.desc }}</small>
        </span>
      </button>
    </section>

    <section class="latest-section">
      <div class="latest-head">
        <div>
          <p class="market-eyebrow">Latest</p>
          <h2>最近发布</h2>
        </div>
        <el-button text @click="router.push('/DailyShop')">查看全部</el-button>
      </div>
      <div class="goods-showcase">
        <template v-if="loading">
          <article v-for="item in 4" :key="`skeleton-${item}`" class="good-card good-skeleton" :class="{ featured: item === 1 }">
            <div class="skeleton-cover"></div>
            <div class="skeleton-info">
              <i class="skeleton-line title"></i>
              <i class="skeleton-line short"></i>
              <i class="skeleton-line bottom"></i>
            </div>
          </article>
        </template>
        <article v-else-if="goods.length === 0" class="empty-card">暂无商品</article>
        <article
          v-else
          v-for="(item, index) in goods"
          :key="item.goodId || item.id"
          class="good-card"
          :class="{ featured: index === 0 }"
          @click="openGoodDetail(item)"
        >
          <div class="good-image">
            <img :src="item.imageUrl || getFallbackImage(item)" :alt="item.title || '商品图片'" />
          </div>
          <div class="good-info">
            <h3>{{ item.title || '未命名商品' }}</h3>
            <p>{{ item.condition || '二手' }}</p>
            <div>
              <strong>¥{{ item.price || 0 }}</strong>
              <span>{{ Number(item.count || 0) > 0 ? `库存 ${item.count}` : '已售罄' }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.front-page { position: relative; display: grid; gap: 26px; isolation: isolate; }
.front-page::before { content: ""; position: fixed; inset: 0; z-index: -3; pointer-events: none; background:
  linear-gradient(120deg, rgba(255,249,236,.84), rgba(238,231,219,.8)),
  radial-gradient(circle at 12% 20%, rgba(47,98,88,.15), transparent 26%),
  radial-gradient(circle at 82% 12%, rgba(65,106,143,.16), transparent 24%),
  radial-gradient(circle at 72% 78%, rgba(194,122,44,.16), transparent 28%),
  repeating-linear-gradient(135deg, rgba(33,44,41,.032) 0, rgba(33,44,41,.032) 1px, transparent 1px, transparent 22px); }
.front-page::after { content: ""; position: fixed; inset: 0; z-index: -2; pointer-events: none; opacity: .45; background-image:
  linear-gradient(90deg, rgba(255,255,255,.24) 1px, transparent 1px),
  linear-gradient(0deg, rgba(255,255,255,.2) 1px, transparent 1px);
  background-size: 44px 44px; mask-image: linear-gradient(to bottom, #000, transparent 78%); }
.page-art { position: absolute; z-index: -1; pointer-events: none; border: 1px solid rgba(84,67,45,.14); background: rgba(255,252,245,.32); backdrop-filter: blur(10px); }
.page-art-a { width: 170px; height: 170px; right: -34px; top: 52px; border-radius: 34px; transform: rotate(11deg); box-shadow: 0 24px 54px rgba(50,38,25,.09); }
.page-art-b { width: 120px; height: 120px; left: -44px; top: 520px; border-radius: 999px; background: rgba(47,98,88,.12); }
.hero-layout { min-height: 430px; display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(360px, .78fr); gap: 24px; align-items: stretch; }
.hero-copy { position: relative; min-height: 430px; padding: 48px 46px 40px; border-radius: 26px; background:
  linear-gradient(145deg, rgba(255,252,245,.94), rgba(244,232,214,.78)),
  radial-gradient(circle at 18% 18%, rgba(255,255,255,.72), transparent 18%),
  repeating-linear-gradient(135deg, rgba(47,98,88,.035) 0, rgba(47,98,88,.035) 1px, transparent 1px, transparent 18px);
  display: grid; align-content: start; border: 1px solid var(--market-line); box-shadow: var(--market-shadow); overflow: hidden; animation: page-rise .35s ease both; }
.hero-copy::before { content: ""; position: absolute; inset: 18px; border: 1px solid rgba(47,98,88,.13); border-radius: 22px; pointer-events: none; }
.hero-copy::after { content: ""; position: absolute; right: -58px; bottom: -72px; width: 250px; height: 250px; border-radius: 50%; background: rgba(47,98,88,.12); }
.paper-pin { position: absolute; z-index: 2; top: 18px; width: 18px; height: 18px; border-radius: 50%; background: radial-gradient(circle at 35% 35%, #fff7dc, var(--market-gold)); box-shadow: 0 6px 14px rgba(50,38,25,.18); }
.pin-left { left: 24px; }
.pin-right { right: 24px; }
.floating-goods { position: absolute; inset: 0; z-index: 0; pointer-events: none; }
.float-item { position: absolute; display: grid; place-items: center; width: 54px; height: 54px; border-radius: 16px; background: rgba(255,250,241,.74); color: var(--market-green); font-size: 25px; box-shadow: inset 0 0 0 1px rgba(84,67,45,.12), 0 12px 26px rgba(50,38,25,.08); transform: rotate(-8deg); }
.float-item.book { right: 128px; top: 42px; }
.float-item.lamp { right: 66px; top: 134px; color: var(--market-gold); transform: rotate(9deg); }
.float-item.lamp i { position: relative; width: 26px; height: 28px; display: block; }
.float-item.lamp i::before { content: ""; position: absolute; left: 4px; top: 0; width: 18px; height: 13px; border-radius: 13px 13px 4px 4px; background: var(--market-gold); }
.float-item.lamp i::after { content: ""; position: absolute; left: 11px; top: 13px; width: 4px; height: 15px; border-radius: 999px; background: var(--market-green); box-shadow: -7px 14px 0 2px var(--market-green), 7px 14px 0 2px var(--market-green); }
.float-item.ball { right: 154px; bottom: 96px; color: var(--market-red); border-radius: 50%; transform: rotate(13deg); }
.float-item.ball i { width: 34px; height: 34px; display: block; border-radius: 50%; background:
  linear-gradient(90deg, transparent 45%, rgba(255,250,241,.9) 46%, rgba(255,250,241,.9) 54%, transparent 55%),
  linear-gradient(0deg, transparent 45%, rgba(255,250,241,.9) 46%, rgba(255,250,241,.9) 54%, transparent 55%),
  var(--market-red); }
.float-item.keys { right: 54px; bottom: 42px; color: var(--market-blue); transform: rotate(-12deg); }
.hero-copy h1 { position: relative; z-index: 1; margin: 0; color: var(--market-ink); font-family: var(--market-display); font-size: clamp(48px, 7vw, 86px); line-height: 1; font-weight: 900; }
.hero-copy p:not(.market-eyebrow) { position: relative; z-index: 1; max-width: 560px; margin: 20px 0 0; color: #53615d; font-size: 20px; line-height: 1.75; }
.hero-tags { position: relative; z-index: 1; display: flex; flex-wrap: wrap; gap: 10px; margin-top: 22px; }
.hero-tags span { padding: 7px 11px; border-radius: 999px; background: rgba(255,250,241,.8); color: var(--market-green); font-size: 13px; font-weight: 850; box-shadow: inset 0 0 0 1px rgba(47,98,88,.18); }
.hero-actions { position: relative; z-index: 1; display: flex; gap: 12px; margin-top: 24px; flex-wrap: wrap; }
.hero-metrics { position: relative; z-index: 1; display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 10px; margin-top: 30px; }
.hero-metrics button { min-height: 92px; display: grid; align-content: end; gap: 6px; padding: 14px; border-radius: 18px; background: rgba(255,250,241,.72); color: var(--market-ink); text-align: left; box-shadow: inset 0 0 0 1px rgba(84,67,45,.14); transition: .16s ease; }
.hero-metrics button:hover { transform: translateY(-3px); background: rgba(255,250,241,.94); box-shadow: inset 0 0 0 1px rgba(47,98,88,.26), 0 12px 26px rgba(50,38,25,.09); }
.hero-metrics strong { font-family: var(--market-display); font-size: 24px; line-height: 1; color: var(--market-green); }
.hero-metrics span { color: var(--market-muted); font-size: 13px; line-height: 1.4; }
.hero-side { display: grid; grid-template-rows: 1fr auto; gap: 16px; animation: page-rise .42s ease both; }
.hero-photo { position: relative; min-height: 300px; border-radius: 26px; overflow: hidden; box-shadow: var(--market-shadow); border: 1px solid rgba(255,255,255,.42); }
.hero-photo img { width: 100%; height: 100%; display: block; object-fit: cover; filter: saturate(.95) contrast(.98); }
.photo-note { position: absolute; left: 22px; bottom: 22px; min-width: 138px; padding: 16px 18px; border-radius: 16px; background: rgba(36,48,45,.78); backdrop-filter: blur(16px); color: #fffaf1; }
.photo-note span, .photo-note p { margin: 0; color: rgba(255,250,241,.72); }
.photo-note strong { display: block; margin: 4px 0; font-size: 46px; line-height: 1; }
.notice-board { padding: 18px 20px; border-radius: 20px; border: 1px solid var(--market-line); background: rgba(255,252,245,.78); backdrop-filter: blur(14px); box-shadow: 0 16px 38px rgba(50,38,25,.1); }
.notice-board strong { display: block; margin-bottom: 8px; color: var(--market-ink); }
.notice-board p { margin: 6px 0 0; color: var(--market-muted); line-height: 1.55; }
.category-ribbon { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.category-pill { display: flex; align-items: center; gap: 12px; min-height: 96px; padding: 16px; border-radius: 18px; background: rgba(255,252,245,.78); border: 1px solid var(--market-line); box-shadow: 0 14px 34px rgba(50,38,25,.09); text-align: left; transition: .18s ease; animation: page-rise .36s ease both; }
.category-pill:hover { transform: translateY(-4px); background: rgba(255,252,245,.96); }
.category-pill .el-icon { width: 44px; height: 44px; display: grid; place-items: center; border-radius: 13px; background: var(--accent); color: #fff; font-size: 22px; flex: 0 0 auto; }
.category-pill strong, .category-pill small { display: block; }
.category-pill strong { color: var(--market-ink); font-size: 16px; }
.category-pill small { margin-top: 4px; color: var(--market-muted); line-height: 1.4; }
.latest-section { padding: 24px; border-radius: 24px; background: rgba(255,252,245,.58); backdrop-filter: blur(16px); border: 1px solid var(--market-line); box-shadow: var(--market-shadow); }
.latest-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.latest-head h2 { margin: 0; font-family: var(--market-display); font-size: 34px; color: var(--market-ink); }
.goods-showcase { display: grid; grid-template-columns: 1.35fr repeat(3, minmax(0, .82fr)); gap: 14px; align-items: stretch; }
.good-card { min-height: 312px; display: grid; grid-template-rows: 168px 1fr; overflow: hidden; border-radius: 18px; background: #fffaf1; border: 1px solid var(--market-line); cursor: pointer; transition: .18s ease; animation: page-rise .32s ease both; }
.good-card.featured { min-height: 360px; grid-template-rows: 218px 1fr; }
.good-card.featured .good-image { height: 218px; }
.good-card.featured h3 { font-family: var(--market-display); font-size: 23px; min-height: auto; }
.good-card.featured .good-info strong { font-size: 26px; }
.good-card:hover { transform: translateY(-4px); box-shadow: 0 18px 36px rgba(50,38,25,.13); }
.good-skeleton { cursor: default; pointer-events: none; }
.good-skeleton:hover { transform: none; box-shadow: none; }
.skeleton-cover { height: 168px; background: linear-gradient(90deg, rgba(229,218,200,.7), rgba(255,250,241,.92), rgba(229,218,200,.7)); background-size: 220% 100%; animation: skeleton-shine 1.2s ease-in-out infinite; }
.good-card.featured .skeleton-cover { height: 218px; }
.skeleton-info { display: grid; align-content: start; gap: 12px; padding: 16px; }
.skeleton-line { display: block; height: 13px; border-radius: 999px; background: linear-gradient(90deg, rgba(84,67,45,.12), rgba(255,255,255,.7), rgba(84,67,45,.12)); background-size: 220% 100%; animation: skeleton-shine 1.2s ease-in-out infinite; }
.skeleton-line.title { width: 76%; height: 18px; }
.skeleton-line.short { width: 46%; }
.skeleton-line.bottom { width: 62%; margin-top: 26px; }
.good-image { height: 168px; display: grid; place-items: center; background: linear-gradient(135deg, #e5dac8, #d5b47b); color: var(--market-green); font-family: var(--market-display); font-size: 28px; font-weight: 900; overflow: hidden; }
.good-image img { width: 100%; height: 100%; display: block; object-fit: cover; transition: transform .3s ease; }
.good-card:hover img { transform: scale(1.04); }
.good-info { min-height: 144px; display: grid; grid-template-rows: auto auto 1fr; padding: 15px; }
.good-info h3, .good-info p { margin: 0; }
.good-info h3 { min-height: 42px; color: var(--market-ink); font-size: 16px; line-height: 1.35; overflow: hidden; }
.good-info p { margin-top: 6px; color: var(--market-muted); }
.good-info div { align-self: end; display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-top: 14px; }
.good-info strong { color: var(--market-red); font-size: 20px; }
.good-info span { color: var(--market-muted); font-size: 13px; }
.empty-card { grid-column: 1 / -1; min-height: 170px; display: grid; place-items: center; border: 1px dashed var(--market-line); border-radius: 18px; color: var(--market-muted); }
@keyframes page-rise { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes skeleton-shine { 0% { background-position: 120% 0; } 100% { background-position: -120% 0; } }
@media (max-width: 980px) { .hero-layout, .category-ribbon, .goods-showcase { grid-template-columns: 1fr 1fr; } .good-card.featured { grid-column: 1 / -1; } }
@media (max-width: 640px) { .hero-layout, .category-ribbon, .goods-showcase, .hero-metrics { grid-template-columns: 1fr; } .hero-copy { min-height: auto; padding: 30px; } .hero-photo { min-height: 280px; } .page-art, .floating-goods { display: none; } .good-card.featured { grid-column: auto; } }
</style>
