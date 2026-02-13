import { Account } from '../types/finance';

export const ACCOUNT_TYPE_LABELS: Record<Account['type'], string> = {
    BANK: '은행',
    CASH: '현금',
    POINT: '포인트',
    OTHER: '기타',
};

export const getAccountTypeLabel = (type: Account['type']) => {
    return ACCOUNT_TYPE_LABELS[type] ?? '기타';
};
