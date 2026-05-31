package com.zimdugo.locker.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            null,
            user,
            "물품보관함",
            null,
            null,
            null,
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

        assertThat(savedReport.getLocker()).isNull();
        assertThat(savedReport.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedReport.getLockerType()).isEqualTo("UNKNOWN");
        assertThat(savedReport.getStatus()).isEqualTo(LockerReportStatus.SUBMITTED);
        assertThat(savedReport.getImageUrl()).isNull();
        assertThat(savedReport.getFloor()).isNull();
        assertThat(savedReport.getPriceInfo()).isNull();
        assertThat(savedReport.getAdditionalInfo()).isNull();
        assertThat(savedReport.getStartTime()).isNull();
        assertThat(savedReport.getEndTime()).isNull();
        assertThat(savedReport.getCreatedAt()).isNotNull();
        assertThat(savedReport.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("추가 제보 필드를 저장한다")
    void saveReportWithAdditionalFields() {
        UserEntity user = saveUser();

        LockerReportEntity report = lockerReportRepository.save(new LockerReportEntity(
            null,
            user,
            "물품보관함",
            "서울 마포구 양화로 160",
            "UNDERGROUND:2",
            "INDOOR",
            "SUBWAY_STATION",
            "SMALL,MEDIUM",
            "1000~3000",
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

        assertThat(savedReport.getAdditionalInfo()).isEqualTo("B2 화장실 옆");
        assertThat(savedReport.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(savedReport.getEndTime()).isEqualTo(LocalTime.of(22, 30));
        assertThat(savedReport.isLocationConsentAgreed()).isTrue();
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
