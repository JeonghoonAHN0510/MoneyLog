export const trimTextValue = (value: string | null | undefined) => value?.trim() ?? '';

export const trimOptionalTextValue = (value: string | null | undefined) => {
  if (value === null || value === undefined) {
    return undefined;
  }

  return value.trim();
};

export const isTrimmedBlank = (value: string | null | undefined) => trimTextValue(value).length === 0;

export const normalizePhoneValue = (value: string | null | undefined) =>
  value?.replace(/\D/g, '') ?? '';
