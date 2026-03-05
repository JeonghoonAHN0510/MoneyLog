import { useState, useEffect, useMemo, useRef, type ChangeEvent } from 'react';
import { toast } from 'sonner';
import { Button } from './ui/button';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle
} from './ui/dialog';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from './ui/select';
import {
    useTransactionImportPreview,
    useTransactionImportCommit,
} from '../api/queries';
import { getApiErrorMessage } from '../utils/error';
import {
    TransactionImportPreviewResponse,
    TransactionImportCommitRequest,
} from '../types/finance';
import {
    normalizeDirectionType,
    toCommitPayloadRow,
    toIdString,
    validateImportRow,
    type TransactionDirectionType,
} from '../utils/transactionImportValidation';
import '../styles/components/TransactionImportDialog.css';

interface TransactionImportDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

const getDirectionFilteredCategories = (
    directionType: TransactionDirectionType,
    categories: TransactionImportPreviewResponse['availableCategories']
): TransactionImportPreviewResponse['availableCategories'] => {
    if (directionType === 'INCOME' || directionType === 'EXPENSE') {
        return categories.filter((category) => category.type === directionType);
    }
    return categories;
};

const directionAmountLabel = (directionType: TransactionDirectionType) => {
    if (directionType === 'INCOME') {
        return '입금';
    }
    if (directionType === 'EXPENSE') {
        return '출금';
    }
    return '금액';
};

const directionAmountClass = (directionType: TransactionDirectionType) => {
    if (directionType === 'INCOME') {
        return 'text-green-600';
    }
    if (directionType === 'EXPENSE') {
        return 'text-red-600';
    }
    return 'text-foreground';
};

const sanitizeCategoryName = (categoryName: string) => categoryName
    .replace(/^\s*(수입|지출|입금|출금)\s*\/\s*/u, '')
    .trim();

const normalizeAmountLikeText = (value: string) => value
    .replace(/,/g, '')
    .replace(/\s+/g, '')
    .trim();

const isMoneyLikeValue = (value: string) => {
    if (!value) {
        return false;
    }
    const normalized = normalizeAmountLikeText(value);
    return /^[-+]?\d{1,15}(\.\d+)?$/.test(normalized);
};

const isZeroLikeAmountValue = (value: string) => {
    if (!value) {
        return false;
    }
    const normalized = normalizeAmountLikeText(value).replace(/[^0-9.+-]/gu, '');
    if (!normalized) {
        return false;
    }
    if (!/^[-+]?\d+(\.\d+)?$/.test(normalized)) {
        return false;
    }
    return Number(normalized) === 0;
};

