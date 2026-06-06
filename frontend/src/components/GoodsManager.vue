<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import { deleteGoodImage, uploadGoodImages } from '../api'
import marketStudyImage from '../assets/market/market-study.jpg'
import defaultBookImage from '../assets/market/default-book.jpg'
import defaultDigitalImage from '../assets/market/default-digital.jpg'
import defaultSportsImage from '../assets/market/default-sports.jpg'

const router = useRouter()

const user = computed(() => {
  const raw = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
})

const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const searchTitle = ref('')
const searchCategory = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const fileList = ref([])

const categoryOptions = [
  { value: 'daily', label: '日用百货' },
  { value: 'digital', label: '数码设备' },
  { value: 'book', label: '教材书刊' },
  { value: 'sports', label: '体育用品' },
  { value: 'pet', label: '宠物用品' },
]

const defaultImages = {
  daily: marketStudyImage,
  digital: defaultDigitalImage,
  book: defaultBookImage,
  sports: defaultSportsImage,
  pet: marketStudyImage,
}

const form = ref({
  goodId: null,
  sellerId: 0,
  title: '',
  description: '',
  price: 0,
  category: '',
  count: 1,
  condition: '',
  viewCount: 0,
  image: '',
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度为 2 到 50 个字符', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于 0', trigger: 'blur' },
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' },
  ],
  count: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于 0', trigger: 'blur' },
  ],
}

function getCategoryLabel(category) {
  return categoryOptions.find((item) => item.value === category)?.label || category || '未分类'
}

function getCategoryTagType(category) {
  const types = {
    daily: 'primary',
    digital: 'success',
    book: 'warning',
    sports: 'danger',
    pet: 'info',
  }
  return types[category] || 'info'
}

function getStockTagType(count) {
  return Number(count || 0) > 0 ? 'success' : 'info'
}

function getStockName(count) {
  const stock = Number(count || 0)
  return stock > 0 ? `库存 ${stock}` : '已售罄'
}

function fixurl(fileName) {
  if (!fileName) return ''
  if (fileName.startsWith('http')) return fileName
  if (fileName.startsWith('/uploads/')) return `http://localhost:8080${fileName}`
  return `http://localhost:8080/uploads/goods/${fileName}`
}

function getCover(item) {
  return fixurl(item.image || item.imageUrl || item.coverUrl || item.images?.[0]?.imageUrl || '')
}

function getDefaultCover(item) {
  return defaultImages[item?.category] || marketStudyImage
}

function handleImageError(event) {
  event.target.style.display = 'none'
}

async function loadPost() {
  try {
    const response = await fetch('/api/goods/listPage', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        pageSize: pageSize.value,
        pageNum: pageNum.value,
        param: {
          title: searchTitle.value,
          category: searchCategory.value,
          sellerId: user.value?.roleid === 1 ? null : user.value?.userId,
        },
      }),
    })
    const res = await response.json()
    if (res.success && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载商品失败')
    }
  } catch (error) {
    console.error('加载商品失败', error)
    ElMessage.error('加载商品失败')
  }
}

function resetParam() {
  searchTitle.value = ''
  searchCategory.value = ''
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

function resetForm() {
  form.value = {
    goodId: null,
    sellerId: user.value?.userId || 0,
    title: '',
    description: '',
    price: 0,
    category: '',
    count: 1,
    condition: '',
    viewCount: 0,
    image: '',
  }
  fileList.value = []
  formRef.value?.clearValidate()
}

function add() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function mod(row) {
  isEdit.value = true
  form.value = {
    goodId: row.goodId,
    sellerId: row.sellerId,
    title: row.title,
    description: row.description || '',
    price: Number(row.price || 0),
    category: row.category,
    count: Number(row.count || 0),
    condition: row.condition || '',
    viewCount: row.viewCount || 0,
    image: row.image || row.imageUrl || row.coverUrl || '',
  }

  if (Array.isArray(row.images) && row.images.length > 0) {
    fileList.value = row.images.map((image) => ({
      name: image.imageUrl,
      url: fixurl(image.imageUrl),
      imageId: image.imageId,
      status: 'success',
    }))
  } else if (form.value.image) {
    fileList.value = [{
      name: form.value.image,
      url: fixurl(form.value.image),
      status: 'success',
    }]
  } else {
    fileList.value = []
  }
  dialogVisible.value = true
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确定删除这个商品吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await fetch(`/api/goods/${id}`, { method: 'DELETE' })
    const res = await response.json()
    if (res.success) {
      ElMessage.success('删除成功')
      loadPost()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败', error)
      ElMessage.error('删除失败')
    }
  }
}

