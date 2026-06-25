package com.zimdugo.admin.locker.dto;

import com.zimdugo.admin.locker.dto.AdminLockerDetailResult;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public class AdminLockerForm {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String roadAddress;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double longitude;

    private Long placeId;

    @NotNull
    private LockerType lockerType = LockerType.ETC;

    @NotNull
    private IndoorOutdoorType indoorOutdoorType = IndoorOutdoorType.INDOOR;

    private GroundLevelType groundLevelType;

    private Integer floor;

    private Integer minPrice;

    private Integer maxPrice;

    private List<LockerSizeType> lockerSizes = List.of();

    @Size(max = 1000)
    private String detailInfo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    @Size(max = 500)
    private String imageUrl;

    private MultipartFile imageFile;

    public static AdminLockerForm from(AdminLockerDetailResult result) {
        AdminLockerForm form = new AdminLockerForm();
        form.name = result.name();
        form.roadAddress = result.roadAddress();
        form.latitude = result.latitude();
        form.longitude = result.longitude();
        form.placeId = result.placeId();
        form.lockerType = result.lockerType();
        form.indoorOutdoorType = result.indoorOutdoorType();
        form.groundLevelType = result.groundLevelType();
        form.floor = result.floor();
        form.minPrice = result.minPrice();
        form.maxPrice = result.maxPrice();
        form.lockerSizes = List.copyOf(result.lockerSizes() == null ? Set.of() : result.lockerSizes());
        form.detailInfo = result.detailInfo();
        form.startTime = result.startTime();
        form.endTime = result.endTime();
        form.imageUrl = result.imageUrl();
        return form;
    }

    public AdminLockerCommand toCommand() {
        return new AdminLockerCommand(
            trimmed(name),
            trimmed(roadAddress),
            latitude,
            longitude,
            placeId,
            lockerType,
            indoorOutdoorType,
            groundLevelType,
            floor,
            minPrice,
            maxPrice,
            new LinkedHashSet<>(lockerSizes == null ? List.of() : lockerSizes),
            trimmedToNull(detailInfo),
            startTime,
            endTime,
            trimmedToNull(imageUrl)
        );
    }

    private String trimmed(String value) {
        return value == null ? null : value.trim();
    }

    private String trimmedToNull(String value) {
        String trimmed = trimmed(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public LockerType getLockerType() {
        return lockerType;
    }

    public void setLockerType(LockerType lockerType) {
        this.lockerType = lockerType;
    }

    public IndoorOutdoorType getIndoorOutdoorType() {
        return indoorOutdoorType;
    }

    public void setIndoorOutdoorType(IndoorOutdoorType indoorOutdoorType) {
        this.indoorOutdoorType = indoorOutdoorType;
    }

    public GroundLevelType getGroundLevelType() {
        return groundLevelType;
    }

    public void setGroundLevelType(GroundLevelType groundLevelType) {
        this.groundLevelType = groundLevelType;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<LockerSizeType> getLockerSizes() {
        return lockerSizes;
    }

    public void setLockerSizes(List<LockerSizeType> lockerSizes) {
        this.lockerSizes = lockerSizes;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}
