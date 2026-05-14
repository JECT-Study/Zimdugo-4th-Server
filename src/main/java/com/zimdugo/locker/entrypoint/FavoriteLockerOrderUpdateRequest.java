package com.zimdugo.locker.entrypoint;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record FavoriteLockerOrderUpdateRequest(
    @NotEmpty(message = "validation.not_empty")
    List<@NotNull @Positive Long> lockerIds
) {
}
