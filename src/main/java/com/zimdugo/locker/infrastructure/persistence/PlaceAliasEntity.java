package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.common.i18n.SearchTextNormalizer;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.common.i18n.SupportedLanguageConverter;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "place_aliases",
    indexes = {
        @Index(name = "idx_place_aliases_normalized", columnList = "normalized_alias")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_place_aliases_place_normalized",
            columnNames = {"place_id", "normalized_alias"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceAliasEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private PlaceEntity place;

    @Convert(converter = SupportedLanguageConverter.class)
    @Column(name = "language_code", nullable = false, length = 10)
    private SupportedLanguage language;

    @Column(name = "\"alias\"", nullable = false, length = 255)
    private String alias;

    @Column(name = "normalized_alias", nullable = false, length = 255)
    private String normalizedAlias;

    public PlaceAliasEntity(PlaceEntity place, SupportedLanguage language, String alias) {
        this.place = requireValue(place);
        this.language = requireValue(language);
        this.alias = requireAlias(alias);
        this.normalizedAlias = SearchTextNormalizer.normalize(alias);
    }

    private String requireAlias(String value) {
        if (SearchTextNormalizer.normalize(value).isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LOCALIZED_CONTENT);
        }
        return value;
    }

    private <T> T requireValue(T value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_LOCALIZED_CONTENT);
        }
        return value;
    }
}
