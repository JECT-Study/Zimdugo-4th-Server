package com.zimdugo.locker.entrypoint;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record FavoriteLockerOrderUpdateRequest(
    @NotEmpty(message = "validation.not_empty")
    List<Long> lockerIds
) {
}
