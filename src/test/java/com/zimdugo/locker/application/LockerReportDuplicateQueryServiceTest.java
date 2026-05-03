package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerReportDuplicateQueryServiceTest {

    @Mock
    private NearbyLockerReader nearbyLockerReader;

    @InjectMocks
    private LockerReportDuplicateQueryService lockerReportDuplicateQueryService;

    @Test
    @DisplayName("반경 내 보관함이 없으면 중복 후보 없음 응답을 반환한다")
    void findDuplicatesWithoutNearbyLockersReturnsEmptyCandidates() {
        given(nearbyLockerReader.findNearby(37.556, 126.923, 30)).willReturn(List.of());

        LockerReportDuplicateResponse result = lockerReportDuplicateQueryService.findDuplicates(37.556, 126.923, 30);

        assertThat(result.hasDuplicates()).isFalse();
        assertThat(result.radiusMeters()).isEqualTo(30);
        assertThat(result.candidates()).isEmpty();
        verify(nearbyLockerReader).findNearby(37.556, 126.923, 30);
    }

    @Test
    @DisplayName("중복 후보는 가까운 거리순으로 반환한다")
    void findDuplicatesReturnsCandidatesByDistance() {
        given(nearbyLockerReader.findNearby(37.556, 126.923, 30)).willReturn(List.of(
            new NearbyLocker(1L, "보관함 A", "서울 A", 37.556, 126.923, 20.0),
            new NearbyLocker(2L, "보관함 B", "서울 B", 37.557, 126.924, 8.4)
        ));

        LockerReportDuplicateResponse result = lockerReportDuplicateQueryService.findDuplicates(37.556, 126.923, 30);

        assertThat(result.hasDuplicates()).isTrue();
        assertThat(result.candidates())
            .extracting(LockerReportDuplicateCandidateResponse::lockerId)
            .containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("중복 후보는 최대 5개까지만 반환한다")
    void findDuplicatesLimitsCandidates() {
        given(nearbyLockerReader.findNearby(37.556, 126.923, 30)).willReturn(List.of(
            nearbyLocker(1L, 1.0),
            nearbyLocker(2L, 2.0),
            nearbyLocker(3L, 3.0),
            nearbyLocker(4L, 4.0),
            nearbyLocker(5L, 5.0),
            nearbyLocker(6L, 6.0)
        ));

        LockerReportDuplicateResponse result = lockerReportDuplicateQueryService.findDuplicates(37.556, 126.923, 30);

        assertThat(result.candidates())
            .extracting(LockerReportDuplicateCandidateResponse::lockerId)
            .containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    private NearbyLocker nearbyLocker(Long id, double distanceMeters) {
        return new NearbyLocker(id, "보관함 " + id, "서울", 37.556, 126.923, distanceMeters);
    }
}
