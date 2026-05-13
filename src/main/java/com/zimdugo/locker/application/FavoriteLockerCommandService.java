package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.FavoriteLockerStore;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteLockerCommandService {

    private final FavoriteLockerStore favoriteLockerStore;

    public void add(Long userId, Long lockerId) {
        favoriteLockerStore.add(userId, lockerId);
    }

    public void remove(Long userId, Long lockerId) {
        favoriteLockerStore.remove(userId, lockerId);
    }

    public void reorder(Long userId, List<Long> lockerIds) {
        favoriteLockerStore.reorder(userId, lockerIds);
    }
}
