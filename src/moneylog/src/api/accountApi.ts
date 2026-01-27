import api from './axiosConfig';
import { toast } from 'sonner';
import { Account } from '../types/finance';

// Helper to show error toast
const showError = (message: string) => {
  toast.error(message);
};

/**
 * GET: Fetches all accounts for the logged-in user.
 */
export const getAccounts = async (): Promise<Account[]> => {
  try {
    const response = await api.get<Account[]>('/account/list');
    return response.data;
  } catch (error) {
    showError('계좌 목록을 불러오는데 실패했습니다.');
    return [];
  }
};

/**
 * POST: Adds a new account.
 */
export const addAccount = async (accountData: Omit<Account, 'account_id' | 'user_id' | 'created_at' | 'updated_at'>): Promise<Account | null> => {
  try {
    const response = await api.post<Account>('/account', accountData);
    toast.success('계좌가 추가되었습니다.');
    return response.data;
  } catch (error) {
    showError('계좌 추가에 실패했습니다.');
    return null;
  }
};

/**
 * PUT: Updates an existing account.
 */
export const updateAccount = async (accountData: Account): Promise<Account | null> => {
  try {
    const response = await api.put<Account>('/account', accountData);
    toast.success('계좌가 수정되었습니다.');
    return response.data;
  } catch (error) {
    showError('계좌 수정에 실패했습니다.');
    return null;
  }
};

/**
 * DELETE: Deletes an account by its ID.
 */
export const deleteAccount = async (account_id: number): Promise<boolean> => {
  try {
    await api.delete('/account', { params: { account_id } });
    toast.success('계좌가 삭제되었습니다.');
    return true;
  } catch (error) {
    showError('계좌 삭제에 실패했습니다.');
    return false;
  }
};

/**
 * PUT: Transfers an amount between two accounts.
 */
export const transfer = async (from_account_id: number, to_account_id: number, amount: number): Promise<boolean> => {
    try {
      await api.put('/account/transfer', { from_account_id, to_account_id, amount });
      toast.success('계좌 이체가 완료되었습니다.');
      return true;
    } catch (error) {
      showError('계좌 이체에 실패했습니다.');
      return false;
    }
  };
