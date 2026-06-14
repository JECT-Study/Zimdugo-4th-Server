package com.zimdugo.locker.infrastructure;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerTranslationRepository extends JpaRepository<LockerTranslationEntity, Long> {

    Optional<LockerTranslationEntity> findByLockerIdAndLanguage(Long lockerId, SupportedLanguage language);

    List<LockerTranslationEntity> findByLockerIdAndLanguageIn(
        Long lockerId,
        Collection<SupportedLanguage> languages
    );

    List<LockerTranslationEntity> findByLockerIdInAndLanguageIn(
        Collection<Long> lockerIds,
        Collection<SupportedLanguage> languages
    );

    List<LockerTranslationEntity> findByLockerId(Long lockerId);

    List<LockerTranslationEntity> findByLockerIdIn(Collection<Long> lockerIds);

    @Modifying
    @Query("DELETE FROM LockerTranslationEntity translation WHERE translation.locker.id = :lockerId")
    int deleteByLockerId(@Param("lockerId") Long lockerId);
}
