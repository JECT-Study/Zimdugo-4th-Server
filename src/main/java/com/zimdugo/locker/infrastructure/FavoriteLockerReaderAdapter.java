package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.FavoriteLocker;
import com.zimdugo.locker.domain.FavoriteLockerPage;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class FavoriteLockerReaderAdapter implements FavoriteLockerReader {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final UserLockerFavoriteRepository userLockerFavoriteRepository;
    private final LockerReportRepository lockerReportRepository;
    private final FavoriteLockerProperties favoriteLockerProperties;

    @Override
    public FavoriteLockerPage findByUserId(Long userId, int page, int size, Double latitude, Double longitude) {
        Page<UserLockerFavoriteEntity> favorites =
            userLockerFavoriteRepository.findByUserIdAndLockerDeletedFalseOrderByDisplayOrderAscCreatedAtDesc(
                userId,
                PageRequest.of(page, size)
            );
        Coordinate origin = resolveOrigin(latitude, longitude);
        Map<Long, LocalDateTime> lastCompletedVoteAtByLockerId =
            getLastCompletedVoteAtByLockerId(favorites.getContent());

        return new FavoriteLockerPage(
            favorites.getTotalElements(),
            favorites.getNumber(),
            favorites.getSize(),
            favorites.hasNext(),
            favorites.getContent().stream()
                .map(favorite -> toFavoriteLocker(favorite, origin, lastCompletedVoteAtByLockerId))
                .toList()
        );
    }

    @Override
    public boolean existsByUserIdAndLockerId(Long userId, Long lockerId) {
        return userLockerFavoriteRepository.existsByUserIdAndLockerIdAndLockerDeletedFalse(userId, lockerId);
    }

    private FavoriteLocker toFavoriteLocker(
        UserLockerFavoriteEntity favorite,
        Coordinate origin,
        Map<Long, LocalDateTime> lastCompletedVoteAtByLockerId
    ) {
        Long lockerId = favorite.getLocker().getId();
        return new FavoriteLocker(
            lockerId,
            favorite.getLocker().getName(),
            favorite.getLocker().getRoadAddress(),
            favorite.getLocker().getLatitude(),
            favorite.getLocker().getLongitude(),
            favorite.getCreatedAt(),
            lastCompletedVoteAtByLockerId.get(lockerId),
            calculateDistanceMeters(
                origin.latitude(),
                origin.longitude(),
                favorite.getLocker().getLatitude(),
                favorite.getLocker().getLongitude()
            )
        );
    }

    private Map<Long, LocalDateTime> getLastCompletedVoteAtByLockerId(List<UserLockerFavoriteEntity> favorites) {
        List<Long> lockerIds = favorites.stream()
            .map(favorite -> favorite.getLocker().getId())
            .toList();

        if (lockerIds.isEmpty()) {
            return Map.of();
        }

        return lockerReportRepository.findLatestCompletedVoteAtByLockerIdIn(lockerIds).stream()
            .collect(toMap(
                LockerReportLatestUpdateProjection::getLockerId,
                LockerReportLatestUpdateProjection::getLastCompletedVoteAt
            ));
    }

    private Coordinate resolveOrigin(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return new Coordinate(latitude, longitude);
        }

        return new Coordinate(
            favoriteLockerProperties.defaultOrigin().latitude(),
            favoriteLockerProperties.defaultOrigin().longitude()
        );
    }

    private Long calculateDistanceMeters(
        double latitude,
        double longitude,
        double lockerLatitude,
        double lockerLongitude
    ) {
        double latitudeDelta = Math.toRadians(lockerLatitude - latitude);
        double longitudeDelta = Math.toRadians(lockerLongitude - longitude);
        double startLatitude = Math.toRadians(latitude);
        double endLatitude = Math.toRadians(lockerLatitude);

        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
            + Math.cos(startLatitude) * Math.cos(endLatitude)
            * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(EARTH_RADIUS_METERS * c);
    }

    private record Coordinate(
        double latitude,
        double longitude
    ) {
    }
}
