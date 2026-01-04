import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Progress } from './ui/progress';
import { Transaction, Budget, Category } from '../types/finance';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend } from 'recharts';
import { TrendingUp, TrendingDown, Wallet, Target } from 'lucide-react';

interface DashboardViewProps {
  transactions: Transaction[];
  budgets: Budget[];
  categories: Category[];
}

export function DashboardView({ transactions, budgets, categories }: DashboardViewProps) {
  const currentMonth = new Date().getMonth();
  const currentYear = new Date().getFullYear();

  const monthTransactions = transactions.filter((t) => {
    const date = new Date(t.date);
    return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
  });

  const totalIncome = monthTransactions
    .filter((t) => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0);

  const totalExpense = monthTransactions
    .filter((t) => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0);

  const netAmount = totalIncome - totalExpense;
  const savingsRate = totalIncome > 0 ? ((netAmount / totalIncome) * 100).toFixed(1) : '0';

  // Category breakdown
  const expenseByCategory = categories
    .filter((cat) => cat.type === 'expense')
    .map((cat) => {
      const amount = monthTransactions
        .filter((t) => t.type === 'expense' && t.category === cat.name)
        .reduce((sum, t) => sum + t.amount, 0);
      return {
        name: cat.name,
        value: amount,
        color: cat.color,
      };
    })
    .filter((item) => item.value > 0);

  // Budget tracking
  const budgetStatus = budgets.map((budget) => {
    const spent = monthTransactions
      .filter((t) => t.type === 'expense' && t.category === budget.category)
      .reduce((sum, t) => sum + t.amount, 0);
    const percentage = (spent / budget.amount) * 100;
    return {
      category: budget.category,
      budget: budget.amount,
      spent,
      percentage,
      remaining: budget.amount - spent,
    };
  });

  // Monthly spending trend (last 3 months)
  const last3Months = Array.from({ length: 3 }, (_, i) => {
    const date = new Date();
    date.setMonth(date.getMonth() - (2 - i));
    return date;
  });

  const monthlyTrend = last3Months.map((date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    
    const monthTransactions = transactions.filter((t) => {
      const tDate = new Date(t.date);
      return tDate.getFullYear() === year && tDate.getMonth() === month;
    });
    
    const income = monthTransactions
      .filter((t) => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);
    const expense = monthTransactions
      .filter((t) => t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);
    
    return {
      date: `${year}.${month + 1}`,
      수입: income,
      지출: expense,
    };
  });

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">이번 달 수입</CardTitle>
            <TrendingUp className="size-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-green-600">{formatCurrency(totalIncome)}원</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">이번 달 지출</CardTitle>
            <TrendingDown className="size-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-red-600">{formatCurrency(totalExpense)}원</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">순자산 변동</CardTitle>
            <Wallet className="size-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className={netAmount >= 0 ? 'text-green-600' : 'text-red-600'}>
              {formatCurrency(netAmount)}원
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">저축률</CardTitle>
            <Target className="size-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-purple-600">{savingsRate}%</div>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Breakdown */}
        <Card>
          <CardHeader>
            <CardTitle>카테고리별 지출</CardTitle>
          </CardHeader>
          <CardContent>
            {expenseByCategory.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={expenseByCategory}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {expenseByCategory.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value: number) => `${formatCurrency(value)}원`} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                이번 달 지출 내역이 없습니다
              </div>
            )}
          </CardContent>
        </Card>

        {/* Monthly Trend */}
        <Card>
          <CardHeader>
            <CardTitle>최근 3개월 추세</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={monthlyTrend}>
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip formatter={(value: number) => `${formatCurrency(value)}원`} />
                <Legend />
                <Bar dataKey="수입" fill="#22c55e" />
                <Bar dataKey="지출" fill="#ef4444" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Budget Tracking */}
      <Card>
        <CardHeader>
          <CardTitle>예산 사용 현황</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {budgetStatus.length > 0 ? (
            budgetStatus.map((budget) => (
              <div key={budget.category} className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>{budget.category}</span>
                  <span className={budget.percentage > 100 ? 'text-red-600' : ''}>
                    {formatCurrency(budget.spent)} / {formatCurrency(budget.budget)}원
                  </span>
                </div>
                <Progress
                  value={Math.min(budget.percentage, 100)}
                  className={budget.percentage > 90 ? 'bg-red-100' : ''}
                />
                <div className="flex justify-between text-xs text-muted-foreground">
                  <span>{budget.percentage.toFixed(1)}% 사용</span>
                  <span>
                    {budget.remaining > 0
                      ? `${formatCurrency(budget.remaining)}원 남음`
                      : `${formatCurrency(Math.abs(budget.remaining))}원 초과`}
                  </span>
                </div>
              </div>
            ))
          ) : (
            <div className="text-center text-muted-foreground py-4">
              설정된 예산이 없습니다
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}