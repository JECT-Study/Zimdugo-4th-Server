package com.zimdugo.locker.infrastructure.persistence;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "place_translations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_place_translations_place_language",
            columnNames = {"place_id", "language_code"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private PlaceEntity place;

    @Convert(converter = SupportedLanguageConverter.class)
    @Column(name = "language_code", nullable = false, length = 10)
    private SupportedLanguage language;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String roadAddress;

    public PlaceTranslationEntity(
        PlaceEntity place,
        SupportedLanguage language,
        String name,
        String roadAddress
    ) {
        this.place = requireValue(place);
        this.language = requireValue(language);
        this.name = requireText(name, "name");
        this.roadAddress = roadAddress;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
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
