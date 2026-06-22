package com.zimdugo.admin.entrypoint.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.admin.i18n.dto.AdminLockerI18nRequest;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nRequest;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminLockerReportTranslationsFormTest {

    @Test
    void convertsPlaceAndLockerFieldsIncludingLineSeparatedAliases() {
        AdminLockerReportTranslationsForm form = new AdminLockerReportTranslationsForm();
        form.setPlaceTranslations(List.of(placeTranslation()));
        form.setLockerTranslations(List.of(lockerTranslation()));

        AdminPlaceI18nRequest place = form.toPlaceRequest();
        AdminLockerI18nRequest locker = form.toLockerRequest();

        assertThat(place.translations()).containsExactly(
            new AdminPlaceI18nRequest.Translation(
                SupportedLanguage.ENGLISH,
                "Seoul Station",
                "405 Hangang-daero"
            )
        );
        assertThat(place.aliases()).containsExactly(
            new AdminPlaceI18nRequest.Alias(SupportedLanguage.ENGLISH, "Seoul Station"),
            new AdminPlaceI18nRequest.Alias(SupportedLanguage.ENGLISH, "Seoul Stn")
        );
        assertThat(locker.translations()).containsExactly(
            new AdminLockerI18nRequest.Translation(
                SupportedLanguage.ENGLISH,
                "Seoul Station Locker",
                "405 Hangang-daero",
                "Near exit 1"
            )
        );
        assertThat(locker.aliases()).containsExactly(
            new AdminLockerI18nRequest.Alias(SupportedLanguage.ENGLISH, "Station Locker")
        );
    }

    @Test
    void createsAnEditableFormFromSavedPageValues() {
        AdminLockerReportTranslationPageResult page = new AdminLockerReportTranslationPageResult(
            null,
            "서울역",
            "서울역 보관함",
            new AdminPlaceI18nResponse(
                10L,
                List.of(new AdminPlaceI18nResponse.Translation(
                    SupportedLanguage.ENGLISH,
                    "Seoul Station",
                    "Address"
                )),
                List.of()
            ),
            new AdminLockerI18nResponse(
                20L,
                List.of(new AdminLockerI18nResponse.Translation(
                    SupportedLanguage.ENGLISH,
                    "Locker",
                    "Address",
                    "Detail"
                )),
                List.of()
            ),
            false,
            false
        );

        AdminLockerReportTranslationsForm form = AdminLockerReportTranslationsForm.from(page);

        assertThat(form.getPlaceTranslations()).hasSize(SupportedLanguage.all().size());
        assertThat(form.getLockerTranslations()).hasSize(SupportedLanguage.all().size());
        assertThat(form.getPlaceTranslations().get(1).getName()).isEqualTo("Seoul Station");
        assertThat(form.getLockerTranslations().get(1).getDetailInfo()).isEqualTo("Detail");
    }

    private AdminLockerReportTranslationsForm.PlaceTranslationForm placeTranslation() {
        AdminLockerReportTranslationsForm.PlaceTranslationForm translation =
            new AdminLockerReportTranslationsForm.PlaceTranslationForm();
        translation.setLanguage(SupportedLanguage.ENGLISH);
        translation.setName("Seoul Station");
        translation.setRoadAddress("405 Hangang-daero");
        translation.setAliases(" Seoul Station\n\nSeoul Stn ");
        return translation;
    }

    private AdminLockerReportTranslationsForm.LockerTranslationForm lockerTranslation() {
        AdminLockerReportTranslationsForm.LockerTranslationForm translation =
            new AdminLockerReportTranslationsForm.LockerTranslationForm();
        translation.setLanguage(SupportedLanguage.ENGLISH);
        translation.setName("Seoul Station Locker");
        translation.setRoadAddress("405 Hangang-daero");
        translation.setDetailInfo("Near exit 1");
        translation.setAliases("Station Locker");
        return translation;
    }
}
