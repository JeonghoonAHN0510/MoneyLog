import { COMMON_HELPER_TEXT, COMMON_PLACEHOLDERS } from '../constants/commonPlaceholders';

export const AUTH_PLACEHOLDERS = {
  loginId: '4~20자 아이디',
  signupName: '예: 홍길동',
  email: 'name@example.com',
  bankSelect: COMMON_PLACEHOLDERS.bankSelect,
  accountNumber: COMMON_PLACEHOLDERS.accountNumber,
  phone: '01012345678',
} as const;

export const AUTH_HELPER_TEXT = {
  password: '최소 6자 이상 입력해 주세요.',
  phone: '하이픈 포함/미포함 모두 입력할 수 있습니다.',
  accountNumber: COMMON_HELPER_TEXT.accountNumber,
} as const;
