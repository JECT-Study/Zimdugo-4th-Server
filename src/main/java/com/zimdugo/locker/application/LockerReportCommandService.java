package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.DuplicateHandlingType;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.LockerStore;
import com.zimdugo.locker.domain.ReportLocker;
import com.zimdugo.locker.domain.SavedLockerReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LockerReportCommandService {

    private final LockerStore lockerStore;
    private final LockerReportStore lockerReportStore;

    public LockerReportCreateResult create(Long userId, LockerReportCreateCommand command) {
        ReportLocker locker = resolveLocker(command);
        SavedLockerReport report = lockerReportStore.create(toCreateInfo(userId, locker.id(), command));

        return new LockerReportCreateResult(
            report.id(),
            locker.id(),
            locker.name(),
            locker.roadAddress(),
            locker.latitude(),
            locker.longitude(),
            report.status()
        );
    }

    private ReportLocker resolveLocker(LockerReportCreateCommand command) {
        if (command.duplicateHandlingType() == DuplicateHandlingType.CREATE_NEW) {
            return lockerStore.create(
                command.name(),
                command.roadAddress(),
                command.latitude(),
                command.longitude()
            );
        }
        return lockerStore.getById(command.existingLockerId());
    }

    private LockerReportCreateInfo toCreateInfo(
        Long userId,
        Long lockerId,
        LockerReportCreateCommand command
    ) {
        return new LockerReportCreateInfo(
            lockerId,
            userId,
            command.duplicateHandlingType(),
            command.name(),
            command.roadAddress(),
            command.detailLocation(),
            command.buildingName(),
            command.floor(),
            command.indoorOutdoorType(),
            command.lockerType(),
            command.sizeInfo(),
            command.priceInfo(),
            command.operatingHours(),
            command.imageUrl(),
            command.latitude(),
            command.longitude()
        );
    }
}
