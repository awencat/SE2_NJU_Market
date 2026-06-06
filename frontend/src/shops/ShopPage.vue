<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Coin, Goods, Phone, Refresh, Search, StarFilled, User, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchGoodRatingSummary, fetchPage } from '../api'
import marketStudyImage from '../assets/market/market-study.jpg'
import defaultBookImage from '../assets/market/default-book.jpg'
import defaultDigitalImage from '../assets/market/default-digital.jpg'
import defaultSportsImage from '../assets/market/default-sports.jpg'

const props = defineProps({
  category: { type: String, required: true },
  title: { type: String, required: true },
  eyebrow: { type: String, default: 'Market' },
  description: { type: String, default: '挑一件合适的闲置，约个方便的时间面交。' },
  placeholder: { type: String, default: '搜索商品' },
  emptyText: { type: String, default: '暂时没有找到商品' },
  accent: { type: String, default: '#2f6258' },
})

const router = useRouter()
const tableData = ref([])
const pageSize = ref(8)
const pageNum = ref(1)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)
const stockFilter = ref('all')
const priceFilter = ref('all')
const minRating = ref(0)
const ratingMap = ref({})

const stockOptions = [
  { label: '全部', value: 'all' },
  { label: '有货', value: 'available' },
  { label: '售罄', value: 'soldout' },
]

const priceOptions = [
  { label: '全部', value: 'all' },
  { label: '< 30', value: 'low' },
  { label: '30-100', value: 'mid' },
  { label: '100+', value: 'high' },
]

const defaultImages = {
  daily: marketStudyImage,
  book: defaultBookImage,
  digital: defaultDigitalImage,
  sports: defaultSportsImage,
  pet: marketStudyImage,
}

const filteredProducts = computed(() => tableData.value.filter((product) => {
  const stock = Number(product?.count || 0)
  const price = Number(product?.price || 0)
  const rating = getRating(product)
  const score = Number(rating.averageScore || 0)
  const ratingCount = Number(rating.ratingCount || 0)
  const stockOk = stockFilter.value === 'all' || (stockFilter.value === 'available' ? stock > 0 : stock <= 0)
  const priceOk = priceFilter.value === 'all'
    || (priceFilter.value === 'low' && price < 30)
    || (priceFilter.value === 'mid' && price >= 30 && price <= 100)
    || (priceFilter.value === 'high' && price > 100)
  const ratingOk = minRating.value <= 0 || (ratingCount > 0 && score >= minRating.value)
  return stockOk && priceOk && ratingOk
}))

const hasProducts = computed(() => filteredProducts.value.length > 0)

function getGoodId(product) { return product.goodId || product.id }
function getGoodTitle(product) { return product.title || product.goodsname || '未命名商品' }
function getSellerName(product) { return product.sellerName || product.authorName || '未知卖家' }
function getSellerPhone(product) { return product.sellerPhone || product.authorNumber || '' }
function isPurchasable(product) { return Number(product?.count || 0) > 0 }
function statusText(product) {
  const stock = Number(product?.count || 0)
  return stock > 0 ? `库存 ${stock}` : '已售罄'
}
function defaultLabel(product) { return getGoodTitle(product).slice(0, 2) }
function getDefaultImage() { return defaultImages[props.category] || marketStudyImage }
function getRating(product) {
  return ratingMap.value[getGoodId(product)] || { averageScore: 0, ratingCount: 0 }
}
function getRatingText(product) {
  const rating = getRating(product)
  return rating.ratingCount > 0 ? `${Number(rating.averageScore || 0).toFixed(1)} (${rating.ratingCount})` : '暂无评分'
}

