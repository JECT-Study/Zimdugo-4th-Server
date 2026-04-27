package com.zimdugo.locker.entrypoint;

import com.zimdugo.locker.application.LockerReportCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LockerReportCreateRequest(
    @Schema(example = "CREATE_NEW", allowableValues = {"CREATE_NEW", "ADD_TO_EXISTING"})
    @NotBlank
    String duplicateHandlingType,

    @Schema(description = "Existing locker id required for ADD_TO_EXISTING", example = "1")
    Long existingLockerId,

    @Schema(description = "Locker display name", example = "Hongdae Exit 2 Test Locker")
    @NotBlank
    @Size(max = 100)
    String name,

    @Schema(
        description = "Road address. If reverse geocoding fails, the user should enter it manually.",
        example = "160 Yanghwa-ro, Mapo-gu, Seoul"
    )
    @NotBlank
    @Size(max = 255)
    String roadAddress,

    @Schema(description = "Detailed location description", example = "Inside exit 2")
    @Size(max = 255)
    String detailLocation,

    @Schema(description = "Building name", example = "Hongdae Station")
    @Size(max = 100)
    String buildingName,

    @Schema(description = "Floor information", example = "B1")
    @Size(max = 30)
    String floor,

    @Schema(description = "Indoor or outdoor type", example = "INDOOR")
    @Size(max = 20)
    String indoorOutdoorType,

    @Schema(description = "Locker type. Defaults to UNKNOWN when omitted.", example = "UNKNOWN")
    @Size(max = 20)
    String lockerType,

    @Schema(description = "Size information", example = "S,M,L")
    @Size(max = 100)
    String sizeInfo,

    @Schema(description = "Price information", example = "1000~3000 KRW")
    @Size(max = 100)
    String priceInfo,

    @Schema(description = "Operating hours", example = "05:00~24:00")
    @Size(max = 100)
    String operatingHours,

    @Schema(description = "Image URL", example = "https://cdn.example.com/locker/1.jpg")
    @Size(max = 500)
    String imageUrl,

    @Schema(description = "Latitude", example = "37.556")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double latitude,

    @Schema(description = "Longitude", example = "126.923")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double longitude
) {

    private static final String DEFAULT_LOCKER_TYPE = "UNKNOWN";

    public String lockerTypeOrDefault() {
        if (lockerType == null || lockerType.isBlank()) {
            return DEFAULT_LOCKER_TYPE;
        }
        return lockerType;
    }

    public LockerReportCreateCommand toCommand() {
        return LockerReportCreateCommand.of(
            duplicateHandlingType,
            existingLockerId,
            name,
            roadAddress,
            detailLocation,
            buildingName,
            floor,
            indoorOutdoorType,
            lockerTypeOrDefault(),
            sizeInfo,
            priceInfo,
            operatingHours,
            imageUrl,
            latitude,
            longitude
        );
    }
}
