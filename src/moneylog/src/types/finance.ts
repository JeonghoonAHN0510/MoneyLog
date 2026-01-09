export interface Transaction {
  id: string;
  date: string; // ISO date string
  type: 'income' | 'expense';
  category: string;
  amount: number;
  description: string;
  isFixed: boolean;
  accountId?: string; // Added account selection
  // Fixed transaction fields
  fixedDay?: number; // Day of month for fixed transactions (1-31)
  startDate?: string; // Start date for fixed transactions
  endDate?: string; // End date for fixed transactions
  paymentMethod?: string; // Payment method for general transactions
  memo?: string; // Memo field
}

export interface Budget {
  id: string;
  category: string;
  amount: number;
  period: 'monthly' | 'yearly';
}

export interface Category {
  id: string;
  name: string;
  type: 'income' | 'expense';
  color: string;
}

export interface Account {
  account_id: number,
  user_id: number,
  bank_id: number,
  nickname: string,
  balance: number,
  account_number: string;
}

export interface Transfer {
  id: string;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  date: string;
  memo?: string;
}