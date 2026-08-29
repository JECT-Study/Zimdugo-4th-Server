package com.zimdugo.maintenance.application;

import com.zimdugo.maintenance.application.dto.AdminMaintenanceNoticeResult;
import com.zimdugo.maintenance.application.dto.MaintenanceNoticeUpdateCommand;
import com.zimdugo.maintenance.application.dto.PublicMaintenanceNoticeResult;
import com.zimdugo.maintenance.domain.MaintenanceNotice;
import com.zimdugo.maintenance.domain.MaintenanceNoticeReader;
import com.zimdugo.maintenance.domain.MaintenanceNoticeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceNoticeService {

    private final MaintenanceNoticeReader maintenanceNoticeReader;
    private final MaintenanceNoticeStore maintenanceNoticeStore;

    public AdminMaintenanceNoticeResult getAdminNotice() {
        return maintenanceNoticeReader.find()
            .map(AdminMaintenanceNoticeResult::from)
            .orElseGet(AdminMaintenanceNoticeResult::empty);
    }

    public PublicMaintenanceNoticeResult getPublicNotice() {
        return maintenanceNoticeReader.find()
            .map(PublicMaintenanceNoticeResult::from)
            .orElseGet(PublicMaintenanceNoticeResult::inactive);
    }

    @Transactional
    public AdminMaintenanceNoticeResult update(MaintenanceNoticeUpdateCommand command) {
        MaintenanceNotice notice = MaintenanceNotice.of(
            command.enabled(),
            command.title(),
            command.message(),
            command.startedAt(),
            command.endedAt()
        );
        MaintenanceNotice saved = maintenanceNoticeStore.save(notice);
        return AdminMaintenanceNoticeResult.from(saved);
    }
}
