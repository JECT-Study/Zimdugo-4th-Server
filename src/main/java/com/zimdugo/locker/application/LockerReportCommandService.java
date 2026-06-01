package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.IndoorOutdoorType;
import com.zimdugo.locker.infrastructure.persistence.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerType;
import java.util.Set;
import java.util.stream.Collectors;
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
            toGroundLevelType(command.hasFloor(), command.floorType()),
            command.floorNumber(),
            IndoorOutdoorType.valueOf(command.indoorOutdoorType()),
            LockerType.valueOf(command.lockerType()),
            toLockerSize(command.sizeTypes()),
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

    private GroundLevelType toGroundLevelType(boolean hasFloor, String floorType) {
        if (!hasFloor || floorType == null || floorType.isBlank()) {
            return null;
        }
        return GroundLevelType.valueOf(floorType);
    }

    private Set<LockerSizeType> toLockerSize(java.util.List<String> sizeTypes) {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return Set.of();
        }
        return sizeTypes.stream()
            .map(LockerSizeType::from)
            .collect(Collectors.toUnmodifiableSet());
    }
}
