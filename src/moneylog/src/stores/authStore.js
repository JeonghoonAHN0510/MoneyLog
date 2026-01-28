import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

const useUserStore = create(
  persist(
    (set) => ({
      // 초기 상태
      accessToken: null,
      isAuthenticated: false,
      userInfo: null,

      login: (token) => set({
        accessToken: token,
        isAuthenticated: true
      }),

      logout: () => set({
        accessToken: null,
        isAuthenticated: false,
        userInfo: null
      }),

      setUserInfo: (user) => set({
        userInfo: user
      }),

      setAccessToken: (token) => set({
        accessToken: token
      })
    }),
    {
      name: 'user-session-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useUserStore;