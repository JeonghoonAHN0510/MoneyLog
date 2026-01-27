import api from './axiosConfig';
import { toast } from 'sonner';
import { Budget } from '../types/finance';

// Helper to show error toast
const showError = (message: string) => {
  toast.error(message);
};

/**
 * GET: Fetches all budgets for the logged-in user.
 */
export const getBudgets = async (): Promise<Budget[]> => {
  try {
    const response = await api.get<Budget[]>('/budget');
    return response.data;
  } catch (error) {
    showError('예산 목록을 불러오는데 실패했습니다.');
    return [];
  }
};

/**
 * POST: Adds a new budget.
 */
export const addBudget = async (budgetData: Omit<Budget, 'budget_id' | 'user_id' | 'created_at' | 'updated_at'>): Promise<Budget | null> => {
  try {
    const response = await api.post<Budget>('/budget', budgetData);
    toast.success('예산이 추가되었습니다.');
    return response.data;
  } catch (error) {
    showError('예산 추가에 실패했습니다.');
    return null;
  }
};

/**
 * PUT: Updates an existing budget.
 */
export const updateBudget = async (budgetData: Budget): Promise<Budget | null> => {
  try {
    const response = await api.put<Budget>('/budget', budgetData);
    toast.success('예산이 수정되었습니다.');
    return response.data;
  } catch (error) {
    showError('예산 수정에 실패했습니다.');
    return null;
  }
};

/**
 * DELETE: Deletes a budget by its ID.
 */
export const deleteBudget = async (budget_id: number): Promise<boolean> => {
  try {
    await api.delete('/budget', { params: { budget_id } });
    toast.success('예산이 삭제되었습니다.');
    return true;
  } catch (error) {
    showError('예산 삭제에 실패했습니다.');
    return false;
  }
};