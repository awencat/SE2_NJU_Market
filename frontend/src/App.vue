<script setup>
import { computed, ref } from 'vue'
import {
  createRecord,
  deleteRecord,
  fetchById,
  fetchList,
  searchRecords,
  updateRecord,
} from './api'

const resources = [
  {
    label: '用户 User',
    value: 'users',
    idKey: 'userId',
    sample: {
      username: 'alice',
      password: '123456',
      email: 'alice@example.com',
      phone: '13800000000',
      campus: 'Xianlin',
      address: 'Dorm 1',
    },
  },
  {
    label: '管理员 Admin',
    value: 'admins',
    idKey: 'adminId',
    sample: {
      username: 'root',
      password: '123456',
      role: 'SUPER_ADMIN',
    },
  },
  {
    label: '商品 Good',
    value: 'goods',
    idKey: 'goodId',
    sample: {
      sellerId: 1,
      title: '二手高数教材',
      description: '九成新，可当面交易',
      price: 25.5,
      category: 'book',
      status: 'ON_SALE',
      condition: 'USED_GOOD',
      viewCount: 0,
    },
  },
  {
    label: '订单 Order',
    value: 'orders',
    idKey: 'orderId',
    sample: {
      buyerId: 1,
      sellerId: 2,
      orderNumber: 'ORD20260426001',
      totalAmount: 25.5,
      status: 'PENDING',
    },
  },
  {
    label: '举报 Report',
    value: 'reports',
    idKey: 'reportId',
    sample: {
      reporterId: 1,
      targetUserId: 2,
      reportType: 'USER',
      reason: '违规发布',
      status: 'PENDING',
    },
  },
  {
    label: '评论 Comment',
    value: 'comments',
    idKey: 'commentId',
    sample: {
      userId: 1,
      goodId: 1,
      content: '请问还在吗？',
    },
  },
  {
    label: '评分 Rating',
    value: 'ratings',
    idKey: 'ratingId',
    sample: {
      userId: 1,
      goodId: 1,
      score: 5,
    },
  },
  {
    label: '收藏 Favorite',
    value: 'favorites',
    idKey: 'favoriteId',
    sample: {
      userId: 1,
      goodId: 1,
    },
  },
  {
    label: '商品图片 GoodImage',
    value: 'good-images',
    idKey: 'imageId',
    sample: {
      goodId: 1,
      imageUrl: 'https://example.com/image.png',
      sortOrder: 1,
    },
  },
  {
    label: '订单项 OrderItem',
    value: 'order-items',
    idKey: 'itemId',
    sample: {
      orderId: 1,
      goodId: 1,
      quantity: 1,
      unitPrice: 25.5,
      subtotal: 25.5,
    },
  },
]

const currentResource = ref(resources[0].value)
const idInput = ref('')
const searchText = ref('{\n  "username": "alice"\n}')
const editText = ref(JSON.stringify(resources[0].sample, null, 2))
const tableRows = ref([])
const currentRecord = ref(null)
const loading = ref(false)
const feedback = ref('等待请求')

const activeResource = computed(() =>
  resources.find((item) => item.value === currentResource.value) ?? resources[0],
)

const columns = computed(() => {
  const row = tableRows.value[0] ?? currentRecord.value
  return row ? Object.keys(row) : []
})

function resetEditorFromSample() {
  editText.value = JSON.stringify(activeResource.value.sample, null, 2)
  searchText.value = JSON.stringify(
    activeResource.value.value === 'users' ? { username: 'alice' } : {},
    null,
    2,
  )
  idInput.value = ''
  currentRecord.value = null
  tableRows.value = []
}

function parseJson(text) {
  if (!text.trim()) return {}
  return JSON.parse(text)
}

async function runAction(action, successMessage) {
  loading.value = true
  feedback.value = '请求中...'
  try {
    const result = await action()
    feedback.value = successMessage
    return result
  } catch (error) {
    feedback.value = error.message || '请求失败'
    throw error
  } finally {
    loading.value = false
  }
}

async function loadList() {
  const result = await runAction(
    () => fetchList(currentResource.value),
    '列表查询成功',
  )
  tableRows.value = Array.isArray(result.data) ? result.data : result.data.records ?? []
  currentRecord.value = null
}

