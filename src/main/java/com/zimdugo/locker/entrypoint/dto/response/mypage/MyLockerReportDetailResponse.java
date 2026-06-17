package com.zimdugo.locker.entrypoint.dto.response.mypage;

import com.zimdugo.locker.application.result.mypage.MyLockerReportDetailResult;
import java.time.LocalTime;
import java.util.List;

public record MyLockerReportDetailResponse(
    Long reportId,
    String lockerName,
    String roadAddress,
    double latitude,
    double longitude,
    boolean hasFloor,
    String floorType,
    Integer floorNumber,
    String indoorOutdoorType,
    String lockerType,
    List<String> sizeTypes,
    String priceType,
    Integer minPrice,
    Integer maxPrice,
    String operatingTimeType,
    LocalTime startTime,
    LocalTime endTime,
    String additionalInfo,
    String imageUrl,
    boolean locationConsentAgreed
) {
    public static MyLockerReportDetailResponse from(MyLockerReportDetailResult result) {
        return new MyLockerReportDetailResponse(
            result.reportId(),
            result.lockerName(),
            result.roadAddress(),
            result.latitude(),
            result.longitude(),
            result.hasFloor(),
            result.floorType(),
            result.floorNumber(),
            result.indoorOutdoorType(),
            result.lockerType(),
            result.sizeTypes(),
            result.priceType(),
            result.minPrice(),
            result.maxPrice(),
            result.operatingTimeType(),
            result.startTime(),
            result.endTime(),
            result.additionalInfo(),
            result.imageUrl(),
            result.locationConsentAgreed()
        );
    }
}
