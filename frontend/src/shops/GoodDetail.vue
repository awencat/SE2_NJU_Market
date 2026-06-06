<script setup>
import { computed,nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  ArrowRight,
  ChatLineRound,
  Coin,
  Goods,
  Phone,
  Message,
  ShoppingCart,
  Star,
  User,
  Warning,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  createRecord,
  fetchGoodComments,
  fetchGoodRatingSummary,
  fetchGoodRatings,
  fetchPage,
  purchaseGood,
} from '../api'

const route = useRoute()
const router = useRouter()
const goodId = computed(() => Number(route.params.id))

const loading = ref(false)
const submitting = ref(false)
const buying = ref(false)
const good = ref(null)
const comments = ref([])
const ratings = ref([])
const ratingSummary = ref({ averageScore: 0, ratingCount: 0 })
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref(null)
const ratingScore = ref(5)
const reportReason = ref('')
const activeTab = ref('comments')
const purchaseDialogVisible = ref(false)
const purchaseResult = ref(null)
const currentImageIndex = ref(0)
const purchaseCount = ref(1)

const maxPurchaseCount = computed(() => {
  const stock = getStockCount()
  return stock > 0 ? stock : 1
})

const imageUrls = computed(() => {
  const images = good.value?.images || []
  const urls = images
    .map((item) => normalizeImageUrl(item.imageUrl || item.url || item))
    .filter(Boolean)
  const cover = normalizeImageUrl(good.value?.image || good.value?.imageUrl || good.value?.coverUrl)
  if (cover && !urls.includes(cover)) {
    urls.unshift(cover)
  }
  return urls
})

const currentImageUrl = computed(() => imageUrls.value[currentImageIndex.value] || '')
const topLevelComments = computed(() => comments.value.filter((comment) => !comment.parentCommentId))

function readCurrentUser() {
  const raw = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

function getCurrentUserId() {
  const user = readCurrentUser()
  return user?.userId || user?.id
}

function requireLogin() {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    router.push('/')
    return null
  }
  return userId
}

function getGoodTitle() {
  return good.value?.title || good.value?.goodsname || '未命名商品'
}

function getSellerName() {
  return good.value?.sellerName || good.value?.authorName || '未知卖家'
}

function getSellerPhone() {
  return good.value?.sellerPhone || good.value?.authorNumber || ''
}

function getSellerEmail() {
  return good.value?.sellerEmail || good.value?.authorEmail || ''
}

function getSellerId() {
  return good.value?.sellerId || good.value?.authorId || good.value?.userId
}

