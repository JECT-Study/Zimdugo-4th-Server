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

    private final UserLockerFavoriteRepository userLockerFavoriteRepository;
    private final LockerReportRepository lockerReportRepository;
    private final LockerRepository lockerRepository;
    private final FavoriteLockerProperties favoriteLockerProperties;

    @Override
    public FavoriteLockerPage findByUserId(Long userId, int page, int size, Double latitude, Double longitude) {
        Page<UserLockerFavoriteEntity> favorites =
            userLockerFavoriteRepository.findActiveFavoritesByUserId(
                userId,
                PageRequest.of(page, size)
            );
        Coordinate origin = resolveOrigin(latitude, longitude);
        Map<Long, LocalDateTime> lastCompletedVoteAtByLockerId =
            getLastCompletedVoteAtByLockerId(favorites.getContent());
        Map<Long, Long> distanceMetersByLockerId = getDistanceMetersByLockerId(favorites.getContent(), origin);

        return new FavoriteLockerPage(
            favorites.getTotalElements(),
            favorites.getNumber(),
            favorites.getSize(),
            favorites.hasNext(),
            favorites.getContent().stream()
                .map(favorite -> toFavoriteLocker(favorite, lastCompletedVoteAtByLockerId, distanceMetersByLockerId))
                .toList()
        );
    }

    @Override
    public boolean existsByUserIdAndLockerId(Long userId, Long lockerId) {
        return userLockerFavoriteRepository.countActiveFavoritesByUserIdAndLockerId(userId, lockerId) > 0;
    }

    private FavoriteLocker toFavoriteLocker(
        UserLockerFavoriteEntity favorite,
        Map<Long, LocalDateTime> lastCompletedVoteAtByLockerId,
        Map<Long, Long> distanceMetersByLockerId
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
            distanceMetersByLockerId.get(lockerId)
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

    private Map<Long, Long> getDistanceMetersByLockerId(List<UserLockerFavoriteEntity> favorites, Coordinate origin) {
        List<Long> lockerIds = favorites.stream()
            .map(favorite -> favorite.getLocker().getId())
            .toList();

        if (lockerIds.isEmpty()) {
            return Map.of();
        }

        return lockerRepository.findDistancesByLockerIds(origin.latitude(), origin.longitude(), lockerIds).stream()
            .collect(toMap(
                LockerDistanceProjection::getLockerId,
                LockerDistanceProjection::getDistanceMeters
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

    private record Coordinate(
        double latitude,
        double longitude
    ) {
    }
}
