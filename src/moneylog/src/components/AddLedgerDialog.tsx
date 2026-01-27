import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Textarea } from './ui/textarea';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Ledger, Category, Account } from '../types/finance';

interface AddLedgerDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onAdd: (ledger: Omit<Ledger, 'ledger_id'>) => void;
  categories: Category[];
  accounts: Account[];
}

export function AddLedgerDialog({
  open,
  onOpenChange,
  onAdd,
  categories,
  accounts,
}: AddLedgerDialogProps) {
  const [transactionType, setTransactionType] = useState<'general' | 'fixed'>('general');
  const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
  
  // Common fields
  const [category, setCategory] = useState('');
  const [amount, setAmount] = useState('');
  
  // General transaction fields
  const [paymentMethod, setPaymentMethod] = useState('');
  const [accountId, setAccountId] = useState('');
  const [description, setDescription] = useState('');
  const [memo, setMemo] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  
  // Fixed transaction fields
  const [fixedName, setFixedName] = useState('');
  const [fixedDay, setFixedDay] = useState('1');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState('');

  const filteredCategories = categories.filter((cat) => cat.type === type);

  const resetForm = () => {
    setCategory('');
    setAmount('');
    setPaymentMethod('');
    setAccountId('');
    setDescription('');
    setMemo('');
    setDate(new Date().toISOString().split('T')[0]);
    setFixedName('');
    setFixedDay('1');
    setStartDate(new Date().toISOString().split('T')[0]);
    setEndDate('');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!category || !amount) return;

    if (transactionType === 'general') {
      if (!date) return;
      
      onAdd({
        type,
        category,
        amount: parseFloat(amount),
        date,
        description,
        isFixed: false,
        accountId: accountId || undefined,
        paymentMethod: paymentMethod || undefined,
        memo: memo || undefined,
      });
    } else {
      // Fixed transaction
      if (!fixedName || !startDate) return;
      
      onAdd({
        type,
        category,
        amount: parseFloat(amount),
        date: startDate, // Use start date as the base date
        description: fixedName,
        isFixed: true,
        fixedDay: parseInt(fixedDay),
        startDate,
        endDate: endDate || undefined,
      });
    }

    resetForm();
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

          {/* General Transaction Form */}
          <TabsContent value="general">
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
                      setCategory('');
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
                      setCategory('');
                    }}
                  >
                    수입
                  </Button>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="category">카테고리</Label>
                <Select value={category} onValueChange={setCategory}>
                  <SelectTrigger id="category">
                    <SelectValue placeholder="카테고리 선택" />
                  </SelectTrigger>
                  <SelectContent>
                    {filteredCategories.map((cat) => (
                      <SelectItem key={cat.category_id} value={cat.category_id}>
                        {cat.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="payment-method">결제수단</Label>
                <Select value={paymentMethod} onValueChange={setPaymentMethod}>
                  <SelectTrigger id="payment-method">
                    <SelectValue placeholder="결제수단 선택 (선택사항)" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="card">카드</SelectItem>
                    <SelectItem value="cash">현금</SelectItem>
                    <SelectItem value="transfer">계좌이체</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="account">계좌</Label>
                <Select value={accountId} onValueChange={setAccountId}>
                  <SelectTrigger id="account">
                    <SelectValue placeholder="계좌 선택 (선택사항)" />
                  </SelectTrigger>
                  <SelectContent>
                    {accounts.map((acc) => (
                      <SelectItem key={acc.id} value={acc.id}>
                        {acc.name}
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
                <Button
                  type="button"
                  variant="outline"
                  className="flex-1"
                  onClick={() => onOpenChange(false)}
                >
                  취소
                </Button>
                <Button type="submit" className="flex-1">
                  추가
                </Button>
              </div>
            </form>
          </TabsContent>

          {/* Fixed Transaction Form */}
          <TabsContent value="fixed">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="fixed-category">카테고리</Label>
                <Select value={category} onValueChange={setCategory}>
                  <SelectTrigger id="fixed-category">
                    <SelectValue placeholder="카테고리 선택" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.filter(cat => cat.type === 'EXPENSE').map((cat) => (
                      <SelectItem key={cat.id} value={cat.name}>
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
                <Button
                  type="button"
                  variant="outline"
                  className="flex-1"
                  onClick={() => onOpenChange(false)}
                >
                  취소
                </Button>
                <Button type="submit" className="flex-1">
                  추가
                </Button>
              </div>
            </form>
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}