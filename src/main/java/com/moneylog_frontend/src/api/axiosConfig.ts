import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError, AxiosResponse } from 'axios';

// 1. Axios 인스턴스 생성
const client: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
});

// 2. 요청 인터셉터
client.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // todo : 토큰 추가 로직 필요
        return config;
    },
    (error: AxiosError) => {
        return Promise.reject(error);
    }
);

// 3. 응답 인터셉터
client.interceptors.response.use(
    (response: AxiosResponse) => {
        return response;
    },
    (error: AxiosError) => {
        console.error('[API Error]', error.response?.data || error.message);
        return Promise.reject(error);
    }
);

export default client;