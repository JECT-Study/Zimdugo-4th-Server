package com.zimdugo.locker.infrastructure.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LockerSuggestSearchRepository extends ElasticsearchRepository<LockerSuggestDocument, String> {
}
