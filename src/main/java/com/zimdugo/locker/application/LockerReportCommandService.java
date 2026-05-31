package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LockerReportCommandService {

    private final LockerReportStore lockerReportStore;

    public LockerReportCreateResult create(Long userId, LockerReportCreateCommand command) {
        SavedLockerReport report = lockerReportStore.create(toCreateInfo(userId, command));

        return new LockerReportCreateResult(
            report.id(),
            null,
            command.name(),
            command.roadAddress(),
            command.latitude(),
            command.longitude(),
            report.status()
        );
    }

    private LockerReportCreateInfo toCreateInfo(Long userId, LockerReportCreateCommand command) {
        return new LockerReportCreateInfo(
            userId,
            command.name(),
            command.roadAddress(),
            command.hasFloor(),
            command.floorType(),
            command.floorNumber(),
            command.indoorOutdoorType(),
            command.lockerType(),
            command.sizeTypes(),
            command.isFree(),
            command.minPrice(),
            command.maxPrice(),
            command.startTime(),
            command.endTime(),
            command.additionalInfo(),
            command.imageUrl(),
            command.locationConsentAgreed(),
            command.latitude(),
            command.longitude()
        );
    }
}
