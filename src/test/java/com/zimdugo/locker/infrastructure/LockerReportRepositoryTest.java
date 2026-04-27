package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.DuplicateHandlingType;
import com.zimdugo.locker.domain.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReport;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LockerReportRepositoryTest {

    @Autowired
    private LockerReportRepository lockerReportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("선택 입력값이 없어도 제보 원본 정보를 저장한다")
    void saveReportWithOptionalFieldsNull() {
        UserEntity user = saveUser();
        LockerEntity locker = saveLocker();

        LockerReport report = lockerReportRepository.save(new LockerReport(
            locker,
            user,
            DuplicateHandlingType.CREATE_NEW,
            "홍대입구역 보관함",
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
            37.556,
            126.923
        ));

        entityManager.flush();
        entityManager.clear();

        LockerReport savedReport = lockerReportRepository.findById(report.getId()).orElseThrow();

        assertThat(savedReport.getLocker().getId()).isEqualTo(locker.getId());
        assertThat(savedReport.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedReport.getLockerType()).isEqualTo("UNKNOWN");
        assertThat(savedReport.getStatus()).isEqualTo(LockerReportStatus.COMPLETED);
        assertThat(savedReport.getImageUrl()).isNull();
        assertThat(savedReport.getFloor()).isNull();
        assertThat(savedReport.getPriceInfo()).isNull();
        assertThat(savedReport.getCreatedAt()).isNotNull();
        assertThat(savedReport.getUpdatedAt()).isNotNull();
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

    private LockerEntity saveLocker() {
        entityManager.createNativeQuery("""
            INSERT INTO lockers (name, road_address, latitude, longitude)
            VALUES ('홍대입구역 보관함', '서울 마포구 양화로 160', 37.556, 126.923)
            """).executeUpdate();

        Long lockerId = ((Number) entityManager.createNativeQuery("""
            SELECT id
            FROM lockers
            WHERE name = '홍대입구역 보관함'
            """).getSingleResult()).longValue();

        return entityManager.getReference(LockerEntity.class, lockerId);
    }
}
