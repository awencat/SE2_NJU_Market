<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Goods, Coin, Box, Phone } from '@element-plus/icons-vue'
import { fetchPage } from '../api'

const router = useRouter()

const tableData = ref([])
const pageSize = ref(8)
const pageNum = ref(1)
const total = ref(0)
const goodsname = ref('')

function fixurl(fileName) {
  return `http://localhost:8095/images/${fileName}`
}

function handleImageError(event) {
  event.target.style.display = 'none'
  const nextElement = event.target.nextElementSibling
  if (nextElement) {
    nextElement.style.display = 'flex'
  }
}

async function loadPost() {
  try {
    const res = await fetchPage({
      pageSize: pageSize.value,
      pageNum: pageNum.value,
      param: {
        goodsname: goodsname.value,
        tag: 1,
      },
    })

    console.log('完整响应:', res)
    console.log('res.data 类型:', typeof res.data)
    console.log('res.data 内容:', res.data)
    console.log('res.data 是否为数组:', Array.isArray(res.data))

    let dataList = []

    if (Array.isArray(res.data)) {
      dataList = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      dataList = res.data.records
    } else if (res.data && Array.isArray(res.data.list)) {
      dataList = res.data.list
    } else {
      console.warn('无法识别的数据格式，使用空数组')
      dataList = []
    }

    console.log('处理后的数据列表:', dataList)

    tableData.value = dataList.map((product) => ({
      ...product,
      rateAvg: product.rateAvg ? parseFloat(product.rateAvg) : 0,
    }))

    total.value = res.total || res.data?.total || 0

    console.log('最终表格数据:', tableData.value)
    console.log('总数:', total.value)
  } catch (error) {
    console.error('加载数据失败:', error)
    console.error('错误堆栈:', error.stack)
    tableData.value = []
    total.value = 0
  }
}

function resetParam() {
  goodsname.value = ''
  pageNum.value = 1
  loadPost()
}

function handleSizeChange(val) {
  pageNum.value = 1
  pageSize.value = val
  loadPost()
}

function handleCurrentChange(val) {
  pageNum.value = val
  loadPost()
}

function viewDetail(row) {
  router.push({
    name: 'GoodsDetail',
    query: { id: row.id },
  })
}

onMounted(() => {
  loadPost()
})
</script>

<template>
  <div class="dailyshop-container">
    <!-- 页面标题和搜索区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">🏠 日用商店</h1>
          <p class="page-subtitle">精选生活好物，提升生活品质</p>
        </div>

        <div class="search-section">
          <div class="search-box">
            <el-input
                v-model="goodsname"
                placeholder="搜索日用品名称..."
                suffix-icon="Search"
                class="search-input"
                @keyup.enter="loadPost"
            />
            <el-button type="primary" class="search-btn" @click="loadPost">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button type="default" class="reset-btn" @click="resetParam">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 商品卡片网格 -->
      <div class="products-grid" v-if="tableData.length > 0">
        <el-row :gutter="24">
          <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="product in tableData"
              :key="product.id"
          >
            <el-card class="product-card" shadow="hover" :body-style="{ padding: '0px' }">
              <!-- 商品图片 -->
              <div class="product-image">
                <img
                    v-if="product.image"
                    :src="fixurl(product.image)"
                    :alt="product.goodsname"
                    class="product-img"
                    @error="handleImageError"
                />
                <div class="no-image" v-else>
                  <el-icon :size="48"><Goods /></el-icon>
                  <span>暂无图片</span>
                </div>
                <div class="product-overlay">
                  <el-button type="primary" class="detail-btn" round @click="viewDetail(product)">
                    查看详情
                  </el-button>
                </div>
                <!-- 商品标签 -->
                <div class="product-badge">
                  <span class="badge daily">日用品</span>
                </div>
              </div>

              <!-- 商品信息 -->
              <div class="product-info">
                <h3 class="product-title">{{ product.goodsname }}</h3>

                <div class="product-meta">
                  <div class="meta-item price">
                    <el-icon><Coin /></el-icon>
                    <span class="price-text">¥{{ product.price }}</span>
                  </div>
                  <div class="meta-item stock">
                    <el-icon><Box /></el-icon>
                    <span>库存 {{ product.storage }}</span>
                  </div>
                </div>

                <div class="product-footer">
                  <div class="seller-info">
                    <el-avatar
                        :size="24"
                        :src="fixurl(product.authorAvatar)"
                        v-if="product.authorAvatar"
                    >
                      {{ product.authorName ? product.authorName.charAt(0) : 'U' }}
                    </el-avatar>
                    <el-avatar :size="24" v-else>
                      {{ product.authorName ? product.authorName.charAt(0) : 'U' }}
                    </el-avatar>
                    <span class="seller-name">{{ product.authorName || '未知卖家' }}</span>
                  </div>

                  <div class="rating" v-if="product.rateAvg">
                    <el-rate
                        v-model="product.rateAvg"
                        disabled
                        show-score
                        text-color="#ff9900"
                        score-template="{value}"
                    />
                  </div>
                </div>

                <div class="contact-info" v-if="product.authorNumber">
                  <el-icon><Phone /></el-icon>
                  <span>{{ product.authorNumber }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-else>
        <div class="empty-content">
          <el-icon :size="64" class="empty-icon"><Goods /></el-icon>
          <h3>暂无日用品数据</h3>
          <p>当前没有找到相关的日用品信息</p>
          <el-button type="primary" @click="resetParam">
            <el-icon><Refresh /></el-icon> 重新加载
          </el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-section" v-if="tableData.length > 0">
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
    </div>
  </div>
</template>

<style scoped>
.dailyshop-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 30px;
}

