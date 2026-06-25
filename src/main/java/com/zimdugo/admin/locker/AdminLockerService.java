package com.zimdugo.admin.locker;

import com.zimdugo.admin.i18n.LockerContentI18nChangedEvent;
import com.zimdugo.admin.locker.dto.AdminLockerCommand;
import com.zimdugo.admin.locker.dto.AdminLockerDetailResult;
import com.zimdugo.admin.locker.dto.AdminLockerDisplay;
import com.zimdugo.admin.locker.dto.AdminLockerOption;
import com.zimdugo.admin.locker.dto.AdminLockerPageResult;
import com.zimdugo.admin.locker.dto.AdminLockerSummaryResult;
import com.zimdugo.admin.translation.LockerReportTranslationDraftGenerator;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerRepository;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailUpdateValues;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerUpdateValues;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.locker.infrastructure.projection.AdminLockerPlaceGroupProjection;
import com.zimdugo.locker.infrastructure.projection.AdminLockerSummaryProjection;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLockerService {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final LockerRepository lockerRepository;
    private final LockerDetailRepository lockerDetailRepository;
    private final PlaceRepository placeRepository;
    private final LockerAliasRepository lockerAliasRepository;
    private final LockerTranslationRepository lockerTranslationRepository;
    private final FavoriteLockerRepository favoriteLockerRepository;
    private final LockerVoteRepository lockerVoteRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LockerReportTranslationDraftGenerator draftGenerator;

    public AdminLockerPageResult getLockers(String keyword, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), DEFAULT_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<AdminLockerPlaceGroupProjection> placeGroups = findPlaceGroups(normalizedKeyword, pageable);
        List<AdminLockerSummaryResult> lockers = findLockers(normalizedKeyword, placeGroups.getContent()).stream()
            .map(AdminLockerSummaryResult::from)
            .toList();
        return AdminLockerPageResult.fromGroups(placeGroups, lockers);
    }

    public List<AdminLockerOption> getLockerTypeOptions() {
        return Arrays.stream(LockerType.values())
            .map(value -> new AdminLockerOption(value.name(), AdminLockerDisplay.lockerType(value)))
            .toList();
    }

    public List<AdminLockerOption> getIndoorOutdoorTypeOptions() {
        return Arrays.stream(IndoorOutdoorType.values())
            .map(value -> new AdminLockerOption(value.name(), AdminLockerDisplay.indoorOutdoor(value)))
            .toList();
    }

    public List<AdminLockerOption> getGroundLevelTypeOptions() {
        return Arrays.stream(GroundLevelType.values())
            .map(value -> new AdminLockerOption(value.name(), AdminLockerDisplay.groundLevel(value)))
            .toList();
    }

    public List<AdminLockerOption> getLockerSizeOptions() {
        return Arrays.stream(LockerSizeType.values())
            .map(value -> new AdminLockerOption(value.name(), AdminLockerDisplay.lockerSize(value)))
            .toList();
    }

    public AdminLockerDetailResult getLocker(Long id) {
        LockerEntity locker = getLockerEntity(id);
        return AdminLockerDetailResult.from(locker, getDetailEntity(locker.getId()));
    }

    public AdminTranslationDraftResult generateDraft(Long id) {
        return draftGenerator.generate(translationSource(id));
    }

    public AdminTranslationDraftResult generateDraft(Long id, SupportedLanguage language) {
        return draftGenerator.generate(translationSource(id), language);
    }

    @Transactional
    public AdminLockerDetailResult updateLocker(Long id, AdminLockerCommand command) {
        validate(command);
        LockerEntity locker = getLockerEntity(id);
        PlaceEntity place = getPlace(command.placeId());
        locker.update(new LockerUpdateValues(
            command.name(),
            command.roadAddress(),
            command.latitude(),
            command.longitude(),
            place,
            PublicationStatus.DRAFT
        ));
        LockerDetailEntity detail = getDetailEntity(id);
        detail.update(new LockerDetailUpdateValues(
            command.lockerType(),
            command.indoorOutdoorType(),
            command.groundLevelType(),
            command.floor(),
            command.minPrice(),
            command.maxPrice(),
            command.lockerSizes(),
            command.detailInfo(),
            command.startTime(),
            command.endTime(),
            command.imageUrl()
        ));
        publishChanged(id);
        return AdminLockerDetailResult.from(locker, detail);
    }

    @Transactional
    public void deleteLocker(Long id) {
        LockerEntity locker = getLockerEntity(id);
        lockerAliasRepository.deleteByLockerId(id);
        lockerTranslationRepository.deleteByLockerId(id);
        favoriteLockerRepository.deleteByLockerId(id);
        lockerVoteRepository.deleteByLockerId(id);
        lockerDetailRepository.deleteByLockerId(id);
        lockerRepository.delete(locker);
        publishChanged(id);
    }

    @Transactional
    public void approveLocker(Long id) {
        LockerEntity locker = getLockerEntity(id);
        if (!hasRequiredTranslations(id)) {
            throw new BusinessException(ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS);
        }
        locker.activate();
        publishChanged(id);
    }

    private LockerEntity getLockerEntity(Long id) {
        return lockerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private LockerDetailEntity getDetailEntity(Long lockerId) {
        return lockerDetailRepository.findByLockerId(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private PlaceEntity getPlace(Long placeId) {
        if (placeId == null) {
            return null;
        }
        return placeRepository.findById(placeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }

    private void validate(AdminLockerCommand command) {
        if (command.latitude() < MIN_LATITUDE || command.latitude() > MAX_LATITUDE
            || command.longitude() < MIN_LONGITUDE || command.longitude() > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION_RANGE);
        }
        if (command.minPrice() != null && command.maxPrice() != null
            && command.minPrice() > command.maxPrice()) {
            throw new BusinessException(ErrorCode.INVALID_ADMIN_LOCKER_INPUT);
        }
        if (command.floor() != null && command.groundLevelType() == null) {
            throw new BusinessException(ErrorCode.INVALID_ADMIN_LOCKER_INPUT);
        }
    }

    private boolean hasRequiredTranslations(Long lockerId) {
        List<LockerTranslationEntity> translations = lockerTranslationRepository.findByLockerId(lockerId);
        return SupportedLanguage.translationTargets().stream()
            .allMatch(language -> translations.stream()
                .anyMatch(translation -> translation.getLanguage() == language
                    && hasText(translation.getName())
                    && hasText(translation.getRoadAddress())));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void publishChanged(Long lockerId) {
        eventPublisher.publishEvent(LockerContentI18nChangedEvent.locker(lockerId));
    }

    private LockerReportTranslationSource translationSource(Long lockerId) {
        LockerEntity locker = getLockerEntity(lockerId);
        LockerDetailEntity detail = getDetailEntity(lockerId);
        return new LockerReportTranslationSource(
            lockerId,
            locker.getPlace() == null ? locker.getName() : locker.getPlace().getName(),
            locker.getPlace() == null ? locker.getRoadAddress() : locker.getPlace().getRoadAddress(),
            locker.getName(),
            locker.getRoadAddress(),
            detail.getGroundLevelType() == null ? null : detail.getGroundLevelType().name(),
            detail.getFloor(),
            detail.getIndoorOutdoorType().name(),
            detail.getLockerType().name(),
            detail.getLockerSize() == null ? "" : detail.getLockerSize().stream()
                .map(Enum::name)
                .sorted()
                .collect(java.util.stream.Collectors.joining(", ")),
            "UNKNOWN",
            detail.getMinPrice(),
            detail.getMaxPrice(),
            detail.getDetailInfo(),
            "UNKNOWN",
            detail.getStartTime() == null ? null : detail.getStartTime().toString(),
            detail.getEndTime() == null ? null : detail.getEndTime().toString()
        );
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private Page<AdminLockerPlaceGroupProjection> findPlaceGroups(String keyword, Pageable pageable) {
        if (keyword == null) {
            return lockerRepository.findAdminPlaceGroups(pageable);
        }
        return lockerRepository.searchAdminPlaceGroups(keyword, pageable);
    }

    private List<AdminLockerSummaryProjection> findLockers(
        String keyword,
        List<AdminLockerPlaceGroupProjection> placeGroups
    ) {
        List<Long> placeIds = placeGroups.stream()
            .map(AdminLockerPlaceGroupProjection::getPlaceId)
            .filter(java.util.Objects::nonNull)
            .toList();
        boolean includesUnassigned = placeGroups.stream()
            .anyMatch(group -> group.getPlaceId() == null);

        if (placeIds.isEmpty()) {
            return includesUnassigned ? findUnassignedLockers(keyword) : List.of();
        }
        if (includesUnassigned) {
            return keyword == null
                ? lockerRepository.findAdminSummariesByPlaceIdsOrWithoutPlace(placeIds)
                : lockerRepository.searchAdminSummariesByPlaceIdsOrWithoutPlace(placeIds, keyword);
        }
        return keyword == null
            ? lockerRepository.findAdminSummariesByPlaceIds(placeIds)
            : lockerRepository.searchAdminSummariesByPlaceIds(placeIds, keyword);
    }

    private List<AdminLockerSummaryProjection> findUnassignedLockers(String keyword) {
        return keyword == null
            ? lockerRepository.findAdminSummariesWithoutPlace()
            : lockerRepository.searchAdminSummariesWithoutPlace(keyword);
    }

}
