import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Category } from '../types/finance';
import { List, Trash, Plus, Pencil } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from './ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';

interface CategoryManagerProps {
  categories: Category[];
  onAdd: (category: Omit<Category, "category_id" | "user_id" | "created_at" | "updated_at">) => void;
  onUpdate: (category: Partial<Category> & Pick<Category, 'category_id'>) => void;
  onDelete: (category_id: string) => void;
}

const defaultColors = [
  '#ef4444', '#f59e0b', '#eab308', '#84cc16', '#22c55e', '#10b981',
  '#14b8a6', '#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899', '#64748b',
];

interface CategoryFormProps {
  name: string;
  setName: (name: string) => void;
  type: 'INCOME' | 'EXPENSE';
  setType: (type: 'INCOME' | 'EXPENSE') => void;
  color: string;
  setColor: (color: string) => void;
}

function CategoryForm({ name, setName, type, setType, color, setColor }: CategoryFormProps) {
  return (
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
              className={`size-10 rounded-full border-2 ${color === c ? 'border-foreground' : 'border-transparent'}`}
              style={{ backgroundColor: c }}
              onClick={() => setColor(c)}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

interface CategoryListProps {
  items: Category[];
  onEdit: (category: Category) => void;
  onDelete: (category_id: string) => void;
}

function CategoryList({ items, onEdit, onDelete }: CategoryListProps) {
  return (
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
              <div className="size-6 rounded-full" style={{ backgroundColor: category.color }} />
              <span>{category.name}</span>
            </div>
            <div className="flex gap-2">
              <Button variant="ghost" size="icon" onClick={() => onEdit(category)}>
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
}

interface CategoryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  category: Category | null;
  onAdd: (category: Omit<Category, "category_id" | "user_id" | "created_at" | "updated_at">) => void;
  onUpdate: (category: Partial<Category> & Pick<Category, 'category_id'>) => void;
}

function CategoryDialog({ open, onOpenChange, category, onAdd, onUpdate }: CategoryDialogProps) {
  const [name, setName] = useState('');
  const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
  const [color, setColor] = useState(defaultColors[0]);

  const isEditMode = category !== null;

  useEffect(() => {
    if (open) {
      if (isEditMode) {
        setName(category.name);
        setType(category.type);
        setColor(category.color);
      } else {
        setName('');
        setType('EXPENSE');
        setColor(defaultColors[0]);
      }
    }
  }, [open, category, isEditMode]);

  const handleSubmit = () => {
    if (!name) return;

    if (isEditMode) {
      onUpdate({ category_id: category.category_id, name, type, color });
    } else {
      onAdd({ name, type, color });
    }
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditMode ? '카테고리 수정' : '카테고리 추가'}</DialogTitle>
          <DialogDescription>
            {isEditMode ? '카테고리를 수정하세요.' : '새로운 카테고리를 추가하세요.'}
          </DialogDescription>
        </DialogHeader>
        <CategoryForm name={name} setName={setName} type={type} setType={setType} color={color} setColor={setColor} />
        <DialogFooter className="flex gap-2 pt-4">
          <Button variant="outline" className="flex-1" onClick={() => onOpenChange(false)}>취소</Button>
          <Button className="flex-1" onClick={handleSubmit}>{isEditMode ? '수정' : '추가'}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export function CategoryManager({ categories, onAdd, onUpdate, onDelete }: CategoryManagerProps) {
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  const handleOpenAddDialog = () => {
    setEditingCategory(null);
    setIsDialogOpen(true);
  };

  const handleOpenEditDialog = (category: Category) => {
    setEditingCategory(category);
    setIsDialogOpen(true);
  };

  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');
  const incomeCategories = categories.filter((c) => c.type === 'INCOME');

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <List className="size-5" />
              카테고리 관리
            </CardTitle>
            <Button onClick={handleOpenAddDialog} size="sm">
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
              <CategoryList items={expenseCategories} onEdit={handleOpenEditDialog} onDelete={onDelete} />
            </TabsContent>
            <TabsContent value="INCOME" className="mt-4">
              <CategoryList items={incomeCategories} onEdit={handleOpenEditDialog} onDelete={onDelete} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      <CategoryDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        category={editingCategory}
        onAdd={onAdd}
        onUpdate={onUpdate}
      />
    </>
  );
}