function normalizeImageUrl(image) {
  if (!image) return ''
  if (/^https?:\/\//.test(image)) return image
  if (image.startsWith('/uploads/')) return `http://localhost:8080${image}`
  if (image.startsWith('/')) return image
  return `http://localhost:8095/images/${image}`
}

function switchImage(offset) {
  if (imageUrls.value.length <= 1) return
  currentImageIndex.value = (currentImageIndex.value + offset + imageUrls.value.length) % imageUrls.value.length
}

function selectImage(index) {
  currentImageIndex.value = index
}

function getStockCount() {
  return Number(good.value?.count || 0)
}

function isPurchasable() {
  return getStockCount() > 0
}

function stockText() {
  const stock = getStockCount()
  return stock > 0 ? `库存 ${stock}` : '已售罄'
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

function normalizeList(data) {
  return Array.isArray(data) ? data : (data?.records || data?.list || [])
}

async function loadGood() {
  const res = await fetchPage({
    pageSize: 1,
    pageNum: 1,
    param: { goodId: goodId.value },
  })
  const records = normalizeList(res.data)
  good.value = records[0] || null
  if (!good.value) {
    throw new Error('商品不存在')
  }
  currentImageIndex.value = 0
  await nextTick()
  const stock = getStockCount()
  if (stock > 0) {
    purchaseCount.value = Math.min(Math.max(Number(purchaseCount.value || 1), 1), stock)
  } else {
    purchaseCount.value = 1
  }
}

async function loadFeedback() {
  const [commentsRes, ratingsRes, summaryRes] = await Promise.all([
    fetchGoodComments(goodId.value),
    fetchGoodRatings(goodId.value),
    fetchGoodRatingSummary(goodId.value),
  ])
  comments.value = Array.isArray(commentsRes.data) ? commentsRes.data : []
  ratings.value = Array.isArray(ratingsRes.data) ? ratingsRes.data : []
  ratingSummary.value = summaryRes.data || { averageScore: 0, ratingCount: 0 }
}

async function loadPage() {
  loading.value = true
  try {
    await Promise.all([loadGood(), loadFeedback()])
  } catch (error) {
    ElMessage.error(error.message || '加载商品详情失败')
  } finally {
    loading.value = false
  }
}

async function buyProduct() {
  const buyerId = requireLogin()
  if (!buyerId || !good.value) return
  const count = Number(purchaseCount.value || 1)
  if (count < 1 || count > getStockCount()) {
    ElMessage.warning('请选择有效的购买数量')
    return
  }

  buying.value = true
  try {
    const res = await purchaseGood({ buyerId, goodId: goodId.value, count })
    purchaseResult.value = res.data
    purchaseDialogVisible.value = true
    ElMessage.success('订购成功')
    await loadGood()
  } catch (error) {
    ElMessage.error(error.message || '订购失败')
  } finally {
    buying.value = false
  }
}

async function submitComment() {
  const userId = requireLogin()
  if (!userId) return
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createRecord('comments', {
      userId,
      goodId: goodId.value,
      content: commentContent.value.trim(),
    })
    commentContent.value = ''
    ElMessage.success('评论已发布')
    await loadFeedback()
  } catch (error) {
    ElMessage.error(error.message || '发布失败，只有购买过该商品的用户可以评论')
  } finally {
    submitting.value = false
  }
}

function getReplies(commentId) {
  return comments.value.filter((comment) => comment.parentCommentId === commentId)
}

function startReply(comment) {
  replyingTo.value = comment.commentId
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

async function submitReply(comment) {
  const userId = requireLogin()
  if (!userId) return
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    await createRecord('comments', {
      userId,
      goodId: goodId.value,
      parentCommentId: comment.commentId,
      content: replyContent.value.trim(),
    })
    ElMessage.success('回复已发布')
    cancelReply()
    await loadFeedback()
  } catch (error) {
    ElMessage.error(error.message || '回复失败，只有购买过该商品的用户可以回复')
  } finally {
    submitting.value = false
  }
}

async function submitRating() {
  const userId = requireLogin()
  if (!userId) return

  submitting.value = true
  try {
    await createRecord('ratings', {
      userId,
      goodId: goodId.value,
      score: ratingScore.value,
    })
    ElMessage.success('评分已提交')
    await loadFeedback()
  } catch (error) {
    ElMessage.error(error.message || '评分失败，只有购买过该商品的用户可以评分')
  } finally {
    submitting.value = false
  }
}

async function submitReport() {
  const userId = requireLogin()
  if (!userId) return
  if (!reportReason.value.trim()) {
    ElMessage.warning('请输入举报原因')
    return
  }

  submitting.value = true
  try {
    await createRecord('reports', {
      reporterId: userId,
      targetUserId: getSellerId(),
      targetGoodId: goodId.value,
      reportType: 'GOOD',
      reason: reportReason.value.trim(),
    })
    reportReason.value = ''
    ElMessage.success('举报已提交，等待管理员处理')
  } catch (error) {
    ElMessage.error(error.message || '举报提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="detail-page">
    <button class="back-button" type="button" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon>
      返回
    </button>

    <main v-loading="loading" class="detail-shell">
      <section v-if="good" class="hero-section">
        <div class="image-panel">
          <div class="main-image-wrap">
            <img v-if="currentImageUrl" :src="currentImageUrl" :alt="getGoodTitle()" />
            <div v-else class="image-empty">
              <el-icon><Goods /></el-icon>
              <span>暂无图片</span>
            </div>

            <button
              v-if="imageUrls.length > 1"
              class="gallery-button gallery-button--prev"
              type="button"
              aria-label="上一张图片"
              @click="switchImage(-1)"
            >
              <el-icon><ArrowLeft /></el-icon>
            </button>
            <button
              v-if="imageUrls.length > 1"
              class="gallery-button gallery-button--next"
              type="button"
              aria-label="下一张图片"
              @click="switchImage(1)"
            >
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>

          <div v-if="imageUrls.length > 1" class="thumbnail-row">
            <button
              v-for="(image, index) in imageUrls"
              :key="image"
              class="thumbnail-button"
              :class="{ active: index === currentImageIndex }"
              type="button"
              @click="selectImage(index)"
            >
              <img :src="image" :alt="`${getGoodTitle()} 图片 ${index + 1}`" />
            </button>
          </div>
        </div>

        <div class="info-panel">
          <div class="title-row">
            <el-tag :type="isPurchasable() ? 'success' : 'info'" effect="dark">
              {{ stockText() }}
            </el-tag>
            <span class="category">{{ good.category || '日用' }}</span>
          </div>

          <h1>{{ getGoodTitle() }}</h1>
          <p class="description">{{ good.description || '卖家暂时没有填写商品描述。' }}</p>

          <div class="price-line">
            <el-icon><Coin /></el-icon>
            ￥{{ good.price || 0 }}
          </div>

          <div class="purchase-count-box">
            <span>购买数量</span>
            <el-input-number
                v-model="purchaseCount"
                :min="1"
                :max="maxPurchaseCount"
                :disabled="!isPurchasable()"
                step-strictly
                controls-position="right"
            />
          </div>
          <div class="seller-box">
            <div class="seller-title">
              <el-icon><User /></el-icon>
              卖家信息
            </div>
            <p>{{ getSellerName() }}</p>
            <p v-if="getSellerPhone()"><el-icon><Phone /></el-icon>{{ getSellerPhone() }}</p>
            <p v-if="getSellerEmail()"><el-icon><Message /></el-icon>{{ getSellerEmail() }}</p>
            <p v-if="!getSellerPhone() && !getSellerEmail()" class="muted">卖家暂未填写联系方式。</p>
          </div>

          <el-button
            type="primary"
            size="large"
            class="buy-button"
            :disabled="!isPurchasable()"
            :loading="buying"
            @click="buyProduct"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ isPurchasable() ? '订购商品' : '暂不可订购' }}
          </el-button>
        </div>
      </section>

      <section v-if="good" class="feedback-section">
        <aside class="score-card">
          <p>综合评分</p>
          <strong>{{ ratingSummary?.averageScore || 0 }}</strong>
          <el-rate :model-value="Number(ratingSummary?.averageScore || 0)" disabled allow-half />
          <span>{{ ratingSummary?.ratingCount || 0 }} 人评分</span>
          <div class="hint">购买过该商品后，才可以评论和评分。</div>
        </aside>

        <div class="feedback-panel">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="评论" name="comments">
              <div class="submit-box">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="4"
                  maxlength="500"
                  show-word-limit
                  placeholder="写下你的使用感受"
                />
                <el-button type="primary" :loading="submitting" @click="submitComment">
                  <el-icon><ChatLineRound /></el-icon>
                  发布评论
                </el-button>
              </div>

              <div class="comment-list">
                <el-empty v-if="topLevelComments.length === 0" description="暂无评论" :image-size="80" />
                <article v-for="comment in topLevelComments" :key="comment.commentId" class="comment-item">
                  <div>
                    <strong>用户 {{ comment.userId }}</strong>
                    <time>{{ formatTime(comment.createdAt) }}</time>
                  </div>
                  <p>{{ comment.content }}</p>
                  <button class="reply-link" type="button" @click="startReply(comment)">回复</button>
                  <div v-if="getReplies(comment.commentId).length > 0" class="reply-list">
                    <div v-for="reply in getReplies(comment.commentId)" :key="reply.commentId" class="reply-item">
                      <div>
                        <strong>用户 {{ reply.userId }}</strong>
                        <time>{{ formatTime(reply.createdAt) }}</time>
                      </div>
                      <p>{{ reply.content }}</p>
                    </div>
                  </div>
                  <div v-if="replyingTo === comment.commentId" class="reply-box">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="3"
                      maxlength="300"
                      show-word-limit
                      placeholder="回复这条评论"
                    />
                    <div>
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button size="small" type="primary" :loading="submitting" @click="submitReply(comment)">
                        发布回复
                      </el-button>
                    </div>
                  </div>
                </article>
              </div>
            </el-tab-pane>

            <el-tab-pane label="评分" name="ratings">
              <div class="rating-submit">
                <span>我的评分</span>
                <el-rate v-model="ratingScore" :max="5" />
                <el-button type="primary" :loading="submitting" @click="submitRating">
                  <el-icon><Star /></el-icon>
                  提交评分
                </el-button>
              </div>

              <div class="rating-list">
                <el-empty v-if="ratings.length === 0" description="暂无评分" :image-size="80" />
                <div v-for="rating in ratings" :key="rating.ratingId" class="rating-item">
                  <span>用户 {{ rating.userId }}</span>
                  <el-rate :model-value="rating.score" disabled />
                  <time>{{ formatTime(rating.createdAt) }}</time>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="举报" name="report">
              <div class="report-box">
                <div class="report-title">
                  <el-icon><Warning /></el-icon>
                  如果商品信息不真实、违规或存在交易风险，可以提交给管理员处理。
                </div>
                <el-input
                  v-model="reportReason"
                  type="textarea"
                  :rows="5"
                  maxlength="255"
                  show-word-limit
                  placeholder="请说明举报原因"
                />
                <el-button type="warning" :loading="submitting" @click="submitReport">提交举报</el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </section>
    </main>

    <el-dialog v-model="purchaseDialogVisible" title="订购成功" width="430px">
      <div v-if="purchaseResult" class="purchase-dialog">
        <strong>{{ purchaseResult.goodTitle }}</strong>
        <p>订单号：{{ purchaseResult.orderNumber }}</p>
        <p>数量：{{ purchaseResult.count }}</p>
        <p>金额：￥{{ purchaseResult.totalAmount }}</p>
        <p>卖家：{{ purchaseResult.sellerName }}</p>
        <p v-if="purchaseResult.sellerPhone">电话：{{ purchaseResult.sellerPhone }}</p>
        <p v-if="purchaseResult.sellerEmail">邮箱：{{ purchaseResult.sellerEmail }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="purchaseDialogVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 26px 20px 54px;
  background:
    linear-gradient(115deg, rgba(255,249,236,.86), rgba(242,234,223,.76)),
    radial-gradient(circle at 8% 14%, rgba(194,122,44,.18), transparent 25%),
    radial-gradient(circle at 88% 10%, rgba(65,106,143,.14), transparent 26%),
    repeating-linear-gradient(135deg, rgba(33,44,41,.026) 0, rgba(33,44,41,.026) 1px, transparent 1px, transparent 20px);
  color: var(--market-ink);
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 auto 16px;
  padding: 9px 13px;
  border: 1px solid var(--market-line);
  border-radius: 999px;
  background: rgba(255,252,245,.82);
  color: var(--market-ink);
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(50,38,25,.08);
  backdrop-filter: blur(14px);
}

.detail-shell {
  max-width: 1120px;
  margin: 0 auto;
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  gap: 24px;
  align-items: stretch;
  animation: detail-rise .32s ease both;
}

.image-panel,
.info-panel,
.score-card,
.feedback-panel {
  border: 1px solid #e0d3c3;
  border-radius: 24px;
  background: rgba(255,252,245,.86);
  box-shadow: var(--market-shadow);
  backdrop-filter: blur(14px);
}

.image-panel {
  min-height: 440px;
  overflow: hidden;
}

.main-image-wrap {
  position: relative;
  height: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, rgba(47,98,88,.12), rgba(194,122,44,.13)),
    #f1eadf;
}

.main-image-wrap img,
.image-empty {
  width: 100%;
}

.main-image-wrap img {
  max-width: 100%;
  max-height: 100%;
  display: block;
  object-fit: contain;
}

.image-empty {
  min-height: 440px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
  color: #8d978f;
}

.image-empty .el-icon {
  font-size: 56px;
}

.gallery-button {
  position: absolute;
  top: 50%;
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 50%;
  background: rgba(31, 39, 37, 0.66);
  color: #fff;
  cursor: pointer;
  transform: translateY(-50%);
  transition: background 0.18s ease, transform 0.18s ease;
}

.gallery-button:hover {
  background: rgba(31, 39, 37, 0.86);
  transform: translateY(-50%) scale(1.05);
}

.gallery-button--prev {
  left: 14px;
}

.gallery-button--next {
  right: 14px;
}

.thumbnail-row {
  display: flex;
  gap: 10px;
  padding: 14px;
  overflow-x: auto;
  border-top: 1px solid var(--market-line);
  background: rgba(255,250,242,.82);
}

.thumbnail-button {
  flex: 0 0 74px;
  height: 58px;
  padding: 0;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 12px;
  background: #eadfce;
  cursor: pointer;
}

.thumbnail-button.active {
  border-color: var(--market-green);
}

.thumbnail-button img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  background: #f1eadf;
}

