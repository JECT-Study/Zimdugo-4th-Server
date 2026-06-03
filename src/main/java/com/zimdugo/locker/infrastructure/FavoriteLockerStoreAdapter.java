package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.FavoriteLocker;
import com.zimdugo.locker.domain.FavoriteLockerStore;
import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteLockerStoreAdapter implements FavoriteLockerStore {

    private final FavoriteLockerRepository favoriteLockerRepository;
    private final EntityManager entityManager;

    @Override
    public FavoriteLocker save(Long userId, Long lockerId) {
        try {
            FavoriteLockerEntity favoriteLocker = favoriteLockerRepository.save(
                new FavoriteLockerEntity(
                    entityManager.getReference(UserEntity.class, userId),
                    entityManager.getReference(LockerEntity.class, lockerId)
                )
            );
            return toDomain(favoriteLocker);
        } catch (DataIntegrityViolationException e) {
            if (favoriteLockerRepository.existsByUserIdAndLockerId(userId, lockerId)) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public void delete(Long userId, Long lockerId) {
        favoriteLockerRepository.deleteByUserIdAndLockerId(userId, lockerId);
    }

    private FavoriteLocker toDomain(FavoriteLockerEntity favoriteLocker) {
        return new FavoriteLocker(
            favoriteLocker.getId(),
            favoriteLocker.getUser().getId(),
            favoriteLocker.getLocker().getId(),
            favoriteLocker.getCreatedAt()
        );
    }
}
