package com.moneylog_backend.moneylog.schedule.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.moneylog_backend.moneylog.schedule.service.ScheduleService;

@ExtendWith(MockitoExtension.class)
class ScheduleQuartzSyncListenerTest {
    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleQuartzSyncListener listener;

    @Test
    void 리스너는_AFTER_COMMIT_단계에서_실행된다() throws Exception {
        Method method = ScheduleQuartzSyncListener.class
            .getDeclaredMethod("handle", ScheduleQuartzSyncRequestedEvent.class);

        TransactionalEventListener annotation = method.getAnnotation(TransactionalEventListener.class);

        assertNotNull(annotation);
        assertEquals(TransactionPhase.AFTER_COMMIT, annotation.phase());
    }

    @Test
    void 리스너_내부_SchedulerException은_외부로_전파하지_않는다() throws Exception {
        doThrow(new SchedulerException("boom")).when(scheduleService).synchronizeQuartzJob("logCleanupJob");

        assertDoesNotThrow(() -> listener.handle(new ScheduleQuartzSyncRequestedEvent("logCleanupJob")));
    }

    @Test
    void 리스너_내부_RuntimeException도_외부로_전파하지_않는다() throws Exception {
        doThrow(new RuntimeException("boom")).when(scheduleService).synchronizeQuartzJob("logCleanupJob");

        assertDoesNotThrow(() -> listener.handle(new ScheduleQuartzSyncRequestedEvent("logCleanupJob")));
    }
}
