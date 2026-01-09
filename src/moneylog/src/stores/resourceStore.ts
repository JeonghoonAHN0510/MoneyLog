import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

interface Bank {
  bank_id: string,
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
      banks: [],

      setBanks: (bankList) => set({ banks: bankList }),
    }),
    {
      name: 'resource-session-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useResourceStore;