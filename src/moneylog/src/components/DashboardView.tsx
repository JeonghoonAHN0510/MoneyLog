import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Progress } from './ui/progress';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend } from 'recharts';
import { TrendingUp, TrendingDown, Wallet, Target, Loader2 } from 'lucide-react';
import { useDashboard, useBudgets, useCategories } from '../api/queries';

// 카테고리 색상 팔레트 (순환 사용)
const CHART_COLORS = [
  '#3b82f6', '#ef4444', '#22c55e', '#f59e0b', '#8b5cf6',
  '#ec4899', '#14b8a6', '#f97316', '#06b6d4', '#84cc16',
  '#6366f1', '#e11d48'
];

export function DashboardView() {
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1; // 1-indexed for API

  // TanStack Query로 데이터 조회
  const { data: dashboardData, isLoading: dashLoading } = useDashboard(currentYear, currentMonth);
  const { data: budgets = [] } = useBudgets();
  const { data: categories = [] } = useCategories();

  // 최근 3개월 추세 데이터
  const month1 = new Date(currentYear, currentMonth - 3, 1);
  const month2 = new Date(currentYear, currentMonth - 2, 1);
  const { data: dash1 } = useDashboard(month1.getFullYear(), month1.getMonth() + 1);
  const { data: dash2 } = useDashboard(month2.getFullYear(), month2.getMonth() + 1);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  if (dashLoading || !dashboardData) {
    return (
      <div className="flex items-center justify-center h-64 gap-2 text-muted-foreground">
        <Loader2 className="size-5 animate-spin" />
        <span>대시보드 데이터를 불러오는 중...</span>
      </div>
    );
  }

  const { totalIncome, totalExpense, totalBalance, categoryStats } = dashboardData;
  const savingsRate = totalIncome > 0 ? ((totalBalance / totalIncome) * 100).toFixed(1) : '0';

  // 카테고리 차트 데이터
  const pieData = categoryStats
    .filter(s => s.totalAmount > 0)
    .map((s, i) => ({
      name: s.categoryName,
      value: s.totalAmount,
      color: CHART_COLORS[i % CHART_COLORS.length],
      ratio: s.ratio,
    }));

  // 최근 3개월 추세
  const monthlyTrend = [
    {
      date: `${month1.getFullYear()}.${month1.getMonth() + 1}`,
      수입: dash1?.totalIncome || 0,
      지출: dash1?.totalExpense || 0,
    },
    {
      date: `${month2.getFullYear()}.${month2.getMonth() + 1}`,
      수입: dash2?.totalIncome || 0,
      지출: dash2?.totalExpense || 0,
    },
    {
      date: `${currentYear}.${currentMonth}`,
      수입: totalIncome,
      지출: totalExpense,
    },
  ];

  // 예산 추적
  const budgetStatus = budgets.map((budget) => {
    const categoryStat = categoryStats.find(
      (s) => {
        const cat = categories.find(c => String(c.categoryId) === String(budget.categoryId));
        return cat && s.categoryName === cat.name;
      }
    );
    const spent = categoryStat ? categoryStat.totalAmount : 0;
    const percentage = budget.amount > 0 ? (spent / budget.amount) * 100 : 0;
    const categoryName = budget.categoryName ||
      categories.find(c => String(c.categoryId) === String(budget.categoryId))?.name ||
      '알 수 없음';
    return {
      category: categoryName,
      budget: budget.amount,
      spent,
      percentage,
      remaining: budget.amount - spent,
    };
  });

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
            <div className="text-2xl font-bold text-green-600">{formatCurrency(totalIncome)}원</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">이번 달 지출</CardTitle>
            <TrendingDown className="size-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">{formatCurrency(totalExpense)}원</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">순자산 변동</CardTitle>
            <Wallet className="size-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className={`text-2xl font-bold ${totalBalance >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {formatCurrency(totalBalance)}원
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm">저축률</CardTitle>
            <Target className="size-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">{savingsRate}%</div>
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
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, payload }) => `${name} ${(payload?.ratio ?? 0).toFixed(1)}%`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value: any) => `${formatCurrency(Number(value))}원`} />
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
                <Tooltip formatter={(value: any) => `${formatCurrency(Number(value))}원`} />
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