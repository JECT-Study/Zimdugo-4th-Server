package com.zimdugo.locker.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportImageMetadata;
import com.zimdugo.locker.domain.LockerReportImageMetadataReader;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportImageEntity;
import com.zimdugo.user.infrastructure.UserRepository;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerReportStoreAdapter implements LockerReportStore {

    private final LockerReportRepository lockerReportRepository;
    private final UserRepository userRepository;
    private final LockerReportImageMetadataReader imageMetadataReader;

    @Override
    public SavedLockerReport create(LockerReportCreateInfo createInfo) {
        UserEntity user = userRepository.findById(createInfo.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LockerReportEntity report = LockerReportEntity.of(createInfo, user);

        if (!isBlank(createInfo.imageUrl())) {
            LockerReportImageMetadata imageMetadata = readImageMetadata(createInfo.imageUrl());
            LockerReportImageEntity imageEntity = LockerReportImageEntity.builder()
                .report(report)
                .imageUrl(createInfo.imageUrl())
                .exifMetadataJson(imageMetadata.metadataJson())
                .exifExtractedAt(imageMetadata.extractedAt())
                .gpsLatitude(imageMetadata.gpsLatitude())
                .gpsLongitude(imageMetadata.gpsLongitude())
                .gpsAltitude(imageMetadata.gpsAltitude())
                .capturedAt(imageMetadata.capturedAt())
                .build();
            report.addImage(imageEntity);
        }

        lockerReportRepository.save(report);

        return new SavedLockerReport(report.getId(), report.getStatus().name());
    }

    private LockerReportImageMetadata readImageMetadata(String imageUrl) {
        return imageMetadataReader.readMetadata(imageUrl);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
