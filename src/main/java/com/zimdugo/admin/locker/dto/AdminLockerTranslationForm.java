package com.zimdugo.admin.locker.dto;

import com.zimdugo.admin.i18n.dto.AdminLockerI18nRequest;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.common.i18n.SupportedLanguage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdminLockerTranslationForm {

    @Valid
    private List<TranslationForm> translations = new ArrayList<>();

    public static AdminLockerTranslationForm from(AdminLockerI18nResponse response) {
        Map<SupportedLanguage, AdminLockerI18nResponse.Translation> translationsByLanguage =
            response.translations().stream()
                .collect(Collectors.toMap(AdminLockerI18nResponse.Translation::language, Function.identity()));
        Map<SupportedLanguage, String> aliasesByLanguage = response.aliases().stream()
            .collect(Collectors.groupingBy(
                AdminLockerI18nResponse.Alias::language,
                Collectors.mapping(AdminLockerI18nResponse.Alias::alias, Collectors.joining("\n"))
            ));

        AdminLockerTranslationForm form = new AdminLockerTranslationForm();
        form.translations = SupportedLanguage.translationTargets().stream()
            .map(language -> TranslationForm.from(
                language,
                translationsByLanguage.get(language),
                aliasesByLanguage.get(language)
            ))
            .toList();
        return form;
    }

    public AdminLockerI18nRequest toRequest() {
        return new AdminLockerI18nRequest(
            translations.stream()
                .map(TranslationForm::toTranslationRequest)
                .toList(),
            translations.stream()
                .flatMap(TranslationForm::toAliasRequests)
                .toList()
        );
    }

    public List<TranslationForm> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslationForm> translations) {
        this.translations = translations;
    }

    public static class TranslationForm {

        @NotNull
        private SupportedLanguage language;

        @NotBlank
        private String name;

        @NotBlank
        private String roadAddress;

        private String detailInfo;

        private String aliases;

        static TranslationForm from(
            SupportedLanguage language,
            AdminLockerI18nResponse.Translation translation,
            String aliases
        ) {
            TranslationForm form = new TranslationForm();
            form.language = language;
            form.name = translation == null ? "" : translation.name();
            form.roadAddress = translation == null ? "" : translation.roadAddress();
            form.detailInfo = translation == null ? "" : translation.detailInfo();
            form.aliases = aliases == null ? "" : aliases;
            return form;
        }

        AdminLockerI18nRequest.Translation toTranslationRequest() {
            return new AdminLockerI18nRequest.Translation(
                language,
                trim(name),
                trim(roadAddress),
                trimToNull(detailInfo)
            );
        }

        java.util.stream.Stream<AdminLockerI18nRequest.Alias> toAliasRequests() {
            if (aliases == null || aliases.isBlank()) {
                return java.util.stream.Stream.empty();
            }
            return Arrays.stream(aliases.split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(alias -> new AdminLockerI18nRequest.Alias(language, alias));
        }

        private String trim(String value) {
            return value == null ? null : value.trim();
        }

        private String trimToNull(String value) {
            String trimmed = trim(value);
            return trimmed == null || trimmed.isBlank() ? null : trimmed;
        }

        public SupportedLanguage getLanguage() {
            return language;
        }

        public void setLanguage(SupportedLanguage language) {
            this.language = language;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoadAddress() {
            return roadAddress;
        }

        public void setRoadAddress(String roadAddress) {
            this.roadAddress = roadAddress;
        }

        public String getDetailInfo() {
            return detailInfo;
        }

        public void setDetailInfo(String detailInfo) {
            this.detailInfo = detailInfo;
        }

        public String getAliases() {
            return aliases;
        }

        public void setAliases(String aliases) {
            this.aliases = aliases;
        }
    }
}
