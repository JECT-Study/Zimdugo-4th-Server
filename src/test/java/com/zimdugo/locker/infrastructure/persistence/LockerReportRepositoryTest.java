package com.zimdugo.locker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class LockerReportRepositoryTest {

    @Autowired
    private LockerReportRepository lockerReportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("선택 입력값이 없어도 제보 기본 정보를 저장한다")
    void saveReportWithOptionalFieldsNull() {
        UserEntity user = saveUser();

        LockerReportEntity report = lockerReportRepository.save(LockerReportEntity.builder()
            .user(user)
            .name("물품보관함")
            .indoorOutdoorType(IndoorOutdoorType.INDOOR)
            .lockerType(LockerType.ETC)
            .lockerSize(Set.of())
            .priceType(LockerReportPriceType.FREE)
            .operatingTimeType(LockerReportOperatingTimeType.OPEN_24_HOURS)
            .locationConsentAgreed(false)
            .latitude(37.556)
            .longitude(126.923)
            .build());

        entityManager.flush();
        entityManager.clear();

        LockerReportEntity savedReport = lockerReportRepository.findById(report.getId()).orElseThrow();

        assertThat(savedReport.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedReport.getLockerType()).isEqualTo(LockerType.ETC);
        assertThat(savedReport.getGroundLevelType()).isNull();
        assertThat(savedReport.getFloor()).isNull();
        assertThat(savedReport.getLockerSize()).isEmpty();
        assertThat(savedReport.getMinPrice()).isNull();
        assertThat(savedReport.getMaxPrice()).isNull();
        assertThat(savedReport.getAdditionalInfo()).isNull();
        assertThat(savedReport.getStartTime()).isNull();
        assertThat(savedReport.getEndTime()).isNull();
        assertThat(savedReport.getLatitude()).isEqualTo(37.556);
        assertThat(savedReport.getLongitude()).isEqualTo(126.923);
        assertThat(savedReport.getStatus()).isEqualTo(LockerReportStatus.SUBMITTED);
        assertThat(savedReport.getCreatedAt()).isNotNull();
        assertThat(savedReport.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("추가 제보 필드를 저장한다")
    void saveReportWithAdditionalFields() {
        UserEntity user = saveUser();

        LockerReportEntity report = lockerReportRepository.save(LockerReportEntity.builder()
            .user(user)
            .name("물품보관함")
            .roadAddress("서울 마포구 양화로 160")
            .groundLevelType(GroundLevelType.UNDERGROUND)
            .floor(2)
            .indoorOutdoorType(IndoorOutdoorType.INDOOR)
            .lockerType(LockerType.SUBWAY_STATION)
            .lockerSize(Set.of(LockerSizeType.SMALL, LockerSizeType.MEDIUM))
            .priceType(LockerReportPriceType.PAID)
            .minPrice(1000)
            .maxPrice(3000)
            .additionalInfo("B2 화장실 옆")
            .operatingTimeType(LockerReportOperatingTimeType.TIME_RANGE)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(22, 30))
            .locationConsentAgreed(true)
            .latitude(37.556)
            .longitude(126.923)
            .build());

        entityManager.flush();
        entityManager.clear();

        LockerReportEntity savedReport = lockerReportRepository.findById(report.getId()).orElseThrow();

        assertThat(savedReport.getRoadAddress()).isEqualTo("서울 마포구 양화로 160");
        assertThat(savedReport.getGroundLevelType()).isEqualTo(GroundLevelType.UNDERGROUND);
        assertThat(savedReport.getFloor()).isEqualTo(2);
        assertThat(savedReport.getIndoorOutdoorType()).isEqualTo(IndoorOutdoorType.INDOOR);
        assertThat(savedReport.getLockerType()).isEqualTo(LockerType.SUBWAY_STATION);
        assertThat(savedReport.getLockerSize()).containsExactlyInAnyOrder(LockerSizeType.SMALL, LockerSizeType.MEDIUM);
        assertThat(savedReport.getPriceType()).isEqualTo(LockerReportPriceType.PAID);
        assertThat(savedReport.getMinPrice()).isEqualTo(1000);
        assertThat(savedReport.getMaxPrice()).isEqualTo(3000);
        assertThat(savedReport.getAdditionalInfo()).isEqualTo("B2 화장실 옆");
        assertThat(savedReport.getOperatingTimeType()).isEqualTo(LockerReportOperatingTimeType.TIME_RANGE);
        assertThat(savedReport.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(savedReport.getEndTime()).isEqualTo(LocalTime.of(22, 30));
        assertThat(savedReport.isLocationConsentAgreed()).isTrue();
        assertThat(savedReport.getLatitude()).isEqualTo(37.556);
        assertThat(savedReport.getLongitude()).isEqualTo(126.923);
        assertThat(savedReport.getStatus()).isEqualTo(LockerReportStatus.SUBMITTED);
    }

    private UserEntity saveUser() {
        UserEntity user = new UserEntity(
            null,
            "reporter@example.com",
            "reporter",
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        entityManager.persist(user);
        return user;
    }
}
