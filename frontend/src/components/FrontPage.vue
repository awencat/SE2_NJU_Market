<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Box, Collection, Goods, Reading, Search, ShoppingBag } from '@element-plus/icons-vue'
import { fetchPage } from '../api'
import marketStudyImage from '../assets/market/hero-market.jpg'
import defaultBookImage from '../assets/market/default-book.jpg'
import defaultDigitalImage from '../assets/market/default-digital.jpg'
import defaultSportsImage from '../assets/market/default-sports.jpg'
import catDailyImage from '../assets/market/cat-daily.jpg'
import catBookImage from '../assets/market/cat-book.jpg'
import catDigitalImage from '../assets/market/cat-digital.jpg'
import catSportsImage from '../assets/market/cat-sports.jpg'
import banner1 from '../assets/market/banner-1.jpg'
import banner2 from '../assets/market/banner-2.jpg'
import banner3 from '../assets/market/banner-3.jpg'

const router = useRouter()
const goods = ref([])
const loading = ref(false)

const banners = [
  { image: banner1, caption: '毕业季腾退，宿舍好物半价出' },
  { image: banner2, caption: '教材资料，上一届传下一届' },
  { image: banner3, caption: '键盘耳机，当面验机再付款' },
]

const categories = [
  { title: '日用百货', desc: '宿舍用品、小家电、收纳清洁', path: '/DailyShop', icon: ShoppingBag, color: '#2f6258', cover: catDailyImage },
  { title: '教材书刊', desc: '教材、资料、课外读物', path: '/BookShop', icon: Reading, color: '#8b5e2f', cover: catBookImage },
  { title: '数码设备', desc: '键盘、耳机、配件和外设', path: '/DigitalShop', icon: Box, color: '#416a8f', cover: catDigitalImage },
  { title: '体育用品', desc: '球类、护具、训练装备', path: '/SportsShop', icon: Goods, color: '#b34b34', cover: catSportsImage },
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

    <section class="hero">
      <el-carousel class="hero-carousel" height="460px" :interval="5000" arrow="never">
        <el-carousel-item v-for="banner in banners" :key="banner.caption">
          <div class="hero-bg" :style="{ backgroundImage: `url(${banner.image})` }"></div>
        </el-carousel-item>
      </el-carousel>

      <div class="hero-content">
        <div class="hero-main">
          <p class="hero-eyebrow">NJU Market · 校园二手交易</p>
          <h1>南大二手集市</h1>
          <div class="hero-tags">
            <span>线下面交</span>
            <span>校园闲置</span>
            <span>评论评分</span>
          </div>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/DailyShop')">
              <el-icon><Search /></el-icon>逛商品
            </el-button>
            <el-button size="large" class="ghost-btn" @click="router.push('/GoodsManager')">
              <el-icon><Collection /></el-icon>发布闲置
            </el-button>
          </div>
        </div>

        <div class="hero-aside">
          <p class="hero-lead">毕业季腾退、换季清仓，<br>闲置好物在校内继续流转。</p>
          <div class="hero-stat">
            <strong>{{ goods.length }}</strong>
            <span>件好物<br>最近上新</span>
          </div>
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
        <span class="category-photo" :style="{ backgroundImage: `url(${category.cover})` }"></span>
        <span class="category-body">
          <span class="category-icon"><el-icon><component :is="category.icon" /></el-icon></span>
          <span class="category-text">
            <strong>{{ category.title }}</strong>
            <small>{{ category.desc }}</small>
          </span>
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

    <section class="notice-strip">
      <span class="notice-tag">交易提醒</span>
      <ul>
        <li v-for="notice in notices" :key="notice">{{ notice }}</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.front-page { position: relative; display: grid; gap: 26px; isolation: isolate; }

/* 英雄区：背景轮播大图 + 叠加标题 */
.hero { position: relative; min-height: 460px; border-radius: 26px; overflow: hidden; border: 1px solid var(--market-line); box-shadow: var(--market-shadow); animation: page-rise .34s ease both; }
.hero-carousel { position: absolute; inset: 0; }
.hero-carousel :deep(.el-carousel__indicators) { bottom: 10px; right: 16px; left: auto; transform: none; }
.hero-carousel :deep(.el-carousel__button) { width: 26px; height: 4px; border-radius: 999px; background: rgba(255,250,241,.55); }
.hero-carousel :deep(.is-active .el-carousel__button) { background: var(--market-gold); }
.hero-bg { width: 100%; height: 100%; background-size: cover; background-position: center; animation: hero-zoom 9s ease-out both; }
.hero::after { content: ""; position: absolute; inset: 0; z-index: 1; pointer-events: none; background:
  linear-gradient(90deg, rgba(24,33,30,.82) 0%, rgba(24,33,30,.32) 34%, rgba(24,33,30,.1) 50%, rgba(24,33,30,.46) 78%, rgba(24,33,30,.8) 100%),
  linear-gradient(0deg, rgba(24,33,30,.4), transparent 40%); }
.hero-content { position: relative; z-index: 2; min-height: 460px; display: flex; align-items: center; justify-content: space-between; gap: 30px; padding: 0 clamp(30px, 5vw, 60px); color: #fffaf1; }
.hero-main { display: grid; gap: 16px; }
.hero-eyebrow { margin: 0; font-size: 13px; font-weight: 900; letter-spacing: .14em; color: var(--market-gold); }
.hero-content h1 { margin: 0; font-family: var(--market-display); font-size: clamp(46px, 6vw, 78px); line-height: 1; font-weight: 900; text-shadow: 0 6px 26px rgba(0,0,0,.4); }
.hero-tags { display: flex; flex-wrap: wrap; gap: 10px; }
.hero-tags span { padding: 7px 13px; border-radius: 999px; background: rgba(255,250,241,.16); color: #fffaf1; font-size: 13px; font-weight: 800; backdrop-filter: blur(4px); box-shadow: inset 0 0 0 1px rgba(255,250,241,.3); }
.hero-actions { display: flex; gap: 12px; margin-top: 6px; flex-wrap: wrap; }
.ghost-btn { --el-button-bg-color: rgba(255,250,241,.14); --el-button-border-color: rgba(255,250,241,.55); --el-button-text-color: #fffaf1; --el-button-hover-bg-color: rgba(255,250,241,.26); --el-button-hover-border-color: #fffaf1; --el-button-hover-text-color: #fffaf1; backdrop-filter: blur(4px); }
.hero-aside { flex: 0 0 auto; display: grid; gap: 18px; justify-items: end; text-align: right; max-width: 260px; }
.hero-lead { margin: 0; font-size: 18px; line-height: 1.7; color: rgba(255,250,241,.94); text-shadow: 0 2px 12px rgba(0,0,0,.45); }
.hero-stat { display: flex; align-items: center; gap: 12px; padding: 14px 20px; border-radius: 16px; background: rgba(24,33,30,.42); backdrop-filter: blur(10px); box-shadow: inset 0 0 0 1px rgba(255,250,241,.18); }
.hero-stat strong { font-family: var(--market-display); font-size: 46px; line-height: 1; color: var(--market-gold); }
.hero-stat span { font-size: 13px; line-height: 1.4; color: rgba(255,250,241,.9); text-align: left; }
@keyframes hero-zoom { from { transform: scale(1.08); } to { transform: scale(1); } }

.page-art { position: absolute; z-index: -1; pointer-events: none; border: 1px solid rgba(47,98,88,.14); background: rgba(247,251,249,.36); backdrop-filter: blur(10px); }
.page-art-a { width: 170px; height: 170px; right: -34px; top: 52px; border-radius: 34px; transform: rotate(11deg); box-shadow: 0 24px 54px rgba(50,38,25,.09); }
.page-art-b { width: 120px; height: 120px; left: -44px; top: 520px; border-radius: 999px; background: rgba(47,98,88,.12); }
.category-ribbon { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.category-pill { position: relative; display: block; min-height: 150px; padding: 0; border-radius: 18px; overflow: hidden; border: 1px solid var(--market-line); box-shadow: 0 14px 34px rgba(50,38,25,.09); text-align: left; transition: .2s ease; animation: page-rise .36s ease both; }
.category-pill:hover { transform: translateY(-4px); box-shadow: 0 20px 42px rgba(50,38,25,.16); }
.category-photo { position: absolute; inset: 0; background-size: cover; background-position: center; transition: transform .35s ease; }
.category-pill:hover .category-photo { transform: scale(1.07); }
.category-pill::after { content: ""; position: absolute; inset: 0; background: linear-gradient(180deg, rgba(28,38,34,.05) 0%, rgba(28,38,34,.32) 48%, var(--accent) 168%); opacity: .92; }
.category-body { position: absolute; z-index: 1; inset: auto 0 0 0; display: flex; align-items: center; gap: 12px; padding: 16px; }
.category-icon { width: 42px; height: 42px; display: grid; place-items: center; border-radius: 12px; background: rgba(255,250,241,.92); color: var(--accent); font-size: 21px; flex: 0 0 auto; box-shadow: 0 6px 16px rgba(0,0,0,.18); }
.category-text { min-width: 0; }
.category-pill strong, .category-pill small { display: block; color: #fffaf1; }
.category-pill strong { font-size: 17px; text-shadow: 0 2px 8px rgba(0,0,0,.35); }
.category-pill small { margin-top: 3px; font-size: 12px; line-height: 1.4; color: rgba(255,250,241,.85); }
.latest-section { padding: 24px; border-radius: 24px; background: rgba(250,253,251,.5); backdrop-filter: blur(18px); border: 1px solid var(--market-line); box-shadow: var(--market-shadow); }
.latest-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.latest-head h2 { margin: 0; font-family: var(--market-display); font-size: 34px; color: var(--market-ink); }
.goods-showcase { display: grid; grid-template-columns: 1.35fr repeat(3, minmax(0, .82fr)); gap: 14px; align-items: stretch; }
.good-card { min-height: 312px; display: grid; grid-template-rows: 168px 1fr; overflow: hidden; border-radius: 18px; background: #fcfefd; border: 1px solid var(--market-line); cursor: pointer; transition: .18s ease; animation: page-rise .32s ease both; }
.good-card.featured { min-height: 360px; grid-template-rows: 218px 1fr; }
.good-card.featured .good-image { height: 218px; }
.good-card.featured h3 { font-family: var(--market-display); font-size: 23px; min-height: auto; }
.good-card.featured .good-info strong { font-size: 26px; }
.good-card:hover { transform: translateY(-4px); box-shadow: 0 18px 36px rgba(50,38,25,.13); }
.good-skeleton { cursor: default; pointer-events: none; }
.good-skeleton:hover { transform: none; box-shadow: none; }
.skeleton-cover { height: 168px; background: linear-gradient(90deg, rgba(214,233,224,.7), rgba(250,253,251,.92), rgba(214,233,224,.7)); background-size: 220% 100%; animation: skeleton-shine 1.2s ease-in-out infinite; }
.good-card.featured .skeleton-cover { height: 218px; }
.skeleton-info { display: grid; align-content: start; gap: 12px; padding: 16px; }
.skeleton-line { display: block; height: 13px; border-radius: 999px; background: linear-gradient(90deg, rgba(84,67,45,.12), rgba(255,255,255,.7), rgba(84,67,45,.12)); background-size: 220% 100%; animation: skeleton-shine 1.2s ease-in-out infinite; }
.skeleton-line.title { width: 76%; height: 18px; }
.skeleton-line.short { width: 46%; }
.skeleton-line.bottom { width: 62%; margin-top: 26px; }
.good-image { height: 168px; display: grid; place-items: center; background: linear-gradient(135deg, #cfe3da, #9cc3b4); color: var(--market-green); font-family: var(--market-display); font-size: 28px; font-weight: 900; overflow: hidden; }
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
.notice-strip { display: flex; align-items: center; gap: 18px; padding: 14px 22px; border-radius: 16px; border: 1px solid var(--market-line); background: rgba(250,253,251,.72); backdrop-filter: blur(10px); box-shadow: 0 10px 26px rgba(28,60,53,.08); }
.notice-strip .notice-tag { flex: 0 0 auto; padding: 5px 12px; border-radius: 999px; background: var(--market-green); color: #fffaf1; font-size: 13px; font-weight: 800; }
.notice-strip ul { display: flex; flex-wrap: wrap; gap: 8px 26px; margin: 0; padding: 0; list-style: none; }
.notice-strip li { position: relative; padding-left: 16px; color: var(--market-muted); font-size: 14px; }
.notice-strip li::before { content: ""; position: absolute; left: 0; top: 8px; width: 6px; height: 6px; border-radius: 50%; background: var(--market-gold); }
@keyframes page-rise { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes skeleton-shine { 0% { background-position: 120% 0; } 100% { background-position: -120% 0; } }
@media (max-width: 980px) { .category-ribbon, .goods-showcase { grid-template-columns: 1fr 1fr; } .good-card.featured { grid-column: 1 / -1; } }
@media (max-width: 760px) { .hero-content { flex-direction: column; align-items: flex-start; justify-content: center; gap: 20px; } .hero-aside { justify-items: start; text-align: left; max-width: none; } }
@media (max-width: 640px) { .category-ribbon, .goods-showcase { grid-template-columns: 1fr; } .hero { min-height: 420px; } .hero-content { min-height: 420px; } .page-art { display: none; } .good-card.featured { grid-column: auto; } }
</style>
