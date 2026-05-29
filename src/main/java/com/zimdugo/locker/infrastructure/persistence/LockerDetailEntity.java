package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "locker_details",
    indexes = {
        @Index(name = "idx_locker_details_locker_id", columnList = "locker_id", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locker_id", nullable = false, unique = true)
    private LockerEntity locker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LockerType lockerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IndoorOutdoorType indoorOutdoorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroundLevelType groundLevelType;

    @Column
    private Integer floor;

    @Column(nullable = false)
    private Integer minPrice;

    @Column(nullable = false)
    private Integer maxPrice;

    @Convert(converter = LockerSizeTypeConverter.class)
    @Column(length = 100)
    private LockerSizeType lockerSize;

    @Column(length = 1000)
    private String detailInfo;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int accurateVoteCount;

    @Column(nullable = false)
    private int inaccurateVoteCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public LockerDetailEntity(LockerEntity locker, CreateSpec createSpec) {
        this.locker = locker;
        this.lockerType = createSpec.lockerType();
        this.indoorOutdoorType = createSpec.indoorOutdoorType();
        this.groundLevelType = createSpec.groundLevelType();
        this.floor = createSpec.floor();
        this.minPrice = createSpec.minPrice();
        this.maxPrice = createSpec.maxPrice();
        this.lockerSize = createSpec.lockerSize();
        this.detailInfo = createSpec.detailInfo();
        this.startTime = createSpec.startTime();
        this.endTime = createSpec.endTime();
        this.imageUrl = createSpec.imageUrl();
        this.accurateVoteCount = 0;
        this.inaccurateVoteCount = 0;
    }

    public record CreateSpec(
        LockerType lockerType,
        IndoorOutdoorType indoorOutdoorType,
        GroundLevelType groundLevelType,
        Integer floor,
        Integer minPrice,
        Integer maxPrice,
        LockerSizeType lockerSize,
        String detailInfo,
        LocalTime startTime,
        LocalTime endTime,
        String imageUrl
    ) {
    }

    public void voteAccurate() {
        this.accurateVoteCount++;
    }

    public void voteInaccurate() {
        this.inaccurateVoteCount++;
    }
}
