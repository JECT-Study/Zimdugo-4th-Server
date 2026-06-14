package com.zimdugo.admin.ui.dto;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.List;

public record AdminPlaceI18nResponse(
    Long placeId,
    List<Translation> translations,
    List<Alias> aliases
) {
    public record Translation(SupportedLanguage language, String name, String roadAddress) {
        static Translation from(PlaceTranslationEntity entity) {
            return new Translation(entity.getLanguage(), entity.getName(), entity.getRoadAddress());
        }
    }

    public record Alias(SupportedLanguage language, String alias) {
        static Alias from(PlaceAliasEntity entity) {
            return new Alias(entity.getLanguage(), entity.getAlias());
        }
    }

    public static AdminPlaceI18nResponse of(
        Long placeId,
        List<PlaceTranslationEntity> translations,
        List<PlaceAliasEntity> aliases
    ) {
        return new AdminPlaceI18nResponse(
            placeId,
            translations.stream().map(Translation::from).toList(),
            aliases.stream().map(Alias::from).toList()
        );
    }
}
