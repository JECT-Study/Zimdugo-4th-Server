package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.publication.PublicationStatus;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "lockers")
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
}
