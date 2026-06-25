package com.zimdugo.admin.locker.dto;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Page;

public record AdminLockerPageResult(
    List<AdminLockerGroupResult> groups,
    int number,
    int totalPages,
    long totalElements,
    boolean first,
    boolean last,
    boolean hasContent
) {

    public static AdminLockerPageResult from(Page<AdminLockerSummaryResult> page) {
        return new AdminLockerPageResult(
            groups(page.getContent()),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isFirst(),
            page.isLast(),
            page.hasContent()
        );
    }

    public static AdminLockerPageResult fromGroups(
        Page<?> groupPage,
        List<AdminLockerSummaryResult> lockers
    ) {
        return new AdminLockerPageResult(
            groups(lockers),
            groupPage.getNumber(),
            groupPage.getTotalPages(),
            groupPage.getTotalElements(),
            groupPage.isFirst(),
            groupPage.isLast(),
            groupPage.hasContent()
        );
    }

    private static List<AdminLockerGroupResult> groups(List<AdminLockerSummaryResult> lockers) {
        Map<Long, List<AdminLockerSummaryResult>> byPlace = new LinkedHashMap<>();
        for (AdminLockerSummaryResult locker : lockers) {
            byPlace.computeIfAbsent(locker.placeId(), ignored -> new ArrayList<>()).add(locker);
        }
        return byPlace.values().stream()
            .map(group -> new AdminLockerGroupResult(
                group.getFirst().placeId(),
                group.getFirst().placeName() == null ? "장소 미지정" : group.getFirst().placeName(),
                List.copyOf(group)
            ))
            .toList();
    }
}
