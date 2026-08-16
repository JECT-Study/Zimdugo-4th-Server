package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locker_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locker_id", nullable = false)
    private LockerEntity locker;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "list_order", nullable = false)
    private int listOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LockerImageEntity(LockerEntity locker, String imageUrl, int listOrder) {
        this.locker = locker;
        this.imageUrl = imageUrl;
        this.listOrder = listOrder;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static LockerImageEntity of(LockerEntity locker, String imageUrl, int listOrder) {
        return new LockerImageEntity(locker, imageUrl, listOrder);
    }
}
