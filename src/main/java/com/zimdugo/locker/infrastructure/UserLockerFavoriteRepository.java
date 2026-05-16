package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLockerFavoriteRepository extends JpaRepository<UserLockerFavoriteEntity, Long> {

    @EntityGraph(attributePaths = "locker")
    @Query(
        value = """
            select favorite
            from UserLockerFavoriteEntity favorite
            join favorite.locker locker
            where favorite.user.id = :userId
              and locker.deleted = false
            order by favorite.displayOrder asc, favorite.createdAt desc
            """,
        countQuery = """
            select count(favorite)
            from UserLockerFavoriteEntity favorite
            join favorite.locker locker
            where favorite.user.id = :userId
              and locker.deleted = false
            """
    )
    Page<UserLockerFavoriteEntity> findActiveFavoritesByUserId(
        @Param("userId") Long userId,
        Pageable pageable
    );

    @Query("""
        select count(favorite)
        from UserLockerFavoriteEntity favorite
        join favorite.locker locker
        where favorite.user.id = :userId
          and locker.id = :lockerId
          and locker.deleted = false
        """)
    long countActiveFavoritesByUserIdAndLockerId(
        @Param("userId") Long userId,
        @Param("lockerId") Long lockerId
    );

    void deleteByUserIdAndLockerId(Long userId, Long lockerId);

    @EntityGraph(attributePaths = "locker")
    @Query("""
        select favorite
        from UserLockerFavoriteEntity favorite
        join favorite.locker locker
        where favorite.user.id = :userId
          and locker.id in :lockerIds
          and locker.deleted = false
        """)
    List<UserLockerFavoriteEntity> findActiveFavoritesByUserIdAndLockerIds(
        @Param("userId") Long userId,
        @Param("lockerIds") Collection<Long> lockerIds
    );

    @Query("""
        select count(favorite)
        from UserLockerFavoriteEntity favorite
        join favorite.locker locker
        where favorite.user.id = :userId
          and locker.deleted = false
        """)
    long countActiveFavoritesByUserId(@Param("userId") Long userId);

    @Query("""
        select max(favorite.displayOrder)
        from UserLockerFavoriteEntity favorite
        join favorite.locker locker
        where favorite.user.id = :userId
          and locker.deleted = false
        """)
    Integer findMaxDisplayOrderAmongActiveFavoritesByUserId(@Param("userId") Long userId);

    @Query("""
        select count(favorite)
        from UserLockerFavoriteEntity favorite
        join favorite.locker locker
        where favorite.user.id = :userId
          and locker.id in :lockerIds
          and locker.deleted = false
        """)
    long countActiveFavoritesByUserIdAndLockerIds(
        @Param("userId") Long userId,
        @Param("lockerIds") Collection<Long> lockerIds
    );
}
