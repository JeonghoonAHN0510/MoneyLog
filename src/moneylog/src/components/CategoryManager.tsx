import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Account, Category, Payment } from '../types/finance';
import { List, Trash, Plus, Pencil, CreditCard } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "./ui/alert-dialog";
import { useCategories, usePayments, useAccounts } from '../api/queries';

interface CategoryManagerProps {
    onAdd: (category: Omit<Category, "categoryId" | "userId" | "createdAt" | "updatedAt">) => void;
    onUpdate: (category: Partial<Category>) => void;
    onDelete: (categoryId: string) => void;

    onAddPayment: (payment: Omit<Payment, "paymentId" | "userId" | "createdAt" | "updatedAt">) => void;
    onUpdatePayment: (payment: Partial<Payment>) => void;
    onDeletePayment: (paymentId: string) => void;
}

const defaultColors = [
    '#ef4444', '#f59e0b', '#eab308', '#84cc16', '#22c55e', '#10b981',
    '#14b8a6', '#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899', '#64748b',
];

interface CategoryFormProps {
    name: string;
    setName: (value: string) => void;
    type: 'INCOME' | 'EXPENSE';
    setType: (value: 'INCOME' | 'EXPENSE') => void;
    color: string;
    setColor: (value: string) => void;
}

const CategoryForm = ({ name, setName, type, setType, color, setColor }: CategoryFormProps) => (
    <div className="space-y-4">
        <div className="space-y-2">
            <Label htmlFor="category-name">카테고리명</Label>
            <Input
                id="category-name"
                placeholder="식비"
                value={name}
                onChange={(e) => setName(e.target.value)}
            />
        </div>

        <div className="space-y-2">
            <Label>유형</Label>
            <div className="flex gap-2">
                <Button
                    type="button"
                    variant={type === 'EXPENSE' ? 'default' : 'outline'}
                    className="flex-1"
                    onClick={() => setType('EXPENSE')}
                >
                    지출
                </Button>
                <Button
                    type="button"
                    variant={type === 'INCOME' ? 'default' : 'outline'}
                    className="flex-1"
                    onClick={() => setType('INCOME')}
                >
                    수입
                </Button>
            </div>
        </div>

        <div className="space-y-2">
            <Label>색상</Label>
            <div className="grid grid-cols-6 gap-2">
                {defaultColors.map((c) => (
                    <button
                        key={c}
                        type="button"
                        className={`size-10 rounded-full border-2 ${color === c ? 'border-foreground' : 'border-transparent'
                            }`}
                        style={{ backgroundColor: c }}
                        onClick={() => setColor(c)}
                    />
                ))}
            </div>
        </div>
    </div>
);

