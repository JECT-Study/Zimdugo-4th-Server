package com.zimdugo.locker.entrypoint.dto.request.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LockerReportCreateRequestTest {

    @Test
    void acceptsPriceWhenBothValuesAreMissingOrPresent() {
        assertThat(request(null, null, null, null).isPriceInputValid()).isTrue();
        assertThat(request(0, 0, null, null).isPriceInputValid()).isTrue();
        assertThat(request(1000, 3000, null, null).isPriceInputValid()).isTrue();
    }

    @Test
    void rejectsPriceWhenOnlyOneValueIsMissing() {
        assertThat(request(null, 3000, null, null).isPriceInputValid()).isFalse();
        assertThat(request(1000, null, null, null).isPriceInputValid()).isFalse();
        assertThat(request(0, 1000, null, null).isPriceInputValid()).isFalse();
        assertThat(request(1000, 0, null, null).isPriceInputValid()).isFalse();
    }

    @Test
    void acceptsOperatingHoursWhenBothValuesAreMissingOrPresent() {
        assertThat(request(null, null, null, null).isOperatingHoursValid()).isTrue();
        assertThat(request(null, null, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)
            .isOperatingHoursValid()).isTrue();
        assertThat(request(null, null, LocalTime.MIDNIGHT, LocalTime.of(23, 59))
            .isOperatingHoursValid()).isTrue();
        assertThat(request(null, null, LocalTime.of(9, 0), LocalTime.of(22, 0))
            .isOperatingHoursValid()).isTrue();
    }

    @Test
    void rejectsOperatingHoursWhenOnlyOneValueIsMissing() {
        assertThat(request(null, null, null, LocalTime.of(22, 0)).isOperatingHoursValid()).isFalse();
        assertThat(request(null, null, LocalTime.of(9, 0), null).isOperatingHoursValid()).isFalse();
    }

    @Test
    void acceptsFloorWhenBothValuesAreMissingOrPresentWithPositiveNumber() {
        assertThat(requestWithFloor(null, null).isFloorInputValid()).isTrue();
        assertThat(requestWithFloor("ABOVE_GROUND", 1).isFloorInputValid()).isTrue();
        assertThat(requestWithFloor("UNDERGROUND", 2).isFloorInputValid()).isTrue();
    }

    @Test
    void rejectsFloorWhenOnlyOneValueIsMissingOrNumberIsNotPositive() {
        assertThat(requestWithFloor(null, 1).isFloorInputValid()).isFalse();
        assertThat(requestWithFloor("ABOVE_GROUND", null).isFloorInputValid()).isFalse();
        assertThat(requestWithFloor("ABOVE_GROUND", 0).isFloorInputValid()).isFalse();
        assertThat(requestWithFloor("UNDERGROUND", -1).isFloorInputValid()).isFalse();
        assertThat(requestWithFloor("BASEMENT", 1).isFloorInputValid()).isFalse();
    }

    private LockerReportCreateRequest request(
        Integer minPrice,
        Integer maxPrice,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return request(
            new FloorInput("ABOVE_GROUND", 1),
            new PriceInput(minPrice, maxPrice),
            new OperatingHours(startTime, endTime)
        );
    }

    private LockerReportCreateRequest requestWithFloor(
        String floorType,
        Integer floorNumber
    ) {
        return request(
            new FloorInput(floorType, floorNumber),
            new PriceInput(null, null),
            new OperatingHours(null, null)
        );
    }

    private LockerReportCreateRequest request(
        FloorInput floor,
        PriceInput price,
        OperatingHours operatingHours
    ) {
        return new LockerReportCreateRequest(
            "서울 중구 세종대로 1",
            37.55,
            126.97,
            floor.type(),
            floor.number(),
            "INDOOR",
            "SUBWAY_STATION",
            List.of("SMALL"),
            price.min(),
            price.max(),
            operatingHours.start(),
            operatingHours.end(),
            null,
            null,
            false
        );
    }

    private record FloorInput(String type, Integer number) {
    }

    private record PriceInput(Integer min, Integer max) {
    }

    private record OperatingHours(LocalTime start, LocalTime end) {
    }
}
