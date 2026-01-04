import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

const useUserStore = create(
  persist(
    (set) => ({
      // 1. 초기 상태
      accessToken: null,
      isAuthenticated: false,

      // 2. 액션 (로그인)
      login: (token) => set({
        accessToken: token,
        isAuthenticated: true
      }),

      // 3. 액션 (로그아웃)
      logout: () => set({
        accessToken: null,
        isAuthenticated: false
      }),
    }),
    {
      name: 'user-session-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useUserStore;