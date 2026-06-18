package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface PlaceAliasRepository extends JpaRepository<PlaceAliasEntity, Long> {

    List<PlaceAliasEntity> findByNormalizedAlias(String normalizedAlias);

    List<PlaceAliasEntity> findByNormalizedAliasContaining(String normalizedAlias);

    List<PlaceAliasEntity> findByPlaceId(Long placeId);

    List<PlaceAliasEntity> findByPlaceIdIn(List<Long> placeIds);

    @Modifying
    @Query("DELETE FROM PlaceAliasEntity aliasEntity WHERE aliasEntity.place.id = :placeId")
    int deleteByPlaceId(@Param("placeId") Long placeId);
}
