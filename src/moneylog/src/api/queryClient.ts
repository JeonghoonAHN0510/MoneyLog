import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30 * 1000,       // 30초 동안 fresh 유지
            gcTime: 5 * 60 * 1000,      // 5분간 캐시 보존
            retry: 1,                    // 실패 시 1회 재시도
            refetchOnWindowFocus: false, // 윈도우 포커스 시 자동 refetch 비활성화
        },
    },
});

// Query Key 상수 관리
export const queryKeys = {
    banks: ['banks'] as const,
    categories: ['categories'] as const,
    payments: ['payments'] as const,
    accounts: ['accounts'] as const,
    schedules: ['schedules'] as const,
    transactions: ['transactions'] as const,
    budgets: ['budgets'] as const,
    userInfo: ['userInfo'] as const,
    dashboard: (year: number, month: number) => ['dashboard', year, month] as const,
    calendar: (year: number, month: number) => ['calendar', year, month] as const,
};
