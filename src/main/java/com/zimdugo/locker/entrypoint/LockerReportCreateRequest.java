package com.zimdugo.locker.entrypoint;

import com.zimdugo.locker.domain.DuplicateHandlingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LockerReportCreateRequest(
    @Schema(example = "CREATE_NEW", allowableValues = {"CREATE_NEW", "ADD_TO_EXISTING"})
    @NotNull
    DuplicateHandlingType duplicateHandlingType,

    @Schema(description = "ADD_TO_EXISTING 선택 시 필수", example = "1")
    Long existingLockerId,

    @Schema(description = "보관함 이름", example = "홍대입구역 보관함")
    @NotBlank
    @Size(max = 100)
    String name,

    @Schema(description = "도로명 주소. 주소 변환 실패 시 비워둘 수 있다.", example = "서울 마포구 양화로 160")
    @Size(max = 255)
    String roadAddress,

    @Schema(description = "상세 위치", example = "2번 출구 안쪽")
    @Size(max = 255)
    String detailLocation,

    @Schema(description = "건물명", example = "홍대입구역")
    @Size(max = 100)
    String buildingName,

    @Schema(description = "층수", example = "B1")
    @Size(max = 30)
    String floor,

    @Schema(description = "실내/실외 여부", example = "INDOOR")
    @Size(max = 20)
    String indoorOutdoorType,

    @Schema(description = "보관함 유형. 값이 없으면 UNKNOWN으로 처리한다.", example = "UNKNOWN")
    @Size(max = 20)
    String lockerType,

    @Schema(description = "규격 정보", example = "S,M,L")
    @Size(max = 100)
    String sizeInfo,

    @Schema(description = "가격 정보", example = "1000~3000원")
    @Size(max = 100)
    String priceInfo,

    @Schema(description = "운영시간", example = "05:00~24:00")
    @Size(max = 100)
    String operatingHours,

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/locker/1.jpg")
    @Size(max = 500)
    String imageUrl,

    @Schema(description = "위도", example = "37.556")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double latitude,

    @Schema(description = "경도", example = "126.923")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double longitude
) {

    private static final String DEFAULT_LOCKER_TYPE = "UNKNOWN";

    @AssertTrue
    public boolean isExistingLockerIdValid() {
        if (duplicateHandlingType != DuplicateHandlingType.ADD_TO_EXISTING) {
            return true;
        }
        return existingLockerId != null;
    }

    public String lockerTypeOrDefault() {
        if (lockerType == null || lockerType.isBlank()) {
            return DEFAULT_LOCKER_TYPE;
        }
        return lockerType;
    }
}
