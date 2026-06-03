package com.zimdugo.locker.infrastructure;

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
    public void save(Long userId, Long lockerId) {
        try {
            favoriteLockerRepository.save(
                new FavoriteLockerEntity(
                    entityManager.getReference(UserEntity.class, userId),
                    entityManager.getReference(LockerEntity.class, lockerId)
                )
            );
        } catch (DataIntegrityViolationException e) {
            if (favoriteLockerRepository.existsByUserIdAndLockerId(userId, lockerId)) {
                return;
            }
            throw e;
        }
    }

    @Override
    public void delete(Long userId, Long lockerId) {
        favoriteLockerRepository.deleteByUserIdAndLockerId(userId, lockerId);
    }
}
