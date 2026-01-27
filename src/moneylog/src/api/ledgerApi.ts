import api from './axiosConfig';
import { toast } from 'sonner';
import { Ledger } from '../types/finance';

// Helper to show error toast
const showError = (message: string) => {
  toast.error(message);
};

/**
 * GET: Fetches all ledger entries for the logged-in user.
 */
export const getLedgers = async (): Promise<Ledger[]> => {
  try {
    const response = await api.get<Ledger[]>('/ledger');
    return response.data;
  } catch (error) {
    showError('거래 내역을 불러오는데 실패했습니다.');
    return [];
  }
};

/**
 * POST: Adds a new ledger entry.
 * The backend will assign the ID.
 */
export const addLedger = async (ledgerData: Omit<Ledger, 'ledger_id' | 'user_id' | 'created_at' | 'updated_at'>): Promise<Ledger | null> => {
  try {
    const response = await api.post<Ledger>('/ledger', ledgerData);
    toast.success('거래 내역이 추가되었습니다.');
    return response.data;
  } catch (error) {
    showError('거래 내역 추가에 실패했습니다.');
    return null;
  }
};

/**
 * PUT: Updates an existing ledger entry.
 */
export const updateLedger = async (ledgerData: Ledger): Promise<Ledger | null> => {
  try {
    const response = await api.put<Ledger>('/ledger', ledgerData);
    toast.success('거래 내역이 수정되었습니다.');
    return response.data;
  } catch (error) {
    showError('거래 내역 수정에 실패했습니다.');
    return null;
  }
};

/**
 * DELETE: Deletes a ledger entry by its ID.
 */
export const deleteLedger = async (ledger_id: number): Promise<boolean> => {
  try {
    await api.delete('/ledger', { params: { ledger_id } });
    toast.success('거래 내역이 삭제되었습니다.');
    return true;
  } catch (error) {
    showError('거래 내역 삭제에 실패했습니다.');
    return false;
  }
};