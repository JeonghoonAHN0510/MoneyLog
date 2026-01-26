import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";
import { Account, Bank, Budget, Category, Ledger } from "../types/finance";



interface ResourceState {
  // State
  banks: Bank[];
  budgets: Budget[];
  categories: Category[];
  accounts: Account[];
  ledgers: Ledger[];


  // Action
  setBanks: (bankList: Bank[]) => void;
  setBudgets: (budgetList: Budget[]) => void;
  setCategories: (categoryList: Category[]) => void;
  setAccounts: (accountList: Account[]) => void;
  setLedgers: (ledgerList: Ledger[]) => void;
}

const useResourceStore = create<ResourceState>()(
  persist(
    (set) => ({
      banks: [],
      budgets: [],
      categories: [],
      accounts: [],
      ledgers: [],

      setBanks: (bankList) => set({ banks: bankList }),
      setBudgets: (budgetList) => set({ budgets: budgetList }),
      setCategories: (categoryList) => set({ categories: categoryList }),
      setAccounts: (accountList) => set({ accounts: accountList }),
      setLedgers: (ledgerList) => set({ ledgers: ledgerList }),
    }),
    {
      name: 'resource-session-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useResourceStore;