.info-panel {
  position: relative;
  overflow: hidden;
  padding: 30px;
}

.info-panel::after {
  content: "";
  position: absolute;
  right: -72px;
  bottom: -82px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(47,98,88,.09);
  pointer-events: none;
}

.title-row {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.category {
  color: #7c6d60;
  font-size: 14px;
}

.info-panel h1 {
  position: relative;
  z-index: 1;
  margin: 0 0 12px;
  font-family: var(--market-display);
  font-size: clamp(36px, 5vw, 58px);
  line-height: 1.05;
}

.description {
  position: relative;
  z-index: 1;
  margin: 0 0 20px;
  color: var(--market-muted);
  line-height: 1.7;
}

.price-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 22px;
  color: var(--market-red);
  font-size: 34px;
  font-weight: 900;
}

.seller-box {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 8px;
  margin-bottom: 22px;
  padding: 16px;
  border: 1px solid var(--market-line);
  background: rgba(255,250,242,.78);
  border-radius: 16px;
}

.purchase-count-box {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid var(--market-line);
  border-radius: 16px;
  background: rgba(255,250,242,.78);
}

.purchase-count-box span {
  color: #3f6f67;
  font-weight: 800;
}

.seller-title,
.seller-box p {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
}

.seller-title {
  color: #3f6f67;
  font-weight: 800;
}

