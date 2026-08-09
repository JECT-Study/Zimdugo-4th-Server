package com.zimdugo.locker.domain.search;

import java.util.Arrays;

public enum LockerSearchMatchSource {

    PLACE_NAME("place_name"),
    LOCKER_NAME("locker_name");

    private final String queryName;

    LockerSearchMatchSource(String queryName) {
        this.queryName = queryName;
    }

    public String queryName() {
        return queryName;
    }

    public static LockerSearchMatchSource fromQueryName(String queryName) {
        return Arrays.stream(values())
            .filter(source -> source.queryName.equals(queryName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 검색 매칭 원천입니다: " + queryName));
    }
}
