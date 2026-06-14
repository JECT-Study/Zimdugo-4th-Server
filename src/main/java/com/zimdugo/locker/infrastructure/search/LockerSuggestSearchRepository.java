package com.zimdugo.locker.infrastructure.search;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LockerSuggestSearchRepository extends ElasticsearchRepository<LockerSuggestDocument, String> {
    void deleteByPlaceIdIn(List<Long> placeIds);
}
