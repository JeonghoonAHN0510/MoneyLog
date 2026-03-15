import { COMMON_HELPER_TEXT, COMMON_PLACEHOLDERS } from '../constants/commonPlaceholders';

export const FINANCE_SELECT_PLACEHOLDERS = {
  category: '카테고리를 선택해 주세요',
  payment: '결제수단을 선택해 주세요',
  account: '계좌를 선택해 주세요',
  bank: COMMON_PLACEHOLDERS.bankSelect,
  fromAccount: '출금 계좌를 선택해 주세요',
  toAccount: '입금 계좌를 선택해 주세요',
  importAccount: '매핑할 계좌 선택',
  importCategory: '매핑할 카테고리 선택',
  importPayment: '매핑할 결제수단 선택',
} as const;

export const FINANCE_INPUT_PLACEHOLDERS = {
  amount: '예: 15000',
  transactionTitle: '예: 점심 식사',
  fixedTitle: '예: 월세',
  memo: '필요한 경우에만 입력해 주세요',
  installmentCount: '2~36개월 입력',
  categoryName: '예: 식비',
  paymentName: '예: 국민카드',
  accountNickname: '예: 생활비 통장',
  accountNumber: COMMON_PLACEHOLDERS.accountNumber,
} as const;

export const FINANCE_HELPER_TEXT = {
  accountNumber: COMMON_HELPER_TEXT.accountNumber,
} as const;
