import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Account, Bank } from '../types/finance';
import { Wallet, Trash, Plus, Building, Banknote, Pencil, ArrowRightLeft, Coins } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
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
import { useAccounts, useBanks } from '../api/queries';

interface AccountManagerProps {
    onAdd: (account: Omit<Account, "accountId" | "userId" | "createdAt" | "updatedAt" | "bankName">) => void;
    onUpdate: (account: Partial<Account>) => void;
    onDelete: (accountId: string) => void;
    onTransferClick?: () => void;
}

const accountTypeLabels = {
    bank: '은행',
    cash: '현금',
    point: '포인트',
    other: '기타',
};

const accountTypeIcons: Record<string, any> = {
    bank: Building,
    cash: Banknote,
    point: Coins,
    other: Wallet,
};

const defaultColors = [
    '#ef4444', '#f59e0b', '#eab308', '#84cc16', '#22c55e', '#10b981',
    '#14b8a6', '#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899', '#64748b',
];

const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
};

interface AccountFormProps {
    nickname: string;
    setNickname: (val: string) => void;
    type: 'BANK' | 'CASH' | 'POINT' | 'OTHER';
    setType: (val: 'BANK' | 'CASH' | 'POINT' | 'OTHER') => void;
    balance: number | string;
    setBalance: (val: string) => void;
    color: string;
    setColor: (val: string) => void;
    banks: Bank[];
    bankId: string;
    setBankId: (val: string) => void;
    accountNumber: string;
    setAccountNumber: (val: string) => void;
}

