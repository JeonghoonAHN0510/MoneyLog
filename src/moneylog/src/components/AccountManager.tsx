import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Account } from '../types/finance';
// [변경] CreditCard 제거, Coins 추가 (포인트 아이콘용)
import { Wallet, Trash, Plus, Building, Banknote, Pencil, ArrowRightLeft, Coins } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';

interface AccountManagerProps {
  accounts: Account[];
  onAdd: (account: any) => void;
  onUpdate: (id: number, account: any) => void;
  onDelete: (id: number) => void;
  onTransferClick?: () => void;
}

// [변경] 'card' 제거, 'point' 추가
const accountTypeLabels = {
  bank: '은행',
  cash: '현금',
  point: '포인트',
  other: '기타',
};

// [변경] 'card' 제거, 'point'에 Coins 아이콘 연결
const accountTypeIcons = {
  bank: Building,
  cash: Banknote,
  point: Coins,
  other: Wallet,
};

const defaultColors = ['#3b82f6', '#ef4444', '#22c55e', '#eab308', '#8b5cf6', '#ec4899', '#06b6d4'];

export function AccountManager({ accounts, onAdd, onUpdate, onDelete, onTransferClick }: AccountManagerProps) {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);
  
  const [name, setName] = useState('');
  // [변경] 타입 정의에서 'card' 제거, 'point' 추가
  const [type, setType] = useState<'bank' | 'cash' | 'point' | 'other'>('bank');
  const [balance, setBalance] = useState('');
  const [color, setColor] = useState<String>(defaultColors[0]);

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
    setName(account.nickname);
    setType(account.type);
    setBalance(account.balance.toString());
    setColor(account.color);
    setIsEditDialogOpen(true);
  };

  const handleUpdate = () => {
    if (!editingAccount || !name) return;

    onUpdate(editingAccount.account_id, {
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
          placeholder="예: 네이버페이 포인트, 신한은행"
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
            <SelectItem value="cash">현금</SelectItem>
            <SelectItem value="point">포인트</SelectItem>
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
                // @ts-ignore: 기존 데이터에 card 타입이 남아있을 경우 대비 (기본값 Wallet)
                const Icon = accountTypeIcons[account.type] || Wallet;
                return (
                  <div
                    key={account.account_id}
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
                          {/* @ts-ignore: 기존 데이터 호환성 */}
                          {accountTypeLabels[account.type] || '기타'}
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
                        onClick={() => onDelete(account.account_id)}
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
            <DialogDescription>계좌 정보를 수정합니다.</DialogDescription>
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