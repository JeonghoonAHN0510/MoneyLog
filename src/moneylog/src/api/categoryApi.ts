import api from './axiosConfig';
import { toast } from 'sonner';
import { Category } from '../types/finance';

// Helper to show error toast
const showError = (message: string) => {
  toast.error(message);
};

/**
 * GET: Fetches all categories for the logged-in user.
 */
export const getCategories = async (): Promise<Category[]> => {
  try {
    const response = await api.get<Category[]>('/category');
    return response.data;
  } catch (error) {
    showError('카테고리 목록을 불러오는데 실패했습니다.');
    return [];
  }
};

/**
 * POST: Adds a new category.
 */
export const addCategory = async (categoryData: Omit<Category, 'category_id' | 'user_id' | 'created_at' | 'updated_at'>): Promise<Category | null> => {
  try {
    const response = await api.post<Category>('/category', categoryData);
    toast.success('카테고리가 추가되었습니다.');
    return response.data;
  } catch (error) {
    showError('카테고리 추가에 실패했습니다.');
    return null;
  }
};

/**
 * PUT: Updates an existing category.
 */
export const updateCategory = async (categoryData: Partial<Category> & Pick<Category, 'category_id'>): Promise<Category | null> => {
    try {
      const response = await api.put<Category>('/category', categoryData);
      toast.success('카테고리가 수정되었습니다.');
      return response.data;
    } catch (error) {
      showError('카테고리 수정에 실패했습니다.');
      return null;
    }
  };
  
/**
 * DELETE: Deletes a category by its ID.
 */
export const deleteCategory = async (category_id: number): Promise<boolean> => {
  try {
    await api.delete('/category', { params: { category_id } });
    toast.success('카테고리가 삭제되었습니다.');
    return true;
  } catch (error) {
    showError('카테고리 삭제에 실패했습니다.');
    return false;
  }
};