package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.LockerReportImageMetadata;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locker_report_images")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerReportImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private LockerReportEntity report;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String exifMetadataJson;

    @Column
    private LocalDateTime exifExtractedAt;

    @Column
    private Double gpsLatitude;

    @Column
    private Double gpsLongitude;

    @Column
    private Double gpsAltitude;

    @Column
    private LocalDateTime capturedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateImage(String imageUrl, LockerReportImageMetadata imageMetadata) {
        this.imageUrl = imageUrl;
        applyImageMetadata(imageMetadata);
    }

    private void applyImageMetadata(LockerReportImageMetadata imageMetadata) {
        if (imageMetadata == null) {
            this.exifMetadataJson = null;
            this.exifExtractedAt = null;
            this.gpsLatitude = null;
            this.gpsLongitude = null;
            this.gpsAltitude = null;
            this.capturedAt = null;
            return;
        }
        this.exifMetadataJson = imageMetadata.metadataJson();
        this.exifExtractedAt = imageMetadata.extractedAt();
        this.gpsLatitude = imageMetadata.gpsLatitude();
        this.gpsLongitude = imageMetadata.gpsLongitude();
        this.gpsAltitude = imageMetadata.gpsAltitude();
        this.capturedAt = imageMetadata.capturedAt();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
