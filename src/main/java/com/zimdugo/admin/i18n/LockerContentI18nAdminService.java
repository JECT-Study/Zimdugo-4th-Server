package com.zimdugo.admin.i18n;

import com.zimdugo.admin.ui.dto.AdminLockerI18nRequest;
import com.zimdugo.admin.ui.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.ui.dto.AdminPlaceI18nRequest;
import com.zimdugo.admin.ui.dto.AdminPlaceI18nResponse;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.LockerRepository;
import com.zimdugo.locker.infrastructure.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.PlaceAliasRepository;
import com.zimdugo.locker.infrastructure.PlaceRepository;
import com.zimdugo.locker.infrastructure.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerContentI18nAdminService {

    private final PlaceRepository placeRepository;
    private final LockerRepository lockerRepository;
    private final PlaceTranslationRepository placeTranslationRepository;
    private final LockerTranslationRepository lockerTranslationRepository;
    private final PlaceAliasRepository placeAliasRepository;
    private final LockerAliasRepository lockerAliasRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminPlaceI18nResponse getPlace(Long placeId) {
        requirePlace(placeId);
        return placeResponse(placeId);
    }

    public AdminLockerI18nResponse getLocker(Long lockerId) {
        requireLocker(lockerId);
        return lockerResponse(lockerId);
    }

    @Transactional
    public AdminPlaceI18nResponse replacePlace(Long placeId, AdminPlaceI18nRequest request) {
        PlaceEntity place = requirePlace(placeId);
        validateLanguages(request.translations().stream().map(AdminPlaceI18nRequest.Translation::language).toList());
        placeAliasRepository.deleteByPlaceId(placeId);
        placeTranslationRepository.deleteByPlaceId(placeId);
        placeTranslationRepository.saveAll(request.translations().stream()
            .map(item -> new PlaceTranslationEntity(place, item.language(), item.name(), item.roadAddress()))
            .toList());
        placeAliasRepository.saveAll(aliases(request.aliases()).stream()
            .map(item -> new PlaceAliasEntity(place, item.language(), item.alias()))
            .toList());
        eventPublisher.publishEvent(LockerContentI18nChangedEvent.place(placeId));
        return placeResponse(placeId);
    }

    @Transactional
    public AdminLockerI18nResponse replaceLocker(Long lockerId, AdminLockerI18nRequest request) {
        LockerEntity locker = requireLocker(lockerId);
        validateLanguages(request.translations().stream().map(AdminLockerI18nRequest.Translation::language).toList());
        lockerAliasRepository.deleteByLockerId(lockerId);
        lockerTranslationRepository.deleteByLockerId(lockerId);
        lockerTranslationRepository.saveAll(request.translations().stream()
            .map(item -> new LockerTranslationEntity(
                locker,
                item.language(),
                item.name(),
                item.roadAddress(),
                item.detailInfo()
            ))
            .toList());
        lockerAliasRepository.saveAll(lockerAliases(request.aliases()).stream()
            .map(item -> new LockerAliasEntity(locker, item.language(), item.alias()))
            .toList());
        eventPublisher.publishEvent(LockerContentI18nChangedEvent.locker(lockerId));
        return lockerResponse(lockerId);
    }

    private void validateLanguages(List<SupportedLanguage> languages) {
        Set<SupportedLanguage> unique = new HashSet<>(languages);
        Set<SupportedLanguage> required = new HashSet<>(SupportedLanguage.all());
        if (unique.size() != languages.size() || !unique.equals(required)) {
            throw new BusinessException(ErrorCode.INVALID_I18N_CONTENT);
        }
    }

    private List<AdminPlaceI18nRequest.Alias> aliases(List<AdminPlaceI18nRequest.Alias> aliases) {
        return aliases == null ? List.of() : aliases;
    }

    private List<AdminLockerI18nRequest.Alias> lockerAliases(List<AdminLockerI18nRequest.Alias> aliases) {
        return aliases == null ? List.of() : aliases;
    }

    private PlaceEntity requirePlace(Long placeId) {
        return placeRepository.findById(placeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }

    private LockerEntity requireLocker(Long lockerId) {
        return lockerRepository.findById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private AdminPlaceI18nResponse placeResponse(Long placeId) {
        return AdminPlaceI18nResponse.of(
            placeId,
            placeTranslationRepository.findByPlaceId(placeId),
            placeAliasRepository.findByPlaceId(placeId)
        );
    }

    private AdminLockerI18nResponse lockerResponse(Long lockerId) {
        return AdminLockerI18nResponse.of(
            lockerId,
            lockerTranslationRepository.findByLockerId(lockerId),
            lockerAliasRepository.findByLockerId(lockerId)
        );
    }
}
