package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.publication.PublicationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "lockers")
@SQLDelete(sql = "UPDATE lockers SET deleted_at = NOW(), publication_status = 'DRAFT' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String roadAddress;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private PlaceEntity place;

    @Column(columnDefinition = "geography(Point,4326)", insertable = false, updatable = false)
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicationStatus publicationStatus = PublicationStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("listOrder ASC")
    private List<LockerImageEntity> images = new ArrayList<>();

    public LockerEntity(String name, String roadAddress, double latitude, double longitude) {
        this(name, roadAddress, latitude, longitude, null);
    }

    public LockerEntity(String name, String roadAddress, double latitude, double longitude, PlaceEntity place) {
        this.name = name;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.place = place;
    }

    public static LockerEntity draft(
        String name,
        String roadAddress,
        double latitude,
        double longitude,
        PlaceEntity place
    ) {
        LockerEntity locker = new LockerEntity(name, roadAddress, latitude, longitude, place);
        locker.publicationStatus = PublicationStatus.DRAFT;
        return locker;
    }

    public void activate() {
        this.publicationStatus = PublicationStatus.ACTIVE;
    }

    public void deactivate() {
        this.publicationStatus = PublicationStatus.DRAFT;
    }

    public void update(LockerUpdateValues values) {
        this.name = values.name();
        this.roadAddress = values.roadAddress();
        this.latitude = values.latitude();
        this.longitude = values.longitude();
        this.place = values.place();
        this.publicationStatus = values.publicationStatus();
    }

    public void replaceImages(List<String> imageUrls) {
        images.clear();
        if (imageUrls == null) {
            return;
        }
        for (int index = 0; index < imageUrls.size(); index++) {
            images.add(LockerImageEntity.of(this, imageUrls.get(index), index));
        }
    }
}
