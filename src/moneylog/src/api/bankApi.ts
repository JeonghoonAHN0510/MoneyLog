import api from './axiosConfig';
import { toast } from 'sonner';
import { Bank } from '../types/finance';

/**
 * GET: Fetches all bank details.
 * This is considered reference data and should not change often.
 */
export const getBanks = async (): Promise<Bank[]> => {
  try {
    const response = await api.get<Bank[]>('/bank');
    return response.data;
  } catch (error) {
    toast.error('은행 목록을 불러오는데 실패했습니다.');
    return [];
  }
};