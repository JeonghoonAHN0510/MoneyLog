export interface Budget {
  budget_id: number;
  user_id: number;
  category_id: number;
  amount: number;
  budget_date: string; // YYYY-MM-DD
  created_at: string;
  updated_at: string;
}

export interface Category {
  category_id: number;
  user_id: number;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  color: string;
  created_at: string;
  updated_at: string;
}

export interface Account {
  account_id: number;
  user_id: number;
  bank_id: number;
  type: 'BANK' | 'CASH' | 'POINT' | 'OTHER';
  nickname: string;
  balance: number;
  account_number: string;
  color: string;
  created_at: string;
  updated_at: string;
}

export interface Ledger {
  ledger_id: number;
  user_id: number;
  category_id: number;
  payment_id: number;
  account_id: number;
  fixed_id: number | null;
  title: string;
  amount: number;
  memo: string;
  trading_at: string; // YYYY-MM-DD
  created_at: string;
  updated_at: string;
}

export interface Payment {
  payment_id: number;
  user_id: number;
  name: string;
  type: 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK';
  created_at: string;
  updated_at: string;
}

export interface Fixed {
  fixed_id: number;
  user_id: number;
  category_id: number;
  title: string;
  amount: number;
  fixed_day: number;
  start_date: string;

  end_date: string;
  created_at: string;
  updated_at: string;
}

export interface Transfer {
  transfer_id: number;
  user_id: number;
  from_account_id: number;
  to_account_id: number;
  amount: number;
  transfer_at: string; // YYYY-MM-DD
  memo: string;
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