export function TransactionImportDialog ({ open, onOpenChange }: TransactionImportDialogProps) {
    const importFileInputRef = useRef<HTMLInputElement | null>(null);
    const [importPreview, setImportPreview] = useState<TransactionImportPreviewResponse | null>(null);
    const [importRows, setImportRows] = useState<TransactionImportCommitRequest['rows']>([]);
    const [showUnresolvedDetail, setShowUnresolvedDetail] = useState<boolean>(false);
    const ISSUE_SUMMARY_LIMIT = 3;

    const importPreviewMut = useTransactionImportPreview();
    const importCommitMut = useTransactionImportCommit();

    const clearImportState = () => {
        setImportPreview(null);
        setImportRows([]);
        setShowUnresolvedDetail(false);
        if (importFileInputRef.current) {
            importFileInputRef.current.value = '';
        }
    };

    useEffect(() => {
        if (!open) {
            clearImportState();
        }
    }, [open]);

    const buildDefaultCommitRow = (row: TransactionImportPreviewResponse['rows'][number]): TransactionImportCommitRequest['rows'][number] => ({
        rowIndex: row.rowIndex,
        tradingAt: row.tradingAt,
        title: row.title,
        amount: row.amount,
        memo: row.memo || '',
        accountId: row.resolvedAccountId != null ? String(row.resolvedAccountId) : '',
        categoryId: row.resolvedCategoryId != null ? String(row.resolvedCategoryId) : '',
        paymentId: row.resolvedPaymentId != null ? String(row.resolvedPaymentId) : '',
    });

    const updateImportRow = (rowIndex: number, field: 'accountId' | 'categoryId' | 'paymentId', value: string) => {
        setImportRows((prev) => prev.map((row) => (row.rowIndex === rowIndex ? { ...row, [field]: value } : row)));
    };

    const updateImportCategory = (rowIndex: number, categoryId: string) => {
        if (!importPreview) {
            return;
        }
        const selectedCategory = importPreview.availableCategories.find((category) => toIdString(category.id) === categoryId);
        setImportRows((prev) => prev.map((row) => {
            if (row.rowIndex !== rowIndex) {
                return row;
            }
            if (selectedCategory?.type === 'INCOME') {
                return { ...row, categoryId, paymentId: '' };
            }
            return { ...row, categoryId };
        }));
    };

    const previewRowsByIndex = useMemo(() => {
        const rowsByIndex = new Map<number, TransactionImportPreviewResponse['rows'][number]>();
        if (!importPreview) {
            return rowsByIndex;
        }
        importPreview.rows.forEach((row) => {
            rowsByIndex.set(row.rowIndex, row);
        });
        return rowsByIndex;
    }, [importPreview]);

    const categoriesById = useMemo(() => {
        const map = new Map<string, TransactionImportPreviewResponse['availableCategories'][number]>();
        if (!importPreview) {
            return map;
        }
        importPreview.availableCategories.forEach((category) => {
            map.set(toIdString(category.id), category);
        });
        return map;
    }, [importPreview]);

    const validationResults = useMemo(
        () => importRows.map((commitRow) => validateImportRow({
            importPreview,
            commitRow,
            previewRow: previewRowsByIndex.get(commitRow.rowIndex),
            categoryById: categoriesById,
        })),
        [importRows, importPreview, previewRowsByIndex, categoriesById]
    );

    const validationByRowIndex = useMemo(
        () => new Map(validationResults.map((result) => [result.rowIndex, result])),
        [validationResults]
    );

    const unresolvedRowIndexSet = useMemo(
        () => new Set(validationResults.filter((result) => !result.isReady).map((result) => result.rowIndex)),
        [validationResults]
    );

    const unresolvedImportRows = importRows.filter((row) => unresolvedRowIndexSet.has(row.rowIndex));
    const unresolvedIssues = importPreview?.unresolvedIssues ?? [];
    const issueFieldLabels: Record<string, string> = {
        accountName: '계좌',
        categoryName: '카테고리',
        paymentName: '결제수단',
    };
    const issueReasonLabels: Record<string, string> = {
        MISSING: '미입력',
        NOT_FOUND: '미매칭',
        DUPLICATE: '중복',
        MISALIGNED_LIKELY: '열 오정렬 의심',
    };
    const issueHintLabels: Record<string, string> = {
        MONEY_LIKE: '금액형 값',
        NAME_LIKE: '이름형 값',
        UNKNOWN: '미확인 값',
    };
    const unresolvedIssueCountsByReason = unresolvedIssues.reduce<Record<string, number>>((acc, issue) => {
        acc[issue.reasonCode] = (acc[issue.reasonCode] ?? 0) + 1;
        return acc;
    }, {});
    const unresolvedIssueCountsByField = unresolvedIssues.reduce<Record<string, number>>((acc, issue) => {
        const label = issueFieldLabels[issue.field] ?? issue.field;
        acc[label] = (acc[label] ?? 0) + 1;
        return acc;
    }, {});
    const issueSummaryByGroup = unresolvedIssues.reduce<Map<string, { count: number; field: string; reasonCode: string; valueHint: string; raw: string }>>((acc, issue) => {
        const raw = issue.rawValue.trim() === '' ? '<공백>' : issue.rawValue;
        const key = JSON.stringify({
            field: issue.field,
            reasonCode: issue.reasonCode,
            valueHint: issue.valueHint,
            raw,
        });

        const previous = acc.get(key);
        if (previous) {
            previous.count += 1;
        } else {
            acc.set(key, {
                count: 1,
                field: issue.field,
                reasonCode: issue.reasonCode,
                valueHint: issue.valueHint,
                raw,
            });
        }
        return acc;
    }, new Map<string, { count: number; field: string; reasonCode: string; valueHint: string; raw: string }>());
    const sortedIssueSummaryEntries = [...issueSummaryByGroup.values()]
        .sort((a, b) => b.count - a.count || a.raw.localeCompare(b.raw))
        .slice(0, ISSUE_SUMMARY_LIMIT);
    const canCommitImport = Boolean(
        importPreview &&
            importRows.length > 0 &&
            validationResults.every((result) => result.isReady) &&
            importPreview.summary.invalidRows === 0 &&
            !importCommitMut.isPending
    );

    const startImportPreview = async (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) {
            return;
        }

        clearImportState();
        const fileName = file.name.toLowerCase();
        if (!fileName.endsWith('.csv') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
            toast.error('CSV 또는 XLSX/XLS 파일만 업로드 가능합니다.');
            event.target.value = '';
            return;
        }

        try {
            const preview = await importPreviewMut.mutateAsync(file);
            setImportPreview(preview);
            setImportRows(preview.rows.map(buildDefaultCommitRow));

            if (preview.summary.invalidRows > 0) {
                toast.error('CSV/Excel 업로드 데이터에 오류가 있어 확인이 필요합니다.');
            } else if (preview.summary.unresolvedRows > 0) {
                toast.warning('미해결 항목이 있어 매핑이 필요합니다.');
            } else {
                toast.success('미리보기 완료. 바로 업로드할 수 있습니다.');
            }
        } catch (e) {
            toast.error(getApiErrorMessage(e, '거래 업로드 미리보기에 실패했습니다.'));
        } finally {
            event.target.value = '';
        }
    };

    const commitImport = async () => {
        if (!importPreview || importRows.length === 0) {
            return;
        }
        if (importPreview.summary.invalidRows > 0) {
            toast.error('오류가 있는 행이 있어 업로드할 수 없습니다.');
            return;
        }

        if (unresolvedImportRows.length > 0) {
            toast.error('미해결 항목을 먼저 매핑해야 업로드할 수 있습니다.');
            return;
        }

        const payloadRows = importRows
            .filter((_, index) => validationResults[index]?.isReady)
            .map((row) => toCommitPayloadRow(row));

        if (payloadRows.length !== importRows.length) {
            toast.error('미해결 항목을 먼저 매핑해야 업로드할 수 있습니다.');
            return;
        }

        try {
            const result = await importCommitMut.mutateAsync({ rows: payloadRows });
            toast.success(`${result.importedCount}건의 거래가 업로드되었습니다.`);
            clearImportState();
            onOpenChange(false);
        } catch (e) {
            toast.error(getApiErrorMessage(e, '거래 업로드 반영에 실패했습니다.'));
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="import-dialog-content">
                <DialogHeader>
                    <DialogTitle>거래 업로드 미리보기</DialogTitle>
                    <DialogDescription>
                        CSV/Excel 파일 업로드 후 계정·카테고리·결제수단 매핑을 완료하면 업로드가 가능합니다.
                    </DialogDescription>
                </DialogHeader>

                <input
                    ref={importFileInputRef}
                    type="file"
                    accept=".csv,.xlsx,.xls"
                    className="hidden"
                    onChange={startImportPreview}
                />

                <div className="finance-import-row">
                    <Button size="sm" variant="outline" onClick={() => importFileInputRef.current?.click()}>
                        파일 선택
                    </Button>
                    <Button
                        size="sm"
                        variant="default"
                        onClick={commitImport}
                        disabled={!canCommitImport || importCommitMut.isPending || importPreviewMut.isPending}
                    >
                        선택 항목 업로드
                    </Button>
                    <Button size="sm" variant="ghost" onClick={clearImportState} disabled={!importPreview}>
                        미리보기 초기화
                    </Button>
                </div>

                {importPreview && (
                    <div className="finance-import-summary">
                        <p>
                            총 {importPreview.summary.totalRows}건 ·
                            준비 완료 {importPreview.summary.resolvedRows}건 ·
                            미해결 {unresolvedImportRows.length}건 ·
                            오류 {importPreview.summary.invalidRows}건
                        </p>
                        {unresolvedImportRows.length > 0 && (
                            <p className="finance-import-warning">
                                현재 미리보기 결과에서 매핑되지 않은 항목이 {unresolvedImportRows.length}건 남았습니다.
                            </p>
                        )}
                        {(importPreview.unresolvedAccounts.length > 0 || importPreview.unresolvedCategories.length > 0 || importPreview.unresolvedPayments.length > 0) && (
                            <div className="finance-import-unresolved">
                                {importPreview.unresolvedAccounts.length > 0 && (
                                    <div>미해결 계정: [{importPreview.unresolvedAccounts.join(', ')}]</div>
                                )}
                                {importPreview.unresolvedCategories.length > 0 && (
                                    <div>미해결 카테고리: [{importPreview.unresolvedCategories.join(', ')}]</div>
                                )}
                                {importPreview.unresolvedPayments.length > 0 && (
                                    <div>미해결 결제수단: [{importPreview.unresolvedPayments.join(', ')}]</div>
                                )}
                                {importPreview.unresolvedIssues.length > 0 && (
                                    <div className="finance-import-unresolved-inline">
                                        <div className="finance-import-unresolved-meta">
                                            미해결 항목: {unresolvedIssues.length}건
                                            {' / '}
                                            {Object.entries(unresolvedIssueCountsByField).map(([field, count]) => `${field} ${count}건`).join(', ')}
                                        </div>
                                        <div className="finance-import-unresolved-meta">
                                            {Object.entries(unresolvedIssueCountsByReason).map(([reason, count]) => `${issueReasonLabels[reason] ?? reason} ${count}건`).join(', ')}
                                        </div>
                                        <button
                                            type="button"
                                            className="finance-import-unresolved-toggle"
                                            onClick={() => setShowUnresolvedDetail(true)}
                                        >
                                            미해결 상세 보기
                                        </button>
                                        <div className="finance-import-unresolved-summary">
                                            {sortedIssueSummaryEntries.map((entry) => {
                                                const field = issueFieldLabels[entry.field] ?? entry.field;
                                                const reason = issueReasonLabels[entry.reasonCode] ?? entry.reasonCode;
                                                const hint = issueHintLabels[entry.valueHint] ?? entry.valueHint;
                                                return (
                                                    <div
                                                        key={`${entry.field}-${entry.reasonCode}-${entry.valueHint}-${entry.raw}`}
                                                        className="finance-import-unresolved-summary-item"
                                                    >
                                                        {`${field} / ${entry.raw} / ${reason} (${hint}) × ${entry.count}`}
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}

                {importPreview && importPreview.rows.length > 0 && (
                    <div className="finance-import-rows">
                        {importPreview.rows.map((previewRow) => {
                            const commitRow = importRows.find((row) => row.rowIndex === previewRow.rowIndex);
                            if (!commitRow) {
                                return null;
                            }
                            const categoryLabel = sanitizeCategoryName(previewRow.categoryName);
                            const isCategoryLikeAmount = isMoneyLikeValue(previewRow.categoryName)
                                && typeof previewRow.amount === 'number'
                                && normalizeAmountLikeText(previewRow.categoryName) === String(previewRow.amount);
                            const isZeroCategory = isZeroLikeAmountValue(previewRow.categoryName);
                            const safeCategoryLabel = isCategoryLikeAmount || isZeroCategory ? '' : categoryLabel;
                            const selectedCategory = categoriesById.get(commitRow.categoryId);
                            const validation = validationByRowIndex.get(previewRow.rowIndex);
                            const resolvedDirection = validation?.resolvedDirection
                                ?? normalizeDirectionType(previewRow.transactionDirection, selectedCategory?.type);
                            const needPayment = resolvedDirection === 'EXPENSE';
                            const directionedCategories = getDirectionFilteredCategories(resolvedDirection, importPreview.availableCategories);
                            const previewDirectionLabel = directionAmountLabel(resolvedDirection);
                            const previewDirectionClass = directionAmountClass(resolvedDirection);
                            const isCategoryMismatch = validation?.reason === 'DIRECTION_MISMATCH';

                            return (
                                <div key={`import-preview-${previewRow.rowIndex}`} className="finance-import-card">
                                    <div className="finance-import-main">
                                        <span>{previewRow.rowIndex}행</span>
                                        <span>{previewRow.tradingAt}</span>
                                        <span>{previewRow.title}</span>
                                        <span>{safeCategoryLabel}</span>
                                        <span className={previewDirectionClass}>
                                            {typeof previewRow.amount === 'number'
                                                ? `${previewDirectionLabel} ${previewRow.amount.toLocaleString()}원`
                                                : '-'}
                                        </span>
                                    </div>
                                    <div className="finance-import-fields">
                                        <label>
                                            계좌
                                            <Select
                                                value={commitRow.accountId}
                                                onValueChange={(value) => updateImportRow(previewRow.rowIndex, 'accountId', value)}
                                            >
                                                <SelectTrigger className="w-full">
                                                    <SelectValue placeholder="계좌 선택" />
                                                </SelectTrigger>
                                                <SelectContent>
                                                    {importPreview.availableAccounts.map((account) => (
                                                        <SelectItem key={account.id} value={toIdString(account.id)}>
                                                            {account.name}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        </label>
                                        <label>
                                            카테고리
                                            {isCategoryMismatch && (
                                                <span className="text-[11px] text-amber-600 block">방향에 맞지 않는 카테고리라 재선택이 필요합니다.</span>
                                            )}
                                            <Select
                                                value={commitRow.categoryId}
                                                onValueChange={(value) => updateImportCategory(previewRow.rowIndex, value)}
                                            >
                                                <SelectTrigger className="w-full">
                                                    <SelectValue placeholder="카테고리 선택" />
                                                </SelectTrigger>
                                                <SelectContent>
                                                    {directionedCategories.length === 0 ? (
                                                        <SelectItem value="__EMPTY__" disabled>
                                                            해당 방향의 카테고리가 없습니다
                                                        </SelectItem>
                                                    ) : (
                                                        directionedCategories.map((category) => (
                                                            <SelectItem key={category.id} value={toIdString(category.id)}>
                                                                {category.name} ({category.type})
                                                            </SelectItem>
                                                        ))
                                                    )}
                                                </SelectContent>
                                            </Select>
                                        </label>
                                        {needPayment && (
                                            <label>
                                                결제수단
                                                <Select
                                                    value={commitRow.paymentId || ''}
                                                    onValueChange={(value) => updateImportRow(previewRow.rowIndex, 'paymentId', value)}
                                                >
                                                    <SelectTrigger className="w-full">
                                                        <SelectValue placeholder="결제수단 선택" />
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        {importPreview.availablePayments.map((payment) => (
                                                            <SelectItem key={payment.id} value={toIdString(payment.id)}>
                                                                {payment.name}
                                                            </SelectItem>
                                                        ))}
                                                    </SelectContent>
                                                </Select>
                                            </label>
                                        )}
                                    </div>
                                    {previewRow.errors.length > 0 && (
                                        <div className="finance-import-error">
                                            {previewRow.errors.join(' / ')}
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                )}

                <Dialog open={showUnresolvedDetail} onOpenChange={setShowUnresolvedDetail}>
                    <DialogContent className="finance-import-unresolved-dialog">
                        <DialogHeader>
                            <DialogTitle>미해결 상세 목록</DialogTitle>
                            <DialogDescription>매핑 실패/누락된 항목의 행 단위 상세 정보입니다.</DialogDescription>
                        </DialogHeader>
                        <div className="finance-import-unresolved-table-wrapper">
                            <ul className="finance-import-unresolved-table">
                                <li className="finance-import-unresolved-table-row finance-import-unresolved-table-header">
                                    <span>행</span>
                                    <span>필드</span>
                                    <span>추출열</span>
                                    <span>값</span>
                                    <span>사유</span>
                                    <span>값성격</span>
                                </li>
                                {importPreview?.unresolvedIssues.map((issue, index) => {
                                    const reason = issueReasonLabels[issue.reasonCode] ?? issue.reasonCode;
                                    const hint = issueHintLabels[issue.valueHint] ?? issue.valueHint;
                                    const value = issue.rawValue.trim() === '' ? '<공백>' : issue.rawValue;
                                    return (
                                        <li key={`${issue.rowIndex}-${issue.field}-${index}`} className="finance-import-unresolved-table-row">
                                            <span>{issue.rowIndex}</span>
                                            <span>{issueFieldLabels[issue.field] ?? issue.field}</span>
                                            <span>{issue.headerColumnLabel}</span>
                                            <span title={value} className="finance-import-unresolved-value">
                                                {value}
                                            </span>
                                            <span>{reason}</span>
                                            <span>{hint}</span>
                                        </li>
                                    );
                                })}
                            </ul>
                        </div>
                        <div className="finance-import-unresolved-dialog-footer">
                            <Button size="sm" variant="secondary" onClick={() => setShowUnresolvedDetail(false)}>
                                닫기
                            </Button>
                        </div>
                    </DialogContent>
                </Dialog>
            </DialogContent>
        </Dialog>
    );
}
