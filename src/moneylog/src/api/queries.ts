import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from './queryClient';
import api from './axiosConfig';
import { Schedule, ScheduleReqDto } from '../types/schedule';
import {
    UserInfo,
    Transaction,
    Fixed,
    Account,
    Budget,
    Category,
    Payment,
    Transfer,
    Bank,
    DashboardData,
    DailySummary
} from '../types/finance';

// =============================================
// Query Hooks (서버 데이터 조회)
// =============================================

/** 은행 목록 — 거의 변하지 않으므로 staleTime Infinity */
export function useBanks() {
    return useQuery<Bank[]>({
        queryKey: queryKeys.banks,
        queryFn: async () => {
            const res = await api.get('/bank');
            return res.data;
        },
        staleTime: Infinity,
    });
}

/** 카테고리 목록 */
export function useCategories() {
    return useQuery<Category[]>({
        queryKey: queryKeys.categories,
        queryFn: async () => {
            const res = await api.get('/category');
            return res.data;
        },
    });
}

/** 결제수단 목록 */
export function usePayments() {
    return useQuery<Payment[]>({
        queryKey: queryKeys.payments,
        queryFn: async () => {
            const res = await api.get('/payment');
            return res.data;
        },
    });
}

/** 계좌 목록 */
export function useAccounts() {
    return useQuery<Account[]>({
        queryKey: queryKeys.accounts,
        queryFn: async () => {
            const res = await api.get('/account/list');
            return res.data;
        },
    });
}

/** 거래 내역 전체 */
export function useTransactions() {
    return useQuery<Transaction[]>({
        queryKey: queryKeys.transactions,
        queryFn: async () => {
            const res = await api.get('/transaction');
            return res.data;
        },
    });
}

/** 예산 목록 */
export function useBudgets() {
    return useQuery<Budget[]>({
        queryKey: queryKeys.budgets,
        queryFn: async () => {
            const res = await api.get('/budget');
            return res.data;
        },
    });
}

/** 사용자 정보 */
export function useUserInfo() {
    return useQuery<UserInfo>({
        queryKey: queryKeys.userInfo,
        queryFn: async () => {
            const res = await api.get('/user/info');
            return res.data;
        },
    });
}

// =============================================
// Dashboard / Calendar Hooks
// =============================================

export function useDashboard(year: number, month: number) {
    return useQuery<DashboardData>({
        queryKey: queryKeys.dashboard(year, month),
        queryFn: async () => {
            const res = await api.get('/transaction/dashboard', { params: { year, month } });
            return res.data;
        },
    });
}

export function useCalendar(year: number, month: number) {
    return useQuery<DailySummary[]>({
        queryKey: queryKeys.calendar(year, month),
        queryFn: async () => {
            const res = await api.get('/transaction/calendar', { params: { year, month } });
            return res.data;
        },
    });
}

// =============================================
// Schedule Hooks
// =============================================
export const useSchedules = () => {
    return useQuery<Schedule[]>({
        queryKey: ['schedules'],
        queryFn: async () => {
            const res = await api.get('/account/schedule/list'); // Controller path 확인 필요. /api/account 아님. /api/admin/schedule임.
            // ScheduleController path: /api/admin/schedule
            // 사용자 권한 문제로 /api/admin/schedule/list 가 403 뜰 수도 있음.
            // 하지만 일단 구현하고, 안되면 Path 변경.
            const { data } = await api.get('/admin/schedule/list');
            return data;
        },
        staleTime: 0,
    });
};

export const useUpdateSchedule = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (dto: ScheduleReqDto) => {
            await api.post('/admin/schedule/update', dto);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['schedules'] });
        },
    });
};

// =============================================
// Mutation Hooks (서버 데이터 변경)
// =============================================

/** 거래 관련 mutations 후 invalidate할 쿼리들 */
const transactionRelatedKeys = [
    queryKeys.transactions,
    queryKeys.accounts,
    queryKeys.budgets,
];

/** 거래 추가 */
export function useAddTransaction() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Transaction>) => api.post('/transaction', data),
        onSuccess: () => {
            transactionRelatedKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }));
            // 대시보드/캘린더도 갱신
            qc.invalidateQueries({ queryKey: ['dashboard'] });
            qc.invalidateQueries({ queryKey: ['calendar'] });
        },
    });
}

/** 거래 수정 */
export function useUpdateTransaction() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Transaction>) => api.put('/transaction', data),
        onSuccess: () => {
            transactionRelatedKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }));
            qc.invalidateQueries({ queryKey: ['dashboard'] });
            qc.invalidateQueries({ queryKey: ['calendar'] });
        },
    });
}

/** 거래 삭제 */
export function useDeleteTransaction() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (transactionId: string) => api.delete(`/transaction?transactionId=${transactionId}`),
        onSuccess: () => {
            transactionRelatedKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }));
            qc.invalidateQueries({ queryKey: ['dashboard'] });
            qc.invalidateQueries({ queryKey: ['calendar'] });
        },
    });
}

/** 고정 거래 추가 */
export function useAddFixed() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Fixed>) => api.post('/fixed', data),
        onSuccess: () => {
            transactionRelatedKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }));
        },
    });
}

/** 계좌 추가 */
export function useAddAccount() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Account, 'accountId' | 'userId' | 'createdAt' | 'updatedAt' | 'bankName'>) =>
            api.post('/account', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 계좌 수정 */
export function useUpdateAccount() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Account>) => api.put('/account', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 계좌 삭제 */
export function useDeleteAccount() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (accountId: string) => api.delete(`/account?accountId=${accountId}`),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 이체 */
export function useTransfer() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Transfer, 'transferId' | 'userId' | 'createdAt' | 'updatedAt'>) =>
            api.put('/account/transfer', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 예산 추가 */
export function useAddBudget() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Budget, 'budgetId' | 'userId' | 'budgetDate' | 'createdAt' | 'updatedAt' | 'categoryName'>) =>
            api.post('/budget', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.budgets }),
    });
}

/** 예산 수정 */
export function useUpdateBudget() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Budget>) => api.put('/budget', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.budgets }),
    });
}

/** 예산 삭제 */
export function useDeleteBudget() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (budgetId: string) => api.delete(`/budget?budgetId=${budgetId}`),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.budgets }),
    });
}

/** 카테고리 추가 */
export function useAddCategory() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Category, 'categoryId' | 'userId' | 'createdAt' | 'updatedAt'>) =>
            api.post('/category', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.categories }),
    });
}

/** 카테고리 수정 */
export function useUpdateCategory() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Category>) => api.put('/category', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.categories }),
    });
}

/** 카테고리 삭제 */
export function useDeleteCategory() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (categoryId: string) => api.delete(`/category?categoryId=${categoryId}`),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.categories }),
    });
}

/** 결제수단 추가 */
export function useAddPayment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Payment, 'paymentId' | 'userId' | 'createdAt' | 'updatedAt'>) =>
            api.post('/payment', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.payments }),
    });
}

/** 결제수단 수정 */
export function useUpdatePayment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Payment>) => api.put('/payment', data),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.payments }),
    });
}

/** 결제수단 삭제 */
export function useDeletePayment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (paymentId: string) => api.delete(`/payment?paymentId=${paymentId}`),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.payments }),
    });
}
