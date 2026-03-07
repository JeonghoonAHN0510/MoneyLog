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
  installmentCount?: number;
  flowDirection?: 'INCOME' | 'EXPENSE' | 'UNKNOWN';
  categoryType: 'INCOME' | 'EXPENSE';
  categoryName: string;
  paymentName: string;
  tradingAt: string;
  createdAt: string;
  updatedAt: string;
  installmentPlanId?: string;
  installmentNo?: number;
  installmentTotalCount?: number;
  isInstallment?: boolean;
  isInterestFree?: boolean;
  isSettled?: boolean;
  settledAt?: string;
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
  id: string;
  email: string;
  name: string;
  profileImageUrl?: string | null;
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

export interface TransactionImportReference {
  id: string | number;
  name: string;
  type?: 'INCOME' | 'EXPENSE' | 'ACCOUNT' | 'PAYMENT';
}

export interface TransactionImportUnresolvedIssue {
  rowIndex: number;
  field: string;
  rawValue: string;
  headerColumnLabel: string;
  reasonCode: string;
  valueHint: string;
}

export interface TransactionImportPreviewRow {
  rowIndex: number;
  tradingAt: string;
  title: string;
  amount: number;
  transactionDirection?: 'DEBIT' | 'CREDIT' | 'UNKNOWN';
  memo: string;
  accountName: string;
  categoryName: string;
  paymentName: string;
  resolvedAccountId?: string;
  resolvedCategoryId?: string;
  resolvedPaymentId?: string;
  unresolvedFields: string[];
  errors: string[];
}

export interface TransactionImportSummary {
  totalRows: number;
  resolvedRows: number;
  unresolvedRows: number;
  invalidRows: number;
}

export interface TransactionImportPreviewResponse {
  rows: TransactionImportPreviewRow[];
  summary: TransactionImportSummary;
  unresolvedAccounts: string[];
  unresolvedCategories: string[];
  unresolvedPayments: string[];
  unresolvedIssues: TransactionImportUnresolvedIssue[];
  availableAccounts: TransactionImportReference[];
  availableCategories: TransactionImportReference[];
  availablePayments: TransactionImportReference[];
}

export interface TransactionImportCommitRow {
  rowIndex: number;
  tradingAt: string;
  title: string;
  amount: number;
  memo?: string;
  accountId: string;
  categoryId: string;
  paymentId?: string;
}

export interface TransactionImportCommitRequest {
  rows: TransactionImportCommitRow[];
}

export interface TransactionImportCommitResponse {
  requestedCount: number;
  importedCount: number;
  transactionIds: number[];
}
