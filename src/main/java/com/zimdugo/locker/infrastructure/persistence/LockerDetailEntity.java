package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
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
import java.util.Set;
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
    @Column(length = 20)
    private GroundLevelType groundLevelType;

    @Column
    private Integer floor;

    @Column
    private Integer minPrice;

    @Column
    private Integer maxPrice;

    @Convert(converter = LockerSizeTypeConverter.class)
    @Column(length = 100)
    private Set<LockerSizeType> lockerSize;

    @Column(length = 2000)
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

    public LockerDetailEntity(LockerEntity locker, LockerDetailUpdateValues values) {
        this.locker = locker;
        update(values);
    }

    public void updateVoteCounts(int accurateVoteCount, int inaccurateVoteCount) {
        this.accurateVoteCount = accurateVoteCount;
        this.inaccurateVoteCount = inaccurateVoteCount;
    }

    public void update(LockerDetailUpdateValues values) {
        this.lockerType = values.lockerType();
        this.indoorOutdoorType = values.indoorOutdoorType();
        this.groundLevelType = values.groundLevelType();
        this.floor = values.floor();
        this.minPrice = values.minPrice();
        this.maxPrice = values.maxPrice();
        this.lockerSize = values.lockerSize();
        this.detailInfo = values.detailInfo();
        this.startTime = values.startTime();
        this.endTime = values.endTime();
        this.imageUrl = values.imageUrl();
    }
}
