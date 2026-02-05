import {create} from "zustand";
import {createJSONStorage, persist} from "zustand/middleware";
import {Account, Bank, Budget, Category, Payment, Transaction} from "../types/finance";


interface ResourceState {
    // State
    banks: Bank[];
    budgets: Budget[];
    categories: Category[];
    accounts: Account[];
    transactions: Transaction[];
    payments: Payment[];


    // Action
    setBanks: (bankList: Bank[]) => void;
    setBudgets: (budgetList: Budget[]) => void;
    setCategories: (categoryList: Category[]) => void;
    setAccounts: (accountList: Account[]) => void;
    setTransactions: (transactionList: Transaction[]) => void;
    setPayments: (paymentList: Payment[]) => void;
}

const useResourceStore = create<ResourceState>()(
    persist(
        (set) => ({
            banks: [],
            budgets: [],
            categories: [],
            accounts: [],
            transactions: [],
            payments: [],

            setBanks: (bankList) => set({banks: bankList}),
            setBudgets: (budgetList) => set({budgets: budgetList}),
            setCategories: (categoryList) => set({categories: categoryList}),
            setAccounts: (accountList) => set({accounts: accountList}),
            setTransactions: (transactionList) => set({transactions: transactionList}),
            setPayments: (paymentList) => set({payments: paymentList}),
        }),
        {
            name: 'resource-session-storage',
            storage: createJSONStorage(() => sessionStorage),
        }
    )
);

export default useResourceStore;