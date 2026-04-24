import axios from "axios";

const normalizeApiBaseUrl = () => {
  const configured = import.meta.env.VITE_API_BASE_URL;
  if (!configured || !configured.trim()) {
    return "/api/v1";
  }

  const base = configured.trim().replace(/\/+$/, "");
  if (base.endsWith("/api/v1")) {
    return base;
  }
  if (base.endsWith("/api")) {
    return `${base}/v1`;
  }
  return `${base}/api/v1`;
};

export const apiClient = axios.create({
  baseURL: normalizeApiBaseUrl(),
  timeout: 10000
});

export const setAccessTokenProvider = (provider: () => string | null) => {
  apiClient.interceptors.request.use((config) => {
    const token = provider();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
};
