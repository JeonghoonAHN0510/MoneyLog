import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Progress } from './ui/progress';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ReferenceLine,
} from 'recharts';
import { TrendingUp, TrendingDown, Wallet, Target, Loader2, ArrowUpRight, Minus } from 'lucide-react';
import { useDashboard, useBudgets, useCategories } from '../api/queries';
import { ChartContainer, ChartTooltip, ChartTooltipContent } from './ui/chart';
import { formatKrw } from '../utils/currency';
import { CHART_COLORS, TREND_CHART_CONFIG } from './DashboardView.constants';

type TrendMonthPoint = {
  monthLabel: string;
  income: number | null;
  expense: number | null;
  net: number | null;
  isCurrent: boolean;
  state: 'ready' | 'error';
};

function isReadyTrendPoint(item: TrendMonthPoint): item is TrendMonthPoint & { income: number; expense: number; net: number; state: 'ready' } {
  return item.state === 'ready' && item.income !== null && item.expense !== null && item.net !== null;
}

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
  const { data: dash1, isLoading: trendMonth1Loading, isError: trendMonth1Error } = useDashboard(month1.getFullYear(), month1.getMonth() + 1);
  const { data: dash2, isLoading: trendMonth2Loading, isError: trendMonth2Error } = useDashboard(month2.getFullYear(), month2.getMonth() + 1);

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
  const formatSignedAmount = (value: number) => {
    const sign = value > 0 ? '+' : value < 0 ? '-' : '';
    return `${sign}${formatKrw(Math.abs(value))}원`;
  };
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
    .filter((s) => s.totalAmount > 0)
    .map((s, i) => ({
      name: s.categoryName,
      value: s.totalAmount,
      color: CHART_COLORS[i % CHART_COLORS.length],
      ratio: s.ratio,
    }));

  // 최근 3개월 추세
  const monthlyTrend: TrendMonthPoint[] = [
    {
      monthLabel: `${month1.getFullYear()}.${month1.getMonth() + 1}`,
      income: trendMonth1Error ? null : dash1?.totalIncome ?? 0,
      expense: trendMonth1Error ? null : dash1?.totalExpense ?? 0,
      net: trendMonth1Error ? null : (dash1?.totalIncome ?? 0) - (dash1?.totalExpense ?? 0),
      isCurrent: false,
      state: trendMonth1Error ? 'error' : 'ready',
    },
    {
      monthLabel: `${month2.getFullYear()}.${month2.getMonth() + 1}`,
      income: trendMonth2Error ? null : dash2?.totalIncome ?? 0,
      expense: trendMonth2Error ? null : dash2?.totalExpense ?? 0,
      net: trendMonth2Error ? null : (dash2?.totalIncome ?? 0) - (dash2?.totalExpense ?? 0),
      isCurrent: false,
      state: trendMonth2Error ? 'error' : 'ready',
    },
    {
      monthLabel: `${currentYear}.${currentMonth}`,
      income: totalIncome,
      expense: totalExpense,
      net: totalIncome - totalExpense,
      isCurrent: true,
      state: 'ready',
    },
  ];
  const readyTrendItems = monthlyTrend.filter(isReadyTrendPoint);
  const trendPanelLoading = trendMonth1Loading || trendMonth2Loading;
  const trendHasComparisonError = trendMonth1Error || trendMonth2Error;
  const trendHasAnyData = readyTrendItems.some((item) => item.income > 0 || item.expense > 0);
  const previousTrend = monthlyTrend[1];
  const currentTrend = monthlyTrend[2];
  const currentNet = currentTrend.net ?? 0;
  const netDelta = isReadyTrendPoint(previousTrend) ? currentNet - previousTrend.net : null;
  const bestNetMonth = trendHasComparisonError ? null : readyTrendItems.reduce((best, current) => (current.net > best.net ? current : best), readyTrendItems[0]);
  const highestExpenseMonth = trendHasComparisonError ? null : readyTrendItems.reduce((best, current) => (current.expense > best.expense ? current : best), readyTrendItems[0]);
  const trendHasAnyExpense = readyTrendItems.some((item) => item.expense > 0);
  const currentNetChipClass = currentNet > 0
    ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
    : currentNet < 0
      ? 'border-rose-200 bg-rose-50 text-rose-700'
      : 'border-slate-200 bg-slate-50 text-slate-600';
  const deltaChipClass = netDelta === null
    ? 'border-slate-200 bg-slate-50 text-slate-600'
    : netDelta > 0
      ? 'border-sky-200 bg-sky-50 text-sky-700'
      : netDelta < 0
        ? 'border-amber-200 bg-amber-50 text-amber-700'
        : 'border-slate-200 bg-slate-50 text-slate-600';
  const currentNetChipText = currentNet === 0
    ? '이번 달 순흐름 변동 없음'
    : `이번 달 순흐름 ${formatSignedAmount(currentNet)}`;
  const deltaChipText = netDelta === null
    ? '전월 비교 불가'
    : netDelta === 0
      ? '전월 대비 변동 없음'
      : `전월 대비 ${formatSignedAmount(netDelta)}`;
  const neutralChip = 'bg-slate-50 text-slate-600 border-slate-200';

  // 예산 추적
  const budgetStatus = budgets.map((budget) => {
    const categoryStat = categoryStats.find(
      (s) => {
        const cat = categories.find((c) => String(c.categoryId) === String(budget.categoryId));
        return cat && s.categoryName === cat.name;
      },
    );
    const spent = categoryStat ? categoryStat.totalAmount : 0;
    const percentage = budget.amount > 0 ? (spent / budget.amount) * 100 : 0;
    const categoryName = budget.categoryName ||
      categories.find((c) => String(c.categoryId) === String(budget.categoryId))?.name ||
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
      badgeText: totalIncome > 0 ? '현금유입' : '수입 없음',
      badgeIcon: totalIncome > 0 ? ArrowUpRight : Minus,
      icon: TrendingUp,
      tone: totalIncome > 0 ? 'text-emerald-600' : 'text-slate-600',
      chip: totalIncome > 0 ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : neutralChip,
      subText: totalIncome > 0 ? '지난 달 대비 추세 확인' : '이번 달 기록된 수입이 없습니다',
    },
    {
      title: '이번 달 지출',
      value: `${formatKrw(totalExpense)}원`,
      badgeText: totalExpense > 0 ? '현금유출' : '지출 없음',
      badgeIcon: totalExpense > 0 ? ArrowUpRight : Minus,
      icon: TrendingDown,
      tone: totalExpense > 0 ? 'text-rose-600' : 'text-slate-600',
      chip: totalExpense > 0 ? 'bg-rose-50 text-rose-700 border-rose-200' : neutralChip,
      subText: totalExpense > 0 ? '카테고리별 사용 확인' : '이번 달 기록된 지출이 없습니다',
    },
    {
      title: '순자산 변동',
      value: `${formatKrw(totalBalance)}원`,
      badgeText: totalBalance > 0 ? '흑자' : totalBalance < 0 ? '적자' : '변동 없음',
      badgeIcon: totalBalance === 0 ? Minus : ArrowUpRight,
      icon: Wallet,
      tone: totalBalance > 0 ? 'text-sky-700' : totalBalance < 0 ? 'text-rose-700' : 'text-slate-600',
      chip: totalBalance > 0
        ? 'bg-sky-50 text-sky-700 border-sky-200'
        : totalBalance < 0
          ? 'bg-rose-50 text-rose-700 border-rose-200'
          : neutralChip,
      subText: totalBalance > 0 ? '흑자 흐름 유지 중' : totalBalance < 0 ? '지출 최적화 필요' : '수입과 지출이 같은 수준',
    },
    {
      title: '저축률',
      value: totalIncome > 0 ? `${savingsRate}%` : '-',
      badgeText: totalIncome === 0 ? '집계 대기' : savingsRateValue >= 20 ? '우수' : savingsRateValue >= 0 ? '보통' : '주의',
      badgeIcon: totalIncome === 0 ? Minus : ArrowUpRight,
      icon: Target,
      tone: totalIncome === 0 ? 'text-slate-600' : savingsRateValue >= 20 ? 'text-emerald-700' : savingsRateValue >= 0 ? 'text-amber-700' : 'text-rose-700',
      chip: totalIncome === 0
        ? neutralChip
        : savingsRateValue >= 20
          ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
          : savingsRateValue >= 0
            ? 'bg-amber-50 text-amber-700 border-amber-200'
            : 'bg-rose-50 text-rose-700 border-rose-200',
      subText: totalIncome > 0 ? '수입 대비 잔여 비율' : '수입이 기록되면 계산됩니다',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {summaryCards.map((card) => {
          const Icon = card.icon;
          const BadgeIcon = card.badgeIcon;
          return (
            <Card key={card.title} className="border-border/70 bg-gradient-to-b from-background to-muted/20 shadow-sm">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-sm font-medium text-muted-foreground">{card.title}</CardTitle>
                  <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] ${card.chip}`}>
                    <BadgeIcon className="mr-1 size-3" />
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
        <Card className="border-border/70 bg-gradient-to-br from-background via-white to-sky-50/60 shadow-sm">
          <CardHeader className="pb-1">
            <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
              <div className="space-y-1">
                <CardTitle className="text-base">최근 3개월 추세</CardTitle>
                <p className="text-xs text-muted-foreground">월별 수입, 지출, 순흐름을 한 장에서 읽는 인사이트 패널</p>
              </div>
              {!trendPanelLoading && (
                <div className="flex flex-wrap gap-2">
                  <span className={`inline-flex items-center rounded-full border px-3 py-1 text-[11px] font-medium ${currentNetChipClass}`}>
                    {currentNetChipText}
                  </span>
                  <span className={`inline-flex items-center rounded-full border px-3 py-1 text-[11px] font-medium ${deltaChipClass}`}>
                    {deltaChipText}
                  </span>
                </div>
              )}
            </div>
          </CardHeader>
          <CardContent className="space-y-4 pt-4">
            {trendPanelLoading ? (
              <div className="space-y-4">
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                  {Array.from({ length: 3 }).map((_, index) => (
                    <div key={index} className="animate-pulse rounded-2xl border border-border/60 bg-white/60 p-4">
                      <div className="mb-3 h-4 w-20 rounded bg-muted/70" />
                      <div className="mb-2 h-6 w-28 rounded bg-muted/70" />
                      <div className="h-3 w-24 rounded bg-muted/60" />
                    </div>
                  ))}
                </div>
                <div className="h-[320px] animate-pulse rounded-2xl border border-border/60 bg-white/60" />
              </div>
            ) : (trendHasComparisonError || trendHasAnyData) ? (
              <>
                {trendHasComparisonError ? (
                  <div className="rounded-2xl border border-dashed border-amber-200 bg-amber-50/70 px-4 py-3 text-xs text-amber-800">
                    비교 월 데이터 일부를 불러오지 못해 최고 순흐름과 최대 지출 인사이트는 숨겼습니다.
                  </div>
                ) : (
                  <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
                    <span className="rounded-full border border-border/60 bg-white/80 px-3 py-1">
                      최고 순흐름 {bestNetMonth?.monthLabel}
                    </span>
                    <span className="rounded-full border border-border/60 bg-white/80 px-3 py-1">
                      {trendHasAnyExpense ? `최대 지출 ${highestExpenseMonth?.monthLabel}` : '지출 집계 없음'}
                    </span>
                  </div>
                )}
                <div className="grid gap-4 xl:grid-cols-[minmax(0,320px),1fr]">
                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-3 xl:grid-cols-1">
                    {monthlyTrend.map((item) => {
                      const incomeTone = item.income !== null && item.income > 0 ? 'text-emerald-700' : 'text-slate-500';
                      const expenseTone = item.expense !== null && item.expense > 0 ? 'text-rose-700' : 'text-slate-500';
                      const netTone = item.net !== null && item.net > 0
                        ? 'text-emerald-700'
                        : item.net !== null && item.net < 0
                          ? 'text-rose-700'
                          : 'text-slate-600';
                      const cardSurface = item.state === 'error'
                        ? 'border-amber-200 bg-amber-50/60'
                        : item.isCurrent
                          ? 'border-sky-200 bg-sky-50/80 shadow-sm'
                          : 'border-border/60 bg-white/80';
                      const badgeClass = item.isCurrent
                        ? 'bg-sky-100 text-sky-700'
                        : item.state === 'error'
                          ? 'bg-amber-100 text-amber-700'
                          : 'bg-slate-100 text-slate-600';
                      const badgeText = item.isCurrent ? '이번 달' : item.state === 'error' ? '집계 실패' : '비교 월';

                      return (
                        <div key={item.monthLabel} className={`rounded-2xl border p-4 ${cardSurface}`}>
                          <div className="mb-3 flex items-center justify-between">
                            <div className="text-sm font-semibold">{item.monthLabel}</div>
                            <span className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${badgeClass}`}>
                              {badgeText}
                            </span>
                          </div>
                          {item.state === 'error' ? (
                            <div className="rounded-xl border border-dashed border-amber-200 bg-white/70 px-3 py-4 text-xs text-amber-800">
                              데이터를 불러오지 못했습니다
                            </div>
                          ) : (
                            <>
                              <div className="space-y-2 text-xs">
                                <div className="flex items-center justify-between text-muted-foreground">
                                  <span>수입</span>
                                  <span className={`font-medium ${incomeTone}`}>{formatKrw(item.income ?? 0)}원</span>
                                </div>
                                <div className="flex items-center justify-between text-muted-foreground">
                                  <span>지출</span>
                                  <span className={`font-medium ${expenseTone}`}>{formatKrw(item.expense ?? 0)}원</span>
                                </div>
                              </div>
                              <div className="mt-4 border-t border-border/50 pt-3">
                                <div className="text-[11px] text-muted-foreground">순흐름</div>
                                <div className={`mt-1 text-lg font-semibold tracking-tight ${netTone}`}>
                                  {formatSignedAmount(item.net ?? 0)}
                                </div>
                              </div>
                            </>
                          )}
                        </div>
                      );
                    })}
                  </div>

                  <div className="rounded-2xl border border-border/60 bg-white/75 p-3">
                    <ChartContainer config={TREND_CHART_CONFIG} className="h-[320px] w-full">
                      <ComposedChart data={monthlyTrend} margin={{ top: 12, right: 12, left: 8, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                        <ReferenceLine y={0} stroke="hsl(var(--border))" strokeDasharray="4 4" />
                        <XAxis dataKey="monthLabel" tickLine={false} axisLine={false} />
                        <YAxis tickLine={false} axisLine={false} width={78} tickFormatter={formatAxisAmount} />
                        <ChartTooltip
                          content={(
                            <ChartTooltipContent
                              indicator="line"
                              labelFormatter={(label) => `${label} 흐름`}
                              formatter={(value, name) => {
                                const seriesLabel = TREND_CHART_CONFIG[String(name) as keyof typeof TREND_CHART_CONFIG]?.label ?? String(name);
                                return (
                                  <div className="flex min-w-[150px] items-center justify-between gap-4">
                                    <span className="text-muted-foreground">{seriesLabel}</span>
                                    <span className="font-mono font-medium text-foreground">
                                      {formatKrw(Number(value))}원
                                    </span>
                                  </div>
                                );
                              }}
                            />
                          )}
                        />
                        <Bar dataKey="income" radius={[10, 10, 4, 4]} maxBarSize={24}>
                          {monthlyTrend.map((item) => (
                            <Cell key={`income-${item.monthLabel}`} fill={item.isCurrent ? '#16a34a' : '#a7f3d0'} />
                          ))}
                        </Bar>
                        <Bar dataKey="expense" radius={[10, 10, 4, 4]} maxBarSize={24}>
                          {monthlyTrend.map((item) => (
                            <Cell key={`expense-${item.monthLabel}`} fill={item.isCurrent ? '#dc2626' : '#fca5a5'} />
                          ))}
                        </Bar>
                        <Line
                          type="monotone"
                          dataKey="net"
                          stroke="var(--color-net)"
                          strokeWidth={3}
                          dot={{ r: 4, strokeWidth: 0, fill: 'var(--color-net)' }}
                          activeDot={{ r: 6, fill: 'var(--color-net)' }}
                        />
                      </ComposedChart>
                    </ChartContainer>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex h-[320px] flex-col items-center justify-center rounded-2xl border border-dashed border-border/70 bg-white/60 px-6 text-center">
                <div className="text-sm font-medium text-foreground">아직 집계된 최근 3개월 흐름이 없습니다</div>
                <p className="mt-2 text-xs text-muted-foreground">
                  거래를 추가하면 수입, 지출, 순흐름을 비교하는 인사이트 패널이 여기에 표시됩니다.
                </p>
              </div>
            )}
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
