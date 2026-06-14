package com.zimdugo.admin.i18n;

import com.zimdugo.locker.infrastructure.search.LockerSuggestIndexSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LockerContentI18nIndexListener {

    private final LockerSuggestIndexSyncService indexSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void reindex(LockerContentI18nChangedEvent event) {
        if (event.placeId() != null) {
            indexSyncService.reindexPlace(event.placeId());
        } else if (event.lockerId() != null) {
            indexSyncService.reindexLocker(event.lockerId());
        }
    }
}
