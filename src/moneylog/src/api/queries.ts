import { useQuery, useMutation, useQueryClient, type QueryClient } from '@tanstack/react-query';
import { queryKeys } from './queryClient';
import api from './axiosConfig';
import {
    serializeAccountPayload,
    serializeBudgetPayload,
    serializeCategoryPayload,
    serializeFixedPayload,
    serializeIdParam,
    serializePaymentPayload,
    serializeTransactionPayload,
    serializeTransactionImportCommitPayload,
    serializeTransferPayload,
} from './requestSerializers';
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
    DailySummary,
    TransactionImportPreviewResponse,
    TransactionImportCommitRequest,
    TransactionImportCommitResponse
} from '../types/finance';

const transactionRelatedKeys = [
    queryKeys.transactions,
    queryKeys.transactionsByDateRangeRoot,
    queryKeys.accounts,
    queryKeys.budgets,
];

const invalidateTransactionCaches = (qc: QueryClient) => {
    transactionRelatedKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }));
    qc.invalidateQueries({ queryKey: ['dashboard'] });
    qc.invalidateQueries({ queryKey: ['calendar'] });
};

type TokenRefreshResponse = {
    grantType: string;
    accessToken: string;
    refreshToken: string;
    accessTokenExpireTime: number;
};

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

/** 거래 내역 기간 조회 */
export function useTransactionsByDateRange(startDate?: string, endDate?: string) {
    return useQuery<Transaction[]>({
        queryKey: queryKeys.transactionsByDateRange(startDate ?? '', endDate ?? ''),
        queryFn: async () => {
            const res = await api.get('/transaction/search', {
                params: {
                    startDate,
                    endDate,
                },
            });
            return res.data;
        },
        enabled: Boolean(startDate && endDate),
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

export function useRefreshToken() {
    return useMutation({
        mutationFn: async ({ refreshToken }: { refreshToken: string }) => {
            const res = await api.post('/user/refresh', { refreshToken });
            return res.data as TokenRefreshResponse;
        },
    });
}

export function useUpdateProfileImage() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (file: File) => {
            const formData = new FormData();
            formData.append('file', file);

            const res = await api.put('/user/profile-image', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return res.data as UserInfo;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: queryKeys.userInfo });
        },
    });
}

export function useTransactionImportPreview() {
    return useMutation<TransactionImportPreviewResponse, Error, File>({
        mutationFn: async (file: File) => {
            const formData = new FormData();
            formData.append('file', file);

            const res = await api.post('/transaction/import/preview', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return res.data;
        },
    });
}

export function useTransactionImportCommit() {
    const qc = useQueryClient();
    return useMutation<TransactionImportCommitResponse, Error, TransactionImportCommitRequest>({
        mutationFn: async (payload: TransactionImportCommitRequest) => {
            const res = await api.post('/transaction/import/commit', serializeTransactionImportCommitPayload(payload));
            return res.data;
        },
        onSuccess: () => {
            invalidateTransactionCaches(qc);
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
        queryKey: queryKeys.schedules,
        queryFn: async () => {
            const res = await api.get('/admin/schedule/list');
            return res.data;
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
            queryClient.invalidateQueries({ queryKey: queryKeys.schedules });
        },
    });
};

// =============================================
// Mutation Hooks (서버 데이터 변경)
// =============================================

/** 거래 추가 */
export function useAddTransaction() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Transaction>) => api.post('/transaction', serializeTransactionPayload(data)),
        onSuccess: () => {
            invalidateTransactionCaches(qc);
        },
    });
}

/** 거래 수정 */
export function useUpdateTransaction() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Transaction>) => api.put('/transaction', serializeTransactionPayload(data)),
        onSuccess: () => {
            invalidateTransactionCaches(qc);
        },
    });
}

/** 거래 삭제 */
export function useDeleteTransaction() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (transactionId: string) => api.delete('/transaction', {
            params: { transactionId: serializeIdParam(transactionId) },
        }),
        onSuccess: () => {
            invalidateTransactionCaches(qc);
        },
    });
}

/** 고정 거래 추가 */
export function useAddFixed() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Fixed>) => api.post('/fixed', serializeFixedPayload(data)),
        onSuccess: () => {
            invalidateTransactionCaches(qc);
        },
    });
}

/** 계좌 추가 */
export function useAddAccount() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Account, 'accountId' | 'userId' | 'createdAt' | 'updatedAt' | 'bankName'>) =>
            api.post('/account', serializeAccountPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 계좌 수정 */
export function useUpdateAccount() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Account>) => api.put('/account', serializeAccountPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 계좌 삭제 */
export function useDeleteAccount() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (accountId: string) => api.delete('/account', {
            params: { accountId: serializeIdParam(accountId) },
        }),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 이체 */
export function useTransfer() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Transfer, 'transferId' | 'userId' | 'createdAt' | 'updatedAt'>) =>
            api.put('/account/transfer', serializeTransferPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.accounts }),
    });
}

/** 예산 추가 */
export function useAddBudget() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Budget, 'budgetId' | 'userId' | 'budgetDate' | 'createdAt' | 'updatedAt' | 'categoryName'>) =>
            api.post('/budget', serializeBudgetPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.budgets }),
    });
}

/** 예산 수정 */
export function useUpdateBudget() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Budget>) => api.put('/budget', serializeBudgetPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.budgets }),
    });
}

/** 예산 삭제 */
export function useDeleteBudget() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (budgetId: string) => api.delete('/budget', {
            params: { budgetId: serializeIdParam(budgetId) },
        }),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.budgets }),
    });
}

/** 카테고리 추가 */
export function useAddCategory() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Category, 'categoryId' | 'userId' | 'createdAt' | 'updatedAt'>) =>
            api.post('/category', serializeCategoryPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.categories }),
    });
}

/** 카테고리 수정 */
export function useUpdateCategory() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Category>) => api.put('/category', serializeCategoryPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.categories }),
    });
}

/** 카테고리 삭제 */
export function useDeleteCategory() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (categoryId: string) => api.delete('/category', {
            params: { categoryId: serializeIdParam(categoryId) },
        }),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.categories }),
    });
}

/** 결제수단 추가 */
export function useAddPayment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Omit<Payment, 'paymentId' | 'userId' | 'createdAt' | 'updatedAt'>) =>
            api.post('/payment', serializePaymentPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.payments }),
    });
}

/** 결제수단 수정 */
export function useUpdatePayment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (data: Partial<Payment>) => api.put('/payment', serializePaymentPayload(data)),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.payments }),
    });
}

/** 결제수단 삭제 */
export function useDeletePayment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (paymentId: string) => api.delete('/payment', {
            params: { paymentId: serializeIdParam(paymentId) },
        }),
        onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.payments }),
    });
}
