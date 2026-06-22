package com.zimdugo.admin.translation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminLockerReportTranslationPageResultTest {

    @Test
    void formatsReportSourceLikeReviewPage() {
        LockerReportEntity report = LockerReportEntity.builder()
            .groundLevelType(GroundLevelType.UNDERGROUND)
            .floor(2)
            .priceType(LockerReportPriceType.PAID)
            .minPrice(1000)
            .maxPrice(3000)
            .operatingTimeType(LockerReportOperatingTimeType.TIME_RANGE)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(22, 0))
            .build();

        AdminLockerReportTranslationPageResult.Report source =
            AdminLockerReportTranslationPageResult.Report.from(report);

        assertThat(source.floorLabel()).isEqualTo("지하 2층");
        assertThat(source.priceLabel()).isEqualTo("1000 ~ 3000원");
        assertThat(source.operatingTimeLabel()).isEqualTo("09:00 ~ 22:00");
    }

    @Test
    void formatsUnknownReportSourceLikeReviewPage() {
        LockerReportEntity report = LockerReportEntity.builder()
            .priceType(LockerReportPriceType.UNKNOWN)
            .operatingTimeType(LockerReportOperatingTimeType.UNKNOWN)
            .build();

        AdminLockerReportTranslationPageResult.Report source =
            AdminLockerReportTranslationPageResult.Report.from(report);

        assertThat(source.floorLabel()).isEqualTo("층 없음");
        assertThat(source.priceLabel()).isEqualTo("가격 정보 없음");
        assertThat(source.operatingTimeLabel()).isEqualTo("운영 시간 정보 없음");
    }

    @Test
    void buildsEditableRowsForEverySupportedLanguage() {
        AdminPlaceI18nResponse place = new AdminPlaceI18nResponse(
            10L,
            List.of(new AdminPlaceI18nResponse.Translation(
                SupportedLanguage.ENGLISH,
                "Seoul Station",
                "405 Hangang-daero"
            )),
            List.of(new AdminPlaceI18nResponse.Alias(SupportedLanguage.ENGLISH, "Seoul Stn"))
        );
        AdminLockerI18nResponse locker = new AdminLockerI18nResponse(
            20L,
            List.of(new AdminLockerI18nResponse.Translation(
                SupportedLanguage.ENGLISH,
                "Locker",
                "405 Hangang-daero",
                "Near exit 1"
            )),
            List.of(new AdminLockerI18nResponse.Alias(SupportedLanguage.ENGLISH, "Station Locker"))
        );
        AdminLockerReportTranslationPageResult page =
            new AdminLockerReportTranslationPageResult(null, "서울역", "보관함", place, locker, false, false);

        assertThat(page.languages()).hasSize(SupportedLanguage.all().size());
        AdminLockerReportTranslationPageResult.LanguageReview english = page.languages().stream()
            .filter(item -> item.language() == SupportedLanguage.ENGLISH)
            .findFirst()
            .orElseThrow();
        assertThat(english.place().name()).isEqualTo("Seoul Station");
        assertThat(english.place().aliases()).isEqualTo("Seoul Stn");
        assertThat(english.locker().detailInfo()).isEqualTo("Near exit 1");
        assertThat(english.locker().aliases()).isEqualTo("Station Locker");
        assertThat(page.languages().stream()
            .filter(item -> item.language() == SupportedLanguage.JAPANESE)
            .findFirst()
            .orElseThrow().place().name()).isEmpty();
    }

    @Test
    void findsDraftsByLanguageTag() {
        AdminTranslationDraftResult.PlaceTranslation place =
            new AdminTranslationDraftResult.PlaceTranslation(
                SupportedLanguage.ENGLISH, "Place", "Address", List.of()
            );
        AdminTranslationDraftResult.LockerTranslation locker =
            new AdminTranslationDraftResult.LockerTranslation(
                SupportedLanguage.ENGLISH, "Locker", "Address", "Detail", List.of()
            );
        AdminTranslationDraftResult draft = new AdminTranslationDraftResult(
            List.of(place),
            List.of(locker)
        );

        assertThat(draft.placeTranslationFor("en")).isSameAs(place);
        assertThat(draft.lockerTranslationFor("en")).isSameAs(locker);
        assertThat(draft.placeTranslationFor("ja")).isNull();
    }
}
