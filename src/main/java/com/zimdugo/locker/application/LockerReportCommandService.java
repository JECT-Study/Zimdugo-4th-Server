package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportNameResolver;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.LockerReportUpdateInfo;
import com.zimdugo.locker.domain.SavedLockerReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LockerReportCommandService {

    private final ActiveUserValidator activeUserValidator;
    private final LockerReportStore lockerReportStore;
    private final LockerReportNameResolver lockerReportNameResolver;

    public LockerReportCreateResult create(Long userId, LockerReportCreateCommand command) {
        activeUserValidator.validate(userId);
        String reportName = resolveReportName(command);
        SavedLockerReport report = lockerReportStore.create(toCreateInfo(userId, command, reportName));

        return new LockerReportCreateResult(
            report.id(),
            reportName,
            command.roadAddress(),
            command.latitude(),
            command.longitude(),
            report.status()
        );
    }

    public void update(Long userId, Long reportId, LockerReportCreateCommand command) {
        activeUserValidator.validate(userId);
        lockerReportStore.update(userId, reportId, toUpdateInfo(command, resolveReportName(command)));
    }

    public void delete(Long userId, Long reportId) {
        activeUserValidator.validate(userId);
        lockerReportStore.delete(userId, reportId);
    }

    private LockerReportCreateInfo toCreateInfo(
        Long userId,
        LockerReportCreateCommand command,
        String reportName
    ) {
        return new LockerReportCreateInfo(
            userId,
            reportName,
            command.roadAddress(),
            command.hasFloor() ? command.floorType() : null,
            command.floorNumber(),
            command.indoorOutdoorType(),
            command.lockerType(),
            command.sizeTypes(),
            command.isFree(),
            command.minPrice(),
            command.maxPrice(),
            command.is24Hours(),
            command.is24Hours() ? null : command.startTime(),
            command.is24Hours() ? null : command.endTime(),
            command.additionalInfo(),
            command.imageUrl(),
            command.locationConsentAgreed(),
            command.latitude(),
            command.longitude()
        );
    }

    private LockerReportUpdateInfo toUpdateInfo(LockerReportCreateCommand command, String reportName) {
        return new LockerReportUpdateInfo(
            reportName,
            command.roadAddress(),
            command.hasFloor() ? command.floorType() : null,
            command.floorNumber(),
            command.indoorOutdoorType(),
            command.lockerType(),
            command.sizeTypes(),
            command.isFree(),
            command.minPrice(),
            command.maxPrice(),
            command.is24Hours(),
            command.is24Hours() ? null : command.startTime(),
            command.is24Hours() ? null : command.endTime(),
            command.additionalInfo(),
            command.imageUrl(),
            command.locationConsentAgreed(),
            command.latitude(),
            command.longitude()
        );
    }

    private String resolveReportName(LockerReportCreateCommand command) {
        String resolvedName = lockerReportNameResolver.resolve(
            command.roadAddress(),
            command.lockerType(),
            command.latitude(),
            command.longitude()
        );
        if (resolvedName == null || resolvedName.isBlank()) {
            return command.roadAddress();
        }
        return resolvedName;
    }
}
