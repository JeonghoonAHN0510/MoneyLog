import axios from 'axios';
import authStore from '../stores/authStore.js';

export const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 5000,
    withCredentials: true, // 쿠키/인증 정보 포함
});

// =====================================================
// 1. 요청 인터셉터 (Request Interceptor)
// : 요청을 보낼 때마다 스토어에 있는 토큰을 헤더에 실어 보냅니다.
// =====================================================
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

// =====================================================
// 2. 응답 인터셉터 (Response Interceptor)
// : 응답 헤더에 새 토큰이 있으면(갱신됨), 갈아끼웁니다.
// =====================================================
api.interceptors.response.use(
    (response) => {
        // 백엔드에서 헤더에 새 토큰을 넣어줬는지 확인
        const newAccessToken = response.headers['authorization'];

        if (newAccessToken) {
            const token = newAccessToken.replace('Bearer ', '');
            
            useUserStore.getState().setAccessToken(token);

            // Axios 기본 헤더도 갱신
            api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            console.log("🔄 세션(토큰)이 연장되었습니다.");
        }
        return response;
    },
    (error) => Promise.reject(error)
);

export default api;
