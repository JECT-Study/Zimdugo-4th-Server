package com.zimdugo.locker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class LocalizationEntityTest {

    @Test
    void normalizesAliasesWhenEntitiesAreCreated() {
        PlaceEntity place = place();
        LockerEntity locker = locker(place);

        PlaceAliasEntity placeAlias = new PlaceAliasEntity(
            place,
            SupportedLanguage.ENGLISH,
            "Ｓｅｏｕｌ Station"
        );
        LockerAliasEntity lockerAlias = new LockerAliasEntity(
            locker,
            SupportedLanguage.KOREAN,
            "서울 역 보관함"
        );

        assertThat(placeAlias.getNormalizedAlias()).isEqualTo("seoulstation");
        assertThat(lockerAlias.getNormalizedAlias()).isEqualTo("서울역보관함");
    }

    @Test
    void storesTranslatedLockerDetailContent() {
        LockerTranslationEntity translation = new LockerTranslationEntity(
            locker(place()),
            SupportedLanguage.ENGLISH,
            "Seoul Station Locker",
            "405 Hangang-daero",
            "Next to Exit 1"
        );

        assertThat(translation.getLanguage()).isEqualTo(SupportedLanguage.ENGLISH);
        assertThat(translation.getDetailInfo()).isEqualTo("Next to Exit 1");
    }

    @Test
    void rejectsBlankRequiredContent() {
        assertThatThrownBy(() -> new PlaceTranslationEntity(
            place(),
            SupportedLanguage.ENGLISH,
            " ",
            "405 Hangang-daero"
        ))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOCALIZED_CONTENT)
            );
        assertThatThrownBy(() -> new LockerAliasEntity(locker(place()), SupportedLanguage.ENGLISH, "\t"))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOCALIZED_CONTENT)
            );
    }

    @Test
    void rejectsNullRequiredContentRelationships() {
        assertThatThrownBy(() -> new PlaceTranslationEntity(
            null,
            SupportedLanguage.ENGLISH,
            "Seoul Station",
            "405 Hangang-daero"
        ))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOCALIZED_CONTENT)
            );
        assertThatThrownBy(() -> new LockerAliasEntity(locker(place()), null, "Seoul Station Locker"))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOCALIZED_CONTENT)
            );
    }

    private PlaceEntity place() {
        return new PlaceEntity("서울역", 37.5547, 126.9707, "서울 용산구 한강대로 405");
    }

    private LockerEntity locker(PlaceEntity place) {
        return new LockerEntity(
            "서울역 물품보관함",
            "서울 용산구 한강대로 405",
            37.5547,
            126.9707,
            place
        );
    }
}
