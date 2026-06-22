package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.admin.entrypoint.dto.AdminLockerReportTranslationsForm;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nRequest;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nRequest;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AdminLockerReportTranslationServiceTest {

    @Mock
    private LockerReportRepository lockerReportRepository;
    @Mock
    private LockerContentI18nAdminService i18nAdminService;
    @Mock
    private LockerReportTranslationDraftGenerator draftGenerator;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private LockerRepository lockerRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AdminLockerReportTranslationService service;

    @BeforeEach
    void setUp() {
        service = new AdminLockerReportTranslationService(
            lockerReportRepository,
            placeRepository,
            lockerRepository,
            i18nAdminService,
            draftGenerator,
            eventPublisher
        );
    }

    @Test
    void rejectsTranslationPageWhenReportIsNotApplied() {
        LockerReportEntity report = report();
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.getTranslationPage(1L))
            .isInstanceOf(com.zimdugo.core.exception.BusinessException.class);
        verify(i18nAdminService, never()).getPlace(any());
        verify(i18nAdminService, never()).getLocker(any());
    }

    @Test
    void getsTranslationPageWithExistingI18nWhenReportIsApplied() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        AdminPlaceI18nResponse placeI18n = new AdminPlaceI18nResponse(
            10L, placeTranslations(), List.of()
        );
        AdminLockerI18nResponse lockerI18n = new AdminLockerI18nResponse(
            20L, lockerTranslations(), List.of()
        );
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getId()).thenReturn(10L);
        when(place.getName()).thenReturn("서울역");
        when(locker.getId()).thenReturn(20L);
        when(locker.getName()).thenReturn("서울역 보관함");
        when(i18nAdminService.getPlace(10L)).thenReturn(placeI18n);
        when(i18nAdminService.getLocker(20L)).thenReturn(lockerI18n);

        AdminLockerReportTranslationPageResult result = service.getTranslationPage(1L);

        assertThat(result.placeI18n()).isSameAs(placeI18n);
        assertThat(result.lockerI18n()).isSameAs(lockerI18n);
        assertThat(result.appliedPlaceName()).isEqualTo("서울역");
        assertThat(result.appliedLockerName()).isEqualTo("서울역 보관함");
        assertThat(result.translationComplete()).isTrue();
    }

    @Test
    void generatesDraftFromReportSource() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        AdminTranslationDraftResult draft = new AdminTranslationDraftResult(
            List.of(new AdminTranslationDraftResult.PlaceTranslation(
                SupportedLanguage.ENGLISH,
                "Seoul Station",
                "Seoul Station Road",
                List.of("locker")
            )),
            List.of(new AdminTranslationDraftResult.LockerTranslation(
                SupportedLanguage.ENGLISH,
                "Locker",
                "Seoul Station Road",
                "Near exit 1",
                List.of("locker")
            ))
        );
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getName()).thenReturn("서울역");
        when(place.getRoadAddress()).thenReturn("서울 중구 한강대로");
        when(locker.getName()).thenReturn("서울역 보관함");
        when(locker.getRoadAddress()).thenReturn("서울 중구 세종대로");
        when(draftGenerator.generate(any())).thenReturn(draft);

        AdminTranslationDraftResult result = service.generateDraft(1L);

        ArgumentCaptor<LockerReportTranslationSource> captor =
            ArgumentCaptor.forClass(LockerReportTranslationSource.class);
        verify(draftGenerator).generate(captor.capture());
        assertThat(result).isSameAs(draft);
        assertThat(captor.getValue().reportId()).isEqualTo(1L);
        assertThat(captor.getValue().placeName()).isEqualTo("서울역");
        assertThat(captor.getValue().lockerName()).isEqualTo("서울역 보관함");
    }

    @Test
    void generatesDraftForRequestedLanguageOnly() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        AdminTranslationDraftResult draft = japaneseDraft();
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getName()).thenReturn("서울역");
        when(locker.getName()).thenReturn("서울역 보관함");
        when(draftGenerator.generate(any(), eq(SupportedLanguage.JAPANESE))).thenReturn(draft);

        AdminTranslationDraftResult result = service.generateDraft(
            1L,
            SupportedLanguage.JAPANESE
        );

        verify(draftGenerator).generate(any(), eq(SupportedLanguage.JAPANESE));
        assertThat(result).isSameAs(draft);
    }

    @Test
    void rejectsRequestedLanguageDraftWhenTranslationIsMissing() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        AdminTranslationDraftResult emptyDraft = new AdminTranslationDraftResult(
            List.of(),
            List.of()
        );
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(draftGenerator.generate(any(), eq(SupportedLanguage.JAPANESE)))
            .thenReturn(emptyDraft);

        assertThatThrownBy(() -> service.generateDraft(1L, SupportedLanguage.JAPANESE))
            .isInstanceOf(ExternalApiException.class)
            .hasMessage("요청한 언어의 번역 응답이 없습니다.");
    }

    @Test
    void savesPlaceAndLockerTranslationsAndMarksReportReadyForApproval() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        when(lockerReportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getId()).thenReturn(10L);
        when(locker.getId()).thenReturn(20L);

        service.saveTranslations(1L, completeForm());

        ArgumentCaptor<AdminPlaceI18nRequest> placeRequest =
            ArgumentCaptor.forClass(AdminPlaceI18nRequest.class);
        ArgumentCaptor<AdminLockerI18nRequest> lockerRequest =
            ArgumentCaptor.forClass(AdminLockerI18nRequest.class);
        verify(i18nAdminService).replacePlace(org.mockito.ArgumentMatchers.eq(10L), placeRequest.capture());
        verify(i18nAdminService).replaceLocker(org.mockito.ArgumentMatchers.eq(20L), lockerRequest.capture());
        assertThat(placeRequest.getValue().translations())
            .hasSize(SupportedLanguage.translationTargets().size());
        assertThat(lockerRequest.getValue().translations())
            .hasSize(SupportedLanguage.translationTargets().size());
        assertThat(report.getStatus())
            .isEqualTo(com.zimdugo.locker.domain.report.LockerReportStatus.READY_FOR_APPROVAL);
    }

    @Test
    void allowsTranslationsToBeSavedAgainWhileReadyForApproval() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        report.markTranslationsReady();
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        when(lockerReportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getId()).thenReturn(10L);
        when(locker.getId()).thenReturn(20L);

        service.saveTranslations(1L, completeForm());

        assertThat(report.getStatus())
            .isEqualTo(com.zimdugo.locker.domain.report.LockerReportStatus.READY_FOR_APPROVAL);
    }

    @Test
    void completesReportWhenPlaceAndLockerTranslationsExistForEveryTranslationTarget() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        report.markTranslationsReady();
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        when(lockerReportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getId()).thenReturn(10L);
        when(locker.getId()).thenReturn(20L);
        when(i18nAdminService.getPlace(10L))
            .thenReturn(new AdminPlaceI18nResponse(10L, placeTranslations(), List.of()));
        when(i18nAdminService.getLocker(20L))
            .thenReturn(new AdminLockerI18nResponse(20L, lockerTranslations(), List.of()));

        LocalDateTime beforeCompletion = LocalDateTime.now();
        service.completeApproval(1L);

        assertThat(report.getStatus())
            .isEqualTo(com.zimdugo.locker.domain.report.LockerReportStatus.APPROVED);
        assertThat(report.getAppliedAt())
            .isNotNull()
            .isBetween(beforeCompletion, LocalDateTime.now());
        verify(lockerReportRepository).findByIdForUpdate(1L);
        verify(lockerReportRepository, never()).findActiveByIdWithImage(1L);
        verify(eventPublisher).publishEvent(new com.zimdugo.admin.report.LockerReportApprovedEvent(20L));
    }

    @Test
    void rejectsCompletionWhenTranslationsAreMissing() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        report.markTranslationsReady();
        PlaceEntity place = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity locker = org.mockito.Mockito.mock(LockerEntity.class);
        when(lockerReportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.findById(20L)).thenReturn(Optional.of(locker));
        when(place.getId()).thenReturn(10L);
        when(locker.getId()).thenReturn(20L);
        when(i18nAdminService.getPlace(10L))
            .thenReturn(new AdminPlaceI18nResponse(10L, List.of(), List.of()));
        when(i18nAdminService.getLocker(20L))
            .thenReturn(new AdminLockerI18nResponse(20L, List.of(), List.of()));

        assertThatThrownBy(() -> service.completeApproval(1L))
            .isInstanceOf(com.zimdugo.core.exception.BusinessException.class);
        assertThat(report.getStatus())
            .isEqualTo(com.zimdugo.locker.domain.report.LockerReportStatus.READY_FOR_APPROVAL);
        assertThat(report.getAppliedAt()).isNull();
        verify(lockerReportRepository).findByIdForUpdate(1L);
        verify(lockerReportRepository, never()).findActiveByIdWithImage(1L);
    }

    @Test
    void rejectsFinalApprovalBeforeTranslationsAreSaved() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        when(lockerReportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.completeApproval(1L))
            .isInstanceOf(com.zimdugo.core.exception.BusinessException.class);

        assertThat(report.getStatus())
            .isEqualTo(com.zimdugo.locker.domain.report.LockerReportStatus.TRANSLATION_REQUIRED);
        verify(i18nAdminService, never()).getPlace(any());
        verify(i18nAdminService, never()).getLocker(any());
    }

    private List<AdminPlaceI18nResponse.Translation> placeTranslations() {
        return SupportedLanguage.translationTargets().stream()
            .map(language -> new AdminPlaceI18nResponse.Translation(language, "장소", "주소"))
            .toList();
    }

    private List<AdminLockerI18nResponse.Translation> lockerTranslations() {
        return SupportedLanguage.translationTargets().stream()
            .map(language -> new AdminLockerI18nResponse.Translation(language, "보관함", "주소", "상세"))
            .toList();
    }

    private AdminTranslationDraftResult japaneseDraft() {
        return new AdminTranslationDraftResult(
            List.of(new AdminTranslationDraftResult.PlaceTranslation(
                SupportedLanguage.JAPANESE,
                "ソウル駅",
                "ソウル特別市中区",
                List.of("ソウル駅")
            )),
            List.of(new AdminTranslationDraftResult.LockerTranslation(
                SupportedLanguage.JAPANESE,
                "ソウル駅ロッカー",
                "ソウル特別市中区",
                "1番出口付近",
                List.of("コインロッカー")
            ))
        );
    }

    private AdminLockerReportTranslationsForm completeForm() {
        AdminLockerReportTranslationsForm form = new AdminLockerReportTranslationsForm();
        form.setPlaceTranslations(SupportedLanguage.translationTargets().stream()
            .map(this::placeForm)
            .toList());
        form.setLockerTranslations(SupportedLanguage.translationTargets().stream()
            .map(this::lockerForm)
            .toList());
        return form;
    }

    private AdminLockerReportTranslationsForm.PlaceTranslationForm placeForm(
        SupportedLanguage language
    ) {
        AdminLockerReportTranslationsForm.PlaceTranslationForm form =
            new AdminLockerReportTranslationsForm.PlaceTranslationForm();
        form.setLanguage(language);
        form.setName("장소");
        form.setRoadAddress("주소");
        return form;
    }

    private AdminLockerReportTranslationsForm.LockerTranslationForm lockerForm(
        SupportedLanguage language
    ) {
        AdminLockerReportTranslationsForm.LockerTranslationForm form =
            new AdminLockerReportTranslationsForm.LockerTranslationForm();
        form.setLanguage(language);
        form.setName("보관함");
        form.setRoadAddress("주소");
        form.setDetailInfo("상세");
        return form;
    }

    private LockerReportEntity report() {
        return LockerReportEntity.builder()
            .id(1L)
            .user(user())
            .name("서울역 보관함")
            .roadAddress("서울 중구 세종대로")
            .indoorOutdoorType(IndoorOutdoorType.INDOOR)
            .lockerType(LockerType.SUBWAY_STATION)
            .lockerSize(Set.of())
            .priceType(LockerReportPriceType.PAID)
            .minPrice(1000)
            .maxPrice(3000)
            .additionalInfo("1번 출구 근처")
            .operatingTimeType(LockerReportOperatingTimeType.OPEN_24_HOURS)
            .locationConsentAgreed(true)
            .latitude(37.55)
            .longitude(126.97)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private UserEntity user() {
        return new UserEntity(
            1L,
            "user@example.com",
            "user",
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
