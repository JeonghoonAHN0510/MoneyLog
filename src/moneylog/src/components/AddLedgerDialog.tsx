import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Textarea } from './ui/textarea';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Ledger, Category, Account, Payment, Fixed } from '../types/finance';
import useResourceStore from '../stores/resourceStore';

interface AddLedgerDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onAddLedger: (ledger: Partial<Ledger>) => void;
    onAddFixed: (ledger: Partial<Ledger>) => void;
}

// =========================================================
// 1. 일반 거래 폼 (GeneralTransactionForm)
// =========================================================
interface GeneralFormProps {
    categories: Category[];
    accounts: Account[];
    payments: Payment[];
    onLedgerSubmit: (data: any) => void;
    onCancel: () => void;
}

const GeneralTransactionForm = ({ categories, accounts, payments, onLedgerSubmit, onCancel }: GeneralFormProps) => {
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [categoryId, setCategoryId] = useState('');
    const [amount, setAmount] = useState('');
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    const [paymentId, setPaymentId] = useState('');
    const [accountId, setAccountId] = useState('');
    const [description, setDescription] = useState('');
    const [memo, setMemo] = useState('');

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
        if (!categoryId || !amount || !date) return;

        onLedgerSubmit({
            categoryType: type,
            categoryId,
            accountId,
            paymentId: paymentId || null,
            amount: parseFloat(amount),
            title: description,
            memo,
            tradingAt: date
        });
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <Label>유형</Label>
                <div className="flex gap-2">
                    <Button
                        type="button"
                        variant={type === 'EXPENSE' ? 'default' : 'outline'}
                        className="flex-1"
                        onClick={() => {
                            setType('EXPENSE');
                            setCategoryId('');
                        }}
                    >
                        지출
                    </Button>
                    <Button
                        type="button"
                        variant={type === 'INCOME' ? 'default' : 'outline'}
                        className="flex-1"
                        onClick={() => {
                            setType('INCOME');
                            setCategoryId('');
                        }}
                    >
                        수입
                    </Button>
                </div>
            </div>

            <div className="space-y-2">
                <Label htmlFor="category">카테고리</Label>
                <Select value={categoryId} onValueChange={setCategoryId}>
                    <SelectTrigger id="category">
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

            <div className="space-y-2">
                <Label htmlFor="payment-method">결제수단</Label>
                <Select value={paymentId} onValueChange={handlePaymentMethodChange}>
                    <SelectTrigger id="payment-method">
                        <SelectValue placeholder="결제수단 선택 (선택사항)" />
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

            <div className="space-y-2">
                <Label htmlFor="account">계좌</Label>
                <Select value={accountId} onValueChange={setAccountId}>
                    <SelectTrigger id="account">
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
                <Label htmlFor="description">지출명</Label>
                <Input
                    id="description"
                    placeholder="예: 점심 식사"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="amount">금액</Label>
                <Input
                    id="amount"
                    type="number"
                    placeholder="0"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="memo">메모</Label>
                <Textarea
                    id="memo"
                    placeholder="메모를 입력하세요"
                    value={memo}
                    onChange={(e) => setMemo(e.target.value)}
                    rows={3}
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="date">거래일자</Label>
                <Input
                    id="date"
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    required
                />
            </div>

            <div className="flex gap-2 pt-4">
                <Button type="button" variant="outline" className="flex-1" onClick={onCancel}>
                    취소
                </Button>
                <Button type="submit" className="flex-1">
                    추가
                </Button>
            </div>
        </form>
    );
};

// =========================================================
// 2. 고정 지출 폼 (FixedTransactionForm)
// todo BACK API 만들어야함.
// =========================================================
interface FixedFormProps {
    categories: Category[];
    onFixedSubmit: (data: any) => void;
    onCancel: () => void;
}

