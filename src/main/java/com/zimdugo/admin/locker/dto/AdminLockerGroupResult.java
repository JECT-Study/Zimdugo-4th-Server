package com.zimdugo.admin.locker.dto;

import java.util.List;

public record AdminLockerGroupResult(
    Long placeId,
    String placeName,
    List<AdminLockerSummaryResult> lockers
) {
}
