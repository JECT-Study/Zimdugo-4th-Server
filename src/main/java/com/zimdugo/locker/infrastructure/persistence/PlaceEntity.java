package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.publication.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "places",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_places_name", columnNames = "name")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 255)
    private String roadAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicationStatus publicationStatus = PublicationStatus.ACTIVE;

    public PlaceEntity(String name, double latitude, double longitude, String roadAddress) {
        this(name, latitude, longitude, roadAddress, PublicationStatus.ACTIVE);
    }

    private PlaceEntity(
        String name,
        double latitude,
        double longitude,
        String roadAddress,
        PublicationStatus publicationStatus
    ) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.publicationStatus = publicationStatus;
    }

    public static PlaceEntity draft(String name, double latitude, double longitude, String roadAddress) {
        return new PlaceEntity(name, latitude, longitude, roadAddress, PublicationStatus.DRAFT);
    }

    public void activate() {
        this.publicationStatus = PublicationStatus.ACTIVE;
    }
}
