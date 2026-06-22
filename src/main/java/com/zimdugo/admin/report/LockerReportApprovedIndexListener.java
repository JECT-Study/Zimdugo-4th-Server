package com.zimdugo.admin.report;

import com.zimdugo.locker.infrastructure.search.LockerSuggestIndexSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LockerReportApprovedIndexListener {

    private final LockerSuggestIndexSyncService indexSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void reindex(LockerReportApprovedEvent event) {
        indexSyncService.reindexLocker(event.lockerId());
    }
}
