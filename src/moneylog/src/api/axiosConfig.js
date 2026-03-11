import axios from 'axios';
import authStore from '../stores/authStore.js';

const DEFAULT_API_BASE_URL = 'http://localhost:8080/api';
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() || DEFAULT_API_BASE_URL;
const AUTHORIZATION_HEADER = 'Authorization';

const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 5000,
    withCredentials: true,
});

const setHeaderValue = (headers, value) => {
    if (!headers) {
        return;
    }

    if (typeof headers.set === 'function') {
        headers.set(AUTHORIZATION_HEADER, value);
        return;
    }

    headers[AUTHORIZATION_HEADER] = value;
};

const removeHeaderValue = (headers) => {
    if (!headers) {
        return;
    }

    if (typeof headers.delete === 'function') {
        headers.delete(AUTHORIZATION_HEADER);
        headers.delete('authorization');
        return;
    }

    delete headers[AUTHORIZATION_HEADER];
    delete headers.authorization;
};

export const setAuthorizationHeader = (token) => {
    if (token) {
        setHeaderValue(api.defaults.headers.common, `Bearer ${token}`);
        return;
    }

    removeHeaderValue(api.defaults.headers.common);
};

export const clearAuthorizationHeader = () => {
    setAuthorizationHeader(null);
};

api.interceptors.request.use(
    (config) => {
        const { accessToken } = authStore.getState();
        config.headers = config.headers ?? {};

        if (accessToken) {
            setHeaderValue(config.headers, `Bearer ${accessToken}`);
        } else {
            removeHeaderValue(config.headers);
            clearAuthorizationHeader();
        }

        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => {
        const newAccessToken = response.headers['authorization'];

        if (newAccessToken) {
            const token = newAccessToken.replace('Bearer ', '');

            authStore.getState().setAccessToken(token);
            setAuthorizationHeader(token);

            if (import.meta.env.DEV) {
                console.info('세션(access token)을 갱신했습니다.');
            }
        }

        return response;
    },
    (error) => Promise.reject(error)
);

export default api;
