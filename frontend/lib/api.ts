import { useAuthStore } from '@/store/authStore';

const API_BASE = 'http://localhost:8080';

export async function fetchApi(endpoint: string, options: RequestInit = {}) {
  const token = useAuthStore.getState().token;
  
  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = 'An error occurred';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch (e) {
      errorMessage = response.statusText;
    }
    throw new Error(errorMessage);
  }

  // Some endpoints might return empty body (e.g. 204 or void returns)
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}