function beforeUpload(file) {
  const isAllowedType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isAllowedType) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('单张图片不能超过 2MB')
    return false
  }
  return true
}

function handleImageChange(uploadFile, uploadFiles) {
  fileList.value = uploadFiles
  if (uploadFile.raw && !beforeUpload(uploadFile.raw)) {
    fileList.value = uploadFiles.filter((item) => item.uid !== uploadFile.uid)
  }
}

async function handleImageRemove(uploadFile, uploadFiles) {
  fileList.value = uploadFiles
  if (!uploadFile.imageId) return
  try {
    await deleteGoodImage(uploadFile.imageId)
    ElMessage.success('图片已删除')
  } catch (error) {
    ElMessage.error(error.message || '图片删除失败')
  }
}

async function uploadSelectedImages(goodId) {
  const files = fileList.value.map((item) => item.raw).filter(Boolean)
  if (files.length === 0) return []

  const result = await uploadGoodImages(goodId, files)
  const images = result.data || []
  if (images.length > 0) {
    form.value.image = images[0].imageUrl
  }
  return images
}

async function doSave() {
  try {
    const submitData = {
      ...form.value,
      sellerId: user.value?.userId || 0,
    }
    const response = await fetch('/api/goods', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(submitData),
    })
    const res = await response.json()
    if (res.success) {
      const goodId = res.data?.goodId
      if (goodId) await uploadSelectedImages(goodId)
      ElMessage.success('发布成功')
      dialogVisible.value = false
      resetForm()
      loadPost()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布商品失败', error)
    ElMessage.error('发布失败')
  }
}

