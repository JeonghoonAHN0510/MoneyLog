import type {
  Account,
  Budget,
  Category,
  Fixed,
  Payment,
  Transaction,
  TransactionImportCommitRequest,
  Transfer,
} from '../types/finance';

const toOptionalInteger = (value: string | number | null | undefined) => {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = typeof value === 'number' ? value : Number(value);
  return Number.isInteger(parsed) ? parsed : undefined;
};

const toNumberValue = (value: string | number | null | undefined) => {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
};

export const serializeIdParam = (value: string | number) => {
  const normalized = toOptionalInteger(value);
  return normalized ?? value;
};

export const serializeTransactionPayload = (transaction: Partial<Transaction>) => ({
  ...transaction,
  transactionId: toOptionalInteger(transaction.transactionId),
  categoryId: toOptionalInteger(transaction.categoryId),
  paymentId: toOptionalInteger(transaction.paymentId),
  accountId: toOptionalInteger(transaction.accountId),
  fixedId: toOptionalInteger(transaction.fixedId),
  amount: toNumberValue(transaction.amount),
  installmentCount: toOptionalInteger(transaction.installmentCount),
});

export const serializeFixedPayload = (fixed: Partial<Fixed>) => ({
  ...fixed,
  fixedId: toOptionalInteger(fixed.fixedId),
  categoryId: toOptionalInteger(fixed.categoryId),
  accountId: toOptionalInteger(fixed.accountId),
  amount: toNumberValue(fixed.amount),
  fixedDay: toOptionalInteger(fixed.fixedDay),
});

export const serializeAccountPayload = (account: Partial<Account>) => ({
  ...account,
  accountId: toOptionalInteger(account.accountId),
  bankId: toOptionalInteger(account.bankId),
  balance: toNumberValue(account.balance),
});

export const serializeBudgetPayload = (budget: Partial<Budget>) => ({
  ...budget,
  budgetId: toOptionalInteger(budget.budgetId),
  categoryId: toOptionalInteger(budget.categoryId),
  amount: toNumberValue(budget.amount),
});

export const serializeCategoryPayload = (category: Partial<Category>) => ({
  ...category,
  categoryId: toOptionalInteger(category.categoryId),
});

export const serializePaymentPayload = (payment: Partial<Payment>) => ({
  ...payment,
  paymentId: toOptionalInteger(payment.paymentId),
  accountId: toOptionalInteger(payment.accountId),
});

export const serializeTransferPayload = (transfer: Omit<Transfer, 'transferId' | 'userId' | 'createdAt' | 'updatedAt'>) => ({
  ...transfer,
  fromAccount: toOptionalInteger(transfer.fromAccount),
  toAccount: toOptionalInteger(transfer.toAccount),
  amount: toNumberValue(transfer.amount),
});

export const serializeTransactionImportCommitPayload = (payload: TransactionImportCommitRequest) => ({
  ...payload,
  rows: payload.rows.map((row) => ({
    ...row,
    accountId: toOptionalInteger(row.accountId),
    categoryId: toOptionalInteger(row.categoryId),
    paymentId: toOptionalInteger(row.paymentId),
    amount: toNumberValue(row.amount),
  })),
});
