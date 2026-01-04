import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Textarea } from './ui/textarea';
import { Transfer, Account } from '../types/finance';

interface TransferDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onAdd: (transfer: Omit<Transfer, 'id'>) => void;
  accounts: Account[];
}

export function TransferDialog({
  open,
  onOpenChange,
  onAdd,
  accounts,
}: TransferDialogProps) {
  const [fromAccountId, setFromAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [memo, setMemo] = useState('');

  const resetForm = () => {
    setFromAccountId('');
    setToAccountId('');
    setAmount('');
    setDate(new Date().toISOString().split('T')[0]);
    setMemo('');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!fromAccountId || !toAccountId || !amount || !date) return;
    
    if (fromAccountId === toAccountId) {
      alert('출금 계좌와 입금 계좌가 같을 수 없습니다.');
      return;
    }

    onAdd({
      fromAccountId,
      toAccountId,
      amount: parseFloat(amount),
      date,
      memo: memo || undefined,
    });

    resetForm();
    onOpenChange(false);
  };

  // Filter available accounts for "to" dropdown
  const availableToAccounts = accounts.filter(acc => acc.id !== fromAccountId);
  const availableFromAccounts = accounts.filter(acc => acc.id !== toAccountId);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>계좌 이체</DialogTitle>
          <DialogDescription>계좌 간 이체를 진행하세요.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="from-account">출금계좌</Label>
            <Select value={fromAccountId} onValueChange={setFromAccountId}>
              <SelectTrigger id="from-account">
                <SelectValue placeholder="출금할 계좌 선택" />
              </SelectTrigger>
              <SelectContent>
                {availableFromAccounts.map((acc) => (
                  <SelectItem key={acc.id} value={acc.id}>
                    {acc.name} ({new Intl.NumberFormat('ko-KR').format(acc.balance)}원)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="to-account">입금계좌</Label>
            <Select value={toAccountId} onValueChange={setToAccountId}>
              <SelectTrigger id="to-account">
                <SelectValue placeholder="입금할 계좌 선택" />
              </SelectTrigger>
              <SelectContent>
                {availableToAccounts.map((acc) => (
                  <SelectItem key={acc.id} value={acc.id}>
                    {acc.name} ({new Intl.NumberFormat('ko-KR').format(acc.balance)}원)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="transfer-amount">금액</Label>
            <Input
              id="transfer-amount"
              type="number"
              placeholder="0"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="transfer-date">이체날짜</Label>
            <Input
              id="transfer-date"
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="transfer-memo">메모</Label>
            <Textarea
              id="transfer-memo"
              placeholder="메모를 입력하세요"
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              rows={3}
            />
          </div>

          <div className="flex gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              className="flex-1"
              onClick={() => onOpenChange(false)}
            >
              취소
            </Button>
            <Button type="submit" className="flex-1">
              이체
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