async function doMod() {
  try {
    const response = await fetch(`/api/goods/${form.value.goodId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value),
    })
    const res = await response.json()
    if (res.success) {
      await uploadSelectedImages(form.value.goodId)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      resetForm()
      loadPost()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存商品失败', error)
    ElMessage.error('保存失败')
  }
}

function save() {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请检查表单内容')
      return
    }
    if (isEdit.value) {
      await doMod()
    } else {
      await doSave()
    }
  })
}

onMounted(() => {
  if (!user.value) {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }
  loadPost()
})
</script>

<template>
  <div class="goods-manager-container market-page">
    <section class="manager-hero market-card">
      <div>
        <p class="market-eyebrow">Goods</p>
        <h1 class="market-title">商品管理</h1>
        <p class="market-subtitle">发布、编辑和下架自己的闲置商品。</p>
      </div>
      <el-button type="primary" size="large" @click="add">
        <el-icon><Plus /></el-icon>新增商品
      </el-button>
    </section>

    <section class="search-section">
      <div class="search-box">
        <el-input v-model="searchTitle" placeholder="搜索商品标题" clearable class="search-input" @keyup.enter="loadPost">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="searchCategory" placeholder="选择分类" clearable class="search-select">
          <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="loadPost">
          <el-icon><Search /></el-icon>查询
        </el-button>
        <el-button @click="resetParam">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
      </div>
    </section>

    <section class="goods-card-board">
      <article v-for="item in tableData" :key="item.goodId" class="manager-good-card">
        <div class="manager-good-cover">
          <img :src="getCover(item) || getDefaultCover(item)" :alt="item.title || '商品图片'" @error="handleImageError" />
        </div>
        <div class="manager-good-body">
          <div class="manager-good-top">
            <h3>{{ item.title || '未命名商品' }}</h3>
            <strong>¥{{ item.price || 0 }}</strong>
          </div>
          <p>{{ item.description || '暂无描述' }}</p>
          <div class="manager-good-meta">
            <el-tag :type="getCategoryTagType(item.category)" size="small">{{ getCategoryLabel(item.category) }}</el-tag>
            <el-tag :type="getStockTagType(item.count)" size="small">{{ getStockName(item.count) }}</el-tag>
          </div>
          <div class="manager-good-actions">
            <el-button size="small" type="primary" plain @click="mod(item)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button size="small" type="danger" plain @click="del(item.goodId)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </div>
      </article>
      <el-empty v-if="tableData.length === 0" description="暂无商品" />
    </section>

    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 30]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-drawer v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" size="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width:100%">
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存" prop="count">
          <el-input-number v-model="form.count" :min="0" :step="1" step-strictly controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="成色" prop="condition">
          <el-input v-model="form.condition" placeholder="例如：九成新、全新等" />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-upload
            v-model:file-list="fileList"
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :limit="6"
            multiple
            accept="image/png,image/jpeg,image/gif,image/webp"
            list-type="picture-card"
          >
            <el-icon><Upload /></el-icon>
            <div class="upload-text">上传</div>
          </el-upload>
          <div class="upload-tip">支持 jpg/png/gif/webp，单张不超过 2MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.goods-manager-container { display: grid; gap: 18px; }
.manager-hero { position: relative; overflow: hidden; display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 30px; border-radius: 26px; background: linear-gradient(135deg, rgba(255,252,245,.92), rgba(235,225,209,.7)); animation: rise .3s ease both; }
.manager-hero::after { content: ""; position: absolute; right: -70px; bottom: -80px; width: 230px; height: 230px; border-radius: 50%; background: rgba(194,122,44,.14); }
.manager-hero > * { position: relative; z-index: 1; }
.search-section { padding: 14px; border: 1px solid var(--market-line); border-radius: 18px; background: rgba(255,252,245,.76); box-shadow: 0 14px 32px rgba(50,38,25,.09); backdrop-filter: blur(14px); }
.search-box { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.search-input { width: 260px; }
.search-select { width: 170px; }
.goods-card-board { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 16px; }
.manager-good-card { position: relative; overflow: hidden; border: 1px solid var(--market-line); border-radius: 20px; background: rgba(255,252,245,.88); box-shadow: 0 14px 34px rgba(50,38,25,.09); transition: .18s ease; animation: rise .28s ease both; }
.manager-good-card::after { content: ""; position: absolute; inset: 0 0 auto 0; height: 4px; background: linear-gradient(90deg, var(--market-green), var(--market-gold)); opacity: .9; }
.manager-good-card:hover { transform: translateY(-4px); box-shadow: 0 18px 40px rgba(50,38,25,.13); }
.manager-good-cover { height: 160px; display: grid; place-items: center; background: linear-gradient(135deg,var(--market-green),#d5b47b); color: #fff; font-family: var(--market-display); font-size: 28px; font-weight: 900; overflow: hidden; }
.manager-good-cover img { width: 100%; height: 100%; object-fit: cover; }
.manager-good-body { display: grid; gap: 10px; padding: 15px; }
.manager-good-top { display: flex; justify-content: space-between; gap: 12px; }
.manager-good-top h3 { min-height: 42px; margin: 0; color: var(--market-ink); font-size: 16px; line-height: 1.35; overflow: hidden; }
.manager-good-top strong { color: var(--market-red); font-size: 20px; white-space: nowrap; }
.manager-good-body p { height: 42px; margin: 0; color: var(--market-muted); font-size: 14px; line-height: 1.5; overflow: hidden; }
.manager-good-meta, .manager-good-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.manager-good-actions { justify-content: flex-end; padding-top: 4px; border-top: 1px solid rgba(84,67,45,.12); }
.pagination-section { display: flex; justify-content: center; padding: 16px; border: 1px solid var(--market-line); border-radius: 14px; background: var(--market-card); box-shadow: var(--market-shadow); }
.upload-text { margin-top: 8px; font-size: 12px; color: var(--market-muted); }
.upload-tip { font-size: 12px; color: var(--market-muted); margin-top: 8px; }
@keyframes rise { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width:980px) { .goods-card-board { grid-template-columns: repeat(2,minmax(0,1fr)); } }
@media (max-width:760px) { .manager-hero { align-items: flex-start; flex-direction: column; } .search-input, .search-select { width: 100%; } .goods-card-board { grid-template-columns: 1fr; } }
</style>
