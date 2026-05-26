<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Upload } from '@element-plus/icons-vue'
import { deleteGoodImage, uploadGoodImages } from '../api'

const router = useRouter()

// 用户信息
const user = computed(() => {
  const raw = localStorage.getItem('nju-market-user') || sessionStorage.getItem('User')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
})

// 表格数据
const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const searchTitle = ref('')
const searchCategory = ref('')

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const fileList = ref([])

// 分类选项
const categoryOptions = [
  { value: 'daily', label: '日用' },
  { value: 'digital', label: '数码' },
  { value: 'pet', label: '萌宠' },
  { value: 'book', label: '书刊' },
  { value: 'sports', label: '体育' },
]

// 商品状态选项
const statusOptions = [
  { value: 'available', label: '可售' },
  { value: 'sold', label: '已售' },
  { value: 'offline', label: '下架' },
]

// 表单数据
const form = ref({
  goodId: null,
  sellerId: 0,
  title: '',
  description: '',
  price: 0,
  category: '',
  status: 'available',
  condition: '',
  viewCount: 0,
  image: '',
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' },
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' },
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' },
  ],
}

function getCategoryTagType(category) {
  const types = {
    '日用': 'daily',
    '数码': 'digital',
    '书刊': 'book',
    '体育': 'sports',
  }
  return types[category] || ''
}

function getStatusTagType(status) {
  const types = {
    'available': 'success',
    'sold': 'info',
    'offline': 'danger',
  }
  return types[status] || ''
}

function getStatusName(status) {
  const names = {
    'available': '可售',
    'sold': '已售',
    'offline': '下架',
  }
  return names[status] || '未知'
}

function fixurl(fileName) {
  if (!fileName) return ''
  if (fileName.startsWith('http')) return fileName
  if (fileName.startsWith('/uploads/')) return `http://localhost:8080${fileName}`
  return `http://localhost:8080/uploads/goods/${fileName}`
}

function handleImageError(event) {
  event.target.style.display = 'none'
  const nextElement = event.target.nextElementSibling
  if (nextElement) {
    nextElement.style.display = 'block'
  }
}

async function loadPost() {
  try {
    const response = await fetch('/api/goods/listPage', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
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
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
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
    status: 'available',
    condition: '',
    viewCount: 0,
    image: '',
  }
  fileList.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
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
    price: row.price,
    category: row.category,
    status: row.status,
    condition: row.condition || '',
    viewCount: row.viewCount || 0,
    image: row.image || row.imageUrl || row.coverUrl || '',
  }
  // 如果有图片，设置fileList
  if (Array.isArray(row.images) && row.images.length > 0) {
    fileList.value = row.images.map((image) => ({
      name: image.imageUrl,
      url: fixurl(image.imageUrl),
      imageId: image.imageId,
      status: 'success',
    }))
  } else if (row.image || row.imageUrl || row.coverUrl) {
    const imageUrl = row.image || row.imageUrl || row.coverUrl
    fileList.value = [{
      name: imageUrl,
      url: fixurl(imageUrl),
      status: 'success',
    }]
  } else {
    fileList.value = []
  }
  dialogVisible.value = true
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确定删除吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await fetch(`/api/goods/${id}`, {
      method: 'DELETE',
    })

    const res = await response.json()

    if (res.success) {
      ElMessage.success('删除成功')
      loadPost()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 自定义上传方法
async function customUpload(options) {
  const { file, onSuccess, onError, onProgress } = options

  const formData = new FormData()
  formData.append('files', file)

  try {
    // 如果是编辑模式且有商品ID，则使用商品的ID作为路径参数
    let uploadUrl = '/api/good-images/upload-temp'

    if (isEdit.value && form.value.goodId) {
      uploadUrl = `/api/good-images/${form.value.goodId}/upload`
    }

    const xhr = new XMLHttpRequest()

    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percent = Math.round((event.loaded / event.total) * 100)
        onProgress({ percent })
      }
    }

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const response = JSON.parse(xhr.responseText)
          onSuccess(response, file)
        } catch (e) {
          onError(new Error('响应解析失败'))
        }
      } else {
        onError(new Error(`上传失败: ${xhr.statusText}`))
      }
    }

    xhr.onerror = () => {
      onError(new Error('网络错误'))
    }

    xhr.open('POST', uploadUrl, true)
    xhr.send(formData)
  } catch (error) {
    console.error('上传错误:', error)
    onError(error)
  }
}

