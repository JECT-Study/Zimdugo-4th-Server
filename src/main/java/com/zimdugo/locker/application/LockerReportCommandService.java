package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
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
        ReportLocker locker = lockerStore.create(
            command.name(),
            command.roadAddress(),
            command.latitude(),
            command.longitude()
        );
        LockerReportCreateInfo info = new LockerReportCreateInfo(
            locker.id(),
            userId,
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
        SavedLockerReport report = lockerReportStore.create(info);

        return LockerReportCreateResult.of(report, locker);
    }
}
