package com.zimdugo.locker.domain;

import java.util.List;

public interface FavoriteLockerStore {

    void add(Long userId, Long lockerId);

    void remove(Long userId, Long lockerId);

    void reorder(Long userId, List<Long> lockerIds);
}
