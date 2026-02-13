import { useState } from 'react';
import '../styles/components/CalendarView.css';
import { Card } from './ui/card';
import { Button } from './ui/button';
import { ChevronLeft, ChevronRight, ArrowUpRight, ArrowDownRight, Loader2 } from 'lucide-react';
import { useCalendar } from '../api/queries';
import { formatKrw } from '../utils/currency';

interface CalendarViewProps {
  onDateClick: (date: string) => void;
}

export function CalendarView({ onDateClick }: CalendarViewProps) {
  const [currentDate, setCurrentDate] = useState(new Date());

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth(); // 0-indexed

  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startingDayOfWeek = firstDay.getDay();

  // TanStack Query로 캘린더 데이터 조회
  const { data: dailySummaries = [], isLoading } = useCalendar(year, month + 1);

  // 날짜별 요약 Map 생성
  const summaryMap = new Map<string, { totalIncome: number; totalExpense: number }>();
  dailySummaries.forEach((s) => {
    summaryMap.set(s.date, s);
  });

  const prevMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
  };

  const nextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
  };

  const days = [];
  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push(<div key={`empty-${i}`} className="calendar-day-empty" />);
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const summary = summaryMap.get(dateStr);
    const isToday = dateStr === new Date().toISOString().split('T')[0];
    const hasData = summary && (summary.totalIncome > 0 || summary.totalExpense > 0);

    days.push(
      <Card
        key={day}
        className={`calendar-day-card ${isToday ? 'calendar-day-card-today' : ''}`}
        onClick={() => onDateClick(dateStr)}
      >
        <div className={isToday ? 'calendar-day-number-today' : 'calendar-day-number'}>
          {day}
        </div>
        {hasData && (
          <div className="calendar-summary-container">
            {summary!.totalIncome > 0 && (
              <div className="calendar-summary-income">
                <ArrowUpRight className="calendar-summary-icon" />
                <span>{formatKrw(summary!.totalIncome)}</span>
              </div>
            )}
            {summary!.totalExpense > 0 && (
              <div className="calendar-summary-expense">
                <ArrowDownRight className="calendar-summary-icon" />
                <span>{formatKrw(summary!.totalExpense)}</span>
              </div>
            )}
          </div>
        )}
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <div className="calendar-header">
        <h2 className="calendar-title">
          {year}년 {month + 1}월
        </h2>
        <div className="calendar-nav-group">
          {isLoading && <Loader2 className="calendar-loading" />}
          <Button variant="outline" size="icon" onClick={prevMonth}>
            <ChevronLeft className="calendar-nav-icon" />
          </Button>
          <Button variant="outline" size="icon" onClick={nextMonth}>
            <ChevronRight className="calendar-nav-icon" />
          </Button>
        </div>
      </div>

      <div className="calendar-wrapper">
        <div className="calendar-grid">
          {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
            <div key={day} className="calendar-day-header">
              {day}
            </div>
          ))}
          {days}
        </div>
      </div>
    </div>
  );
}
