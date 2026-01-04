import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Account } from '../types/finance';
import { Wallet, Trash, Plus, Building, CreditCard, Banknote, Pencil, ArrowRightLeft } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';

interface AccountManagerProps {
  accounts: Account[];
  onAdd: (account: Omit<Account, 'id'>) => void;
  onUpdate: (id: string, account: Partial<Account>) => void;
  onDelete: (id: string) => void;
  onTransferClick?: () => void;
}

const accountTypeLabels = {
  bank: '은행',
  card: '카드',
  cash: '현금',
  other: '기타',
};

const accountTypeIcons = {
  bank: Building,
  card: CreditCard,
  cash: Banknote,
  other: Wallet,
};

const defaultColors = ['#3b82f6', '#ef4444', '#22c55e', '#eab308', '#8b5cf6', '#ec4899', '#06b6d4'];

export function AccountManager({ accounts, onAdd, onUpdate, onDelete, onTransferClick }: AccountManagerProps) {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);
  
  const [name, setName] = useState('');
  const [type, setType] = useState<'bank' | 'card' | 'cash' | 'other'>('bank');
  const [balance, setBalance] = useState('');
  const [color, setColor] = useState(defaultColors[0]);

  const resetForm = () => {
    setName('');
    setType('bank');
    setBalance('');
    setColor(defaultColors[0]);
  };

  const handleAdd = () => {
    if (!name) return;

    onAdd({
      name,
      type,
      balance: parseFloat(balance) || 0,
      color,
    });

    resetForm();
    setIsAddDialogOpen(false);
  };

  const handleEdit = (account: Account) => {
    setEditingAccount(account);
    setName(account.name);
    setType(account.type);
    setBalance(account.balance.toString());
    setColor(account.color);
    setIsEditDialogOpen(true);
  };

  const handleUpdate = () => {
    if (!editingAccount || !name) return;

    onUpdate(editingAccount.id, {
      name,
      type,
      balance: parseFloat(balance) || 0,
      color,
    });

    resetForm();
    setEditingAccount(null);
    setIsEditDialogOpen(false);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

  const AccountForm = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="account-name">계좌명</Label>
        <Input
          id="account-name"
          placeholder="신한은행 입출금"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="account-type">유형</Label>
        <Select value={type} onValueChange={(val) => setType(val as typeof type)}>
          <SelectTrigger id="account-type">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="bank">은행</SelectItem>
            <SelectItem value="card">카드</SelectItem>
            <SelectItem value="cash">현금</SelectItem>
            <SelectItem value="other">기타</SelectItem>
          </SelectContent>
        </Select>
      </div>

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
        <div className="flex gap-2">
          {defaultColors.map((c) => (
            <button
              key={c}
              type="button"
              className={`size-8 rounded-full border-2 ${
                color === c ? 'border-foreground' : 'border-transparent'
              }`}
              style={{ backgroundColor: c }}
              onClick={() => setColor(c)}
            />
          ))}
        </div>
      </div>
    </div>
  );

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
              <Button onClick={() => setIsAddDialogOpen(true)} size="sm">
                <Plus className="size-4 mr-2" />
                계좌 추가
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Total Balance */}
          <div className="p-4 bg-primary/10 rounded-lg">
            <div className="text-sm text-muted-foreground mb-1">총 자산</div>
            <div className="text-primary">{formatCurrency(totalBalance)}원</div>
          </div>

          {/* Account List */}
          <div className="space-y-2">
            {accounts.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">등록된 계좌가 없습니다</div>
            ) : (
              accounts.map((account) => {
                const Icon = accountTypeIcons[account.type];
                return (
                  <div
                    key={account.id}
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
                        <div>{account.name}</div>
                        <div className="text-xs text-muted-foreground">
                          {accountTypeLabels[account.type]}
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span>{formatCurrency(account.balance)}원</span>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleEdit(account)}
                      >
                        <Pencil className="size-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => onDelete(account.id)}
                      >
                        <Trash className="size-4" />
                      </Button>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </CardContent>
      </Card>

      {/* Add Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>계좌 추가</DialogTitle>
            <DialogDescription>새로운 계좌를 추가하세요.</DialogDescription>
          </DialogHeader>
          <AccountForm />
          <div className="flex gap-2 pt-4">
            <Button variant="outline" className="flex-1" onClick={() => setIsAddDialogOpen(false)}>
              취소
            </Button>
            <Button className="flex-1" onClick={handleAdd}>
              추가
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>계좌 수정</DialogTitle>
            <DialogDescription>계좌 정보를 수정하세요.</DialogDescription>
          </DialogHeader>
          <AccountForm />
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
    </>
  );
}