export type ScheduleFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface Schedule {
    jobName: string;
    jobGroup: string;
    cronExpression: string;
    description: string | null;
    isActive: boolean;
}

export interface ScheduleReqDto {
    jobName: string;
    frequency: ScheduleFrequency;
    time: string; // HH:mm
    dayOfWeek?: number;
    dayOfMonth?: number;
}
