const API_BASE_URL = '/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  let data
  try {
    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      data = await response.json()
    } else {
      const text = await response.text()
      throw new Error(text || `HTTP error! status: ${response.status}`)
    }
  } catch (parseError) {
    if (parseError instanceof SyntaxError) {
      throw new Error('服务器响应格式错误')
    }
    throw parseError
  }

  if (!response.ok || data.success === false) {
    throw new Error(data.message || 'Request failed')
  }
  return data
}

export function fetchList(resource) {
  return request(`/${resource}`)
}

export function fetchById(resource, id) {
  return request(`/${resource}/${id}`)
}

export function fetchPage(payload) {
  return request('/goods/listPage', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function createRecord(resource, payload) {
  return request(`/${resource}`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateRecord(resource, id, payload) {
  return request(`/${resource}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteRecord(resource, id) {
  return request(`/${resource}/${id}`, {
    method: 'DELETE',
  })
}

export function searchRecords(resource, payload) {
  return request(`/${resource}/search`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function registerUser(payload) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function loginUser(payload) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function loginAdmin(payload) {
  return request('/admins/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function banUser(userId) {
  return request(`/users/${userId}/ban`, {
    method: 'POST',
  })
}

export function unbanUser(userId) {
  return request(`/users/${userId}/unban`, {
    method: 'POST',
  })
}

export function purchaseGood(payload) {
  return request('/orders/purchase', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function cancelOrder(orderId, buyerId) {
  const query = buyerId ? `?buyerId=${encodeURIComponent(buyerId)}` : ''
  return request(`/orders/${orderId}/cancel${query}`, {
    method: 'POST',
  })
}

export function completeOrder(orderId, sellerId) {
  const query = sellerId ? `?sellerId=${encodeURIComponent(sellerId)}` : ''
  return request(`/orders/${orderId}/complete${query}`, {
    method: 'POST',
  })
}

export function fetchSellerOrders(sellerId) {
  return request(`/orders/seller/${sellerId}`)
}

export function fetchBuyerOrders(buyerId) {
  return request(`/orders/buyer/${buyerId}`)
}

export function fetchGoodComments(goodId) {
  return request(`/comments/good/${goodId}`)
}

export function fetchGoodRatings(goodId) {
  return request(`/ratings/good/${goodId}`)
}

export function fetchGoodRatingSummary(goodId) {
  return request(`/ratings/good/${goodId}/summary`)
}

export async function uploadUserAvatar(userId, file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`/api/users/${userId}/avatar`, {
    method: 'POST',
    body: formData,
  })

  const data = await response.json()
  if (!response.ok || data.success === false) {
    throw new Error(data.message || '头像上传失败')
  }
  return data
}

export async function uploadGoodImages(goodId, files) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })

  const response = await fetch(`/api/good-images/${goodId}/upload`, {
    method: 'POST',
    body: formData,
  })

  const data = await response.json()
  if (!response.ok || data.success === false) {
    throw new Error(data.message || '商品图片上传失败')
  }
  return data
}

export function fetchGoodImages(goodId) {
  return request(`/good-images/good/${goodId}`)
}

export function deleteGoodImage(imageId) {
  return request(`/good-images/${imageId}`, {
    method: 'DELETE',
  })
}
