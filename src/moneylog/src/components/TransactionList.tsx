import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Ledger, Category } from '../types/finance';
import { Trash, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { Badge } from './ui/badge';

interface TransactionListProps {
  transactions: Ledger[];
  categories: Category[];
  selectedDate?: string;
  onDelete: (id: string) => void;
}

export function TransactionList({
  transactions,
  categories,
  selectedDate,
  onDelete,
}: TransactionListProps) {
  const filteredTransactions = selectedDate
    ? transactions.filter((t) => t.date === selectedDate)
    : transactions;

  const sortedTransactions = [...filteredTransactions].sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );

  const getCategoryColor = (categoryName: string, type: 'income' | 'expense') => {
    const category = categories.find((c) => c.name === categoryName && c.type === type);
    return category?.color || '#64748b';
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  };

  const groupedByDate = sortedTransactions.reduce((acc, transaction) => {
    if (!acc[transaction.date]) {
      acc[transaction.date] = [];
    }
    acc[transaction.date].push(transaction);
    return acc;
  }, {} as Record<string, Transaction[]>);

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          {selectedDate ? `${formatDate(selectedDate)} 거래 내역` : '전체 거래 내역'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        {sortedTransactions.length === 0 ? (
          <div className="text-center text-muted-foreground py-8">
            거래 내역이 없습니다
          </div>
        ) : (
          <div className="space-y-6">
            {Object.entries(groupedByDate).map(([date, dateTransactions]) => (
              <div key={date} className="space-y-2">
                {!selectedDate && (
                  <div className="text-sm text-muted-foreground">{formatDate(date)}</div>
                )}
                <div className="space-y-2">
                  {dateTransactions.map((transaction) => (
                    <div
                      key={transaction.id}
                      className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors"
                    >
                      <div className="flex items-center gap-3 flex-1">
                        <div
                          className={`p-2 rounded-full ${
                            transaction.type === 'income' ? 'bg-green-100' : 'bg-red-100'
                          }`}
                        >
                          {transaction.type === 'income' ? (
                            <ArrowUpRight className="size-4 text-green-600" />
                          ) : (
                            <ArrowDownRight className="size-4 text-red-600" />
                          )}
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <span>{transaction.description || transaction.category}</span>
                            {transaction.isFixed && (
                              <Badge variant="secondary" className="text-xs">
                                고정
                              </Badge>
                            )}
                          </div>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <span
                              className="px-2 py-0.5 rounded"
                              style={{
                                backgroundColor: `${getCategoryColor(transaction.category, transaction.type)}20`,
                                color: getCategoryColor(transaction.category, transaction.type),
                              }}
                            >
                              {transaction.category}
                            </span>
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <span
                          className={
                            transaction.type === 'income' ? 'text-green-600' : 'text-red-600'
                          }
                        >
                          {transaction.type === 'income' ? '+' : '-'}
                          {formatCurrency(transaction.amount)}원
                        </span>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => onDelete(transaction.id)}
                        >
                          <Trash className="size-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
