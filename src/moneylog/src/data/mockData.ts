import { Transaction, Budget, Category, Account } from '../types/finance';

export const defaultExpenseCategories: Category[] = [
  { id: '1', name: '식비', type: 'expense', color: '#ef4444' },
  { id: '2', name: '교통', type: 'expense', color: '#f59e0b' },
  { id: '3', name: '주거', type: 'expense', color: '#8b5cf6' },
  { id: '4', name: '통신', type: 'expense', color: '#3b82f6' },
  { id: '5', name: '문화/여가', type: 'expense', color: '#ec4899' },
  { id: '6', name: '쇼핑', type: 'expense', color: '#06b6d4' },
  { id: '7', name: '의료', type: 'expense', color: '#10b981' },
  { id: '8', name: '기타', type: 'expense', color: '#64748b' },
];

export const defaultIncomeCategories: Category[] = [
  { id: '9', name: '급여', type: 'income', color: '#22c55e' },
  { id: '10', name: '부수입', type: 'income', color: '#84cc16' },
  { id: '11', name: '기타수입', type: 'income', color: '#14b8a6' },
];

export const mockTransactions: Transaction[] = [
  {
    id: '1',
    date: '2024-12-01',
    type: 'income',
    category: '급여',
    amount: 3000000,
    description: '12월 급여',
    isFixed: true,
  },
  {
    id: '2',
    date: '2024-12-02',
    type: 'expense',
    category: '식비',
    amount: 12000,
    description: '점심',
    isFixed: false,
  },
  {
    id: '3',
    date: '2024-12-03',
    type: 'expense',
    category: '교통',
    amount: 65000,
    description: '지하철 정기권',
    isFixed: true,
  },
  {
    id: '4',
    date: '2024-12-05',
    type: 'expense',
    category: '주거',
    amount: 600000,
    description: '월세',
    isFixed: true,
  },
  {
    id: '5',
    date: '2024-12-05',
    type: 'expense',
    category: '통신',
    amount: 55000,
    description: '통신비',
    isFixed: true,
  },
  {
    id: '6',
    date: '2024-12-07',
    type: 'expense',
    category: '식비',
    amount: 45000,
    description: '저녁 회식',
    isFixed: false,
  },
  {
    id: '7',
    date: '2024-12-10',
    type: 'expense',
    category: '문화/여가',
    amount: 15000,
    description: '영화',
    isFixed: false,
  },
  {
    id: '8',
    date: '2024-12-12',
    type: 'expense',
    category: '쇼핑',
    amount: 89000,
    description: '옷',
    isFixed: false,
  },
  {
    id: '9',
    date: '2024-12-15',
    type: 'income',
    category: '부수입',
    amount: 150000,
    description: '프리랜서 작업',
    isFixed: false,
  },
  {
    id: '10',
    date: '2024-12-17',
    type: 'expense',
    category: '식비',
    amount: 8500,
    description: '커피',
    isFixed: false,
  },
];

export const mockBudgets: Budget[] = [
  { id: '1', category: '식비', amount: 400000, period: 'monthly' },
  { id: '2', category: '교통', amount: 100000, period: 'monthly' },
  { id: '3', category: '문화/여가', amount: 150000, period: 'monthly' },
  { id: '4', category: '쇼핑', amount: 200000, period: 'monthly' },
];

export const mockAccounts: Account[] = [
  { id: '1', name: '신한은행 입출금', type: 'bank', balance: 1500000, color: '#3b82f6' },
  { id: '2', name: '카카오뱅크 저축', type: 'bank', balance: 5000000, color: '#eab308' },
  { id: '3', name: '신용카드', type: 'card', balance: 0, color: '#ef4444' },
  { id: '4', name: '현금', type: 'cash', balance: 50000, color: '#22c55e' },
];