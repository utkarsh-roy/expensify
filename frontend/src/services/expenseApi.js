const API_BASE_URL = import.meta.env.VITE_API_URL ?? '/api/expenses'

async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    let message = 'Something went wrong while calling the API.'

    try {
      const errorPayload = await response.json()
      if (Array.isArray(errorPayload.details) && errorPayload.details.length > 0) {
        message = errorPayload.details.join(' ')
      }
    } catch {
      message = response.statusText || message
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export function getExpenses() {
  return request(API_BASE_URL)
}

export function createExpense(expense) {
  return request(API_BASE_URL, {
    method: 'POST',
    body: JSON.stringify(expense),
  })
}

export function updateExpense(id, expense) {
  return request(`${API_BASE_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(expense),
  })
}

export function deleteExpense(id) {
  return request(`${API_BASE_URL}/${id}`, {
    method: 'DELETE',
  })
}

export function getExpenseAnalysis() {
  return request(`${API_BASE_URL}/analysis`)
}
