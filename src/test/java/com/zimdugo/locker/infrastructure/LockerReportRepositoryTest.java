package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.DuplicateHandlingType;
import com.zimdugo.locker.domain.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
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
        LockerEntity locker = saveLocker("홍대입구역 보관함");

        LockerReportEntity report = lockerReportRepository.save(new LockerReportEntity(
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

        LockerReportEntity savedReport = lockerReportRepository.findById(report.getId()).orElseThrow();

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

    @Test
    @DisplayName("보관함별 최신 완료 제보 시각을 조회한다")
    void findLatestCompletedVoteAtByLockerIdInReturnsLatestUpdatedAt() {
        UserEntity user = saveUser("vote-user@example.com", "vote-user");
        LockerEntity targetLocker = saveLocker("대상 보관함");
        LockerEntity otherLocker = saveLocker("다른 보관함");

        LockerReportEntity olderReport = saveReport(user, targetLocker, "이전 제보");
        LockerReportEntity latestReport = saveReport(user, targetLocker, "최신 제보");
        LockerReportEntity otherReport = saveReport(user, otherLocker, "다른 제보");
        entityManager.flush();

        updateUpdatedAt(olderReport.getId(), LocalDateTime.of(2026, 5, 10, 10, 0));
        updateUpdatedAt(latestReport.getId(), LocalDateTime.of(2026, 5, 13, 19, 30));
        updateUpdatedAt(otherReport.getId(), LocalDateTime.of(2026, 5, 9, 9, 0));
        entityManager.clear();

        List<LockerReportLatestUpdateProjection> result =
            lockerReportRepository.findLatestCompletedVoteAtByLockerIdIn(List.of(targetLocker.getId()));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLockerId()).isEqualTo(targetLocker.getId());
        assertThat(result.get(0).getLastCompletedVoteAt()).isEqualTo(LocalDateTime.of(2026, 5, 13, 19, 30));
    }

    private LockerReportEntity saveReport(UserEntity user, LockerEntity locker, String name) {
        LockerReportEntity report = new LockerReportEntity(
            locker,
            user,
            DuplicateHandlingType.CREATE_NEW,
            name,
            "서울 마포구 양화로 160",
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
        );
        entityManager.persist(report);
        return report;
    }

    private void updateUpdatedAt(Long reportId, LocalDateTime updatedAt) {
        entityManager.createNativeQuery("""
            UPDATE locker_reports
            SET updated_at = :updatedAt
            WHERE id = :reportId
            """)
            .setParameter("updatedAt", updatedAt)
            .setParameter("reportId", reportId)
            .executeUpdate();
    }

    private UserEntity saveUser() {
        return saveUser("reporter@example.com", "reporter");
    }

    private UserEntity saveUser(String email, String nickname) {
        UserEntity user = new UserEntity(
            null,
            email,
            nickname,
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        entityManager.persist(user);
        return user;
    }

    private LockerEntity saveLocker(String name) {
        LockerEntity locker = new LockerEntity(
            name,
            "서울 마포구 양화로 160",
            37.556,
            126.923
        );
        entityManager.persist(locker);
        return locker;
    }
}
