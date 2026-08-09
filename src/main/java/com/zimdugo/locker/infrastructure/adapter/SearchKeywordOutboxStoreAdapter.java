package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.search.SearchKeywordOutboxEvent;
import com.zimdugo.locker.domain.search.SearchKeywordOutboxStore;
import com.zimdugo.locker.infrastructure.persistence.SearchKeywordOutboxEntity;
import com.zimdugo.locker.infrastructure.persistence.SearchKeywordOutboxRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchKeywordOutboxStoreAdapter implements SearchKeywordOutboxStore {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final SearchKeywordOutboxRepository searchKeywordOutboxRepository;

    @Override
    public void record(String rawKeyword, String normalizedKeyword) {
        searchKeywordOutboxRepository.save(SearchKeywordOutboxEntity.pending(
            rawKeyword,
            normalizedKeyword,
            LocalDateTime.now(SEOUL_ZONE)
        ));
    }

    @Override
    public List<SearchKeywordOutboxEvent> claimPendingEvents(int batchSize) {
        return searchKeywordOutboxRepository.lockPendingEvents(batchSize).stream()
            .map(this::toEvent)
            .toList();
    }

    @Override
    public void complete(List<UUID> eventIds) {
        searchKeywordOutboxRepository.complete(eventIds, LocalDateTime.now(SEOUL_ZONE));
    }

    private SearchKeywordOutboxEvent toEvent(SearchKeywordOutboxEntity entity) {
        return new SearchKeywordOutboxEvent(
            entity.getId(),
            entity.getRawKeyword(),
            entity.getNormalizedKeyword(),
            entity.getSearchedAt()
        );
    }
}
