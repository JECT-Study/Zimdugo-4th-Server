package com.zimdugo.locker.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.user.infrastructure.UserRepository;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import java.util.List;
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
            null,
            user,
            createInfo.name(),
            createInfo.roadAddress(),
            floorValue(createInfo.hasFloor(), createInfo.floorType(), createInfo.floorNumber()),
            createInfo.indoorOutdoorType(),
            createInfo.lockerType(),
            sizeInfo(createInfo.sizeTypes()),
            priceInfo(createInfo.isFree(), createInfo.minPrice(), createInfo.maxPrice()),
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

    private String floorValue(boolean hasFloor, String floorType, Integer floorNumber) {
        if (!hasFloor || floorType == null || floorNumber == null) {
            return null;
        }
        return floorType + ":" + floorNumber;
    }

    private String sizeInfo(List<String> sizeTypes) {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return null;
        }
        return String.join(",", sizeTypes);
    }

    private String priceInfo(Boolean isFree, Integer minPrice, Integer maxPrice) {
        if (isFree == null) {
            return null;
        }
        if (Boolean.TRUE.equals(isFree)) {
            return "FREE";
        }
        if (minPrice == null && maxPrice == null) {
            return null;
        }

        String minPriceValue = minPrice == null ? "" : minPrice.toString();
        String maxPriceValue = maxPrice == null ? "" : maxPrice.toString();
        return minPriceValue + "~" + maxPriceValue;
    }
}
