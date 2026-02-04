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

export interface Ledger {
  ledgerId: string;
  userId: string;
  categoryId: string;
  paymentId: string;
  accountId: string;
  fixedId: string;
  title: string;
  amount: number;
  memo: string;
  categoryType: string;
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