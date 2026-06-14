package com.zimdugo.admin.ui.dto;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import java.util.List;

public record AdminLockerI18nResponse(
    Long lockerId,
    List<Translation> translations,
    List<Alias> aliases
) {
    public record Translation(
        SupportedLanguage language,
        String name,
        String roadAddress,
        String detailInfo
    ) {
        static Translation from(LockerTranslationEntity entity) {
            return new Translation(
                entity.getLanguage(),
                entity.getName(),
                entity.getRoadAddress(),
                entity.getDetailInfo()
            );
        }
    }

    public record Alias(SupportedLanguage language, String alias) {
        static Alias from(LockerAliasEntity entity) {
            return new Alias(entity.getLanguage(), entity.getAlias());
        }
    }

    public static AdminLockerI18nResponse of(
        Long lockerId,
        List<LockerTranslationEntity> translations,
        List<LockerAliasEntity> aliases
    ) {
        return new AdminLockerI18nResponse(
            lockerId,
            translations.stream().map(Translation::from).toList(),
            aliases.stream().map(Alias::from).toList()
        );
    }
}