const FixedTransactionForm = ({ categories, onFixedSubmit, onCancel }: FixedFormProps) => {
    const type = 'EXPENSE';
    const [category, setCategory] = useState('');
    const [amount, setAmount] = useState('');
    const [fixedName, setFixedName] = useState('');
    const [fixedDay, setFixedDay] = useState('1');
    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [endDate, setEndDate] = useState('');

    const expenseCategories = categories.filter(cat => cat.type === 'EXPENSE');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!fixedName || !startDate || !amount || !category) return;

        onFixedSubmit({
            type,
            category,
            amount: parseFloat(amount),
            date: startDate,
            description: fixedName,
            isFixed: true,
            fixedDay: parseInt(fixedDay),
            startDate,
            endDate: endDate || undefined,
        });
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
                <Label htmlFor="fixed-category">카테고리</Label>
                <Select value={category} onValueChange={setCategory}>
                    <SelectTrigger id="fixed-category">
                        <SelectValue placeholder="카테고리 선택" />
                    </SelectTrigger>
                    <SelectContent>
                        {expenseCategories.map((cat) => (
                            <SelectItem key={cat.categoryId} value={cat.categoryId}>
                                {cat.name}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>

            <div className="space-y-2">
                <Label htmlFor="fixed-name">고정지출명</Label>
                <Input
                    id="fixed-name"
                    placeholder="예: 월세, 통신비 등"
                    value={fixedName}
                    onChange={(e) => setFixedName(e.target.value)}
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="fixed-amount">금액</Label>
                <Input
                    id="fixed-amount"
                    type="number"
                    placeholder="0"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="fixed-day">고정지출일 (매월)</Label>
                <Select value={fixedDay} onValueChange={setFixedDay}>
                    <SelectTrigger id="fixed-day">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        {Array.from({ length: 31 }, (_, i) => i + 1).map((day) => (
                            <SelectItem key={day} value={day.toString()}>
                                {day}일
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>

            <div className="space-y-2">
                <Label htmlFor="start-date">시작일</Label>
                <Input
                    id="start-date"
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="end-date">종료일 (선택)</Label>
                <Input
                    id="end-date"
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                />
            </div>

            <div className="flex gap-2 pt-4">
                <Button type="button" variant="outline" className="flex-1" onClick={onCancel}>
                    취소
                </Button>
                <Button type="submit" className="flex-1">
                    추가
                </Button>
            </div>
        </form>
    );
};

// =========================================================
// 3. 메인 AddLedgerDialog 컴포넌트
// =========================================================
export function AddLedgerDialog({
    open,
    onOpenChange,
    onAddLedger,
    onAddFixed
}: AddLedgerDialogProps) {
    const { categories, accounts, payments } = useResourceStore(); 
    
    const [transactionType, setTransactionType] = useState<'general' | 'fixed'>('general');

    const handleLedgerSubmit = (ledger: Partial<Ledger>) => {
        onAddLedger(ledger);
        onOpenChange(false);
    };

    const handleFixedSubmit = (fixed: Partial<Fixed>) => {
        onAddFixed(fixed);
        onOpenChange(false);
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>거래 추가</DialogTitle>
                    <DialogDescription>새로운 거래를 추가하세요.</DialogDescription>
                </DialogHeader>
                
                <Tabs value={transactionType} onValueChange={(v) => setTransactionType(v as 'general' | 'fixed')}>
                    <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value="general">일반 거래</TabsTrigger>
                        <TabsTrigger value="fixed">고정 지출</TabsTrigger>
                    </TabsList>

                    <TabsContent value="general" className="mt-4">
                        <GeneralTransactionForm 
                            categories={categories}
                            accounts={accounts}
                            payments={payments}
                            onLedgerSubmit={handleLedgerSubmit}
                            onCancel={() => onOpenChange(false)}
                        />
                    </TabsContent>

                    <TabsContent value="fixed" className="mt-4">
                        <FixedTransactionForm 
                            categories={categories}
                            onFixedSubmit={handleFixedSubmit}
                            onCancel={() => onOpenChange(false)}
                        />
                    </TabsContent>
                </Tabs>
            </DialogContent>
        </Dialog>
    );
}