.muted {
  color: #7d8783;
}

.buy-button {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 48px;
  background: var(--market-green);
  border-color: var(--market-green);
  font-weight: 900;
}

.feedback-section {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 22px;
  margin-top: 24px;
  animation: detail-rise .38s ease both;
}

.score-card {
  height: fit-content;
  padding: 22px;
  text-align: center;
  position: sticky;
  top: 92px;
}

.score-card p {
  margin: 0 0 8px;
  color: #68736f;
}

.score-card strong {
  display: block;
  margin-bottom: 8px;
  color: var(--market-red);
  font-family: var(--market-display);
  font-size: 50px;
}

.score-card span {
  display: block;
  margin-top: 8px;
  color: #68736f;
}

.hint {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--market-line);
  color: #7a695b;
  font-size: 13px;
  line-height: 1.6;
  text-align: left;
}

.feedback-panel {
  padding: 10px 22px 22px;
}

.submit-box,
.report-box {
  display: grid;
  gap: 12px;
}

.submit-box .el-button,
.report-box .el-button {
  justify-self: end;
}

.comment-list,
.rating-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.comment-item,
.rating-item {
  padding: 14px;
  border: 1px solid var(--market-line);
  border-radius: 16px;
  background: rgba(255,250,242,.84);
}

.comment-item div,
.rating-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.comment-item strong,
.rating-item span {
  color: #2f5f58;
}

