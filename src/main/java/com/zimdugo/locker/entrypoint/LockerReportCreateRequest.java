package com.zimdugo.locker.entrypoint;

import com.zimdugo.locker.application.LockerReportCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "층수 정보 존재 여부", example = "true")
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
    @Size(max = 3)
    List<String> sizeTypes,

    @Schema(description = "무료 여부", example = "false")
    Boolean isFree,

    @Schema(description = "최소 가격", example = "1000")
    Integer minPrice,

    @Schema(description = "최대 가격", example = "3000")
    Integer maxPrice,

    @Schema(description = "운영 시작 시간", example = "09:00")
    LocalTime startTime,

    @Schema(description = "운영 종료 시간", example = "22:30")
    LocalTime endTime,

    @Schema(description = "보관함 추가 정보", example = "B2 화장실 옆")
    @Size(max = 255)
    String additionalInfo,

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/locker/1.jpg")
    @Size(max = 500)
    String imageUrl,

    @Schema(description = "위치정보 수집 및 이용 동의", example = "true")
    @NotNull
    @AssertTrue
    Boolean locationConsentAgreed
) {

    private static final String DEFAULT_REPORT_NAME = "물품보관함";

    @AssertTrue(message = "validation.invalid_floor")
    public boolean isFloorInputValid() {
        if (Boolean.FALSE.equals(hasFloor)) {
            return floorType == null && floorNumber == null;
        }
        if (Boolean.TRUE.equals(hasFloor)) {
            return floorType != null && !floorType.isBlank() && floorNumber != null && floorNumber > 0;
        }
        return false;
    }

    @AssertTrue(message = "validation.invalid_price")
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

    @AssertTrue(message = "validation.invalid_operating_hours")
    public boolean isOperatingHoursValid() {
        if (startTime == null && endTime == null) {
            return true;
        }
        if (startTime == null || endTime == null) {
            return false;
        }
        return !startTime.isAfter(endTime);
    }

    @AssertTrue(message = "validation.invalid_size_types")
    public boolean isSizeTypesValid() {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return true;
        }

        Set<String> allowedSizeTypes = Set.of("SMALL", "MEDIUM", "LARGE");
        return sizeTypes.stream().allMatch(allowedSizeTypes::contains);
    }

    private String reportName() {
        if (additionalInfo == null || additionalInfo.isBlank()) {
            return DEFAULT_REPORT_NAME;
        }
        return additionalInfo;
    }

    public LockerReportCreateCommand toCommand() {
        return new LockerReportCreateCommand(
            reportName(),
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
            startTime,
            endTime,
            additionalInfo,
            imageUrl,
            Boolean.TRUE.equals(locationConsentAgreed)
        );
    }
}
