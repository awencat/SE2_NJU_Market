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
  return request(`/goods/listPage`, {
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

