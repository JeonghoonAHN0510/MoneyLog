import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

const useUserStore = create(
  persist(
    (set) => ({
      // 초기 상태
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      userInfo: null,

      login: (accessToken, refreshToken) => set({
        accessToken,
        refreshToken,
        isAuthenticated: true
      }),

      logout: () => set({
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        userInfo: null
      }),

      setUserInfo: (user) => set({
        userInfo: user
      }),

      setAccessToken: (token) => set({
        accessToken: token
      }),

      setRefreshToken: (token) => set({
        refreshToken: token
      }),

      setTokens: (accessToken, refreshToken) => set({
        accessToken,
        refreshToken,
        isAuthenticated: true
      })
    }),
    {
      name: 'user-session-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useUserStore;
