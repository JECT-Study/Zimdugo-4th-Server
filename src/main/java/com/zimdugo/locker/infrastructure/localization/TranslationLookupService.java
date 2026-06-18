package com.zimdugo.locker.infrastructure.localization;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TranslationLookupService {

    private final CurrentRequestLanguage currentRequestLanguage;
    private final PlaceRepository placeRepository;
    private final LockerRepository lockerRepository;
    private final PlaceTranslationRepository placeTranslationRepository;
    private final LockerTranslationRepository lockerTranslationRepository;

    public LocalizedPlaceContent resolvePlace(Long placeId) {
        PlaceEntity place = placeRepository.findById(placeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        return resolvePlace(place, currentRequestLanguage.resolve());
    }

    public LocalizedLockerContent resolveLocker(Long lockerId) {
        LockerEntity locker = lockerRepository.findById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
        return resolveLocker(locker, currentRequestLanguage.resolve());
    }

    public Map<Long, LocalizedPlaceContent> resolvePlaces(Collection<Long> placeIds) {
        List<Long> ids = distinctIds(placeIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        SupportedLanguage requestedLanguage = currentRequestLanguage.resolve();
        Map<Long, PlaceEntity> places = requiredPlaces(ids);
        Map<Long, PlaceTranslationEntity> translations = placeTranslationRepository
            .findByPlaceIdInAndLanguageIn(ids, List.of(requestedLanguage))
            .stream()
            .collect(Collectors.toMap(translation -> translation.getPlace().getId(), Function.identity()));
        
        Map<Long, LocalizedPlaceContent> contents = new LinkedHashMap<>();
        for (Long id : ids) {
            PlaceEntity place = places.get(id);
            PlaceTranslationEntity content = translations.get(id);
            if (content == null) {
                throw new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING);
            }
            contents.put(id, new LocalizedPlaceContent(
                content.getName(),
                content.getRoadAddress(),
                content.getLanguage()
            ));
        }
        return Map.copyOf(contents);
    }

    public Map<Long, LocalizedLockerContent> resolveLockers(Collection<Long> lockerIds) {
        List<Long> ids = distinctIds(lockerIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        SupportedLanguage requestedLanguage = currentRequestLanguage.resolve();
        Map<Long, LockerEntity> lockers = requiredLockers(ids);
        Map<Long, LockerTranslationEntity> translations = lockerTranslationRepository
            .findByLockerIdInAndLanguageIn(ids, List.of(requestedLanguage))
            .stream()
            .collect(Collectors.toMap(translation -> translation.getLocker().getId(), Function.identity()));
        
        Map<Long, LocalizedLockerContent> contents = new LinkedHashMap<>();
        for (Long id : ids) {
            LockerEntity locker = lockers.get(id);
            LockerTranslationEntity content = translations.get(id);
            if (content == null) {
                throw new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING);
            }
            contents.put(id, new LocalizedLockerContent(
                content.getName(),
                content.getRoadAddress(),
                content.getDetailInfo(),
                content.getLanguage()
            ));
        }
        return Map.copyOf(contents);
    }

    public LocalizedPlaceContent resolvePlace(PlaceEntity place, SupportedLanguage requestedLanguage) {
        PlaceTranslationEntity content = placeTranslationRepository
            .findByPlaceIdAndLanguage(place.getId(), requestedLanguage)
            .orElseThrow(() -> new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING));
        return new LocalizedPlaceContent(
            content.getName(),
            content.getRoadAddress(),
            content.getLanguage()
        );
    }

    public LocalizedLockerContent resolveLocker(LockerEntity locker, SupportedLanguage requestedLanguage) {
        LockerTranslationEntity content = lockerTranslationRepository
            .findByLockerIdAndLanguage(locker.getId(), requestedLanguage)
            .orElseThrow(() -> new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING));
        return new LocalizedLockerContent(
            content.getName(),
            content.getRoadAddress(),
            content.getDetailInfo(),
            content.getLanguage()
        );
    }

    private List<Long> distinctIds(Collection<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private Map<Long, PlaceEntity> requiredPlaces(List<Long> ids) {
        Map<Long, PlaceEntity> places = placeRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(PlaceEntity::getId, Function.identity()));
        if (places.size() != ids.size()) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        }
        return places;
    }

    private Map<Long, LockerEntity> requiredLockers(List<Long> ids) {
        Map<Long, LockerEntity> lockers = lockerRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(LockerEntity::getId, Function.identity()));
        if (lockers.size() != ids.size()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_FOUND);
        }
        return lockers;
    }

}
