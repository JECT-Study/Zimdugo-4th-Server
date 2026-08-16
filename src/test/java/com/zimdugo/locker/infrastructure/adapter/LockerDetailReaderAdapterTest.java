package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.detail.LockerDetail;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerImageEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerImageRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.projection.LockerDetailQueryProjection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;




@ExtendWith(MockitoExtension.class)
class LockerDetailReaderAdapterTest {

    @Mock
    private LockerRepository lockerRepository;

    @Mock
    private LockerImageRepository lockerImageRepository;

    @InjectMocks
    private LockerDetailReaderAdapter lockerDetailReaderAdapter;

    @Test
    @DisplayName("상세 조회 projection을 도메인으로 변환한다")
    void convertsProjectionToDomain() {
        LockerDetailQueryProjection projection = projection();
        given(lockerRepository.findDetailById(10L, null, "ko")).willReturn(Optional.of(projection));

        Optional<LockerDetail> result = lockerDetailReaderAdapter.readById(10L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().lockerId()).isEqualTo(10L);
        assertThat(result.orElseThrow().lockerSizes()).containsExactlyInAnyOrder(
            LockerSizeType.SMALL,
            LockerSizeType.LARGE
        );
        assertThat(result.orElseThrow().placeName()).isEqualTo("신촌역");
    }

    @Test
    @DisplayName("실시간 데이터가 있으면 잔여 수량을 도메인에 포함한다")
    void convertsRealtimeAvailabilityToDomain() {
        LockerDetailQueryProjection projection = projection();
        LocalDateTime fetchedAt = LocalDateTime.of(2026, 8, 13, 10, 0);
        given(projection.getSmallAvailableCount()).willReturn(3);
        given(projection.getMediumAvailableCount()).willReturn(2);
        given(projection.getLargeAvailableCount()).willReturn(1);
        given(projection.getRealtimeFetchedAt()).willReturn(fetchedAt);
        given(lockerRepository.findDetailById(10L, null, "ko")).willReturn(Optional.of(projection));

        LockerDetail result = lockerDetailReaderAdapter.readById(10L).orElseThrow();

        assertThat(result.realtimeAvailability()).isNotNull();
        assertThat(result.realtimeAvailability().smallAvailableCount()).isEqualTo(3);
        assertThat(result.realtimeAvailability().mediumAvailableCount()).isEqualTo(2);
        assertThat(result.realtimeAvailability().largeAvailableCount()).isEqualTo(1);
        assertThat(result.realtimeAvailability().fetchedAt()).isEqualTo(fetchedAt);
    }

    @Test
    @DisplayName("상세정보가 없는 보관함이면 빈 결과를 반환한다")
    void returnsEmptyWhenDetailDoesNotExist() {
        given(lockerRepository.findDetailById(999L, null, "ko")).willReturn(Optional.empty());

        Optional<LockerDetail> result = lockerDetailReaderAdapter.readById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상세 조회에 등록 순서대로 모든 이미지를 포함한다")
    void includesAllImagesInOrder() {
        LockerDetailQueryProjection projection = projection();
        given(lockerRepository.findDetailById(10L, null, "ko")).willReturn(Optional.of(projection));
        given(lockerImageRepository.findByLockerIdOrderByListOrderAsc(10L)).willReturn(List.of(
            LockerImageEntity.of(new LockerEntity("보관함", "서울", 37.5, 127.0), "https://cdn.example.com/first.jpg", 0),
            LockerImageEntity.of(new LockerEntity("보관함", "서울", 37.5, 127.0), "https://cdn.example.com/second.jpg", 1)
        ));

        LockerDetail result = lockerDetailReaderAdapter.readById(10L).orElseThrow();

        assertThat(result.imageUrls()).containsExactly(
            "https://cdn.example.com/first.jpg",
            "https://cdn.example.com/second.jpg"
        );
        assertThat(result.imageUrl()).isEqualTo("https://cdn.example.com/first.jpg");
    }

    private LockerDetailQueryProjection projection() {
        LockerDetailQueryProjection projection = Mockito.mock(
            LockerDetailQueryProjection.class,
            Mockito.withSettings().lenient()
        );
        given(projection.getLockerId()).willReturn(10L);
        given(projection.getLockerName()).willReturn("신촌역 보관함");
        given(projection.getRoadAddress()).willReturn("서울 서대문구");
        given(projection.getLatitude()).willReturn(37.55);
        given(projection.getLongitude()).willReturn(126.93);
        given(projection.getPlaceId()).willReturn(101L);
        given(projection.getPlaceName()).willReturn("신촌역");
        given(projection.getLockerType()).willReturn("SUBWAY_STATION");
        given(projection.getIndoorOutdoorType()).willReturn("INDOOR");
        given(projection.getGroundLevelType()).willReturn("UNDERGROUND");
        given(projection.getFloor()).willReturn(-1);
        given(projection.getMinPrice()).willReturn(1000);
        given(projection.getMaxPrice()).willReturn(3000);
        given(projection.getLockerSizes()).willReturn("SMALL,LARGE");
        given(projection.getDetailInfo()).willReturn("개찰구 옆");
        given(projection.getStartTime()).willReturn(LocalTime.of(9, 0));
        given(projection.getEndTime()).willReturn(LocalTime.of(22, 0));
        given(projection.getImageUrl()).willReturn("https://cdn.example.com/locker.jpg");
        given(projection.getAccurateVoteCount()).willReturn(10);
        given(projection.getInaccurateVoteCount()).willReturn(2);
        given(projection.getCreatedAt()).willReturn(LocalDateTime.of(2026, 6, 1, 12, 0));
        given(projection.getUpdatedAt()).willReturn(LocalDateTime.of(2026, 6, 7, 12, 0));
        return projection;
    }
}
