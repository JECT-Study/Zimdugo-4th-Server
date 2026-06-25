package com.zimdugo.admin.locker.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class AdminLockerPageResultTest {

    @Test
    void groupsLockersByPlaceAndKeepsUnassignedLockersTogether() {
        AdminLockerSummaryResult stationLocker = locker(1L, 10L, "서울역");
        AdminLockerSummaryResult secondStationLocker = locker(2L, 10L, "서울역");
        AdminLockerSummaryResult unassignedLocker = locker(3L, null, null);

        AdminLockerPageResult result = AdminLockerPageResult.from(
            new PageImpl<>(List.of(stationLocker, secondStationLocker, unassignedLocker))
        );

        assertThat(result.groups()).hasSize(2);
        assertThat(result.groups().getFirst().placeName()).isEqualTo("서울역");
        assertThat(result.groups().getFirst().lockers()).containsExactly(stationLocker, secondStationLocker);
        assertThat(result.groups().get(1).placeName()).isEqualTo("장소 미지정");
    }

    @Test
    void exposesKoreanLabelsAndFreePrice() {
        AdminLockerSummaryResult locker = locker(1L, 10L, "서울역");

        assertThat(locker.lockerTypeLabel()).isEqualTo("지하철역");
        assertThat(locker.indoorOutdoorLabel()).isEqualTo("실내");
        assertThat(locker.priceLabel()).isEqualTo("무료");
        assertThat(locker.active()).isTrue();
    }

    private AdminLockerSummaryResult locker(Long id, Long placeId, String placeName) {
        return new AdminLockerSummaryResult(
            id,
            "서울역 보관함",
            "서울 중구",
            PublicationStatus.ACTIVE,
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR,
            placeId,
            placeName,
            0,
            0
        );
    }
}