function getImageUrl(product) {
  const image = product.image || product.imageUrl || product.coverUrl || product.images?.[0]?.imageUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8080/uploads/goods/${image}`
}

function handleImageError(event) {
  event.target.style.display = 'none'
  const nextElement = event.target.nextElementSibling
  if (nextElement) nextElement.style.display = 'flex'
}

async function loadRatings(products) {
  const ids = products.map(getGoodId).filter(Boolean)
  if (ids.length === 0) {
    ratingMap.value = {}
    return
  }

  const entries = await Promise.all(ids.map(async (id) => {
    try {
      const res = await fetchGoodRatingSummary(id)
      return [id, res.data || { averageScore: 0, ratingCount: 0 }]
    } catch {
      return [id, { averageScore: 0, ratingCount: 0 }]
    }
  }))
  ratingMap.value = Object.fromEntries(entries)
}

async function loadPost() {
  loading.value = true
  try {
    const res = await fetchPage({
      pageSize: pageSize.value,
      pageNum: pageNum.value,
      param: {
        title: keyword.value,
        goodsname: keyword.value,
        category: props.category,
      },
    })
    const records = Array.isArray(res.data) ? res.data : (res.data?.records || res.data?.list || [])
    tableData.value = records
    total.value = res.data?.total || res.total || records.length
    await loadRatings(records)
  } catch (error) {
    ElMessage.error(error.message || '加载商品失败')
    tableData.value = []
    total.value = 0
    ratingMap.value = {}
  } finally {
    loading.value = false
  }
}

function resetParam() {
  keyword.value = ''
  stockFilter.value = 'all'
  priceFilter.value = 'all'
  minRating.value = 0
  pageNum.value = 1
  loadPost()
}
function handleSizeChange(val) { pageSize.value = val; pageNum.value = 1; loadPost() }
function handleCurrentChange(val) { pageNum.value = val; loadPost() }
function openDetail(product) {
  const goodId = getGoodId(product)
  if (!goodId) return ElMessage.warning('商品编号不存在')
  router.push(`/goods/${goodId}`)
}

onMounted(loadPost)
</script>

<template>
  <div class="shop-page market-page" :style="{ '--shop-accent': accent }">
    <section class="shop-hero">
      <div class="hero-text">
        <p class="market-eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
      <div class="search-panel">
        <el-input v-model="keyword" clearable :placeholder="placeholder" @keyup.enter="loadPost" />
        <el-button type="primary" @click="loadPost">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="resetParam">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
      </div>
    </section>

    <section class="filter-strip">
      <div class="filter-group">
        <span>库存</span>
        <button
          v-for="item in stockOptions"
          :key="item.value"
          type="button"
          :class="{ active: stockFilter === item.value }"
          @click="stockFilter = item.value"
        >
          {{ item.label }}
        </button>
      </div>
      <div class="filter-group">
        <span>价格</span>
        <button
          v-for="item in priceOptions"
          :key="item.value"
          type="button"
          :class="{ active: priceFilter === item.value }"
          @click="priceFilter = item.value"
        >
          {{ item.label }}
        </button>
      </div>
      <div class="filter-group">
        <span>评分</span>
        <div class="rating-filter">
          <el-rate v-model="minRating" :max="5" :allow-half="true" clearable />
          <b>{{ minRating > 0 ? `${minRating} 分以上` : '不限' }}</b>
        </div>
      </div>
    </section>

    <section v-loading="loading" class="product-area">
      <div v-if="hasProducts" class="products-grid">
        <article
          v-for="product in filteredProducts"
          :key="getGoodId(product)"
          class="product-card"
          @click="openDetail(product)"
        >
          <div class="image-wrap">
            <img
              :src="getImageUrl(product) || getDefaultImage()"
              :alt="getGoodTitle(product)"
              @error="handleImageError"
            />
            <div class="image-empty">
              <el-icon><Goods /></el-icon>
              <span>{{ defaultLabel(product) }}</span>
            </div>
            <el-tag class="status-tag" :type="isPurchasable(product) ? 'success' : 'info'" effect="dark">
              {{ statusText(product) }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="title-row">
              <h2>{{ getGoodTitle(product) }}</h2>
              <span class="rating-pill">
                <el-icon><StarFilled /></el-icon>{{ getRatingText(product) }}
              </span>
            </div>
            <p class="description">{{ product.description || '卖家暂时没有填写商品描述。' }}</p>
            <div class="price-row">
              <span class="price"><el-icon><Coin /></el-icon>¥{{ product.price || 0 }}</span>
              <span class="detail-link"><el-icon><View /></el-icon>详情</span>
            </div>
            <div class="seller-row">
              <span><el-icon><User /></el-icon>{{ getSellerName(product) }}</span>
              <span v-if="getSellerPhone(product)"><el-icon><Phone /></el-icon>{{ getSellerPhone(product) }}</span>
            </div>
          </div>
        </article>
      </div>
      <el-empty v-else :description="emptyText">
        <el-button type="primary" @click="resetParam">重新加载</el-button>
      </el-empty>
      <div v-if="hasProducts" class="pagination-section">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.shop-page { display: grid; gap: 18px; position: relative; }
.shop-page::before { content: ""; position: fixed; inset: 0; z-index: -1; pointer-events: none; background:
  linear-gradient(135deg, color-mix(in srgb, var(--shop-accent) 10%, transparent), transparent 34%),
  radial-gradient(circle at 88% 18%, color-mix(in srgb, var(--shop-accent) 16%, transparent), transparent 28%),
  repeating-linear-gradient(135deg, rgba(33,44,41,.026) 0, rgba(33,44,41,.026) 1px, transparent 1px, transparent 18px);
}
.shop-hero { position: relative; display: grid; grid-template-columns: minmax(0,1fr) minmax(330px,.72fr); gap: 22px; align-items: end; padding: 34px; border-radius: 26px; border: 1px solid color-mix(in srgb, var(--shop-accent) 22%, var(--market-line)); background: linear-gradient(135deg,rgba(255,252,245,.92), color-mix(in srgb, var(--shop-accent) 12%, rgba(244,232,214,.66))); box-shadow: var(--market-shadow); overflow: hidden; animation: rise .3s ease both; }
.shop-hero::before { content: ""; position: absolute; inset: 18px; border: 1px solid color-mix(in srgb, var(--shop-accent) 20%, transparent); border-radius: 21px; pointer-events: none; }
.shop-hero::after { content: ""; position: absolute; right: -70px; top: -80px; width: 230px; height: 230px; border-radius: 50%; background: color-mix(in srgb, var(--shop-accent) 18%, transparent); }
.hero-text { position: relative; z-index: 1; }
.hero-text h1 { margin: 0; font-family: var(--market-display); font-size: clamp(38px,5vw,62px); line-height: 1; color: var(--market-ink); }
.hero-text p:not(.market-eyebrow) { max-width: 560px; margin: 14px 0 0; color: var(--market-muted); font-size: 16px; line-height: 1.75; }
.search-panel { position: relative; z-index: 1; display: grid; grid-template-columns: minmax(0,1fr) auto auto; gap: 10px; padding: 10px; border: 1px solid var(--market-line); border-radius: 14px; background: rgba(255,250,241,.82); backdrop-filter: blur(12px); }
.filter-strip { display: flex; align-items: center; justify-content: space-between; gap: 14px; flex-wrap: wrap; padding: 12px 14px; border: 1px solid color-mix(in srgb, var(--shop-accent) 18%, rgba(84,67,45,.15)); border-radius: 18px; background: rgba(255,252,245,.74); box-shadow: 0 12px 28px rgba(50,38,25,.08); backdrop-filter: blur(14px); }
.filter-group { min-width: 0; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.filter-group span { color: var(--market-muted); font-size: 13px; font-weight: 800; margin-right: 2px; }
.filter-group button { height: 30px; padding: 0 12px; border-radius: 999px; border: 1px solid transparent; background: rgba(255,250,241,.86); color: var(--market-ink); font-size: 13px; font-weight: 800; transition: .16s ease; box-shadow: inset 0 0 0 1px rgba(84,67,45,.12); }
.filter-group button:hover { transform: translateY(-1px); border-color: color-mix(in srgb, var(--shop-accent) 34%, transparent); }
.filter-group button.active { background: var(--shop-accent); color: #fffaf1; box-shadow: 0 10px 20px color-mix(in srgb, var(--shop-accent) 22%, transparent); }
.rating-filter { min-height: 34px; display: inline-flex; align-items: center; gap: 10px; padding: 4px 10px; border-radius: 999px; background: rgba(255,250,241,.86); box-shadow: inset 0 0 0 1px rgba(84,67,45,.12); }
.rating-filter :deep(.el-rate) { height: 24px; display: inline-flex; align-items: center; }
.rating-filter :deep(.el-rate__item) { margin-right: 2px; }
.rating-filter b { min-width: 62px; color: var(--market-ink); font-size: 13px; font-weight: 850; white-space: nowrap; }
.products-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 16px; }
.product-card { min-height: 350px; display: grid; grid-template-rows: 188px 1fr; overflow: hidden; border: 1px solid var(--market-line); border-radius: 18px; background: rgba(255,250,241,.88); cursor: pointer; transition: .18s ease; box-shadow: 0 12px 28px rgba(50,38,25,.08); animation: rise .28s ease both; }
.product-card:hover { transform: translateY(-4px); border-color: var(--shop-accent); box-shadow: 0 18px 38px rgba(50,38,25,.14); }
.image-wrap { position: relative; height: 188px; background: #e5dac8; overflow: hidden; }
.image-wrap img, .image-empty { width: 100%; height: 100%; }
.image-wrap img { display: block; object-fit: cover; transition: transform .3s ease; }
.product-card:hover img { transform: scale(1.04); }
.image-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; color: #fff; background: linear-gradient(135deg,var(--shop-accent),#d5b47b); }
.image-empty .el-icon { font-size: 42px; }
.image-empty span { font-family: var(--market-display); font-size: 28px; font-weight: 900; }
.status-tag { position: absolute; top: 10px; left: 10px; border: 0; }
.card-body { min-height: 162px; display: grid; grid-template-rows: auto auto 1fr auto; padding: 15px; }
.title-row { display: grid; gap: 8px; }
.title-row h2 { min-height: 44px; margin: 0; color: var(--market-ink); font-size: 17px; line-height: 1.3; overflow: hidden; }
.rating-pill { width: max-content; max-width: 100%; display: inline-flex; align-items: center; gap: 4px; padding: 4px 9px; border-radius: 999px; background: rgba(194,122,44,.13); color: #8b5e2f; font-size: 12px; font-weight: 850; }
.rating-pill .el-icon { color: #c27a2c; }
.description { height: 42px; margin: 10px 0 12px; color: var(--market-muted); font-size: 14px; line-height: 1.5; overflow: hidden; }
.price-row, .seller-row, .price, .detail-link, .seller-row span { display: flex; align-items: center; }
.price-row { align-self: end; justify-content: space-between; gap: 10px; margin-bottom: 12px; }
.price { gap: 5px; color: var(--market-red); font-size: 20px; font-weight: 850; }
.detail-link { gap: 4px; color: var(--shop-accent); font-size: 13px; font-weight: 800; }
.seller-row { justify-content: space-between; gap: 8px; color: var(--market-muted); font-size: 13px; }
.seller-row span { min-width: 0; gap: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pagination-section { display: flex; justify-content: center; padding: 26px 0 0; }
@keyframes rise { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width:1080px) { .products-grid { grid-template-columns: repeat(2,minmax(0,1fr)); } }
@media (max-width:820px) { .shop-hero, .search-panel { grid-template-columns: 1fr; } .filter-strip { align-items: flex-start; flex-direction: column; } .products-grid { grid-template-columns: repeat(2,minmax(0,1fr)); } }
@media (max-width:560px) { .products-grid { grid-template-columns: 1fr; } }
</style>