async function loadById() {
  if (!idInput.value) {
    feedback.value = '请输入 id'
    return
  }
  const result = await runAction(
    () => fetchById(currentResource.value, idInput.value),
    '按 id 查询成功',
  )
  currentRecord.value = result.data
  tableRows.value = result.data ? [result.data] : []
}

async function runSearch() {
  const payload = parseJson(searchText.value)
  const result = await runAction(
    () => searchRecords(currentResource.value, payload),
    '条件查询成功',
  )
  tableRows.value = Array.isArray(result.data) ? result.data : []
  currentRecord.value = null
}

async function createItem() {
  const payload = parseJson(editText.value)
  await runAction(() => createRecord(currentResource.value, payload), '新增成功')
  await loadList()
}

async function updateItem() {
  if (!idInput.value) {
    feedback.value = '更新前请先输入 id'
    return
  }
  const payload = parseJson(editText.value)
  await runAction(
    () => updateRecord(currentResource.value, idInput.value, payload),
    '更新成功',
  )
  await loadById()
}

async function removeItem() {
  if (!idInput.value) {
    feedback.value = '删除前请先输入 id'
    return
  }
  await runAction(
    () => deleteRecord(currentResource.value, idInput.value),
    '删除成功',
  )
  tableRows.value = []
  currentRecord.value = null
}

function onResourceChange() {
  resetEditorFromSample()
}

resetEditorFromSample()
</script>

<template>
  <div class="page-shell">
    <header class="hero-panel">
      <div>
        <p class="eyebrow">NJU Market Admin Console</p>
        <h1>校园二手交易平台数据面板</h1>
        <p class="hero-copy">
          这个页面直接对接 Spring Boot CRUD 接口，方便你在数据库建好以后快速联调。
        </p>
      </div>
      <div class="status-card">
        <span class="status-label">当前状态</span>
        <strong>{{ loading ? '请求中' : '空闲' }}</strong>
        <p>{{ feedback }}</p>
      </div>
    </header>

    <main class="workspace">
      <section class="control-panel">
        <div class="panel-head">
          <h2>资源选择</h2>
          <p>切换不同实体，下面的示例 JSON 会自动变化。</p>
        </div>

        <label class="field">
          <span>实体类型</span>
          <select v-model="currentResource" @change="onResourceChange">
            <option v-for="resource in resources" :key="resource.value" :value="resource.value">
              {{ resource.label }}
            </option>
          </select>
        </label>

        <label class="field">
          <span>记录 ID</span>
          <input
            v-model="idInput"
            :placeholder="`请输入 ${activeResource.idKey}`"
            type="text"
          />
        </label>

        <div class="button-row">
          <button class="solid" :disabled="loading" @click="loadList">查询全部</button>
          <button class="ghost" :disabled="loading" @click="loadById">按 ID 查询</button>
          <button class="warn" :disabled="loading" @click="removeItem">删除</button>
        </div>
      </section>

      <section class="editor-panel">
        <div class="panel-head">
          <h2>条件查询</h2>
          <p>传入 JSON 字段做等值筛选，例如商品可按 `sellerId`、`status` 查询。</p>
        </div>
        <textarea v-model="searchText" class="json-box" spellcheck="false" />
        <button class="solid wide" :disabled="loading" @click="runSearch">执行条件查询</button>
      </section>

      <section class="editor-panel">
        <div class="panel-head">
          <h2>新增 / 更新</h2>
          <p>新增直接提交，更新时会使用上面输入的 id 覆盖主键。</p>
        </div>
        <textarea v-model="editText" class="json-box large" spellcheck="false" />
        <div class="button-row">
          <button class="solid" :disabled="loading" @click="createItem">新增</button>
          <button class="ghost" :disabled="loading" @click="updateItem">更新</button>
        </div>
      </section>

      <section class="result-panel">
        <div class="panel-head">
          <h2>查询结果</h2>
          <p>后端返回的结果会在这里展开成表格。</p>
        </div>

        <div v-if="tableRows.length" class="table-wrap">
          <table>
            <thead>
              <tr>
                <th v-for="column in columns" :key="column">{{ column }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in tableRows" :key="index">
                <td v-for="column in columns" :key="column">{{ row[column] }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state">
          <p>还没有结果。你可以先点“查询全部”，或者执行一次条件查询。</p>
        </div>
      </section>
    </main>
  </div>
</template>
