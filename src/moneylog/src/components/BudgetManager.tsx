import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Budget, Category } from '../types/finance';
import { Target, Trash, Plus, Pencil } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
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

interface BudgetManagerProps {
    onAdd: (budget: Omit<Budget, 'budgetId' | "userId" | "budgetDate" | "createdAt" | "updatedAt" | "categoryName">) => void;
    onUpdate: (budget: Partial<Budget>) => void;
    onDelete: (budgetId: string) => void;
}

const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
};

// =========================================================
// 1. BudgetForm 컴포넌트
// =========================================================
interface BudgetFormProps {
    categoryId: string;
    setCategoryId: (val: string) => void;
    amount: number | string;
    setAmount: (val: string) => void;
    categories: Category[];
}

const BudgetForm = ({ categoryId, setCategoryId, amount, setAmount, categories }: BudgetFormProps) => (
    <div className="space-y-4">
        <div className="space-y-2">
            <Label htmlFor="budget-category">카테고리</Label>
            <Select value={categoryId} onValueChange={setCategoryId}>
                <SelectTrigger id="budget-category">
                    <SelectValue placeholder="카테고리 선택" />
                </SelectTrigger>
                <SelectContent>
                    {categories.map((cat) => (
                        <SelectItem key={cat.categoryId} value={cat.categoryId}>
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
    </div>
);

// =========================================================
// 2. BudgetList 컴포넌트
// =========================================================
const BudgetList = ({ items, categories, onEdit, onDelete }: {
    items: Budget[];
    categories: Category[];
    onEdit: (budget: Budget) => void;
    onDelete: (id: string) => void;
}) => (
    <div className="space-y-2">
        {items.length === 0 ? (
            <div className="text-center text-muted-foreground py-8">설정된 예산이 없습니다</div>
        ) : (
            items.map((budget) => {
                const categoryName = categories.find(c => c.categoryId === budget.categoryId)?.name || budget.categoryName || '알 수 없음';
                
                return (
                    <div
                        key={budget.budgetId}
                        className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors"
                    >
                        <div className="flex items-center gap-3">
                            <div className="p-2 bg-primary/10 rounded-full">
                                <Target className="size-4 text-primary" />
                            </div>
                            <div>
                                <div className="font-medium">{categoryName}</div>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <span className="font-medium">{formatCurrency(Number(budget.amount))}원</span>
                            <Button variant="ghost" size="icon" onClick={() => onEdit(budget)}>
                                <Pencil className="size-4" />
                            </Button>
                            <Button variant="ghost" size="icon" onClick={() => onDelete(budget.budgetId)}>
                                <Trash className="size-4" />
                            </Button>
                        </div>
                    </div>
                );
            })
        )}
    </div>
);

// =========================================================
// 3. 메인 BudgetManager 컴포넌트
// =========================================================
export function BudgetManager({ onAdd, onUpdate, onDelete }: BudgetManagerProps) {
    const { categories, budgets } = useResourceStore();

    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
    const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
    const [editingBudget, setEditingBudget] = useState<Budget | null>(null);
    const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);

    const [categoryId, setCategoryId] = useState('');
    const [amount, setAmount] = useState<number | string>('');

    const expenseCategories = categories.filter((category) => category.type === 'EXPENSE');

    const resetForm = () => {
        setCategoryId('');
        setAmount('');
    };

    const handleAdd = () => {
        if (!categoryId || !amount) return;

        onAdd({
            categoryId: categoryId,
            amount: Number(amount)
        });

        resetForm();
        setIsAddDialogOpen(false);
    };

    const handleEdit = (budget: Budget) => {
        setEditingBudget(budget);
        setCategoryId(budget.categoryId);
        setAmount(budget.amount);
        setIsEditDialogOpen(true);
    };

    const handleUpdate = () => {
        if (!editingBudget || !amount) return;

        onUpdate({
            budgetId: editingBudget.budgetId,
            categoryId: categoryId, 
            amount: Number(amount)
        });

        resetForm();
        setEditingBudget(null);
        setIsEditDialogOpen(false);
    };

    const handleDeleteClick = (id: string) => {
        setDeleteTargetId(id);
    };

    const confirmDelete = () => {
        if (!deleteTargetId) return;
        onDelete(deleteTargetId);
        setDeleteTargetId(null);
    };

    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <CardTitle className="flex items-center gap-2">
                            <Target className="size-5" />
                            예산 관리
                        </CardTitle>
                        <Button onClick={() => setIsAddDialogOpen(true)} size="sm">
                            <Plus className="size-4 mr-2" />
                            예산 추가
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    <BudgetList 
                        items={budgets} 
                        categories={categories}
                        onEdit={handleEdit} 
                        onDelete={handleDeleteClick} 
                    />
                </CardContent>
            </Card>

            {/* Add Dialog */}
            <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>예산 추가</DialogTitle>
                        <DialogDescription>새로운 카테고리 예산을 설정하세요.</DialogDescription>
                    </DialogHeader>
                    
                    <BudgetForm 
                        categoryId={categoryId} setCategoryId={setCategoryId}
                        amount={amount} setAmount={setAmount}
                        categories={expenseCategories}
                    />

                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsAddDialogOpen(false)}>
                            취소
                        </Button>
                        <Button className="flex-1" onClick={handleAdd}>
                            추가
                        </Button>
                    </div>
                </DialogContent>
            </Dialog>

            {/* Edit Dialog */}
            <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>예산 수정</DialogTitle>
                        <DialogDescription>설정된 예산을 수정합니다.</DialogDescription>
                    </DialogHeader>

                    <BudgetForm 
                        categoryId={categoryId} setCategoryId={setCategoryId}
                        amount={amount} setAmount={setAmount}
                        categories={expenseCategories}
                    />

                    <div className="flex gap-2 pt-4">
                        <Button variant="outline" className="flex-1" onClick={() => setIsEditDialogOpen(false)}>
                            취소
                        </Button>
                        <Button className="flex-1" onClick={handleUpdate}>
                            수정
                        </Button>
                    </div>
                </DialogContent>
            </Dialog>

            {/* Delete Alert Dialog */}
            <AlertDialog open={!!deleteTargetId} onOpenChange={(open) => !open && setDeleteTargetId(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>예산을 삭제하시겠습니까?</AlertDialogTitle>
                        <AlertDialogDescription>
                            설정된 예산 정보가 삭제됩니다.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel onClick={() => setDeleteTargetId(null)}>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={confirmDelete} className="bg-red-600 hover:bg-red-700 focus:ring-red-600">
                            삭제
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}