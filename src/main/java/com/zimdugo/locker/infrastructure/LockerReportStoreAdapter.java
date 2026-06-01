package com.zimdugo.locker.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.IndoorOutdoorType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerType;
import com.zimdugo.user.infrastructure.UserRepository;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerReportStoreAdapter implements LockerReportStore {

    private final LockerReportRepository lockerReportRepository;
    private final UserRepository userRepository;

    @Override
    public SavedLockerReport create(LockerReportCreateInfo createInfo) {
        UserEntity user = userRepository.findById(createInfo.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LockerReportEntity report = lockerReportRepository.save(new LockerReportEntity(
            user,
            createInfo.name(),
            createInfo.roadAddress(),
            toGroundLevelType(createInfo.groundLevelType()),
            createInfo.floorNumber(),
            IndoorOutdoorType.valueOf(createInfo.indoorOutdoorType()),
            LockerType.valueOf(createInfo.lockerType()),
            toLockerSize(createInfo.sizeTypes()),
            createInfo.isFree(),
            createInfo.minPrice(),
            createInfo.maxPrice(),
            createInfo.additionalInfo(),
            createInfo.startTime(),
            createInfo.endTime(),
            createInfo.imageUrl(),
            createInfo.locationConsentAgreed(),
            createInfo.latitude(),
            createInfo.longitude()
        ));

        return new SavedLockerReport(report.getId(), report.getStatus().name());
    }

    private GroundLevelType toGroundLevelType(String groundLevelType) {
        if (groundLevelType == null || groundLevelType.isBlank()) {
            return null;
        }
        return GroundLevelType.valueOf(groundLevelType);
    }

    private Set<LockerSizeType> toLockerSize(List<String> sizeTypes) {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return Set.of();
        }
        return sizeTypes.stream()
            .map(LockerSizeType::from)
            .collect(Collectors.toUnmodifiableSet());
    }
}
