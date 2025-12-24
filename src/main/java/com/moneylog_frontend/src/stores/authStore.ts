import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

// store 타입 정의
interface AuthState {
    accessToken: string | null;
    isAuthenticated: boolean;
    login: (token: string) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            // 초기 상태
            accessToken: null,
            isAuthenticated: false,

            // Login Action
            login: (token: string) => set({accessToken: token, isAuthenticated: true}),

            // Logout Action
            logout: () => set({accessToken: null, isAuthenticated: false}),
        }),
        {
            name: 'auth-storage',                               // loclaStorage에 저장될 키 이름
            storage: createJSONStorage(() => localStorage),     // 저장소
        }
    )
);