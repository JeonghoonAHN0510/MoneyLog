export interface Budget {
  id: string;
  category: string;
  amount: string;
  period: 'monthly' | 'yearly';
  created_at: string;
  updated_at: string;
}

export interface Category {
  category_id: string;
  user_id: string;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  color: string;
  created_at: string;
  updated_at: string;
}

export interface Account {
  account_id: string;
  user_id: string;
  bank_id?: string;
  type: 'BANK' | 'CASH' | 'POINT' | 'OTHER';
  nickname: string;
  balance: number;
  account_number?: string;
  color: string;
  created_at: string;
  updated_at: string;
}

export interface Ledger {
  ledger_id: string;
  user_id: string;
  category_id: string;
  payment_id: string;
  account_id: string;
  fixed_id: string;
  title: string;
  amount: string;
  memo: string;
  trading_at: string;
  created_at: string;
  updated_at: string;
}

export interface Payment {
  payment_id: string;
  user_id: string;
  account_id?: string;
  name: string;
  type: 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK';
  created_at: string;
  updated_at: string;
}

export interface Fixed {
  fixed_id: string;
  user_id: string;
  category_id: string;
  title: string;
  amount: string;
  fixed_day: string;
  start_date: string;
  end_date: string;
  created_at: string;
  updated_at: string;
}

export interface Transfer {
  transfer_id: string;
  user_id: string;
  from_account: string;
  to_account: string;
  amount: number;
  transfer_at: string;
  memo?: string;
  created_at: string;
  updated_at: string;
}

export interface Bank {
  bank_id: string;
  name: string;
  code: string;
  logo_image_url: string;
  created_at: string;
  updated_at: string;
}