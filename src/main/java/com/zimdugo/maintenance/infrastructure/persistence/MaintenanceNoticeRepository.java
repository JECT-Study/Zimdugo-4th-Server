package com.zimdugo.maintenance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceNoticeRepository extends JpaRepository<MaintenanceNoticeEntity, Long> {
}
