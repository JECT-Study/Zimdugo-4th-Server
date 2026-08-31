package com.zimdugo.maintenance.infrastructure.adapter;

import com.zimdugo.maintenance.domain.MaintenanceNotice;
import com.zimdugo.maintenance.domain.MaintenanceNoticeReader;
import com.zimdugo.maintenance.domain.MaintenanceNoticeStore;
import com.zimdugo.maintenance.infrastructure.persistence.MaintenanceNoticeEntity;
import com.zimdugo.maintenance.infrastructure.persistence.MaintenanceNoticeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaintenanceNoticePersistenceAdapter implements MaintenanceNoticeReader, MaintenanceNoticeStore {

    private final MaintenanceNoticeRepository maintenanceNoticeRepository;

    @Override
    public Optional<MaintenanceNotice> find() {
        return maintenanceNoticeRepository.findById(MaintenanceNoticeEntity.DEFAULT_ID)
            .map(MaintenanceNoticeEntity::toDomain);
    }

    @Override
    public MaintenanceNotice save(MaintenanceNotice notice) {
        MaintenanceNoticeEntity entity = maintenanceNoticeRepository.findById(MaintenanceNoticeEntity.DEFAULT_ID)
            .orElseGet(() -> MaintenanceNoticeEntity.create(notice));
        entity.update(notice);
        return maintenanceNoticeRepository.save(entity).toDomain();
    }
}
