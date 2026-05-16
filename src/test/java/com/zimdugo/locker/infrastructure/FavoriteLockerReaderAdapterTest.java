package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.FavoriteLockerPage;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FavoriteLockerReaderAdapterTest {

    @Mock
    private UserLockerFavoriteRepository userLockerFavoriteRepository;

    @Mock
    private LockerReportRepository lockerReportRepository;

    @Test
    @DisplayName("현재 위치가 없으면 기본 위치로 거리를 계산하고 최신 완료 제보 시각을 반영한다")
    void findByUserIdUsesConfiguredDefaultOriginAndLatestCompletedVoteAt() {
        double defaultLatitude = 37.497942;
        double defaultLongitude = 127.027621;
        FavoriteLockerReaderAdapter adapter = new FavoriteLockerReaderAdapter(
            userLockerFavoriteRepository,
            lockerReportRepository,
            new FavoriteLockerProperties(
                new FavoriteLockerProperties.DefaultOrigin(defaultLatitude, defaultLongitude)
            )
        );

        UserLockerFavoriteEntity favorite = favoriteLocker(10L, 37.556, 126.923);
        LocalDateTime lastCompletedVoteAt = LocalDateTime.of(2026, 5, 13, 19, 45);

        given(userLockerFavoriteRepository.findActiveFavoritesByUserId(
            1L,
            PageRequest.of(0, 20)
        ))
            .willReturn(new PageImpl<>(List.of(favorite), PageRequest.of(0, 20), 1));
        given(lockerReportRepository.findLatestCompletedVoteAtByLockerIdIn(List.of(10L)))
            .willReturn(List.of(latestUpdateProjection(10L, lastCompletedVoteAt)));

        FavoriteLockerPage result = adapter.findByUserId(1L, 0, 20, null, null);

        assertThat(result.favorites()).hasSize(1);
        assertThat(result.favorites().get(0).distanceMeters())
            .isEqualTo(calculateDistanceMeters(defaultLatitude, defaultLongitude, 37.556, 126.923));
        assertThat(result.favorites().get(0).lastCompletedVoteAt()).isEqualTo(lastCompletedVoteAt);
    }

    private UserLockerFavoriteEntity favoriteLocker(Long lockerId, double latitude, double longitude) {
        UserEntity user = new UserEntity(
            null,
            "favorite-reader@example.com",
            "favorite-reader",
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        LockerEntity locker = new LockerEntity(
            "홍대입구역 보관함",
            "서울 마포구 양화로 160",
            latitude,
            longitude
        );
        ReflectionTestUtils.setField(locker, "id", lockerId);
        return new UserLockerFavoriteEntity(user, locker, 0);
    }

    private LockerReportLatestUpdateProjection latestUpdateProjection(
        Long lockerId,
        LocalDateTime lastCompletedVoteAt
    ) {
        return new LockerReportLatestUpdateProjection() {
            @Override
            public Long getLockerId() {
                return lockerId;
            }

            @Override
            public LocalDateTime getLastCompletedVoteAt() {
                return lastCompletedVoteAt;
            }
        };
    }

    private long calculateDistanceMeters(
        double originLatitude,
        double originLongitude,
        double lockerLatitude,
        double lockerLongitude
    ) {
        double earthRadiusMeters = 6_371_000;
        double latitudeDelta = Math.toRadians(lockerLatitude - originLatitude);
        double longitudeDelta = Math.toRadians(lockerLongitude - originLongitude);
        double startLatitude = Math.toRadians(originLatitude);
        double endLatitude = Math.toRadians(lockerLatitude);

        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
            + Math.cos(startLatitude) * Math.cos(endLatitude)
            * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusMeters * c);
    }
}
