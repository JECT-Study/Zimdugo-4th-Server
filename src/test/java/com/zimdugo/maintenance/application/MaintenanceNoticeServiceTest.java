package com.zimdugo.maintenance.application;

import com.zimdugo.maintenance.application.dto.MaintenanceNoticeUpdateCommand;
import com.zimdugo.maintenance.infrastructure.adapter.MaintenanceNoticePersistenceAdapter;
import com.zimdugo.maintenance.infrastructure.persistence.MaintenanceNoticeRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({MaintenanceNoticeService.class, MaintenanceNoticePersistenceAdapter.class})
@Transactional
class MaintenanceNoticeServiceTest {

    @Autowired
    private MaintenanceNoticeService maintenanceNoticeService;

    @Autowired
    private MaintenanceNoticeRepository maintenanceNoticeRepository;

    @Test
    @DisplayName("저장된 설정이 없으면 공개 조회는 비활성 상태를 반환한다")
    void returnsDisabledPublicStatusWhenNoNoticeExists() {
        assertThat(maintenanceNoticeService.getPublicNotice().isUnderMaintenance()).isFalse();
    }

    @Test
    @DisplayName("관리자 수정 값은 다음 조회에 그대로 반환된다")
    void updatesAndReadsBackCurrentMaintenanceNotice() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 30, 2, 0);
        LocalDateTime endedAt = startedAt.plusHours(2);

        maintenanceNoticeService.update(new MaintenanceNoticeUpdateCommand(
            true,
            "서비스 점검 중입니다",
            "더 나은 서비스 제공을 위해 점검을 진행하고 있습니다.",
            startedAt,
            endedAt
        ));
        maintenanceNoticeRepository.flush();

        var response = maintenanceNoticeService.getAdminNotice();

        assertThat(response.enabled()).isTrue();
        assertThat(response.title()).isEqualTo("서비스 점검 중입니다");
        assertThat(response.message()).isEqualTo("더 나은 서비스 제공을 위해 점검을 진행하고 있습니다.");
        assertThat(response.startedAt()).isEqualTo(startedAt);
        assertThat(response.endedAt()).isEqualTo(endedAt);
    }
}
