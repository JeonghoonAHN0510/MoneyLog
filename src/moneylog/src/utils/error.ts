import axios from 'axios';

type ErrorPayload = {
  errorMessage?: unknown;
  message?: unknown;
};

export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (!axios.isAxiosError(error)) {
    return fallback;
  }

  const responseData = error.response?.data;

  if (typeof responseData === 'string' && responseData.trim() !== '') {
    return responseData;
  }

  if (responseData && typeof responseData === 'object') {
    const payload = responseData as ErrorPayload;

    if (typeof payload.errorMessage === 'string' && payload.errorMessage.trim() !== '') {
      return payload.errorMessage;
    }

    if (typeof payload.message === 'string' && payload.message.trim() !== '') {
      return payload.message;
    }
  }

  return fallback;
}
