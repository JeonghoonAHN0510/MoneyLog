import { useState } from 'react';
import { Card } from './ui/card';
import { Button } from './ui/button';
import { ChevronLeft, ChevronRight, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { Transaction } from '../types/finance';

interface CalendarViewProps {
  transactions: Transaction[];
  onDateClick: (date: string) => void;
}

export function CalendarView({ transactions, onDateClick }: CalendarViewProps) {
  const [currentDate, setCurrentDate] = useState(new Date());

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startingDayOfWeek = firstDay.getDay();

  const prevMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
  };

  const nextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
  };

  const getDayTransactions = (day: number) => {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return transactions.filter((t) => t.date === dateStr);
  };

  const getDaySummary = (day: number) => {
    const dayTransactions = getDayTransactions(day);
    const income = dayTransactions
      .filter((t) => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);
    const expense = dayTransactions
      .filter((t) => t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);
    return { income, expense, net: income - expense };
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const days = [];
  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push(<div key={`empty-${i}`} className="min-h-24" />);
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const summary = getDaySummary(day);
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const isToday = dateStr === new Date().toISOString().split('T')[0];
    const hasTransactions = summary.income > 0 || summary.expense > 0;

    days.push(
      <Card
        key={day}
        className={`min-h-24 p-2 cursor-pointer hover:bg-accent transition-colors ${
          isToday ? 'border-primary border-2' : ''
        }`}
        onClick={() => onDateClick(dateStr)}
      >
        <div className={isToday ? 'text-primary' : 'text-muted-foreground'}>
          {day}
        </div>
        {hasTransactions && (
          <div className="mt-1 space-y-1 text-xs">
            {summary.income > 0 && (
              <div className="flex items-center gap-1 text-green-600">
                <ArrowUpRight className="size-3" />
                <span>{formatCurrency(summary.income)}</span>
              </div>
            )}
            {summary.expense > 0 && (
              <div className="flex items-center gap-1 text-red-600">
                <ArrowDownRight className="size-3" />
                <span>{formatCurrency(summary.expense)}</span>
              </div>
            )}
          </div>
        )}
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2>
          {year}년 {month + 1}월
        </h2>
        <div className="flex gap-2">
          <Button variant="outline" size="icon" onClick={prevMonth}>
            <ChevronLeft className="size-4" />
          </Button>
          <Button variant="outline" size="icon" onClick={nextMonth}>
            <ChevronRight className="size-4" />
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-7 gap-2">
        {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
          <div key={day} className="text-center p-2">
            {day}
          </div>
        ))}
        {days}
      </div>
    </div>
  );
}