const AccountForm = ({
    nickname, setNickname,
    type, setType,
    balance, setBalance,
    color, setColor,
    banks, bankId, setBankId,
    accountNumber, setAccountNumber
}: AccountFormProps) => (
    <div className="space-y-4">
        <div className="space-y-2">
            <Label htmlFor="account-name">계좌명(별칭)</Label>
            <Input
                id="account-name"
                placeholder="예: 월급통장, 비상금"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
            />
        </div>

        <div className="space-y-2">
            <Label htmlFor="account-type">유형</Label>
            <Select value={type} onValueChange={(val) => setType(val as typeof type)}>
                <SelectTrigger id="account-type">
                    <SelectValue />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="BANK">은행</SelectItem>
                    <SelectItem value="CASH">현금</SelectItem>
                    <SelectItem value="POINT">포인트</SelectItem>
                    <SelectItem value="OTHER">기타</SelectItem>
                </SelectContent>
            </Select>
        </div>

        {type === 'BANK' && (
            <>
                <div className="space-y-2">
                    <Label htmlFor="bank-select">은행 선택</Label>
                    <Select value={bankId} onValueChange={setBankId}>
                        <SelectTrigger id="bank-select">
                            <SelectValue placeholder="은행을 선택해주세요" />
                        </SelectTrigger>
                        <SelectContent>
                            {banks.map((bank) => (
                                <SelectItem key={bank.bankId} value={String(bank.bankId)}>
                                    {bank.name}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
                <div className="space-y-2">
                    <Label htmlFor="account-number">계좌번호</Label>
                    <Input
                        id="account-number"
                        placeholder="110-123-456789"
                        value={accountNumber}
                        onChange={(e) => setAccountNumber(e.target.value)}
                    />
                </div>
            </>
        )}

        <div className="space-y-2">
            <Label htmlFor="account-balance">잔액</Label>
            <Input
                id="account-balance"
                type="number"
                placeholder="0"
                value={balance}
                onChange={(e) => setBalance(e.target.value)}
            />
        </div>

        <div className="space-y-2">
            <Label>색상</Label>
            <div className="grid grid-cols-6 gap-2">
                {defaultColors.map((c) => (
                    <button
                        key={c}
                        type="button"
                        className={`size-8 rounded-full border-2 ${color === c ? 'border-foreground' : 'border-transparent'
                            }`}
                        style={{ backgroundColor: c }}
                        onClick={() => setColor(c)}
                    />
                ))}
            </div>
        </div>
    </div>
);

const AccountList = ({ items, onEdit, onDelete }: {
    items: Account[];
    onEdit: (account: Account) => void;
    onDelete: (id: string) => void;
}) => (
    <div className="space-y-2">
        {items.length === 0 ? (
            <div className="text-center text-muted-foreground py-8">등록된 계좌가 없습니다</div>
        ) : (
            items.map((account) => {
                const typeKey = account.type.toLowerCase();
                const Icon = accountTypeIcons[typeKey] || Wallet;

                return (
                    <div
                        key={account.accountId}
                        className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors"
                    >
                        <div className="flex items-center gap-3">
                            <div
                                className="p-2 rounded-full"
                                style={{ backgroundColor: `${account.color}20` }}
                            >
                                <Icon className="size-4" style={{ color: account.color }} />
                            </div>
                            <div>
                                <div>{account.nickname}</div>
                                <div className="text-xs text-muted-foreground">
                                    {accountTypeLabels[typeKey as keyof typeof accountTypeLabels] || '기타'}
                                    {account.type === 'BANK' && account.accountNumber && ` · ${account.accountNumber}`}
                                </div>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <span>{formatCurrency(account.balance)}원</span>
                            <Button variant="ghost" size="icon" onClick={() => onEdit(account)}>
                                <Pencil className="size-4" />
                            </Button>
                            <Button variant="ghost" size="icon" onClick={() => onDelete(account.accountId)}>
                                <Trash className="size-4" />
                            </Button>
                        </div>
                    </div>
                );
            })
        )}
    </div>
);

export function AccountManager({ onAdd, onUpdate, onDelete, onTransferClick }: AccountManagerProps) {
    const { data: accounts = [] } = useAccounts();
    const { data: banks = [] } = useBanks();

    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
    const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
    const [editingAccount, setEditingAccount] = useState<Account | null>(null);
    const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);

    const [type, setType] = useState<'BANK' | 'CASH' | 'POINT' | 'OTHER'>('BANK');
    const [nickname, setNickname] = useState('');
    const [balance, setBalance] = useState<number | string>(0);
    const [color, setColor] = useState<string>(defaultColors[0]);
    const [bankId, setBankId] = useState<string>('');
    const [accountNumber, setAccountNumber] = useState('');

    const resetForm = () => {
        setBankId('');
        setType('BANK');
        setNickname('');
        setBalance(0);
        setAccountNumber('');
        setColor(defaultColors[0]);
    };

    const handleOpenAddDialog = () => {
        resetForm();
        setEditingAccount(null);
        setIsAddDialogOpen(true);
    };

    const handleAddDialogOpenChange = (open: boolean) => {
        setIsAddDialogOpen(open);
        if (!open) {
            resetForm();
        }
    };

    const handleAdd = () => {
        if (!nickname) return;
        if (type === 'BANK' && !bankId) return;

        onAdd({
            bankId: type === 'BANK' ? bankId : undefined,
            type,
            nickname,
            balance: Number(balance),
            color,
            accountNumber: type === 'BANK' ? accountNumber : undefined
        });

        resetForm();
        setIsAddDialogOpen(false);
    };

    const handleEdit = (account: Account) => {
        setEditingAccount(account);
        setBankId(account.bankId ? String(account.bankId) : '');
        setType(account.type);
        setNickname(account.nickname);
        setBalance(account.balance);
        setAccountNumber(account.accountNumber || '');
        setColor(account.color);
        setIsEditDialogOpen(true);
    };

    const handleUpdate = () => {
        if (!editingAccount) return;
        if (type === 'BANK' && !bankId) return;

        onUpdate({
            accountId: editingAccount.accountId,
            bankId: type === 'BANK' ? bankId : undefined,
            nickname,
            balance: Number(balance),
            type,
            color,
            accountNumber: type === 'BANK' ? accountNumber : undefined
        });

        resetForm();
        setEditingAccount(null);
        setIsEditDialogOpen(false);
    };

    const handleDeleteClick = (accountId: string) => {
        setDeleteTargetId(accountId);
    };

    const confirmDelete = () => {
        if (!deleteTargetId) return;
        onDelete(deleteTargetId);
        setDeleteTargetId(null);
    };

    const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between flex-wrap gap-2">
                        <CardTitle className="flex items-center gap-2">
                            <Wallet className="size-5" />
                            계좌 관리
                        </CardTitle>
                        <div className="flex gap-2">
                            {onTransferClick && accounts.length >= 2 && (
                                <Button onClick={onTransferClick} size="sm" variant="outline">
                                    <ArrowRightLeft className="size-4 mr-2" />
                                    계좌 이체
                                </Button>
                            )}
                            <Button onClick={handleOpenAddDialog} size="sm">
                                <Plus className="size-4 mr-2" />
                                계좌 추가
                            </Button>
                        </div>
                    </div>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="p-4 bg-primary/10 rounded-lg">
                        <div className="text-sm text-muted-foreground mb-1">총 자산</div>
                        <div className="text-primary font-bold text-xl">{formatCurrency(totalBalance)}원</div>
                    </div>

                    <AccountList
                        items={accounts}
                        onEdit={handleEdit}
                        onDelete={handleDeleteClick}
                    />
                </CardContent>
            </Card>

            <Dialog open={isAddDialogOpen} onOpenChange={handleAddDialogOpenChange}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>계좌 추가</DialogTitle>
                        <DialogDescription>새로운 계좌를 추가하세요.</DialogDescription>
                    </DialogHeader>

                    <AccountForm
                        nickname={nickname} setNickname={setNickname}
                        type={type} setType={setType}
                        balance={balance} setBalance={setBalance}
                        color={color} setColor={setColor}
                        banks={banks} bankId={bankId} setBankId={setBankId}
                        accountNumber={accountNumber} setAccountNumber={setAccountNumber}
                    />

                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => handleAddDialogOpenChange(false)}>
                            취소
                        </Button>
                        <Button className="flex-1" onClick={handleAdd}>
                            추가
                        </Button>
                    </div>
                </DialogContent>
            </Dialog>

            <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>계좌 수정</DialogTitle>
                        <DialogDescription>계좌 정보를 수정합니다.</DialogDescription>
                    </DialogHeader>

                    <AccountForm
                        nickname={nickname} setNickname={setNickname}
                        type={type} setType={setType}
                        balance={balance} setBalance={setBalance}
                        color={color} setColor={setColor}
                        banks={banks} bankId={bankId} setBankId={setBankId}
                        accountNumber={accountNumber} setAccountNumber={setAccountNumber}
                    />

                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsEditDialogOpen(false)}>
                            취소
                        </Button>
                        <Button className="flex-1" onClick={handleUpdate}>
                            수정
                        </Button>
                    </div>
                </DialogContent>
            </Dialog>

            <AlertDialog open={!!deleteTargetId} onOpenChange={(open) => !open && setDeleteTargetId(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>정말 삭제하시겠습니까?</AlertDialogTitle>
                        <AlertDialogDescription>
                            삭제된 계좌는 복구할 수 없습니다.<br />관련 내역은 보존되지만 정보는 사라집니다.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel onClick={() => setDeleteTargetId(null)}>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={confirmDelete} className="bg-red-600 hover:bg-red-700 focus:ring-red-600">삭제</AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}
