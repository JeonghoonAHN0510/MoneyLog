import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Textarea } from './ui/textarea';
import { Transfer } from '../types/finance';
import { useAccounts } from '../api/queries';
import { formatKrw } from '../utils/currency';

interface TransferDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onAdd: (transfer: Omit<Transfer, "transferId" | "userId" | "createdAt" | "updatedAt">) => void;
}

export function TransferDialog({
  open,
  onOpenChange,
  onAdd,
}: TransferDialogProps) {
  const { data: accounts = [] } = useAccounts();

  const [fromAccountId, setFromAccountId] = useState<string>('');
  const [toAccountId, setToAccountId] = useState<string>('');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [memo, setMemo] = useState<string>('');

  const resetForm = () => {
    setFromAccountId('');
    setToAccountId('');
    setAmount('');
    setMemo('');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!fromAccountId || !toAccountId || !amount) return;

    if (fromAccountId === toAccountId) {
      alert('출금 계좌와 입금 계좌가 같을 수 없습니다.');
      return;
    }

    onAdd({
      fromAccount: fromAccountId,
      toAccount: toAccountId,
      transferAt: date,
      amount: parseFloat(amount),
      memo: memo || undefined,
    });

    resetForm();
    onOpenChange(false);
  };

  const availableToAccounts = accounts.filter(acc => acc.accountId != fromAccountId);
  const availableFromAccounts = accounts.filter(acc => acc.accountId != toAccountId);

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
                {availableFromAccounts
                  .filter((acc) => acc.balance > 0)
                  .map((acc) => (
                    <SelectItem key={acc.accountId} value={String(acc.accountId)}>
                      {acc.nickname} ({formatKrw(acc.balance)}원)
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
                  <SelectItem key={acc.accountId} value={String(acc.accountId)}>
                    {acc.nickname} ({formatKrw(acc.balance)}원)
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
