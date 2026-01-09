import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

interface Bank {
  bankId: string,
  name: string,
  code: string
}

interface ResourceState {
  // State
  banks: Bank[];

  // Action
  setBanks: (bankList: Bank[]) => void;
}

const useResourceStore = create<ResourceState>()(
  persist(
    (set) => ({
      // 초기 상태
      banks: [],

      // 액션 구현
      setBanks: (bankList) => set({ banks: bankList }),
    }),
    {
      name: 'resource-session-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useResourceStore;