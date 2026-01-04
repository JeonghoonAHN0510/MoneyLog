import axios from 'axios';
import authStore from '../stores/authStore.js';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 5000,
    withCredentials: true, // 쿠키/인증 정보 포함
});

// 요청 인터셉터
api.interceptors.request.use(
    (config) => {
        // ★ 핵심: Zustand 스토어에서 직접 토큰을 꺼냅니다.
        // getState()는 훅이 아니라서 컴포넌트 밖에서도 쓸 수 있습니다.
        const { accessToken } = authStore.getState();

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;