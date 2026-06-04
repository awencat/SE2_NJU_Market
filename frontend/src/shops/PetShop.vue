<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Goods, Coin, User, Phone, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchPage } from '../api'

const router = useRouter()
const tableData = ref([])
const pageSize = ref(8)
const pageNum = ref(1)
const total = ref(0)
const title = ref('')
const loading = ref(false)

const hasProducts = computed(() => tableData.value.length > 0)

function getGoodId(product) {
  return product.goodId || product.id
}

function getGoodTitle(product) {
  return product.title || product.goodsname || '未命名商品'
}

function getSellerName(product) {
  return product.sellerName || product.authorName || '未知卖家'
}

function getSellerPhone(product) {
  return product.sellerPhone || product.authorNumber || ''
}

function getImageUrl(product) {
  const image = product.image || product.imageUrl || product.coverUrl
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8095/images/${image}`
}

function isPurchasable(product) {
  return Number(product?.count || 0) > 0
}

function statusText(product) {
  const stock = Number(product?.count || 0)
  return stock > 0 ? `库存 ${stock}` : '已售罄'
}

function handleImageError(event) {
  event.target.style.display = 'none'
  const nextElement = event.target.nextElementSibling
  if (nextElement) nextElement.style.display = 'flex'
}

async function loadPost() {
  loading.value = true
  try {
    const res = await fetchPage({
      pageSize: pageSize.value,
      pageNum: pageNum.value,
      param: {
        title: title.value,
        category: 'pet',
        tag: 5,
      },
    })
    const data = Array.isArray(res.data) ? res.data : (res.data?.records || res.data?.list || [])
    tableData.value = data.map((product) => ({
      ...product,
      rateAvg: product.rateAvg ? parseFloat(product.rateAvg) : 0,
    }))
    total.value = res.data?.total || res.total || data.length
  } catch (error) {
    ElMessage.error(error.message || '加载商品失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function resetParam() {
  title.value = ''
  pageNum.value = 1
  loadPost()
}

function handleSizeChange(val) {
  pageSize.value = val
  pageNum.value = 1
  loadPost()
}

function handleCurrentChange(val) {
  pageNum.value = val
  loadPost()
}

function openDetail(product) {
  const goodId = getGoodId(product)
  if (!goodId) {
    ElMessage.warning('商品编号不存在')
    return
  }
  router.push(`/goods/${goodId}`)
}

onMounted(loadPost)
</script>

<template>
  <div class="petshop-page">
    <section class="shop-toolbar">
      <div>
        <p class="eyebrow">Pets & Supplies</p>
        <h1>宠物商城</h1>
        <p class="subtitle">点击商品进入详情页，购买、评分、评论和举报都在那里完成。</p>
      </div>
      <div class="search-panel">
        <el-input
            v-model="title"
            clearable
            placeholder="搜索宠物用品"
            class="search-input"
            @keyup.enter="loadPost"
        />
        <el-button type="primary" @click="loadPost">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetParam">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </section>

    <section v-loading="loading" class="product-area">
      <div v-if="hasProducts" class="products-grid">
        <article
            v-for="product in tableData"
            :key="getGoodId(product)"
            class="product-card"
            @click="openDetail(product)"
        >
          <div class="image-wrap">
            <img
                v-if="getImageUrl(product)"
                :src="getImageUrl(product)"
                :alt="getGoodTitle(product)"
                @error="handleImageError"
            />
            <div class="image-empty">
              <el-icon><Goods /></el-icon>
              <span>暂无图片</span>
            </div>
            <el-tag class="status-tag" :type="isPurchasable(product) ? 'success' : 'info'" effect="dark">
              {{ statusText(product) }}
            </el-tag>
          </div>

          <div class="card-body">
            <h2>{{ getGoodTitle(product) }}</h2>
            <p class="description">{{ product.description || '卖家暂时没有填写商品描述。' }}</p>

            <div class="price-row">
              <span class="price"><el-icon><Coin /></el-icon>￥{{ product.price || 0 }}</span>
              <span class="detail-link"><el-icon><View /></el-icon>查看详情</span>
            </div>

            <div class="seller-row">
              <span><el-icon><User /></el-icon>{{ getSellerName(product) }}</span>
              <span v-if="getSellerPhone(product)"><el-icon><Phone /></el-icon>{{ getSellerPhone(product) }}</span>
            </div>
          </div>
        </article>
      </div>

      <el-empty v-else description="暂时没有找到宠物用品">
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
.petshop-page {
  min-height: 100vh;
  background: #f6f3ee;
  color: #26312f;
}

.shop-toolbar {
  max-width: 1180px;
  margin: 0 auto;
  padding: 34px 20px 22px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #8c5b3f;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
}

.subtitle {
  margin: 8px 0 0;
  color: #65716d;
  font-size: 15px;
}

.search-panel {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px;
  background: #fffaf2;
  border: 1px solid #e4d7c5;
  border-radius: 8px;
}

.search-input {
  width: 260px;
}

.product-area {
  max-width: 1180px;
  margin: 0 auto;
  padding: 8px 20px 42px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.product-card {
  min-width: 0;
  overflow: hidden;
  background: #fffdf8;
  border: 1px solid #e3d8c9;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  border-color: #b78354;
  box-shadow: 0 16px 34px rgba(71, 52, 34, 0.13);
}

.image-wrap {
  position: relative;
  height: 184px;
  background: #e9e0d4;
  overflow: hidden;
}

.image-wrap img,
.image-empty {
  width: 100%;
  height: 100%;
}

.image-wrap img {
  display: block;
  object-fit: cover;
}

.image-empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: #8d978f;
}

.image-empty .el-icon {
  font-size: 40px;
}

.status-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  border: 0;
}

.card-body {
  padding: 15px;
}

.card-body h2 {
  height: 44px;
  margin: 0 0 8px;
  color: #26312f;
  font-size: 17px;
  line-height: 1.3;
  overflow: hidden;
}

.description {
  height: 42px;
  margin: 0 0 14px;
  color: #68736f;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
}

.price-row,
.seller-row,
.price,
.detail-link,
.seller-row span {
  display: flex;
  align-items: center;
}

.price-row {
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.price {
  gap: 5px;
  color: #b54716;
  font-size: 20px;
  font-weight: 800;
}

.detail-link {
  gap: 4px;
  color: #3f6f67;
  font-size: 13px;
  font-weight: 700;
}

.seller-row {
  justify-content: space-between;
  gap: 8px;
  color: #66706c;
  font-size: 13px;
}

.seller-row span {
  min-width: 0;
  gap: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 28px 0 0;
}

@media (max-width: 1080px) {
  .products-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .shop-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .search-panel {
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }

  .products-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>

