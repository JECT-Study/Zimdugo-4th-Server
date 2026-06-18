package com.zimdugo.admin.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.i18n.dto.AdminPlaceI18nRequest;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class LockerContentI18nAdminServiceTest {

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private LockerRepository lockerRepository;
    @Mock
    private PlaceTranslationRepository placeTranslationRepository;
    @Mock
    private LockerTranslationRepository lockerTranslationRepository;
    @Mock
    private PlaceAliasRepository placeAliasRepository;
    @Mock
    private LockerAliasRepository lockerAliasRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LockerContentI18nAdminService service;

    @BeforeEach
    void setUp() {
        service = new LockerContentI18nAdminService(
            placeRepository,
            lockerRepository,
            placeTranslationRepository,
            lockerTranslationRepository,
            placeAliasRepository,
            lockerAliasRepository,
            eventPublisher
        );
    }

    @Test
    void replacesPlaceWhenEverySupportedLanguageIsProvided() {
        PlaceEntity place = new PlaceEntity("서울역", 37.55, 126.97, "서울");
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(placeTranslationRepository.findByPlaceId(1L)).thenReturn(List.of());
        when(placeAliasRepository.findByPlaceId(1L)).thenReturn(List.of());

        service.replacePlace(1L, request(SupportedLanguage.all()));

        verify(placeTranslationRepository).saveAll(anyList());
        verify(eventPublisher).publishEvent(LockerContentI18nChangedEvent.place(1L));
    }

    @Test
    void rejectsPlaceWhenAnySupportedLanguageIsMissingBeforeDeletingExistingContent() {
        PlaceEntity place = new PlaceEntity("서울역", 37.55, 126.97, "서울");
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        List<SupportedLanguage> missingTraditionalChinese = SupportedLanguage.all().stream()
            .filter(language -> language != SupportedLanguage.TRADITIONAL_CHINESE)
            .toList();

        assertThatThrownBy(() -> service.replacePlace(1L, request(missingTraditionalChinese)))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_I18N_CONTENT)
            );

        verify(placeTranslationRepository, never()).deleteByPlaceId(1L);
        verify(placeAliasRepository, never()).deleteByPlaceId(1L);
        verify(eventPublisher, never()).publishEvent(any());
    }

    private AdminPlaceI18nRequest request(List<SupportedLanguage> languages) {
        return new AdminPlaceI18nRequest(
            languages.stream()
                .map(language -> new AdminPlaceI18nRequest.Translation(
                    language,
                    language.languageTag() + " name",
                    language.languageTag() + " address"
                ))
                .toList(),
            List.of()
        );
    }
}
