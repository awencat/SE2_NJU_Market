<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchPage } from '../api'

const router = useRouter()

const categories = [
  { title: '教材书刊', desc: '课程教材、考研资料、杂志读物', path: '/BookShop' },
  { title: '日用百货', desc: '宿舍好物、小家电、收纳用品', path: '/DailyShop' },
  { title: '游戏影音', desc: '主机、手柄、唱片、耳机设备', path: '/VideoShop' },
  { title: '萌宠用品', desc: '笼具、玩具、清洁和喂养用品', path: '/PetShop' },
]

const goods = ref([])
const loading = ref(false)

async function loadLatestGoods() {
  loading.value = true
  try {
    const result = await fetchPage({
      pageSize: 4,
      pageNum: 1,
      param: {},
    })
    const records = Array.isArray(result.data) ? result.data : (result.data?.records || [])

    goods.value = records.map(item => ({
      title: item.title || '未命名商品',
      price: `¥${item.price || 0}`,
      tag: Number(item.count || 0) > 0 ? `库存 ${item.count}` : '已售罄',
      condition: item.condition || '二手',
      goodId: item.goodId || item.id,
      imageUrl: getImageUrl(item),
    }))
  } catch (error) {
    console.error('加载商品失败:', error)
    goods.value = []
  } finally {
    loading.value = false
  }
}

function getImageUrl(item) {
  const image = item.image || item.imageUrl || item.coverUrl || item.images?.[0]?.imageUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8080/uploads/goods/${image}`
}

function openGoodDetail(item) {
  if (!item.goodId) return
  router.push(`/goods/${item.goodId}`)
}

onMounted(() => {
  loadLatestGoods()
})
</script>

<template>
  <div class="front-page">
    <section class="hero-section">
      <div class="hero-copy">
        <p class="eyebrow">NJU MARKET</p>
        <h1>南京大学校园二手交易平台</h1>
        <p>
          找教材、出闲置、约面交，把校园里的好物重新流动起来。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/DailyShop')">逛一逛</el-button>
          <el-button size="large" @click="$router.push('/GoodsManager')">发布闲置</el-button>
        </div>
      </div>
      <div class="hero-card">
        <span class="card-label">今日上新</span>
        <strong>{{ goods.length }}</strong>
        <p>件校园好物等待新主人</p>
      </div>
    </section>

    <section class="section-block">
      <div class="section-head">
        <h2>分类入口</h2>
        <p>先把导航跑通，后续每个分类页可以逐步替换成真实商品列表。</p>
      </div>
      <div class="category-grid">
        <button
            v-for="category in categories"
            :key="category.title"
            class="category-card"
            type="button"
            @click="$router.push(category.path)"
        >
          <strong>{{ category.title }}</strong>
          <span>{{ category.desc }}</span>
        </button>
      </div>
    </section>

    <section class="section-block">
      <div class="section-head">
        <h2>推荐闲置</h2>
        <p>这里展示最新上架的4件商品。</p>
      </div>
      <div class="goods-grid">
        <article v-if="loading" class="good-card loading-card">
          <p>加载中...</p>
        </article>
        <article v-else-if="goods.length === 0" class="good-card empty-card">
          <p>暂无商品</p>
        </article>
        <article
          v-else
          v-for="item in goods"
          :key="item.goodId || item.title"
          class="good-card"
          @click="openGoodDetail(item)"
        >
          <div class="good-image">
            <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" />
            <span v-else>{{ item.title.slice(0, 2) }}</span>
          </div>
          <div class="good-info">
            <h3>{{ item.title }}</h3>
            <p>{{ item.condition }}</p>
            <div>
              <strong>{{ item.price }}</strong>
              <span>{{ item.tag }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.front-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-section {
  min-height: 320px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  align-items: center;
  gap: 24px;
  padding: 38px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(232, 239, 248, 0.92));
  border: 1px solid rgba(32, 48, 64, 0.08);
  box-shadow: 0 18px 48px rgba(32, 48, 64, 0.12);
}

.eyebrow {
  margin: 0 0 10px;
  color: #b36b1f;
  font-weight: 800;
  letter-spacing: 0.16em;
  font-size: 13px;
}

.hero-copy h1 {
  margin: 0;
  color: #172636;
  font-size: 42px;
  line-height: 1.12;
  letter-spacing: 0;
}

.hero-copy p {
  max-width: 560px;
  margin: 16px 0 0;
  color: #526579;
  font-size: 17px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 26px;
}

.hero-card {
  min-height: 210px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 28px;
  border-radius: 8px;
  background: #203040;
  color: #fff;
}

.hero-card strong {
  margin: 10px 0;
  font-size: 56px;
  line-height: 1;
}

.hero-card p,
.card-label {
  margin: 0;
  color: rgba(255, 255, 255, 0.74);
}

.section-block {
  padding: 26px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(32, 48, 64, 0.08);
}

.section-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0;
  color: #172636;
  font-size: 22px;
}

.section-head p {
  margin: 0;
  color: #667789;
}

.category-grid,
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.category-card {
  min-height: 116px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  padding: 18px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #f8fbff;
  color: #172636;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.category-card:hover {
  transform: translateY(-2px);
  border-color: #e4a647;
  box-shadow: 0 12px 28px rgba(32, 48, 64, 0.12);
}

.category-card strong {
  font-size: 17px;
}

.category-card span {
  color: #607286;
  line-height: 1.5;
}

.good-card {
  overflow: hidden;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.good-card:hover {
  transform: translateY(-2px);
  border-color: #e4a647;
  box-shadow: 0 12px 28px rgba(32, 48, 64, 0.12);
}

.loading-card,
.empty-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #667789;
}

.good-image {
  height: 132px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #dce8f6, #f6e2bd);
  color: #203040;
  font-weight: 800;
  font-size: 24px;
}

.good-image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.good-info {
  padding: 15px;
}

.good-info h3,
.good-info p {
  margin: 0;
}

.good-info h3 {
  font-size: 16px;
  color: #172636;
}

.good-info p {
  margin-top: 6px;
  color: #667789;
}

.good-info div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 14px;
}

.good-info strong {
  color: #c06f1f;
  font-size: 20px;
}

.good-info span {
  color: #6c7c8e;
  font-size: 13px;
}

@media (max-width: 900px) {
  .hero-section,
  .category-grid,
  .goods-grid {
    grid-template-columns: 1fr;
  }

  .hero-section {
    padding: 24px;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .section-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

