import {
  TransactionImportCommitRequest,
  TransactionImportPreviewResponse,
} from '../types/finance';

type CommitRow = TransactionImportCommitRequest['rows'][number];
type PreviewRow = TransactionImportPreviewResponse['rows'][number];
type Category = TransactionImportPreviewResponse['availableCategories'][number];

export type TransactionDirectionType = 'INCOME' | 'EXPENSE' | 'UNKNOWN';

export type ImportRowValidationReason =
  | 'NO_PREVIEW'
  | 'PREVIEW_ERROR'
  | 'MISSING_REQUIRED'
  | 'CATEGORY_NOT_FOUND'
  | 'DIRECTION_MISMATCH'
  | 'MISSING_PAYMENT'
  | 'READY';

export interface ImportRowValidationResult {
  rowIndex: number;
  isReady: boolean;
  reason: ImportRowValidationReason;
  resolvedDirection: TransactionDirectionType;
  selectedCategoryType?: Category['type'];
}

export const toIdString = (value: string | number | null | undefined) => {
  if (value == null) {
    return '';
  }
  return String(value);
};

export const normalizeDirectionType = (
  previewDirection?: PreviewRow['transactionDirection'],
  categoryType?: Category['type']
): TransactionDirectionType => {
  if (previewDirection === 'CREDIT') {
    return 'INCOME';
  }
  if (previewDirection === 'DEBIT') {
    return 'EXPENSE';
  }
  if (categoryType === 'INCOME' || categoryType === 'EXPENSE') {
    return categoryType;
  }
  return 'UNKNOWN';
};

interface ValidateImportRowParams {
  importPreview: TransactionImportPreviewResponse | null;
  commitRow: CommitRow;
  previewRow?: PreviewRow;
  categoryById?: Map<string, Category>;
}

export const validateImportRow = ({
  importPreview,
  commitRow,
  previewRow,
  categoryById,
}: ValidateImportRowParams): ImportRowValidationResult => {
  if (!importPreview) {
    return {
      rowIndex: commitRow.rowIndex,
      isReady: false,
      reason: 'NO_PREVIEW',
      resolvedDirection: 'UNKNOWN',
    };
  }

  const resolvedPreviewRow =
    previewRow ??
    importPreview.rows.find((row) => row.rowIndex === commitRow.rowIndex);
  if (!resolvedPreviewRow || resolvedPreviewRow.errors.length > 0) {
    return {
      rowIndex: commitRow.rowIndex,
      isReady: false,
      reason: 'PREVIEW_ERROR',
      resolvedDirection: 'UNKNOWN',
    };
  }

  if (!commitRow.accountId || !commitRow.categoryId) {
    return {
      rowIndex: commitRow.rowIndex,
      isReady: false,
      reason: 'MISSING_REQUIRED',
      resolvedDirection: 'UNKNOWN',
    };
  }

  const selectedCategory =
    categoryById?.get(commitRow.categoryId) ??
    importPreview.availableCategories.find(
      (category) => toIdString(category.id) === commitRow.categoryId
    );

  if (!selectedCategory) {
    return {
      rowIndex: commitRow.rowIndex,
      isReady: false,
      reason: 'CATEGORY_NOT_FOUND',
      resolvedDirection: 'UNKNOWN',
    };
  }

  const rowDirectionType = normalizeDirectionType(
    resolvedPreviewRow.transactionDirection,
    selectedCategory.type
  );

  if (
    rowDirectionType !== 'UNKNOWN' &&
    selectedCategory.type !== rowDirectionType
  ) {
    return {
      rowIndex: commitRow.rowIndex,
      isReady: false,
      reason: 'DIRECTION_MISMATCH',
      resolvedDirection: rowDirectionType,
      selectedCategoryType: selectedCategory.type,
    };
  }

  if (rowDirectionType === 'EXPENSE' && !commitRow.paymentId) {
    return {
      rowIndex: commitRow.rowIndex,
      isReady: false,
      reason: 'MISSING_PAYMENT',
      resolvedDirection: rowDirectionType,
      selectedCategoryType: selectedCategory.type,
    };
  }

  return {
    rowIndex: commitRow.rowIndex,
    isReady: true,
    reason: 'READY',
    resolvedDirection: rowDirectionType,
    selectedCategoryType: selectedCategory.type,
  };
};

export const toCommitPayloadRow = (row: CommitRow): CommitRow => ({
  ...row,
  accountId: String(row.accountId),
  categoryId: String(row.categoryId),
  paymentId: row.paymentId ? String(row.paymentId) : undefined,
});
