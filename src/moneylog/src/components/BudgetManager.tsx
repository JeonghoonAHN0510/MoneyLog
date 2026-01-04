import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Budget, Category } from '../types/finance';
import { Target, Trash, Plus } from 'lucide-react';

interface BudgetManagerProps {
  budgets: Budget[];
  categories: Category[];
  onAdd: (budget: Omit<Budget, 'id'>) => void;
  onDelete: (id: string) => void;
}

export function BudgetManager({ budgets, categories, onAdd, onDelete }: BudgetManagerProps) {
  const [category, setCategory] = useState('');
  const [amount, setAmount] = useState('');
  const [period, setPeriod] = useState<'monthly' | 'yearly'>('monthly');

  const expenseCategories = categories.filter((cat) => cat.type === 'expense');

  const handleAdd = () => {
    if (!category || !amount) return;

    onAdd({
      category,
      amount: parseFloat(amount),
      period,
    });

    setCategory('');
    setAmount('');
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Target className="size-5" />
          예산 관리
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Add Budget Form */}
        <div className="space-y-4 p-4 border rounded-lg">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="budget-category">카테고리</Label>
              <Select value={category} onValueChange={setCategory}>
                <SelectTrigger id="budget-category">
                  <SelectValue placeholder="선택" />
                </SelectTrigger>
                <SelectContent>
                  {expenseCategories.map((cat) => (
                    <SelectItem key={cat.id} value={cat.name}>
                      {cat.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="budget-amount">예산 금액</Label>
              <Input
                id="budget-amount"
                type="number"
                placeholder="0"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="budget-period">기간</Label>
              <Select value={period} onValueChange={(val) => setPeriod(val as 'monthly' | 'yearly')}>
                <SelectTrigger id="budget-period">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="monthly">월간</SelectItem>
                  <SelectItem value="yearly">연간</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <Button onClick={handleAdd} className="w-full">
            <Plus className="size-4 mr-2" />
            예산 추가
          </Button>
        </div>

        {/* Budget List */}
        <div className="space-y-2">
          {budgets.length === 0 ? (
            <div className="text-center text-muted-foreground py-8">설정된 예산이 없습니다</div>
          ) : (
            budgets.map((budget) => (
              <div
                key={budget.id}
                className="flex items-center justify-between p-3 border rounded-lg"
              >
                <div>
                  <div>{budget.category}</div>
                  <div className="text-sm text-muted-foreground">
                    {budget.period === 'monthly' ? '월간' : '연간'}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span>{formatCurrency(budget.amount)}원</span>
                  <Button variant="ghost" size="icon" onClick={() => onDelete(budget.id)}>
                    <Trash className="size-4" />
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}
