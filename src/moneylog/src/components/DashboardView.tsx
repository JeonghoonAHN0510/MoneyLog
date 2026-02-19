import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Progress } from './ui/progress';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { TrendingUp, TrendingDown, Wallet, Target, Loader2, ArrowUpRight } from 'lucide-react';
import { useDashboard, useBudgets, useCategories } from '../api/queries';
import { formatKrw } from '../utils/currency';

// 카테고리 색상 팔레트 (순환 사용)
const CHART_COLORS = [
  '#0ea5e9', '#22c55e', '#f97316', '#ef4444', '#14b8a6',
  '#eab308', '#3b82f6', '#06b6d4', '#84cc16', '#f59e0b',
  '#10b981', '#f43f5e',
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

  if (dashLoading || !dashboardData) {
    return (
      <div className="flex items-center justify-center h-64 gap-2 text-muted-foreground">
        <Loader2 className="size-5 animate-spin" />
        <span>대시보드 데이터를 불러오는 중...</span>
      </div>
    );
  }

  const { totalIncome, totalExpense, totalBalance, categoryStats } = dashboardData;
  const savingsRateValue = totalIncome > 0 ? (totalBalance / totalIncome) * 100 : 0;
  const savingsRate = savingsRateValue.toFixed(1);
  const formatAxisAmount = (value: number) => {
    const abs = Math.abs(value);
    if (abs >= 100000000) {
      return `${(value / 100000000).toFixed(1)}억`;
    }
    if (abs >= 10000) {
      return `${Math.round(value / 10000)}만`;
    }
    return value.toLocaleString('ko-KR');
  };

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

  const summaryCards = [
    {
      title: '이번 달 수입',
      value: `${formatKrw(totalIncome)}원`,
      badgeText: '현금유입',
      icon: TrendingUp,
      tone: 'text-emerald-600',
      chip: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      subText: '지난 달 대비 추세 확인',
    },
    {
      title: '이번 달 지출',
      value: `${formatKrw(totalExpense)}원`,
      badgeText: '현금유출',
      icon: TrendingDown,
      tone: 'text-rose-600',
      chip: 'bg-rose-50 text-rose-700 border-rose-200',
      subText: '카테고리별 사용 확인',
    },
    {
      title: '순자산 변동',
      value: `${formatKrw(totalBalance)}원`,
      badgeText: totalBalance >= 0 ? '흑자' : '적자',
      icon: Wallet,
      tone: totalBalance >= 0 ? 'text-sky-700' : 'text-rose-700',
      chip: totalBalance >= 0
        ? 'bg-sky-50 text-sky-700 border-sky-200'
        : 'bg-rose-50 text-rose-700 border-rose-200',
      subText: totalBalance >= 0 ? '흑자 흐름 유지 중' : '지출 최적화 필요',
    },
    {
      title: '저축률',
      value: `${savingsRate}%`,
      badgeText: savingsRateValue >= 20 ? '우수' : savingsRateValue >= 0 ? '보통' : '주의',
      icon: Target,
      tone: savingsRateValue >= 20 ? 'text-emerald-700' : savingsRateValue >= 0 ? 'text-amber-700' : 'text-rose-700',
      chip: savingsRateValue >= 20
        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
        : savingsRateValue >= 0
          ? 'bg-amber-50 text-amber-700 border-amber-200'
          : 'bg-rose-50 text-rose-700 border-rose-200',
      subText: '수입 대비 잔여 비율',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {summaryCards.map((card) => {
          const Icon = card.icon;
          return (
            <Card key={card.title} className="border-border/70 bg-gradient-to-b from-background to-muted/20 shadow-sm">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-sm font-medium text-muted-foreground">{card.title}</CardTitle>
                  <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] ${card.chip}`}>
                    <ArrowUpRight className="mr-1 size-3" />
                    {card.badgeText}
                  </span>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-end justify-between gap-2">
                  <div className={`text-2xl font-bold tracking-tight ${card.tone}`}>{card.value}</div>
                  <Icon className={`size-5 ${card.tone}`} />
                </div>
                <p className="text-xs text-muted-foreground">{card.subText}</p>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        {/* Category Breakdown */}
        <Card className="border-border/70 shadow-sm">
          <CardHeader className="pb-0">
            <CardTitle className="text-base">카테고리별 지출</CardTitle>
            <p className="text-xs text-muted-foreground">이번 달 지출 비중과 상위 카테고리</p>
          </CardHeader>
          <CardContent>
            {pieData.length > 0 ? (
              <div className="grid grid-cols-1 gap-4 md:grid-cols-[1.1fr,0.9fr] md:items-center">
                <div className="h-[280px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={pieData}
                        dataKey="value"
                        innerRadius={66}
                        outerRadius={100}
                        paddingAngle={3}
                        stroke="hsl(var(--background))"
                        strokeWidth={3}
                      >
                        {pieData.map((entry) => (
                          <Cell key={`cell-${entry.name}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value: number) => `${formatKrw(Number(value))}원`}
                        contentStyle={{
                          borderRadius: '12px',
                          border: '1px solid hsl(var(--border))',
                          background: 'hsl(var(--background))',
                        }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="space-y-2">
                  {pieData.slice(0, 6).map((item) => (
                    <div key={item.name} className="flex items-center justify-between rounded-lg border border-border/60 px-3 py-2">
                      <div className="flex items-center gap-2">
                        <span className="size-2.5 rounded-full" style={{ backgroundColor: item.color }} />
                        <span className="text-sm">{item.name}</span>
                      </div>
                      <span className="text-sm font-medium">{item.ratio.toFixed(1)}%</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                이번 달 지출 내역이 없습니다
              </div>
            )}
          </CardContent>
        </Card>

        {/* Monthly Trend */}
        <Card className="border-border/70 shadow-sm">
          <CardHeader className="pb-0">
            <CardTitle className="text-base">최근 3개월 추세</CardTitle>
            <p className="text-xs text-muted-foreground">월별 수입/지출 흐름 비교</p>
          </CardHeader>
          <CardContent>
            <div className="h-[300px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthlyTrend} barGap={8} margin={{ top: 8, right: 8, left: 20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                  <XAxis dataKey="date" tickLine={false} axisLine={false} />
                  <YAxis
                    tickLine={false}
                    axisLine={false}
                    width={78}
                    tickFormatter={formatAxisAmount}
                  />
                  <Tooltip
                    formatter={(value: number) => `${formatKrw(Number(value))}원`}
                    contentStyle={{
                      borderRadius: '12px',
                      border: '1px solid hsl(var(--border))',
                      background: 'hsl(var(--background))',
                    }}
                  />
                  <Bar dataKey="수입" fill="#16a34a" radius={[8, 8, 0, 0]} maxBarSize={26} />
                  <Bar dataKey="지출" fill="#dc2626" radius={[8, 8, 0, 0]} maxBarSize={26} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Budget Tracking */}
      <Card className="border-border/70 shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-base">예산 사용 현황</CardTitle>
          <p className="text-xs text-muted-foreground">예산 대비 사용률과 잔여 금액</p>
        </CardHeader>
        <CardContent className="space-y-4">
          {budgetStatus.length > 0 ? (
            budgetStatus.map((budget) => (
              <div key={budget.category} className="space-y-2 rounded-lg border border-border/60 p-3">
                <div className="flex justify-between text-sm">
                  <span className="font-medium">{budget.category}</span>
                  <span className={budget.percentage > 100 ? 'text-red-600' : ''}>
                    {formatKrw(budget.spent)} / {formatKrw(budget.budget)}원
                  </span>
                </div>
                <Progress
                  value={Math.min(budget.percentage, 100)}
                  className={budget.percentage > 90 ? 'bg-rose-100' : 'bg-emerald-100/60'}
                />
                <div className="flex justify-between text-xs text-muted-foreground">
                  <span>{budget.percentage.toFixed(1)}% 사용</span>
                  <span>
                    {budget.remaining > 0
                      ? `${formatKrw(budget.remaining)}원 남음`
                      : `${formatKrw(Math.abs(budget.remaining))}원 초과`}
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
