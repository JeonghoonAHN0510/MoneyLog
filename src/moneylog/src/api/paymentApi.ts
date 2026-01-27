import api from './axiosConfig';
import { toast } from 'sonner';
import { Payment } from '../types/finance';

// Helper to show error toast
const showError = (message: string) => {
  toast.error(message);
};

/**
 * GET: Fetches all payments for the logged-in user.
 */
export const getPayments = async (): Promise<Payment[]> => {
  try {
    const response = await api.get<Payment[]>('/payment');
    return response.data;
  } catch (error) {
    showError('결제 수단 목록을 불러오는데 실패했습니다.');
    return [];
  }
};

/**
 * POST: Adds a new payment method.
 */
export const addPayment = async (paymentData: Omit<Payment, 'payment_id' | 'user_id' | 'created_at' | 'updated_at'>): Promise<Payment | null> => {
  try {
    const response = await api.post<Payment>('/payment', paymentData);
    toast.success('결제 수단이 추가되었습니다.');
    return response.data;
  } catch (error) {
    showError('결제 수단 추가에 실패했습니다.');
    return null;
  }
};

/**
 * PUT: Updates an existing payment method.
 */
export const updatePayment = async (paymentData: Payment): Promise<Payment | null> => {
  try {
    const response = await api.put<Payment>('/payment', paymentData);
    toast.success('결제 수단이 수정되었습니다.');
    return response.data;
  } catch (error) {
    showError('결제 수단 수정에 실패했습니다.');
    return null;
  }
};

/**
 * DELETE: Deletes a payment method by its ID.
 */
export const deletePayment = async (payment_id: number): Promise<boolean> => {
  try {
    await api.delete('/payment', { params: { payment_id } });
    toast.success('결제 수단이 삭제되었습니다.');
    return true;
  } catch (error) {
    showError('결제 수단 삭제에 실패했습니다.');
    return false;
  }
};