package com.zimdugo.locker.entrypoint.dto.request.report;

import com.zimdugo.locker.application.LockerReportCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record LockerReportCreateRequest(
    @Schema(description = "도로명주소", example = "서울 마포구 양화로 160")
    @NotBlank
    @Size(max = 255)
    String roadAddress,

    @Schema(description = "위도", example = "37.556")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double latitude,

    @Schema(description = "경도", example = "126.923")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double longitude,

    @Schema(description = "층 정보 존재 여부", example = "true")
    @NotNull
    Boolean hasFloor,

    @Schema(description = "층수 유형", example = "UNDERGROUND")
    @Size(max = 20)
    String floorType,

    @Schema(description = "층수 숫자", example = "2")
    Integer floorNumber,

    @Schema(description = "실내/실외", example = "INDOOR")
    @NotBlank
    @Size(max = 20)
    String indoorOutdoorType,

    @Schema(description = "보관함 유형", example = "SUBWAY_STATION")
    @NotBlank
    @Size(max = 20)
    String lockerType,

    @Schema(description = "보관함 사이즈", example = "[\"SMALL\",\"MEDIUM\",\"LARGE\"]")
    @NotEmpty
    @Size(max = 3)
    List<String> sizeTypes,

    @Schema(description = "무료 여부", example = "false")
    Boolean isFree,

    @Schema(description = "최소 가격", example = "1000")
    Integer minPrice,

    @Schema(description = "최대 가격", example = "3000")
    Integer maxPrice,

    @Schema(description = "24시간 운영 여부", example = "false")
    @NotNull
    Boolean is24Hours,

    @Schema(description = "운영 시작 시간", example = "09:00")
    LocalTime startTime,

    @Schema(description = "운영 종료 시간", example = "22:30")
    LocalTime endTime,

    @Schema(description = "추가 정보", example = "2번 출구 근처")
    @Size(max = 255)
    String additionalInfo,

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/locker/1.jpg")
    @Size(max = 500)
    String imageUrl,

    @Schema(description = "위치 정보 제공 동의 여부", example = "true")
    Boolean locationConsentAgreed
) {
    @AssertTrue(message = "층 정보 입력이 올바르지 않습니다.")
    public boolean isFloorInputValid() {
        if (Boolean.TRUE.equals(hasFloor) && "OUTDOOR".equals(indoorOutdoorType)) {
            return false;
        }
        if (Boolean.FALSE.equals(hasFloor)) {
            return floorType == null && floorNumber == null;
        }
        if (Boolean.TRUE.equals(hasFloor)) {
            return isValidEnumValue(floorType, Set.of("ABOVE_GROUND", "UNDERGROUND"))
                && floorNumber != null
                && floorNumber > 0;
        }
        return false;
    }

    @AssertTrue(message = "보관함 분류 입력이 올바르지 않습니다.")
    public boolean isEnumInputValid() {
        return isValidEnumValue(indoorOutdoorType, Set.of("INDOOR", "OUTDOOR"))
            && isValidEnumValue(
            lockerType,
            Set.of(
                "MUSEUM",
                "SUBWAY_STATION",
                "DEPARTMENT_STORE",
                "CONVENIENCE_STORE",
                "PUBLIC_OFFICE",
                "PRIVATE_LOCKER",
                "TRAIN_STATION",
                "ETC"
            )
        );
    }

    @AssertTrue(message = "가격 정보 입력이 올바르지 않습니다.")
    public boolean isPriceInputValid() {
        if (isFree == null) {
            return true;
        }
        if (Boolean.TRUE.equals(isFree)) {
            return minPrice == null && maxPrice == null;
        }
        if (minPrice == null && maxPrice == null) {
            return false;
        }
        if (minPrice != null && minPrice < 0) {
            return false;
        }
        if (maxPrice != null && maxPrice < 0) {
            return false;
        }
        return minPrice == null || maxPrice == null || minPrice <= maxPrice;
    }

    @AssertTrue(message = "운영 시간 입력이 올바르지 않습니다.")
    public boolean isOperatingHoursValid() {
        if (is24Hours == null) {
            return false;
        }
        if (Boolean.TRUE.equals(is24Hours)) {
            return startTime == null && endTime == null;
        }
        if (startTime == null && endTime == null) {
            return true;
        }
        if (startTime == null || endTime == null) {
            return false;
        }
        return !startTime.isAfter(endTime);
    }

    @AssertTrue(message = "보관함 크기 입력이 올바르지 않습니다.")
    public boolean isSizeTypesValid() {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return false;
        }

        Set<String> allowedSizeTypes = Set.of("SMALL", "MEDIUM", "LARGE");
        return sizeTypes.stream().allMatch(allowedSizeTypes::contains);
    }

    @AssertTrue(message = "이미지 제보 시 위치 정보 제공 동의가 필요합니다.")
    public boolean isLocationConsentValid() {
        if (imageUrl == null || imageUrl.isBlank()) {
            return true;
        }
        return Boolean.TRUE.equals(locationConsentAgreed);
    }

    public LockerReportCreateCommand toCommand() {
        return new LockerReportCreateCommand(
            roadAddress,
            latitude,
            longitude,
            Boolean.TRUE.equals(hasFloor),
            floorType,
            floorNumber,
            indoorOutdoorType,
            lockerType,
            sizeTypes,
            isFree,
            minPrice,
            maxPrice,
            Boolean.TRUE.equals(is24Hours),
            startTime,
            endTime,
            additionalInfo,
            imageUrl,
            Boolean.TRUE.equals(locationConsentAgreed)
        );
    }

    private boolean isValidEnumValue(String value, Set<String> allowedValues) {
        return value != null && !value.isBlank() && allowedValues.contains(value);
    }
}