.title-section {
  flex: 1;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #5d6d7e;
  margin: 0;
}

.search-section {
  flex-shrink: 0;
}

.search-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  padding-left: 20px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 154, 158, 0.1) inset;
}

.search-btn,
.reset-btn {
  border-radius: 20px;
  padding: 10px 20px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  border: none;
}

/* 主要内容 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 商品网格 */
.products-grid {
  margin-bottom: 40px;
}

/* 商品卡片 */
.product-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* 商品图片 */
.product-image {
  position: relative;
  height: 240px;
  overflow: hidden;
  background: linear-gradient(135deg, #fad0c4 0%, #ffd1ff 100%);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ff9a9e;
}

.no-image span {
  font-size: 1rem;
  margin-top: 10px;
  color: #5d6d7e;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.detail-btn {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  border: none;
  padding: 10px 24px;
  font-weight: 500;
}

/* 商品标签 */
.product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
}

.badge.daily {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
}

/* 商品信息 */
.product-info {
  padding: 20px;
}

.product-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 15px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
}

.meta-item.price {
  color: #e74c3c;
}

.price-text {
  font-weight: 600;
  font-size: 1.2rem;
}

.meta-item.stock {
  color: #5d6d7e;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seller-name {
  font-size: 0.9rem;
  color: #5d6d7e;
}

.rating {
  flex-shrink: 0;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: #7f8c8d;
  padding-top: 12px;
  border-top: 1px solid #ecf0f1;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-content {
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  color: #ff9a9e;
  margin-bottom: 20px;
}

.empty-content h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
}

.empty-content p {
  color: #7f8c8d;
  margin-bottom: 30px;
}

/* 分页 */
.pagination-section {
  text-align: center;
  padding: 20px 0;
}

.pagination-section :deep(.el-pagination) {
  justify-content: center;
}

.pagination-section :deep(.el-pagination.is-background .btn-prev),
.pagination-section :deep(.el-pagination.is-background .btn-next),
.pagination-section :deep(.el-pagination.is-background .el-pager li) {
  background-color: rgba(255, 255, 255, 0.8);
}

.pagination-section :deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .page-title {
    font-size: 2rem;
  }

  .search-input {
    width: 100%;
  }

  .search-box {
    width: 100%;
    justify-content: center;
  }

  .products-grid .el-col {
    margin-bottom: 20px;
  }

  .product-image {
    height: 200px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 20px 10px;
  }

  .page-header {
    padding: 30px 0;
  }

  .product-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .product-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .product-title {
    font-size: 1rem;
  }
}
</style>
