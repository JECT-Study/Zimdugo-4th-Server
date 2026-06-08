package com.zimdugo.locker.entrypoint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LockerVoteRequest(
    @NotBlank(message = "투표 타입은 필수입니다.")
    @Pattern(regexp = "CORRECT|INCORRECT", message = "올바르지 않은 투표 타입입니다.")
    @Schema(description = "투표 타입 (CORRECT: 정확해요, INCORRECT: 부정확해요)", example = "CORRECT")
    String voteType
) {
}
