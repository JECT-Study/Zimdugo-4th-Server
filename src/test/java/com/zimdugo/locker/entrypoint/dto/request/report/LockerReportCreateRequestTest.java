package com.zimdugo.locker.entrypoint.dto.request.report;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LockerReportCreateRequestTest {

    private static final String ROAD_ADDRESS = "서울 마포구 양화로 160";
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("올바른 제보 요청이면 validation error가 없다")
    void validRequest() {
        assertThat(validator.validate(createRequest())).isEmpty();
    }

    @Test
    @DisplayName("층 정보가 있는 실외 제보도 validation error가 없다")
    void outdoorReportWithFloorPasses() {
        LockerReportCreateRequest request = createRequest(
            "ABOVE_GROUND",
            1,
            "OUTDOOR",
            List.of("SMALL"),
            null,
            null,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("보관함 규격이 없으면 validation error가 발생한다")
    void withoutSizeTypesFails() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of(),
            null,
            null,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("최소 가격이 최대 가격보다 크면 validation error가 발생한다")
    void invalidPriceRangeFails() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of("SMALL"),
            3000,
            1000,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("가격을 한쪽만 입력하면 validation error가 발생한다")
    void partialPriceFails() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of("SMALL"),
            0,
            null,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("최소 가격이 0원인 제보는 통과한다")
    void paidReportWithZeroMinPricePasses() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of("SMALL"),
            0,
            1000,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("최대 가격이 0원이면 validation error가 발생한다")
    void paidReportWithZeroMaxPriceFails() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of("SMALL"),
            0,
            0,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("시간을 한쪽만 입력하면 validation error가 발생한다")
    void partialOperatingTimeFails() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of("SMALL"),
            null,
            null,
            LocalTime.of(9, 0),
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("24시간 운영 제보는 시간 입력 없이 통과한다")
    void open24HoursWithoutTimesPasses() {
        LockerReportCreateRequest request = createRequest(
            null,
            null,
            "INDOOR",
            List.of("SMALL"),
            null,
            null,
            null,
            null,
            null,
            false
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    private LockerReportCreateRequest createRequest() {
        return createRequest(
            "UNDERGROUND",
            2,
            "INDOOR",
            List.of("SMALL", "MEDIUM"),
            1000,
            3000,
            LocalTime.of(9, 0),
            LocalTime.of(22, 30),
            "https://cdn.example.com/locker/1.jpg",
            true
        );
    }

    private LockerReportCreateRequest createRequest(
        String floorType,
        Integer floorNumber,
        String indoorOutdoorType,
        List<String> sizeTypes,
        Integer minPrice,
        Integer maxPrice,
        LocalTime startTime,
        LocalTime endTime,
        String imageUrl,
        boolean locationConsentAgreed
    ) {
        return new LockerReportCreateRequest(
            ROAD_ADDRESS,
            37.556,
            126.923,
            floorType,
            floorNumber,
            indoorOutdoorType,
            "SUBWAY_STATION",
            sizeTypes,
            minPrice,
            maxPrice,
            startTime,
            endTime,
            "B2 출구 근처",
            imageUrl,
            locationConsentAgreed
        );
    }
}
