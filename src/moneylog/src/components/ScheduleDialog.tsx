import { useState } from 'react';
import { Button } from './ui/button';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
} from './ui/dialog';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from './ui/select';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Loader2, Settings } from 'lucide-react';
import { useSchedules, useUpdateSchedule } from '../api/queries';
import { Schedule, ScheduleReqDto } from '../types/schedule';
import { toast } from 'sonner';

interface ScheduleDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function ScheduleDialog({ open, onOpenChange }: ScheduleDialogProps) {
    const { data: schedules = [], isLoading } = useSchedules();
    const updateScheduleMut = useUpdateSchedule();

    const [editingJob, setEditingJob] = useState<Schedule | null>(null);

    // 편집 상태
    const [frequency, setFrequency] = useState<'DAILY' | 'WEEKLY' | 'MONTHLY'>('DAILY');
    const [time, setTime] = useState('00:00');
    const [dayOfWeek, setDayOfWeek] = useState<string>('1'); // 1(Mon)~7(Sun)
    const [dayOfMonth, setDayOfMonth] = useState<string>('1');

    const handleEditClick = (schedule: Schedule) => {
        setEditingJob(schedule);
        // Parse cron or set default if possible. 
        // Cron parsing on frontend is complex. Instead, we can just reset inputs or try to guess.
        // For now, let's just default to DAILY 03:00 (since we don't have a parser)
        // Or we could rely on Backend to send structured data if we updated ResDto. 
        // We only added List endpoint, which returns ScheduleResDto. 
        // ScheduleResDto has cronExpression only.
        // Let's set default values for User Input.
        setFrequency('DAILY');
        setTime('03:00');
        setDayOfWeek('1');
        setDayOfMonth('1');
    };

    const handleSave = async () => {
        if (!editingJob) return;

        const dto: ScheduleReqDto = {
            jobName: editingJob.jobName,
            frequency,
            time,
            dayOfWeek: frequency === 'WEEKLY' ? parseInt(dayOfWeek) : undefined,
            dayOfMonth: frequency === 'MONTHLY' ? parseInt(dayOfMonth) : undefined,
        };

        try {
            await updateScheduleMut.mutateAsync(dto);
            toast.success('스케줄이 업데이트되었습니다.');
            setEditingJob(null);
        } catch (e) {
            toast.error('스케줄 업데이트 실패');
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle>스케줄 작업 설정</DialogTitle>
                    <DialogDescription>
                        시스템 자동화 작업의 실행 주기를 설정합니다.
                    </DialogDescription>
                </DialogHeader>

                {isLoading ? (
                    <div className="flex justify-center p-4">
                        <Loader2 className="animate-spin" />
                    </div>
                ) : (
                    <div className="space-y-4">
                        {editingJob ? (
                            <div className="space-y-4 border p-4 rounded-md">
                                <div className="font-medium text-lg flex items-center gap-2">
                                    <Settings className="size-4" />
                                    {editingJob.description || editingJob.jobName} 수정
                                </div>

                                <div className="grid gap-2">
                                    <Label>실행 주기</Label>
                                    <Select value={frequency} onValueChange={(v: any) => setFrequency(v)}>
                                        <SelectTrigger>
                                            <SelectValue />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="DAILY">매일</SelectItem>
                                            <SelectItem value="WEEKLY">매주</SelectItem>
                                            <SelectItem value="MONTHLY">매월</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="grid gap-2">
                                    <Label>실행 시간</Label>
                                    <Input
                                        type="time"
                                        value={time}
                                        onChange={(e) => setTime(e.target.value)}
                                    />
                                </div>

                                {frequency === 'WEEKLY' && (
                                    <div className="grid gap-2">
                                        <Label>요일</Label>
                                        <Select value={dayOfWeek} onValueChange={setDayOfWeek}>
                                            <SelectTrigger><SelectValue /></SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="1">월요일</SelectItem>
                                                <SelectItem value="2">화요일</SelectItem>
                                                <SelectItem value="3">수요일</SelectItem>
                                                <SelectItem value="4">목요일</SelectItem>
                                                <SelectItem value="5">금요일</SelectItem>
                                                <SelectItem value="6">토요일</SelectItem>
                                                <SelectItem value="7">일요일</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                )}

                                {frequency === 'MONTHLY' && (
                                    <div className="grid gap-2">
                                        <Label>일 (1-31)</Label>
                                        <Input
                                            type="number"
                                            min={1}
                                            max={31}
                                            value={dayOfMonth}
                                            onChange={(e) => setDayOfMonth(e.target.value)}
                                        />
                                    </div>
                                )}

                                <div className="flex justify-end gap-2 pt-2">
                                    <Button variant="outline" onClick={() => setEditingJob(null)}>취소</Button>
                                    <Button onClick={handleSave} disabled={updateScheduleMut.isPending}>
                                        {updateScheduleMut.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                        저장
                                    </Button>
                                </div>
                            </div>
                        ) : (
                            <div className="space-y-2">
                                {schedules.map((job) => (
                                    <div key={job.jobName} className="flex items-center justify-between p-3 border rounded-md hover:bg-accent/50 transition-colors">
                                        <div>
                                            <div className="font-medium">{job.description || job.jobName}</div>
                                            <div className="text-sm text-muted-foreground">CRON: {job.cronExpression}</div>
                                        </div>
                                        <Button size="sm" variant="outline" onClick={() => handleEditClick(job)}>
                                            설정
                                        </Button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </DialogContent>
        </Dialog>
    );
}
