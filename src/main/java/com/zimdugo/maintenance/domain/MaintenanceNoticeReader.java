package com.zimdugo.maintenance.domain;

import java.util.Optional;

public interface MaintenanceNoticeReader {
    Optional<MaintenanceNotice> find();
}
