package com.zimdugo.admin.locker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.i18n.LockerContentI18nChangedEvent;
import com.zimdugo.admin.locker.dto.AdminLockerCommand;
import com.zimdugo.admin.locker.dto.AdminLockerOption;
import com.zimdugo.admin.locker.dto.AdminLockerSummaryResult;
import com.zimdugo.admin.translation.LockerReportTranslationDraftGenerator;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailUpdateValues;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.locker.infrastructure.projection.AdminLockerPlaceGroupProjection;
import com.zimdugo.locker.infrastructure.projection.AdminLockerSummaryProjection;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AdminLockerServiceTest {

    private LockerRepository lockerRepository;
    private LockerDetailRepository lockerDetailRepository;
    private LockerAliasRepository lockerAliasRepository;
    private LockerTranslationRepository lockerTranslationRepository;
    private FavoriteLockerRepository favoriteLockerRepository;
    private LockerVoteRepository lockerVoteRepository;
    private ApplicationEventPublisher eventPublisher;
    private AdminLockerService service;

    @BeforeEach
    void setUp() {
        lockerRepository = mock(LockerRepository.class);
        lockerDetailRepository = mock(LockerDetailRepository.class);
        lockerAliasRepository = mock(LockerAliasRepository.class);
        lockerTranslationRepository = mock(LockerTranslationRepository.class);
        favoriteLockerRepository = mock(FavoriteLockerRepository.class);
        lockerVoteRepository = mock(LockerVoteRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new AdminLockerService(
            lockerRepository,
            lockerDetailRepository,
            mock(PlaceRepository.class),
            lockerAliasRepository,
            lockerTranslationRepository,
            favoriteLockerRepository,
            lockerVoteRepository,
            eventPublisher,
            mock(LockerReportTranslationDraftGenerator.class)
        );
    }

    @Test
    void updatesActiveLockerBackToDraftUntilTranslationsAreApproved() {
        LockerEntity locker = new LockerEntity("기존", "서울", 37.5, 127.0);
        LockerDetailEntity detail = detail(locker);
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(locker));
        when(lockerDetailRepository.findByLockerId(1L)).thenReturn(Optional.of(detail));

        service.updateLocker(1L, command());

        assertThat(locker.getPublicationStatus()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(locker.getName()).isEqualTo("서울역 보관함");
        assertThat(detail.getDetailInfo()).isEqualTo("1번 출구 옆");
    }

    @Test
    void approveRequiresAllTargetLanguageTranslations() {
        LockerEntity locker = new LockerEntity("서울역 보관함", "서울", 37.5, 127.0);
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(locker));
        when(lockerTranslationRepository.findByLockerId(1L)).thenReturn(List.of(
            translation(locker, SupportedLanguage.ENGLISH)
        ));

        assertThatThrownBy(() -> service.approveLocker(1L))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS)
            );
    }

    @Test
    void approveActivatesLockerWhenRequiredTranslationsExist() {
        LockerEntity locker = LockerEntity.draft("서울역 보관함", "서울", 37.5, 127.0, null);
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(locker));
        when(lockerTranslationRepository.findByLockerId(1L)).thenReturn(
            SupportedLanguage.translationTargets().stream()
                .map(language -> translation(locker, language))
                .toList()
        );

        service.approveLocker(1L);

        assertThat(locker.getPublicationStatus()).isEqualTo(PublicationStatus.ACTIVE);
    }

    @Test
    void permanentlyDeletesLockerAndDependentDataBeforePublishingIndexChange() {
        LockerEntity locker = new LockerEntity("서울역 보관함", "서울", 37.5, 127.0);
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(locker));

        service.deleteLocker(1L);

        InOrder deletionOrder = inOrder(
            lockerAliasRepository,
            lockerTranslationRepository,
            favoriteLockerRepository,
            lockerVoteRepository,
            lockerDetailRepository,
            lockerRepository
        );
        deletionOrder.verify(lockerAliasRepository).deleteByLockerId(1L);
        deletionOrder.verify(lockerTranslationRepository).deleteByLockerId(1L);
        deletionOrder.verify(favoriteLockerRepository).deleteByLockerId(1L);
        deletionOrder.verify(lockerVoteRepository).deleteByLockerId(1L);
        deletionOrder.verify(lockerDetailRepository).deleteByLockerId(1L);
        deletionOrder.verify(lockerRepository).delete(locker);
        verify(eventPublisher).publishEvent(LockerContentI18nChangedEvent.locker(1L));
    }

    @Test
    void exposesKoreanLabelsForFormEnumOptions() {
        assertThat(service.getLockerTypeOptions())
            .contains(new AdminLockerOption("SUBWAY_STATION", "지하철역"));
        assertThat(service.getIndoorOutdoorTypeOptions())
            .containsExactly(
                new AdminLockerOption("INDOOR", "실내"),
                new AdminLockerOption("OUTDOOR", "실외")
            );
        assertThat(service.getGroundLevelTypeOptions())
            .containsExactly(
                new AdminLockerOption("ABOVE_GROUND", "지상"),
                new AdminLockerOption("UNDERGROUND", "지하")
            );
        assertThat(service.getLockerSizeOptions())
            .containsExactly(
                new AdminLockerOption("SMALL", "소형"),
                new AdminLockerOption("MEDIUM", "중형"),
                new AdminLockerOption("LARGE", "대형")
            );
    }

    @Test
    void paginatesLockerDataByPlaceGroups() {
        AdminLockerPlaceGroupProjection hongdae = placeGroup(3L, "홍대입구");
        AdminLockerPlaceGroupProjection seoulStation = placeGroup(1L, "서울역");
        List<AdminLockerSummaryProjection> lockers = List.of(
            summary(35L, 3L, "홍대입구", "홍대입구 지하 1층"),
            summary(34L, 1L, "서울역", "서울역 지상 2층"),
            summary(2L, 1L, "서울역", "서울역 지하1층 물품보관함")
        );
        when(lockerRepository.findAdminPlaceGroups(PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(List.of(hongdae, seoulStation), PageRequest.of(0, 20), 2));
        when(lockerRepository.findAdminSummariesByPlaceIds(List.of(3L, 1L))).thenReturn(lockers);

        var result = service.getLockers(null, 0);

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.groups()).hasSize(2);
        assertThat(result.groups().get(0).placeName()).isEqualTo("홍대입구");
        assertThat(result.groups().get(1).placeName()).isEqualTo("서울역");
        assertThat(result.groups().get(1).lockers())
            .extracting(AdminLockerSummaryResult::id)
            .containsExactly(34L, 2L);
    }

    private LockerTranslationEntity translation(LockerEntity locker, SupportedLanguage language) {
        return new LockerTranslationEntity(locker, language, "Locker " + language.name(), "Seoul", "Near exit");
    }

    private AdminLockerPlaceGroupProjection placeGroup(Long placeId, String placeName) {
        AdminLockerPlaceGroupProjection projection = mock(AdminLockerPlaceGroupProjection.class);
        when(projection.getPlaceId()).thenReturn(placeId);
        when(projection.getPlaceName()).thenReturn(placeName);
        return projection;
    }

    private AdminLockerSummaryProjection summary(Long id, Long placeId, String placeName, String name) {
        AdminLockerSummaryProjection projection = mock(AdminLockerSummaryProjection.class);
        when(projection.getId()).thenReturn(id);
        when(projection.getName()).thenReturn(name);
        when(projection.getRoadAddress()).thenReturn("서울 중구");
        when(projection.getPublicationStatus()).thenReturn(PublicationStatus.ACTIVE);
        when(projection.getLockerType()).thenReturn(LockerType.SUBWAY_STATION);
        when(projection.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.INDOOR);
        when(projection.getPlaceId()).thenReturn(placeId);
        when(projection.getPlaceName()).thenReturn(placeName);
        when(projection.getMinPrice()).thenReturn(0);
        when(projection.getMaxPrice()).thenReturn(0);
        return projection;
    }

    private LockerDetailEntity detail(LockerEntity locker) {
        return new LockerDetailEntity(
            locker,
            new LockerDetailUpdateValues(
                LockerType.ETC,
                IndoorOutdoorType.INDOOR,
                null,
                null,
                null,
                null,
                Set.of(),
                null,
                null,
                null,
                null
            )
        );
    }

    private AdminLockerCommand command() {
        return new AdminLockerCommand(
            "서울역 보관함",
            "서울 중구 세종대로",
            37.5,
            127.0,
            null,
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR,
            null,
            null,
            1000,
            3000,
            Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
            "1번 출구 옆",
            LocalTime.of(9, 0),
            LocalTime.of(22, 0),
            "https://cdn.example.com/locker.jpg"
        );
    }
}
