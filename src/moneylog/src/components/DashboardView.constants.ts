import type { ChartConfig } from './ui/chart';

export const CHART_COLORS = [
  '#1558b3',
  '#1d73d6',
  '#2b8fe5',
  '#0ea5c6',
  '#2f96c6',
  '#2f778f',
  '#22c55e',
  '#38bdf8',
  '#1e40af',
  '#0f766e',
  '#2563eb',
  '#64748b',
];

export const TREND_CHART_CONFIG = {
  income: {
    label: '수입',
    color: '#16a34a',
  },
  expense: {
    label: '지출',
    color: '#dc2626',
  },
  net: {
    label: '순흐름',
    color: '#1558b3',
  },
} satisfies ChartConfig;
