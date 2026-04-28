package com.zimdugo.locker.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerStore;
import com.zimdugo.locker.domain.ReportLocker;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerStoreAdapter implements LockerStore {

    private final LockerRepository lockerRepository;

    @Override
    public ReportLocker create(String name, String roadAddress, double latitude, double longitude) {
        LockerEntity locker = lockerRepository.save(
            new LockerEntity(name, roadAddress, latitude, longitude)
        );
        return toDomain(locker);
    }

    @Override
    public ReportLocker getById(Long id) {
        return lockerRepository.findById(id)
            .map(this::toDomain)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private ReportLocker toDomain(LockerEntity locker) {
        return new ReportLocker(
            locker.getId(),
            locker.getName(),
            locker.getRoadAddress(),
            locker.getLatitude(),
            locker.getLongitude()
        );
    }
}
