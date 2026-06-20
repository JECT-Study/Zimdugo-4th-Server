package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
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

@ExtendWith(MockitoExtension.class)
class AdminLockerReportTranslationServiceTest {

    @Mock
    private LockerReportRepository lockerReportRepository;
    @Mock
    private LockerContentI18nAdminService i18nAdminService;
    @Mock
    private LockerReportTranslationDraftGenerator draftGenerator;

    private AdminLockerReportTranslationService service;

    @BeforeEach
    void setUp() {
        service = new AdminLockerReportTranslationService(
            lockerReportRepository,
            i18nAdminService,
            draftGenerator
        );
    }

    @Test
    void getsTranslationPageWithoutI18nLookupsWhenReportIsNotApplied() {
        LockerReportEntity report = report();
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));

        AdminLockerReportTranslationPageResult result = service.getTranslationPage(1L);

        assertThat(result.report().id()).isEqualTo(1L);
        assertThat(result.placeI18n()).isNull();
        assertThat(result.lockerI18n()).isNull();
        verify(i18nAdminService, never()).getPlace(any());
        verify(i18nAdminService, never()).getLocker(any());
    }

    @Test
    void getsTranslationPageWithExistingI18nWhenReportIsApplied() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin", "ok");
        AdminPlaceI18nResponse placeI18n = new AdminPlaceI18nResponse(10L, List.of(), List.of());
        AdminLockerI18nResponse lockerI18n = new AdminLockerI18nResponse(20L, List.of(), List.of());
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(i18nAdminService.getPlace(10L)).thenReturn(placeI18n);
        when(i18nAdminService.getLocker(20L)).thenReturn(lockerI18n);

        AdminLockerReportTranslationPageResult result = service.getTranslationPage(1L);

        assertThat(result.placeI18n()).isSameAs(placeI18n);
        assertThat(result.lockerI18n()).isSameAs(lockerI18n);
    }

    @Test
    void generatesDraftFromReportSource() {
        LockerReportEntity report = report();
        AdminTranslationDraftResult draft = new AdminTranslationDraftResult(List.of(
            new AdminTranslationDraftResult.Translation(
                SupportedLanguage.ENGLISH,
                "Locker",
                "Seoul Station",
                "Near exit 1",
                List.of("locker")
            )
        ));
        when(lockerReportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(draftGenerator.generate(any())).thenReturn(draft);

        AdminTranslationDraftResult result = service.generateDraft(1L);

        ArgumentCaptor<LockerReportTranslationSource> captor =
            ArgumentCaptor.forClass(LockerReportTranslationSource.class);
        verify(draftGenerator).generate(captor.capture());
        assertThat(result).isSameAs(draft);
        assertThat(captor.getValue().reportId()).isEqualTo(1L);
        assertThat(captor.getValue().roadAddress()).isEqualTo("서울 중구 세종대로");
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
