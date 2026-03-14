import { Calendar, ChartBar, List, Target, Upload, Wallet } from 'lucide-react';

export const financeSections = [
  { value: 'dashboard', label: '대시보드', description: '월간 자산 브리핑', icon: ChartBar },
  { value: 'calendar', label: '캘린더', description: '날짜별 현금흐름', icon: Calendar },
  { value: 'transactions', label: '거래', description: '전체 거래와 업로드', icon: Upload },
  { value: 'accounts', label: '계좌', description: '계좌 포트폴리오', icon: Wallet },
  { value: 'categories', label: '카테고리', description: '분류와 결제수단', icon: List },
  { value: 'budget', label: '예산', description: '예산 사용률 추적', icon: Target },
] as const;

export type FinanceSection = (typeof financeSections)[number]['value'];
