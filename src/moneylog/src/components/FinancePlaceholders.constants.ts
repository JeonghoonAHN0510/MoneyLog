export const FINANCE_SELECT_PLACEHOLDERS = {
  category: '카테고리를 선택해 주세요',
  payment: '결제수단을 선택해 주세요',
  account: '계좌를 선택해 주세요',
  bank: '은행을 선택해 주세요',
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
  accountNumber: '계좌번호를 입력해 주세요',
} as const;

export const FINANCE_HELPER_TEXT = {
  accountNumber: '하이픈 포함/미포함 모두 가능하며 저장 시 은행 형식으로 정리됩니다.',
} as const;
