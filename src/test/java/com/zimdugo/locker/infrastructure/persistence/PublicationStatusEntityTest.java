package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.publication.PublicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationStatusEntityTest {

    @Test
    @DisplayName("일반 생성한 장소와 보관함은 공개 상태다")
    void createsActiveOperationalEntities() {
        PlaceEntity place = new PlaceEntity("서울역", 37.55, 126.97, "서울 중구");
        LockerEntity locker = new LockerEntity("1번 출구", "서울 중구", 37.55, 126.97, place);

        assertThat(place.getPublicationStatus()).isEqualTo(PublicationStatus.ACTIVE);
        assertThat(locker.getPublicationStatus()).isEqualTo(PublicationStatus.ACTIVE);
    }

    @Test
    @DisplayName("제보 검토용 장소와 보관함은 초안 상태로 생성한다")
    void createsDraftReportEntities() {
        PlaceEntity place = PlaceEntity.draft("서울역", 37.55, 126.97, "서울 중구");
        LockerEntity locker = LockerEntity.draft("1번 출구", "서울 중구", 37.55, 126.97, place);

        assertThat(place.getPublicationStatus()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(locker.getPublicationStatus()).isEqualTo(PublicationStatus.DRAFT);
    }

    @Test
    @DisplayName("초안 장소와 보관함을 공개 상태로 전환한다")
    void activatesDraftEntities() {
        PlaceEntity place = PlaceEntity.draft("서울역", 37.55, 126.97, "서울 중구");
        LockerEntity locker = LockerEntity.draft("1번 출구", "서울 중구", 37.55, 126.97, place);

        place.activate();
        locker.activate();

        assertThat(place.getPublicationStatus()).isEqualTo(PublicationStatus.ACTIVE);
        assertThat(locker.getPublicationStatus()).isEqualTo(PublicationStatus.ACTIVE);
    }
}
