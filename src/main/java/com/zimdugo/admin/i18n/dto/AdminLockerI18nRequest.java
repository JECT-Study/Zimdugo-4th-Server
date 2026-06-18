package com.zimdugo.admin.i18n.dto;

import com.zimdugo.common.i18n.SupportedLanguage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminLockerI18nRequest(
    @NotEmpty List<@Valid Translation> translations,
    List<@Valid Alias> aliases
) {
    public record Translation(
        @NotNull SupportedLanguage language,
        @NotBlank String name,
        @NotBlank String roadAddress,
        String detailInfo
    ) {
    }

    public record Alias(
        @NotNull SupportedLanguage language,
        @NotBlank String alias
    ) {
    }
}
