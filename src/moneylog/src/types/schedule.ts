export interface Schedule {
    jobName: string;
    jobGroup: string;
    cronExpression: string;
    description: string;
    isActive: boolean;
}

export interface ScheduleReqDto {
    jobName: string;
    frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY';
    time: string; // HH:mm
    dayOfWeek?: number;
    dayOfMonth?: number;
}
