import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Transaction, Category } from '../types/finance';
import { Trash, ArrowUpRight, ArrowDownRight, Pencil } from 'lucide-react';
import { Badge } from './ui/badge';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "./ui/alert-dialog";
import useResourceStore from '../stores/resourceStore';

interface TransactionListProps {
    selectedDate?: string;
    onEdit: (transaction: Transaction) => void;
    onDelete: (id: string) => void;
}

// =========================================================
// 1. 개별 거래 항목 컴포넌트 (TransactionItem)
// =========================================================
interface TransactionItemProps {
    transaction: Transaction;
    categoryColor: string;
    onEdit: (transaction: Transaction) => void;
    onDeleteClick: (id: string) => void;
}

const TransactionItem = ({ transaction, categoryColor, onEdit, onDeleteClick }: TransactionItemProps) => {
    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('ko-KR').format(amount);
    };

    return (
        <div className="flex items-center justify-between p-2 hover:bg-accent/50 transition-colors rounded-md">
            <div className="flex items-center gap-3 flex-1">
                <div
                    className={`p-2 rounded-full ${transaction.categoryType === 'INCOME' ? 'bg-green-100' : 'bg-red-100'
                        }`}
                >
                    {transaction.categoryType === 'INCOME' ? (
                        <ArrowUpRight className="size-4 text-green-600" />
                    ) : (
                        <ArrowDownRight className="size-4 text-red-600" />
                    )}
                </div>
                <div className="flex-1">
                    <div className="flex items-center gap-2">
                        <span className="font-medium">{transaction.title || transaction.categoryName}</span>
                        {transaction.fixedId && (
                            <Badge variant="secondary" className="text-[10px] px-1 py-0 h-5">
                                고정
                            </Badge>
                        )}
                    </div>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground mt-0.5">
                        <span
                            className="px-1.5 py-0.5 rounded text-[10px]"
                            style={{
                                backgroundColor: `${categoryColor}20`,
                                color: categoryColor,
                            }}
                        >
                            {transaction.categoryName}
                        </span>
                        {transaction.paymentName && <span>· {transaction.paymentName}</span>}
                    </div>
                </div>
            </div>
            <div className="flex items-center gap-1 md:gap-2">
                <span
                    className={`text-sm md:text-base font-semibold ${transaction.categoryType === 'INCOME' ? 'text-green-600' : 'text-red-600'
                        }`}
                >
                    {transaction.categoryType === 'INCOME' ? '+' : '-'}
                    {formatCurrency(transaction.amount)}원
                </span>
                <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 md:h-8 md:w-8 text-muted-foreground hover:text-primary"
                    onClick={() => onEdit(transaction)}
                >
                    <Pencil className="size-3 md:size-4" />
                </Button>
                <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 md:h-8 md:w-8 text-muted-foreground hover:text-red-600"
                    onClick={() => onDeleteClick(transaction.transactionId)}
                >
                    <Trash className="size-3 md:size-4" />
                </Button>
            </div>
        </div>
    );
};

// =========================================================
// 2. 메인 TransactionList 컴포넌트
// =========================================================
export function TransactionList({
    selectedDate,
    onEdit,
    onDelete,
}: TransactionListProps) {
    const { transactions, categories } = useResourceStore();
    const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);

    const handleDeleteClick = (id: string) => {
        setDeleteTargetId(id);
    };

    const confirmDelete = () => {
        if (!deleteTargetId) return;
        onDelete(deleteTargetId);
        setDeleteTargetId(null);
    };
    const filteredTransactions = selectedDate
        ? transactions.filter((transaction) => {
            const datePart = transaction.tradingAt.split('T')[0];
            return datePart === selectedDate;
        })
        : transactions;

    const sortedTransactions = [...filteredTransactions].sort(
        (a, b) => new Date(b.tradingAt).getTime() - new Date(a.tradingAt).getTime()
    );

    const getCategoryColor = (categoryName: string, type: 'INCOME' | 'EXPENSE') => {
        const category = categories.find((c) => c.name === categoryName && c.type === type);
        return category?.color || '#64748b';
    };

    const formatDate = (dateStr: string) => {
        const date = new Date(dateStr);
        const days = ['일', '월', '화', '수', '목', '금', '토'];
        return `${date.getMonth() + 1}월 ${date.getDate()}일 (${days[date.getDay()]})`;
    };

    const getDailyTotal = (transactions: Transaction[]) => {
        return transactions.reduce((acc, curr) => {
            return curr.categoryType === 'INCOME' ? acc + curr.amount : acc - curr.amount;
        }, 0);
    };

    const groupedByDate = sortedTransactions.reduce((acc, transaction) => {
        const dateKey = transaction.tradingAt.split('T')[0];

        if (!acc[dateKey]) {
            acc[dateKey] = [];
        }
        acc[dateKey].push(transaction);
        return acc;
    }, {} as Record<string, Transaction[]>);

    return (
        <Card className="border-none shadow-none">
            <CardHeader className="px-0 pt-0">
                <CardTitle>
                    {selectedDate ? `${formatDate(selectedDate)} 거래 내역` : '전체 거래 내역'}
                </CardTitle>
            </CardHeader>
            <CardContent className="px-0">
                {sortedTransactions.length === 0 ? (
                    <div className="text-center text-muted-foreground py-12 border rounded-lg border-dashed">
                        거래 내역이 없습니다
                    </div>
                ) : (
                    <div className="space-y-6">
                        {Object.entries(groupedByDate).map(([date, dateTransactions]) => {
                            const dailyTotal = getDailyTotal(dateTransactions);

                            return (
                                <div key={date} className="border rounded-xl bg-card shadow-sm overflow-hidden">
                                    <div className="bg-muted/30 p-3 flex justify-between items-center border-b">
                                        <span className="font-medium text-sm text-foreground">
                                            {formatDate(date)}
                                        </span>
                                        <span className={`text-sm font-semibold ${dailyTotal > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                            {dailyTotal > 0 ? '+' : ''}{new Intl.NumberFormat('ko-KR').format(dailyTotal)}원
                                        </span>
                                    </div>

                                    <div className="p-2 space-y-1">
                                        {dateTransactions.map((transaction) => (
                                            <TransactionItem
                                                key={transaction.transactionId}
                                                transaction={transaction}
                                                categoryColor={getCategoryColor(transaction.categoryName, transaction.categoryType)}
                                                onEdit={onEdit}
                                                onDeleteClick={handleDeleteClick}
                                            />
                                        ))}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </CardContent>

            <AlertDialog open={!!deleteTargetId} onOpenChange={(open) => !open && setDeleteTargetId(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>정말 삭제하시겠습니까?</AlertDialogTitle>
                        <AlertDialogDescription>
                            삭제된 거래 내역은 복구할 수 없습니다.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel onClick={() => setDeleteTargetId(null)}>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={confirmDelete} className="bg-red-600 hover:bg-red-700 focus:ring-red-600">삭제</AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </Card>
    );
}