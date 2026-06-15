package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.report.LockerReportNamePreviewResult;
import com.zimdugo.locker.domain.LockerReportNameResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerReportNamePreviewService {

    private final LockerReportNameResolver lockerReportNameResolver;

    public LockerReportNamePreviewResult preview(LockerReportNamePreviewCommand command) {
        String resolvedName = lockerReportNameResolver.resolve(
            command.roadAddress(),
            command.lockerType(),
            command.latitude(),
            command.longitude()
        );
        if (resolvedName == null || resolvedName.isBlank()) {
            resolvedName = command.roadAddress();
        }
        return new LockerReportNamePreviewResult(
            resolvedName,
            command.roadAddress(),
            command.latitude(),
            command.longitude()
        );
    }
}