async function handleSuccess(response, file, uploadedFileList) {
  console.log('上传成功:', response, file, uploadedFileList)

  if (response && response.success && response.data) {
    // 如果是数组，取第一个图片URL
    const imageData = Array.isArray(response.data) ? response.data[0] : response.data
    if (imageData && imageData.imageUrl) {
      form.value.image = imageData.imageUrl
    } else if (typeof imageData === 'string') {
      form.value.image = imageData
    }
  } else if (response && response.url) {
    form.value.image = response.url
  } else {
    form.value.image = file.name
  }

  fileList.value = uploadedFileList
  ElMessage.success('上传成功')
}

function beforeUpload(file) {
  const isAllowedType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isAllowedType) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }

  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
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
  if (!uploadFile.imageId) {
    return
  }

  try {
    await deleteGoodImage(uploadFile.imageId)
    ElMessage.success('图片删除成功')
  } catch (error) {
    ElMessage.error(error.message || '图片删除失败')
  }
}

async function uploadSelectedImages(goodId) {
  const files = fileList.value
      .map((item) => item.raw)
      .filter(Boolean)

  if (files.length === 0) {
    return []
  }

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
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(submitData),
    })

    const res = await response.json()

    if (res.success) {
      const goodId = res.data?.goodId
      if (goodId) {
        await uploadSelectedImages(goodId)
      }
      ElMessage.success('添加成功')
      dialogVisible.value = false
      resetForm()
      loadPost()
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error('添加失败')
  }
}

async function doMod() {
  try {
    const response = await fetch(`/api/goods/${form.value.goodId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(form.value),
    })

    const res = await response.json()

    if (res.success) {
      await uploadSelectedImages(form.value.goodId)
      ElMessage.success('修改成功')
      dialogVisible.value = false
      resetForm()
      loadPost()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
    ElMessage.error('修改失败')
  }
}

function save() {
  if (!formRef.value) return

  formRef.value.validate(async (valid) => {
    if (valid) {
      if (isEdit.value) {
        await doMod()
      } else {
        await doSave()
      }
    } else {
      ElMessage.error('请完善表单信息')
      return false
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
  <div class="goods-manager-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">📦 商品管理</h1>
      <p class="page-subtitle">管理您的商品信息</p>
    </div>

    <!-- 管理员提示 -->
    <el-alert
        v-if="user && user.roleid === 1"
        title="您是管理员，可以管理所有商品"
        type="info"
        show-icon
        :closable="false"
        class="admin-alert"
    />

    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <div class="search-box">
        <el-input
            v-model="searchTitle"
            placeholder="请输入商品标题"
            clearable
            class="search-input"
            @keyup.enter="loadPost"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
            v-model="searchCategory"
            placeholder="选择分类"
            clearable
            class="search-select"
        >
          <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>

        <el-button type="primary" @click="loadPost">
          <el-icon><Search /></el-icon> 查询
        </el-button>
        <el-button type="success" @click="resetParam">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
        <el-button type="primary" @click="add">
          <el-icon><Plus /></el-icon> 新增
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
        :data="tableData"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        border
        stripe
        class="goods-table"
    >
      <el-table-column prop="goodId" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="商品标题" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="120" align="right">
        <template #default="{ row }">
          <span class="price-text">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getCategoryTagType(row.category)" size="small">
            {{ row.category }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="condition" label="成色" width="100" align="center" />
      <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
      <el-table-column prop="sellerName" label="卖家" width="120" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="mod(row)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button size="small" type="danger" @click="del(row.goodId)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 编辑/新增对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑商品' : '新增商品'"
        width="600px"
        :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
              v-model="form.price"
              :min="0.01"
              :precision="2"
              :step="1"
              controls-position="right"
              style="width: 100%"
          >
            <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="成色" prop="condition">
          <el-input v-model="form.condition" placeholder="例如：9成新、全新等" />
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
            <div class="upload-text">点击上传</div>
          </el-upload>
          <div class="upload-tip">只能上传 jpg/png 文件，且不超过 2MB</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.goods-manager-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.admin-alert {
  margin-bottom: 20px;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-box {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
}

.search-select {
  width: 160px;
}

.goods-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.price-text {
  color: #f56c6c;
  font-weight: 600;
}

.product-image {
  max-width: 80px;
  max-height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image {
  color: #909399;
  font-size: 12px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
