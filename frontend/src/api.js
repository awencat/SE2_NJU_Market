const API_BASE_URL = '/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  const data = await response.json()
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