interface PaymentFormProps {
    name: string;
    setName: (value: string) => void;
    type: 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK';
    setType: (value: 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK') => void;
    accounts: Account[];
    accountId: string;
    setAccountId: (value: string) => void;
}

const accountTypeLabelMap: Record<Exclude<Account['type'], 'BANK'>, string> = {
    CASH: '현금',
    POINT: '포인트',
    OTHER: '기타',
};

const getAccountOptionLabel = (account: Account) => {
    if (account.type === 'BANK') {
        return `${account.nickname} (${account.bankName})`;
    }

    return `${account.nickname} (${accountTypeLabelMap[account.type] ?? account.type})`;
};

const PaymentForm = ({ name, setName, type, setType, accountId, setAccountId, accounts }: PaymentFormProps) => (
    <div className="space-y-4">
        <div className="space-y-2">
            <Label htmlFor="payment-name">결제수단명</Label>
            <Input
                id="payment-name"
                placeholder="현금, 국민카드 등"
                value={name}
                onChange={(e) => setName(e.target.value)}
            />
        </div>

        <div className="space-y-2">
            <Label>유형</Label>
            <div className="flex gap-2">
                <Button
                    type="button"
                    variant={type === 'CASH' ? 'default' : 'outline'}
                    className="flex-1"
                    onClick={() => setType('CASH')}
                >
                    현금
                </Button>
                <Button
                    type="button"
                    variant={type === 'CREDIT_CARD' ? 'default' : 'outline'}
                    className="flex-1"
                    onClick={() => setType('CREDIT_CARD')}
                >
                    신용카드
                </Button>
                <Button
                    type="button"
                    variant={type === 'CHECK_CARD' ? 'default' : 'outline'}
                    className="flex-1"
                    onClick={() => setType('CHECK_CARD')}
                >
                    체크카드
                </Button>
                <Button
                    type="button"
                    variant={type === 'BANK' ? 'default' : 'outline'}
                    className="flex-1"
                    onClick={() => setType('BANK')}
                >
                    은행
                </Button>
            </div>
        </div>
        {type !== 'CASH' && (
            <div className="space-y-2">
                <Label htmlFor="bank-select">계좌 선택</Label>
                <Select value={accountId} onValueChange={setAccountId}>
                    <SelectTrigger id="bank-select">
                        <SelectValue placeholder="계좌를 선택해주세요" />
                    </SelectTrigger>
                    <SelectContent>
                        {accounts.map((account) => (
                            <SelectItem key={account.accountId} value={String(account.accountId)}>
                                {getAccountOptionLabel(account)}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
        )}
    </div>
);

const CategoryList = ({ items, onEdit, onDelete }: {
    items: Category[];
    onEdit: (category: Category) => void;
    onDelete: (id: string) => void;
}) => (
    <div className="space-y-2">
        {items.length === 0 ? (
            <div className="text-center text-muted-foreground py-4">카테고리가 없습니다</div>
        ) : (
            items.map((category) => (
                <div
                    key={category.categoryId}
                    className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors"
                >
                    <div className="flex items-center gap-3">
                        <div
                            className="size-6 rounded-full"
                            style={{ backgroundColor: category.color }}
                        />
                        <span>{category.name}</span>
                    </div>
                    <div className="flex gap-2">
                        <Button variant="ghost" size="icon" onClick={() => onEdit(category)}>
                            <Pencil className="size-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={() => onDelete(category.categoryId)}>
                            <Trash className="size-4" />
                        </Button>
                    </div>
                </div>
            ))
        )}
    </div>
);

const PaymentList = ({ items, onEdit, onDelete }: {
    items: Payment[];
    onEdit: (payment: Payment) => void;
    onDelete: (id: string) => void;
}) => (
    <div className="space-y-2">
        {items.length === 0 ? (
            <div className="text-center text-muted-foreground py-4">결제수단이 없습니다</div>
        ) : (
            items.map((payment) => (
                <div
                    key={payment.paymentId}
                    className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors"
                >
                    <div className="flex items-center gap-3">
                        <div className="bg-muted p-2 rounded-full">
                            <CreditCard className="size-4" />
                        </div>
                        <div className="flex flex-col">
                            <span className="font-medium">{payment.name}</span>
                            <span className="text-xs text-muted-foreground">
                                {payment.type === 'CASH' ? '현금' : payment.type === 'CREDIT_CARD' ? '신용카드' : payment.type === 'CHECK_CARD' ? '체크카드' : '은행'}
                            </span>
                        </div>
                    </div>
                    <div className="flex gap-2">
                        <Button variant="ghost" size="icon" onClick={() => onEdit(payment)}>
                            <Pencil className="size-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={() => onDelete(payment.paymentId)}>
                            <Trash className="size-4" />
                        </Button>
                    </div>
                </div>
            ))
        )}
    </div>
);


export function CategoryManager({
    onAdd, onUpdate, onDelete,
    onAddPayment, onUpdatePayment, onDeletePayment
}: CategoryManagerProps) {
    const { data: categories = [] } = useCategories();
    const { data: payments = [] } = usePayments();
    const { data: accounts = [] } = useAccounts();

    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
    const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
    const [editingCategory, setEditingCategory] = useState<Category | null>(null);
    const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);

    const [name, setName] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [color, setColor] = useState(defaultColors[0]);
    const [categoryId, setCategoryId] = useState('');
    const [accountId, setAccountId] = useState('');

    const resetForm = () => {
        setName('');
        setColor(defaultColors[0]);
    };

    const handleAdd = () => {
        if (!name) return;
        onAdd({ name, type, color });
        resetForm();
        setIsAddDialogOpen(false);
    };

    const handleEdit = (category: Category) => {
        setEditingCategory(category);
        setName(category.name);
        setType(category.type);
        setColor(category.color);
        setCategoryId(category.categoryId);
        setIsEditDialogOpen(true);
    };

    const handleUpdate = () => {
        if (!editingCategory || !name) return;
        onUpdate({ categoryId, name, type, color });
        resetForm();
        setEditingCategory(null);
        setIsEditDialogOpen(false);
    };

    const handleDelete = (categoryId: string) => {
        setDeleteTargetId(categoryId);
    };

    const confirmDelete = () => {
        if (!deleteTargetId) return;
        onDelete(deleteTargetId);
        setDeleteTargetId(null);
    };

    const [isAddPaymentOpen, setIsAddPaymentOpen] = useState(false);
    const [isEditPaymentOpen, setIsEditPaymentOpen] = useState(false);
    const [editingPayment, setEditingPayment] = useState<Payment | null>(null);
    const [deletePaymentTargetId, setDeletePaymentTargetId] = useState<string | null>(null);

    const [paymentName, setPaymentName] = useState('');
    const [paymentType, setPaymentType] = useState<'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK'>('CASH');
    const [paymentId, setPaymentId] = useState('');

    const handlePaymentTypeChange = (nextType: 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK') => {
        setPaymentType(nextType);
        if (nextType === 'CASH') {
            setAccountId('');
        }
    };

    const resetPaymentForm = () => {
        setPaymentName('');
        setPaymentType('CASH');
        setAccountId('');
    };

    const handleAddPayment = () => {
        if (!paymentName) return;
        const nextAccountId = paymentType === 'CASH' ? '' : accountId;
        onAddPayment({ name: paymentName, type: paymentType, accountId: nextAccountId });
        resetPaymentForm();
        setIsAddPaymentOpen(false);
    };

    const handleEditPayment = (payment: Payment) => {
        setEditingPayment(payment);
        setPaymentName(payment.name);
        setPaymentType(payment.type as 'CASH' | 'CREDIT_CARD' | 'CHECK_CARD' | 'BANK');
        setPaymentId(payment.paymentId);
        setAccountId(payment.accountId ? String(payment.accountId) : '');

        setIsEditPaymentOpen(true);
    };

    const handleUpdatePayment = () => {
        if (!editingPayment || !paymentName) return;
        const nextAccountId = paymentType === 'CASH' ? '' : accountId;
        onUpdatePayment({ paymentId, name: paymentName, type: paymentType, accountId: nextAccountId });
        resetPaymentForm();
        setEditingPayment(null);
        setIsEditPaymentOpen(false);
    };

    const handleDeletePayment = (id: string) => {
        setDeletePaymentTargetId(id);
    };

    const confirmDeletePayment = () => {
        if (!deletePaymentTargetId) return;
        onDeletePayment(deletePaymentTargetId);
        setDeletePaymentTargetId(null);
    };


    const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');
    const incomeCategories = categories.filter((c) => c.type === 'INCOME');

    return (
        <div className="space-y-6">
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle className="flex items-center gap-2">
                            <List className="size-5" />
                            카테고리 관리
                        </CardTitle>
                        <Button onClick={() => setIsAddDialogOpen(true)} size="sm">
                            <Plus className="size-4 mr-2" />
                            카테고리 추가
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <Tabs defaultValue="EXPENSE">
                        <TabsList className="grid w-full grid-cols-2">
                            <TabsTrigger onClick={() => setType("EXPENSE")} value="EXPENSE">지출 ({expenseCategories.length})</TabsTrigger>
                            <TabsTrigger onClick={() => setType("INCOME")} value="INCOME">수입 ({incomeCategories.length})</TabsTrigger>
                        </TabsList>
                        <TabsContent value="EXPENSE" className="mt-4">
                            <CategoryList items={expenseCategories} onEdit={handleEdit} onDelete={handleDelete} />
                        </TabsContent>
                        <TabsContent value="INCOME" className="mt-4">
                            <CategoryList items={incomeCategories} onEdit={handleEdit} onDelete={handleDelete} />
                        </TabsContent>
                    </Tabs>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle className="flex items-center gap-2">
                            <CreditCard className="size-5" />
                            결제수단 관리
                        </CardTitle>
                        <Button onClick={() => setIsAddPaymentOpen(true)} size="sm">
                            <Plus className="size-4 mr-2" />
                            결제수단 추가
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <PaymentList
                        items={payments}
                        onEdit={handleEditPayment}
                        onDelete={handleDeletePayment}
                    />
                </CardContent>
            </Card>

            <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>카테고리 추가</DialogTitle>
                        <DialogDescription>새로운 카테고리를 추가하세요.</DialogDescription>
                    </DialogHeader>
                    <CategoryForm name={name} setName={setName} type={type} setType={setType} color={color} setColor={setColor} />
                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsAddDialogOpen(false)}>취소</Button>
                        <Button className="flex-1" onClick={handleAdd}>추가</Button>
                    </div>
                </DialogContent>
            </Dialog>

            <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>카테고리 수정</DialogTitle>
                        <DialogDescription>카테고리를 수정하세요.</DialogDescription>
                    </DialogHeader>
                    <CategoryForm name={name} setName={setName} type={type} setType={setType} color={color} setColor={setColor} />
                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsEditDialogOpen(false)}>취소</Button>
                        <Button className="flex-1" onClick={handleUpdate}>수정</Button>
                    </div>
                </DialogContent>
            </Dialog>

            <AlertDialog open={!!deleteTargetId} onOpenChange={(open) => !open && setDeleteTargetId(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>정말 삭제하시겠습니까?</AlertDialogTitle>
                        <AlertDialogDescription>
                            삭제된 카테고리는 복구할 수 없습니다.<br />관련 내역은 보존되지만 정보는 사라집니다.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel onClick={() => setDeleteTargetId(null)}>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={confirmDelete} className="bg-red-600 hover:bg-red-700 focus:ring-red-600">삭제</AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            <Dialog open={isAddPaymentOpen} onOpenChange={setIsAddPaymentOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>결제수단 추가</DialogTitle>
                        <DialogDescription>새로운 결제수단을 추가하세요.</DialogDescription>
                    </DialogHeader>
                    <PaymentForm name={paymentName} setName={setPaymentName} type={paymentType} setType={handlePaymentTypeChange} accountId={accountId} setAccountId={setAccountId} accounts={accounts} />
                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsAddPaymentOpen(false)}>취소</Button>
                        <Button className="flex-1" onClick={handleAddPayment}>추가</Button>
                    </div>
                </DialogContent>
            </Dialog>

            <Dialog open={isEditPaymentOpen} onOpenChange={setIsEditPaymentOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>결제수단 수정</DialogTitle>
                        <DialogDescription>결제수단 정보를 수정하세요.</DialogDescription>
                    </DialogHeader>
                    <PaymentForm name={paymentName} setName={setPaymentName} type={paymentType} setType={handlePaymentTypeChange} accountId={accountId} setAccountId={setAccountId} accounts={accounts} />
                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsEditPaymentOpen(false)}>취소</Button>
                        <Button className="flex-1" onClick={handleUpdatePayment}>수정</Button>
                    </div>
                </DialogContent>
            </Dialog>

            <AlertDialog open={!!deletePaymentTargetId} onOpenChange={(open) => !open && setDeletePaymentTargetId(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>결제수단을 삭제하시겠습니까?</AlertDialogTitle>
                        <AlertDialogDescription>
                            삭제된 결제수단은 복구할 수 없습니다.<br />관련 내역은 보존되지만 정보는 사라집니다.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel onClick={() => setDeletePaymentTargetId(null)}>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={confirmDeletePayment} className="bg-red-600 hover:bg-red-700 focus:ring-red-600">삭제</AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

        </div>
    );
}
