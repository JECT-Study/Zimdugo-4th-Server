package com.zimdugo.locker.domain.search;

import java.util.List;
import java.util.UUID;

public interface SearchKeywordOutboxStore extends SearchKeywordEventStore {

    List<SearchKeywordOutboxEvent> claimPendingEvents(int batchSize);

    void complete(List<UUID> eventIds);
}
