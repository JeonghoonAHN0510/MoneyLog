export interface Budget {
  budgetId: string;
  categoryId: string;
  userId: string;
  amount: number;
  budgetDate: string;
  createdAt: string;
  updatedAt: string;
  categoryName: string;
}

export interface Category {
  categoryId: string;
  userId: string;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  color: string;
  createdAt: string;
  updatedAt: string;
}

export interface Account {
  accountId: string;
  userId: string;
  bankId?: string;
  type: 'BANK' | 'CASH' | 'POINT' | 'OTHER';
  nickname: string;
  balance: number;
  accountNumber?: string;
  color: string;
  bankName: string;
  createdAt: string;
  updatedAt: string;
}

export interface Transaction {
  transactionId: string;
  userId: string;
  categoryId: string;
  paymentId: string;
  accountId: string;
  fixedId: string;
  title: string;
  amount: number;
  memo: string;
  categoryType: 'INCOME' | 'EXPENSE';
  categoryName: string;
  paymentName: string;
  tradingAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface Payment {
  paymentId: string;
  userId: string;
  accountId?: string;
  name: string;
  type: 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK';
  createdAt: string;
  updatedAt: string;
}

export interface Fixed {
  fixedId: string;
  userId: string;
  categoryId: string;
  accountId: string;
  title: string;
  amount: number;
  fixedDay: number;
  startDate: string;
  endDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface Transfer {
  transferId: string;
  userId: string;
  fromAccount: string;
  toAccount: string;
  amount: number;
  transferAt: string;
  memo?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Bank {
  bankId: string;
  name: string;
  code: string;
  logoImageUrl: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserInfo {
  userId: number;
  email: string;
  name: string;
}

export interface DailySummary {
  date: string;
  totalIncome: number;
  totalExpense: number;
}

export interface CategoryStats {
  categoryName: string;
  totalAmount: number;
  ratio: number;
}

export interface DashboardData {
  totalIncome: number;
  totalExpense: number;
  totalBalance: number;
  categoryStats: CategoryStats[];
}