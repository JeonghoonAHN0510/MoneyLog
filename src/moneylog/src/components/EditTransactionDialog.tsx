import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Textarea } from './ui/textarea';
import { Transaction, Category, Account, Payment } from '../types/finance';
import useResourceStore from '../stores/resourceStore';

interface EditTransactionDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    transaction: Transaction | null;
    onUpdate: (transaction: Partial<Transaction>) => void;
}

export function EditTransactionDialog({
    open,
    onOpenChange,
    transaction,
    onUpdate
}: EditTransactionDialogProps) {
    const { categories, accounts, payments } = useResourceStore();

    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [categoryId, setCategoryId] = useState('');
    const [amount, setAmount] = useState('');
    const [date, setDate] = useState('');
    const [paymentId, setPaymentId] = useState('');
    const [accountId, setAccountId] = useState('');
    const [description, setDescription] = useState('');
    const [memo, setMemo] = useState('');

    // transaction이 변경되거나 다이얼로그가 열릴 때 폼 값 초기화
    useEffect(() => {
        if (open && transaction) {
            setType(transaction.categoryType);
            setCategoryId(String(transaction.categoryId));
            setAmount(String(transaction.amount));
            setDate(transaction.tradingAt.split('T')[0]);
            setPaymentId(transaction.paymentId ? String(transaction.paymentId) : '');
            setAccountId(transaction.accountId ? String(transaction.accountId) : '');
            setDescription(transaction.title || '');
            setMemo(transaction.memo || '');
        }
    }, [open, transaction]);

    const filteredCategories = categories.filter((cat) => cat.type === type);

    const handlePaymentMethodChange = (val: string) => {
        setPaymentId(val);
        const selectedPayment = payments.find(p => String(p.paymentId) === val);
        if (selectedPayment && selectedPayment.accountId) {
            setAccountId(String(selectedPayment.accountId));
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!categoryId || !amount || !date || !transaction) return;

        onUpdate({
            transactionId: transaction.transactionId,
            categoryType: type,
            categoryId,
            accountId,
            paymentId: paymentId || undefined,
            amount: parseFloat(amount),
            title: description,
            memo,
            tradingAt: date
        });

        onOpenChange(false);
    };

    const handleTypeChange = (newType: 'INCOME' | 'EXPENSE') => {
        setType(newType);
        // 타입이 변경되면 카테고리 초기화
        const matchingCategory = categories.find(
            c => c.type === newType && c.categoryId === categoryId
        );
        if (!matchingCategory) {
            setCategoryId('');
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>거래 내역 수정</DialogTitle>
                    <DialogDescription>거래 내역을 수정합니다.</DialogDescription>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <Label>유형</Label>
                        <div className="flex gap-2">
                            <Button
                                type="button"
                                variant={type === 'EXPENSE' ? 'default' : 'outline'}
                                className="flex-1"
                                onClick={() => handleTypeChange('EXPENSE')}
                            >
                                지출
                            </Button>
                            <Button
                                type="button"
                                variant={type === 'INCOME' ? 'default' : 'outline'}
                                className="flex-1"
                                onClick={() => handleTypeChange('INCOME')}
                            >
                                수입
                            </Button>
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="edit-category">카테고리</Label>
                        <Select value={categoryId} onValueChange={setCategoryId}>
                            <SelectTrigger id="edit-category">
                                <SelectValue placeholder="카테고리 선택" />
                            </SelectTrigger>
                            <SelectContent>
                                {filteredCategories.map((cat) => (
                                    <SelectItem key={cat.categoryId} value={String(cat.categoryId)}>
                                        {cat.name}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    {type === 'EXPENSE' && (
                        <div className="space-y-2">
                            <Label htmlFor="edit-payment-method">결제수단</Label>
                            <Select value={paymentId} onValueChange={handlePaymentMethodChange}>
                                <SelectTrigger id="edit-payment-method">
                                    <SelectValue placeholder="결제수단 선택" />
                                </SelectTrigger>
                                <SelectContent>
                                    {payments.map((payment) => (
                                        <SelectItem key={payment.paymentId} value={String(payment.paymentId)}>
                                            {payment.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    <div className="space-y-2">
                        <Label htmlFor="edit-account">계좌</Label>
                        <Select value={accountId} onValueChange={setAccountId}>
                            <SelectTrigger id="edit-account">
                                <SelectValue placeholder="계좌 선택" />
                            </SelectTrigger>
                            <SelectContent>
                                {accounts.map((acc) => (
                                    <SelectItem key={acc.accountId} value={String(acc.accountId)}>
                                        {acc.nickname} ({acc.bankName})
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="edit-description">지출명</Label>
                        <Input
                            id="edit-description"
                            placeholder="예: 점심 식사"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="edit-amount">금액</Label>
                        <Input
                            id="edit-amount"
                            type="number"
                            placeholder="0"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="edit-memo">메모</Label>
                        <Textarea
                            id="edit-memo"
                            placeholder="메모를 입력하세요"
                            value={memo}
                            onChange={(e) => setMemo(e.target.value)}
                            rows={3}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="edit-date">거래일자</Label>
                        <Input
                            id="edit-date"
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                            required
                        />
                    </div>

                    <div className="flex gap-2 pt-4">
                        <Button type="button" variant="outline" className="flex-1" onClick={() => onOpenChange(false)}>
                            취소
                        </Button>
                        <Button type="submit" className="flex-1">
                            수정
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
}
