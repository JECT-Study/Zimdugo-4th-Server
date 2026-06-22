package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.admin.i18n.dto.AdminLockerI18nRequest;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nRequest;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminLockerReportTranslationsForm {

    @Valid
    @NotEmpty
    private List<PlaceTranslationForm> placeTranslations = new ArrayList<>();

    @Valid
    @NotEmpty
    private List<LockerTranslationForm> lockerTranslations = new ArrayList<>();

    public static AdminLockerReportTranslationsForm from(
        AdminLockerReportTranslationPageResult page
    ) {
        AdminLockerReportTranslationsForm form = new AdminLockerReportTranslationsForm();
        form.setPlaceTranslations(page.languages().stream()
            .map(PlaceTranslationForm::from)
            .toList());
        form.setLockerTranslations(page.languages().stream()
            .map(LockerTranslationForm::from)
            .toList());
        return form;
    }

    public AdminPlaceI18nRequest toPlaceRequest() {
        return new AdminPlaceI18nRequest(
            placeTranslations.stream()
                .map(PlaceTranslationForm::toTranslation)
                .toList(),
            placeTranslations.stream()
                .flatMap(form -> form.toAliases().stream())
                .toList()
        );
    }

    public AdminLockerI18nRequest toLockerRequest() {
        return new AdminLockerI18nRequest(
            lockerTranslations.stream()
                .map(LockerTranslationForm::toTranslation)
                .toList(),
            lockerTranslations.stream()
                .flatMap(form -> form.toAliases().stream())
                .toList()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PlaceTranslationForm {

        @NotNull
        private SupportedLanguage language;
        @NotBlank
        private String name;
        private String roadAddress;
        private String aliases;

        private AdminPlaceI18nRequest.Translation toTranslation() {
            return new AdminPlaceI18nRequest.Translation(language, name, roadAddress);
        }

        private List<AdminPlaceI18nRequest.Alias> toAliases() {
            return aliasLines(aliases).stream()
                .map(alias -> new AdminPlaceI18nRequest.Alias(language, alias))
                .toList();
        }

        private static PlaceTranslationForm from(
            AdminLockerReportTranslationPageResult.LanguageReview review
        ) {
            PlaceTranslationForm form = new PlaceTranslationForm();
            form.setLanguage(review.language());
            form.setName(review.place().name());
            form.setRoadAddress(review.place().roadAddress());
            form.setAliases(review.place().aliases());
            return form;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LockerTranslationForm {

        @NotNull
        private SupportedLanguage language;
        @NotBlank
        private String name;
        @NotBlank
        private String roadAddress;
        private String detailInfo;
        private String aliases;

        private AdminLockerI18nRequest.Translation toTranslation() {
            return new AdminLockerI18nRequest.Translation(
                language,
                name,
                roadAddress,
                detailInfo
            );
        }

        private List<AdminLockerI18nRequest.Alias> toAliases() {
            return aliasLines(aliases).stream()
                .map(alias -> new AdminLockerI18nRequest.Alias(language, alias))
                .toList();
        }

        private static LockerTranslationForm from(
            AdminLockerReportTranslationPageResult.LanguageReview review
        ) {
            LockerTranslationForm form = new LockerTranslationForm();
            form.setLanguage(review.language());
            form.setName(review.locker().name());
            form.setRoadAddress(review.locker().roadAddress());
            form.setDetailInfo(review.locker().detailInfo());
            form.setAliases(review.locker().aliases());
            return form;
        }
    }

    private static List<String> aliasLines(String aliases) {
        if (aliases == null || aliases.isBlank()) {
            return List.of();
        }
        return aliases.lines()
            .map(String::trim)
            .filter(alias -> !alias.isBlank())
            .distinct()
            .toList();
    }
}
