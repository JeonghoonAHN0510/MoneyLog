import { useState, useEffect, useRef, type ChangeEvent } from 'react';
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
import '../styles/components/TransactionImportDialog.css';

interface TransactionImportDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function TransactionImportDialog ({ open, onOpenChange }: TransactionImportDialogProps) {
    const importFileInputRef = useRef<HTMLInputElement | null>(null);
    const [importPreview, setImportPreview] = useState<TransactionImportPreviewResponse | null>(null);
    const [importRows, setImportRows] = useState<TransactionImportCommitRequest['rows']>([]);

    const importPreviewMut = useTransactionImportPreview();
    const importCommitMut = useTransactionImportCommit();

    const clearImportState = () => {
        setImportPreview(null);
        setImportRows([]);
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
        installmentCount: row.installmentCount,
        isInterestFree: row.isInterestFree,
        accountId: row.resolvedAccountId != null ? String(row.resolvedAccountId) : '',
        categoryId: row.resolvedCategoryId != null ? String(row.resolvedCategoryId) : '',
        paymentId: row.resolvedPaymentId != null ? String(row.resolvedPaymentId) : '',
    });

    const updateImportRow = (rowIndex: number, field: 'accountId' | 'categoryId' | 'paymentId', value: string) => {
        setImportRows((prev) => prev.map((row) => (row.rowIndex === rowIndex ? { ...row, [field]: value } : row)));
    };

    const getPreviewRow = (rowIndex: number) => importPreview?.rows.find((previewRow) => previewRow.rowIndex === rowIndex);

    const isImportRowReady = (commitRow: TransactionImportCommitRequest['rows'][number]) => {
        if (!importPreview) {
            return false;
        }

        const previewRow = getPreviewRow(commitRow.rowIndex);
        if (!previewRow || previewRow.errors.length > 0) {
            return false;
        }
        if (!commitRow.accountId || !commitRow.categoryId) {
            return false;
        }
        const selectedCategory = importPreview.availableCategories.find((category) => category.id === commitRow.categoryId);
        if (!selectedCategory || selectedCategory.type === 'EXPENSE') {
            if (!commitRow.paymentId) {
                return false;
            }
        }
        return true;
    };

    const unresolvedImportRows = importRows.filter((row) => !isImportRowReady(row));

    const canCommitImport = Boolean(
        importPreview &&
            importRows.length > 0 &&
            unresolvedImportRows.length === 0 &&
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
            .filter(isImportRowReady)
            .map((row) => ({
                ...row,
                accountId: String(row.accountId),
                categoryId: String(row.categoryId),
                paymentId: row.paymentId ? String(row.paymentId) : undefined,
            }));

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
                            const selectedCategory = importPreview.availableCategories.find((category) => category.id === commitRow.categoryId);
                            const needPayment = !selectedCategory || selectedCategory.type === 'EXPENSE';

                            return (
                                <div key={`import-preview-${previewRow.rowIndex}`} className="finance-import-card">
                                    <div className="finance-import-main">
                                        <span>{previewRow.rowIndex}행</span>
                                        <span>{previewRow.tradingAt}</span>
                                        <span>{previewRow.title}</span>
                                        <span>
                                            {typeof previewRow.amount === 'number'
                                                ? `${previewRow.amount.toLocaleString()}원`
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
                                                        <SelectItem key={account.id} value={account.id}>
                                                            {account.name}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        </label>
                                        <label>
                                            카테고리
                                            <Select
                                                value={commitRow.categoryId}
                                                onValueChange={(value) => updateImportRow(previewRow.rowIndex, 'categoryId', value)}
                                            >
                                                <SelectTrigger className="w-full">
                                                    <SelectValue placeholder="카테고리 선택" />
                                                </SelectTrigger>
                                                <SelectContent>
                                                    {importPreview.availableCategories.map((category) => (
                                                        <SelectItem key={category.id} value={category.id}>
                                                            {category.name} ({category.type})
                                                        </SelectItem>
                                                    ))}
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
                                                            <SelectItem key={payment.id} value={payment.id}>
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
            </DialogContent>
        </Dialog>
    );
}
