package com.zimdugo.locker.infrastructure;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceTranslationRepository extends JpaRepository<PlaceTranslationEntity, Long> {

    Optional<PlaceTranslationEntity> findByPlaceIdAndLanguage(Long placeId, SupportedLanguage language);

    List<PlaceTranslationEntity> findByPlaceIdAndLanguageIn(
        Long placeId,
        Collection<SupportedLanguage> languages
    );

    List<PlaceTranslationEntity> findByPlaceIdInAndLanguageIn(
        Collection<Long> placeIds,
        Collection<SupportedLanguage> languages
    );

    List<PlaceTranslationEntity> findByPlaceId(Long placeId);

    List<PlaceTranslationEntity> findByPlaceIdIn(Collection<Long> placeIds);

    @Modifying
    @Query("DELETE FROM PlaceTranslationEntity translation WHERE translation.place.id = :placeId")
    int deleteByPlaceId(@Param("placeId") Long placeId);
}
