package com.zimdugo.locker.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReport;
import com.zimdugo.user.infrastructure.UserRepository;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerReportStoreAdapter implements LockerReportStore {

    private final LockerReportRepository lockerReportRepository;
    private final LockerRepository lockerRepository;
    private final UserRepository userRepository;

    @Override
    public SavedLockerReport create(LockerReportCreateInfo createInfo) {
        LockerEntity locker = lockerRepository.findById(createInfo.lockerId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        UserEntity user = userRepository.findById(createInfo.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LockerReport report = lockerReportRepository.save(new LockerReport(
            locker,
            user,
            createInfo.duplicateHandlingType(),
            createInfo.name(),
            createInfo.roadAddress(),
            createInfo.detailLocation(),
            createInfo.buildingName(),
            createInfo.floor(),
            createInfo.indoorOutdoorType(),
            createInfo.lockerType(),
            createInfo.sizeInfo(),
            createInfo.priceInfo(),
            createInfo.operatingHours(),
            createInfo.imageUrl(),
            createInfo.latitude(),
            createInfo.longitude()
        ));

        return new SavedLockerReport(
            report.getId(),
            report.getStatus().name()
        );
    }
}
