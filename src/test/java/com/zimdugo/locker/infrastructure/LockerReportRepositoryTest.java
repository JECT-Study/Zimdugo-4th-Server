package com.zimdugo.locker.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.IndoorOutdoorType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerType;
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

        LockerReportEntity report = lockerReportRepository.save(new LockerReportEntity(
            user,
            "물품보관함",
            null,
            null,
            null,
            IndoorOutdoorType.INDOOR,
            LockerType.ETC,
            Set.of(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            37.556,
            126.923
        ));

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

        LockerReportEntity report = lockerReportRepository.save(new LockerReportEntity(
            user,
            "물품보관함",
            "서울 마포구 양화로 160",
            GroundLevelType.UNDERGROUND,
            2,
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION,
            Set.of(LockerSizeType.SMALL, LockerSizeType.MEDIUM),
            false,
            1000,
            3000,
            "B2 화장실 옆",
            LocalTime.of(9, 0),
            LocalTime.of(22, 30),
            "https://cdn.example.com/locker/1.jpg",
            true,
            37.556,
            126.923
        ));

        entityManager.flush();
        entityManager.clear();

        LockerReportEntity savedReport = lockerReportRepository.findById(report.getId()).orElseThrow();

        assertThat(savedReport.getRoadAddress()).isEqualTo("서울 마포구 양화로 160");
        assertThat(savedReport.getGroundLevelType()).isEqualTo(GroundLevelType.UNDERGROUND);
        assertThat(savedReport.getFloor()).isEqualTo(2);
        assertThat(savedReport.getIndoorOutdoorType()).isEqualTo(IndoorOutdoorType.INDOOR);
        assertThat(savedReport.getLockerType()).isEqualTo(LockerType.SUBWAY_STATION);
        assertThat(savedReport.getLockerSize()).containsExactlyInAnyOrder(LockerSizeType.SMALL, LockerSizeType.MEDIUM);
        assertThat(savedReport.getMinPrice()).isEqualTo(1000);
        assertThat(savedReport.getMaxPrice()).isEqualTo(3000);
        assertThat(savedReport.getAdditionalInfo()).isEqualTo("B2 화장실 옆");
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