.comment-item time,
.rating-item time {
  color: #8b928f;
  font-size: 13px;
}

.comment-item p {
  margin: 9px 0 0;
  color: #4f5a56;
  line-height: 1.65;
}

.reply-link {
  width: max-content;
  margin-top: 10px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(47,98,88,.1);
  color: var(--market-green);
  font-size: 13px;
  font-weight: 850;
}

.reply-list {
  display: grid;
  gap: 8px;
  margin-top: 12px;
  padding-left: 14px;
  border-left: 3px solid rgba(47,98,88,.18);
}

.reply-item {
  padding: 12px;
  border-radius: 14px;
  background: rgba(255,252,245,.78);
  box-shadow: inset 0 0 0 1px rgba(84,67,45,.12);
}

.reply-item div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.reply-item p {
  margin: 8px 0 0;
  color: #4f5a56;
  line-height: 1.6;
}

.reply-box {
  display: grid;
  gap: 10px;
  margin-top: 12px;
  padding: 12px;
  border-radius: 14px;
  background: rgba(255,252,245,.86);
  box-shadow: inset 0 0 0 1px rgba(84,67,45,.12);
}

.reply-box div {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.rating-submit {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 14px;
  border: 1px solid #eadfce;
  border-radius: 8px;
  background: #fffaf2;
}

@keyframes detail-rise {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.report-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8a4a1d;
  font-weight: 700;
}

.purchase-dialog {
  display: grid;
  gap: 8px;
}

.purchase-dialog p {
  margin: 0;
}

@media (max-width: 900px) {
  .hero-section,
  .feedback-section {
    grid-template-columns: 1fr;
  }

  .image-panel,
  .main-image-wrap,
  .image-empty {
    min-height: 320px;
    height: 320px;
  }
}

@media (max-width: 560px) {
  .info-panel h1 {
    font-size: 26px;
  }

  .comment-item div,
  .rating-item {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

