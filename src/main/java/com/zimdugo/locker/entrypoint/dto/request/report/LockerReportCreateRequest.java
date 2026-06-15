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
    @Schema(description = "Road address", example = "서울 마포구 양화로 160")
    @NotBlank
    @Size(max = 255)
    String roadAddress,

    @Schema(description = "Latitude", example = "37.556")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double latitude,

    @Schema(description = "Longitude", example = "126.923")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double longitude,

    @Schema(description = "Whether floor information exists", example = "true")
    @NotNull
    Boolean hasFloor,

    @Schema(description = "Floor type", example = "UNDERGROUND")
    @Size(max = 20)
    String floorType,

    @Schema(description = "Floor number", example = "2")
    Integer floorNumber,

    @Schema(description = "Indoor or outdoor", example = "INDOOR")
    @NotBlank
    @Size(max = 20)
    String indoorOutdoorType,

    @Schema(description = "Locker type", example = "SUBWAY_STATION")
    @NotBlank
    @Size(max = 20)
    String lockerType,

    @Schema(description = "Locker sizes", example = "[\"SMALL\",\"MEDIUM\",\"LARGE\"]")
    @NotEmpty
    @Size(max = 3)
    List<String> sizeTypes,

    @Schema(description = "Whether the locker is free", example = "false")
    Boolean isFree,

    @Schema(description = "Minimum price", example = "1000")
    Integer minPrice,

    @Schema(description = "Maximum price", example = "3000")
    Integer maxPrice,

    @Schema(description = "Whether the locker operates 24 hours", example = "false")
    @NotNull
    Boolean is24Hours,

    @Schema(description = "Operation start time", example = "09:00")
    LocalTime startTime,

    @Schema(description = "Operation end time", example = "22:30")
    LocalTime endTime,

    @Schema(description = "Additional information", example = "B2 entrance nearby")
    @Size(max = 255)
    String additionalInfo,

    @Schema(description = "Image URL", example = "https://cdn.example.com/locker/1.jpg")
    @Size(max = 500)
    String imageUrl,

    @Schema(description = "Location consent agreed", example = "true")
    Boolean locationConsentAgreed
) {
    @AssertTrue(message = "validation.invalid_floor")
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

    @AssertTrue(message = "validation.invalid_enum_value")
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

    @AssertTrue(message = "validation.invalid_size_types")
    public boolean isSizeTypesValid() {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return false;
        }

        Set<String> allowedSizeTypes = Set.of("SMALL", "MEDIUM", "LARGE");
        return sizeTypes.stream().allMatch(allowedSizeTypes::contains);
    }

    @AssertTrue(message = "validation.invalid_location_consent")
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
