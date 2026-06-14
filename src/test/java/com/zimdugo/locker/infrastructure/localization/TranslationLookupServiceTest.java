package com.zimdugo.locker.infrastructure.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.LockerRepository;
import com.zimdugo.locker.infrastructure.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.PlaceRepository;
import com.zimdugo.locker.infrastructure.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TranslationLookupServiceTest {

    @Mock
    private PlaceTranslationRepository placeTranslationRepository;

    @Mock
    private LockerTranslationRepository lockerTranslationRepository;

    @Mock
    private CurrentRequestLanguage currentRequestLanguage;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private LockerRepository lockerRepository;

    private TranslationLookupService service;

    @BeforeEach
    void setUp() {
        service = new TranslationLookupService(
            currentRequestLanguage,
            placeRepository,
            lockerRepository,
            placeTranslationRepository,
            lockerTranslationRepository
        );
    }

    @Test
    void resolvesRequestedLanguagePlaceTitleWhenAvailable() {
        PlaceEntity place = new PlaceEntity("명동역", 37.5609, 126.9863, "서울 중구 퇴계로 126");
        PlaceTranslationEntity japanese = new PlaceTranslationEntity(
            place, SupportedLanguage.JAPANESE, "明洞駅", "ソウル特別市中区退渓路126"
        );
        when(placeTranslationRepository.findByPlaceIdAndLanguage(
            place.getId(),
            SupportedLanguage.JAPANESE
        )).thenReturn(Optional.of(japanese));

        LocalizedPlaceContent content = service.resolvePlace(place, SupportedLanguage.JAPANESE);

        assertThat(content.name()).isEqualTo("明洞駅");
        assertThat(content.roadAddress()).isEqualTo("ソウル特別市中区退渓路126");
        assertThat(content.language()).isEqualTo(SupportedLanguage.JAPANESE);
    }

    @Test
    void resolvesRequestedLanguageLockerTitleWhenAvailable() {
        LockerEntity locker = new LockerEntity(
            "명동역 3번 출구 지하 1층",
            "서울 중구 퇴계로 126",
            37.5609,
            126.9863
        );
        LockerTranslationEntity japanese = new LockerTranslationEntity(
            locker,
            SupportedLanguage.JAPANESE,
            "明洞駅3번출구지하1층",
            "ソウル特別市中区退渓路126",
            "駅務室의隣"
        );
        when(lockerTranslationRepository.findByLockerIdAndLanguage(
            locker.getId(),
            SupportedLanguage.JAPANESE
        )).thenReturn(Optional.of(japanese));

        LocalizedLockerContent content = service.resolveLocker(locker, SupportedLanguage.JAPANESE);

        assertThat(content.name()).isEqualTo("明洞駅3번출구지하1층");
        assertThat(content.roadAddress()).isEqualTo("ソウル特別市中区退渓路126");
        assertThat(content.detailInfo()).isEqualTo("駅務室의隣");
        assertThat(content.language()).isEqualTo(SupportedLanguage.JAPANESE);
    }

    @Test
    void resolvesKoreanFromRequiredTranslationInsteadOfOriginalContent() {
        PlaceEntity place = new PlaceEntity("원문 장소", 37.5609, 126.9863, "원문 주소");
        PlaceTranslationEntity korean = new PlaceTranslationEntity(
            place, SupportedLanguage.KOREAN, "한국어 번역 장소", "한국어 번역 주소"
        );
        when(placeTranslationRepository.findByPlaceIdAndLanguage(
            place.getId(),
            SupportedLanguage.KOREAN
        )).thenReturn(Optional.of(korean));

        LocalizedPlaceContent content = service.resolvePlace(place, SupportedLanguage.KOREAN);

        assertThat(content.name()).isEqualTo("한국어 번역 장소");
        assertThat(content.roadAddress()).isEqualTo("한국어 번역 주소");
    }

    @Test
    void throwsExceptionWhenPlaceTranslationIsMissing() {
        PlaceEntity place = new PlaceEntity("서울역", 37.5547, 126.9707, "서울 용산구 한강대로 405");
        when(placeTranslationRepository.findByPlaceIdAndLanguage(
            place.getId(),
            SupportedLanguage.JAPANESE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolvePlace(place, SupportedLanguage.JAPANESE))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.I18N_TRANSLATION_MISSING)
            );
    }

    @Test
    void throwsExceptionWhenLockerTranslationIsMissing() {
        LockerEntity locker = new LockerEntity("서울역 보관함", "서울 용산구 한강대로 405", 37.5547, 126.9707);
        when(lockerTranslationRepository.findByLockerIdAndLanguage(
            locker.getId(),
            SupportedLanguage.JAPANESE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveLocker(locker, SupportedLanguage.JAPANESE))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.I18N_TRANSLATION_MISSING)
            );
    }

    @Test
    void resolvesPlacesWithSingleBatchTranslationQuery() {
        PlaceEntity firstPlace = place(1L, "명동역");
        PlaceEntity secondPlace = place(2L, "서울역");
        PlaceTranslationEntity firstJapanese = new PlaceTranslationEntity(
            firstPlace, SupportedLanguage.JAPANESE, "明洞駅", "ソウル"
        );
        PlaceTranslationEntity secondJapanese = new PlaceTranslationEntity(
            secondPlace, SupportedLanguage.JAPANESE, "ソウル駅", "ソウル"
        );
        when(currentRequestLanguage.resolve()).thenReturn(SupportedLanguage.JAPANESE);
        when(placeRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(firstPlace, secondPlace));
        when(placeTranslationRepository.findByPlaceIdInAndLanguageIn(
            List.of(1L, 2L),
            List.of(SupportedLanguage.JAPANESE)
        )).thenReturn(List.of(firstJapanese, secondJapanese));

        Map<Long, LocalizedPlaceContent> contents = service.resolvePlaces(List.of(1L, 2L, 1L));

        assertThat(contents).hasSize(2);
        assertThat(contents.get(1L).name()).isEqualTo("明洞駅");
        assertThat(contents.get(2L).name()).isEqualTo("ソウル駅");
        verify(placeTranslationRepository).findByPlaceIdInAndLanguageIn(
            List.of(1L, 2L),
            List.of(SupportedLanguage.JAPANESE)
        );
    }

    @Test
    void resolvesLockersWithSingleBatchTranslationQuery() {
        LockerEntity firstLocker = locker(10L, "명동역 3번 출구 지하 1층");
        LockerEntity secondLocker = locker(11L, "서울역 1번 출구 2층");
        LockerTranslationEntity firstJapanese = new LockerTranslationEntity(
            firstLocker, SupportedLanguage.JAPANESE, "明洞駅3番出口地下1階", "ソウル"
        );
        LockerTranslationEntity secondJapanese = new LockerTranslationEntity(
            secondLocker, SupportedLanguage.JAPANESE, "ソウル駅1番出口2階", "ソウル"
        );
        when(currentRequestLanguage.resolve()).thenReturn(SupportedLanguage.JAPANESE);
        when(lockerRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(firstLocker, secondLocker));
        when(lockerTranslationRepository.findByLockerIdInAndLanguageIn(
            List.of(10L, 11L),
            List.of(SupportedLanguage.JAPANESE)
        )).thenReturn(List.of(firstJapanese, secondJapanese));

        Map<Long, LocalizedLockerContent> contents = service.resolveLockers(List.of(10L, 11L, 10L));

        assertThat(contents).hasSize(2);
        assertThat(contents.get(10L).name()).isEqualTo("明洞駅3番出口地下1階");
        assertThat(contents.get(11L).name()).isEqualTo("ソウル駅1番出口2階");
        verify(lockerTranslationRepository).findByLockerIdInAndLanguageIn(
            List.of(10L, 11L),
            List.of(SupportedLanguage.JAPANESE)
        );
    }

    private PlaceEntity place(Long id, String name) {
        PlaceEntity place = new PlaceEntity(name, 37.55, 126.98, "서울");
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private LockerEntity locker(Long id, String name) {
        LockerEntity locker = new LockerEntity(name, "서울", 37.55, 126.98);
        ReflectionTestUtils.setField(locker, "id", id);
        return locker;
    }
}
