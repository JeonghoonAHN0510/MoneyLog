import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Category } from '../types/finance';
import { List, Trash, Plus, Pencil } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';

interface CategoryManagerProps {
  categories: Category[];
  onAdd: (category: Omit<Category, "category_id" | "user_id" | "created_at" | "updated_at">) => void;
  onUpdate: (category: Partial<Category>) => void;
  onDelete: (category_id: string) => void;
}

const defaultColors = [
  '#ef4444',
  '#f59e0b',
  '#eab308',
  '#84cc16',
  '#22c55e',
  '#10b981',
  '#14b8a6',
  '#06b6d4',
  '#3b82f6',
  '#8b5cf6',
  '#ec4899',
  '#64748b',
];

export function CategoryManager({ categories, onAdd, onUpdate, onDelete }: CategoryManagerProps) {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  const [name, setName] = useState('');
  const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
  const [color, setColor] = useState(defaultColors[0]);
  const [category_id, setCategoryId] = useState('');

  const resetForm = () => {
    setName('');
    setType('EXPENSE');
    setColor(defaultColors[0]);
  };

  const handleAdd = () => {
    if (!name) return;

    onAdd({
      name,
      type,
      color,
    });

    resetForm();
    setIsAddDialogOpen(false);
  };

  const handleEdit = (category: Category) => {
    setEditingCategory(category);
    setName(category.name);
    setType(category.type);
    setColor(category.color);
    setCategoryId(category.category_id);
    setIsEditDialogOpen(true);
  };

  const handleUpdate = () => {
    if (!editingCategory || !name) return;

    onUpdate({
      category_id,
      name,
      type,
      color,
    });

    resetForm();
    setEditingCategory(null);
    setIsEditDialogOpen(false);
  };

  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');
  const incomeCategories = categories.filter((c) => c.type === 'INCOME');

  const CategoryForm = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="category-name">카테고리명</Label>
        <Input
          id="category-name"
          placeholder="식비"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </div>

      <div className="space-y-2">
        <Label>유형</Label>
        <div className="flex gap-2">
          <Button
            type="button"
            variant={type === 'EXPENSE' ? 'default' : 'outline'}
            className="flex-1"
            onClick={() => setType('EXPENSE')}
          >
            지출
          </Button>
          <Button
            type="button"
            variant={type === 'INCOME' ? 'default' : 'outline'}
            className="flex-1"
            onClick={() => setType('INCOME')}
          >
            수입
          </Button>
        </div>
      </div>

      <div className="space-y-2">
        <Label>색상</Label>
        <div className="grid grid-cols-6 gap-2">
          {defaultColors.map((c) => (
            <button
              key={c}
              type="button"
              className={`size-10 rounded-full border-2 ${
                color === c ? 'border-foreground' : 'border-transparent'
              }`}
              style={{ backgroundColor: c }}
              onClick={() => setColor(c)}
            />
          ))}
        </div>
      </div>
    </div>
  );

  const CategoryList = ({ items }: { items: Category[] }) => (
    <div className="space-y-2">
      {items.length === 0 ? (
        <div className="text-center text-muted-foreground py-4">카테고리가 없습니다</div>
      ) : (
        items.map((category) => (
          <div
            key={category.category_id}
            className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors"
          >
            <div className="flex items-center gap-3">
              <div
                className="size-6 rounded-full"
                style={{ backgroundColor: category.color }}
              />
              <span>{category.name}</span>
            </div>
            <div className="flex gap-2">
              <Button variant="ghost" size="icon" onClick={() => handleEdit(category)}>
                <Pencil className="size-4" />
              </Button>
              <Button variant="ghost" size="icon" onClick={() => onDelete(category.category_id)}>
                <Trash className="size-4" />
              </Button>
            </div>
          </div>
        ))
      )}
    </div>
  );

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <List className="size-5" />
              카테고리 관리
            </CardTitle>
            <Button onClick={() => setIsAddDialogOpen(true)} size="sm">
              <Plus className="size-4 mr-2" />
              카테고리 추가
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="EXPENSE">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="EXPENSE">
                지출 ({expenseCategories.length})
              </TabsTrigger>
              <TabsTrigger value="INCOME">
                수입 ({incomeCategories.length})
              </TabsTrigger>
            </TabsList>
            <TabsContent value="EXPENSE" className="mt-4">
              <CategoryList items={expenseCategories} />
            </TabsContent>
            <TabsContent value="INCOME" className="mt-4">
              <CategoryList items={incomeCategories} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Add Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>카테고리 추가</DialogTitle>
            <DialogDescription>새로운 카테고리를 추가하세요.</DialogDescription>
          </DialogHeader>
          <CategoryForm />
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
            <DialogTitle>카테고리 수정</DialogTitle>
            <DialogDescription>카테고리를 수정하세요.</DialogDescription>
          </DialogHeader>
          <CategoryForm />
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
    </>
  );
}