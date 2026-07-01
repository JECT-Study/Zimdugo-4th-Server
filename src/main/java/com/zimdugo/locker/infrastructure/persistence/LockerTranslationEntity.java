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
    name = "locker_translations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_locker_translations_locker_language",
            columnNames = {"locker_id", "language_code"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locker_id", nullable = false)
    private LockerEntity locker;

    @Convert(converter = SupportedLanguageConverter.class)
    @Column(name = "language_code", nullable = false, length = 10)
    private SupportedLanguage language;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String roadAddress;

    @Column(length = 2000)
    private String detailInfo;

    public LockerTranslationEntity(
        LockerEntity locker,
        SupportedLanguage language,
        String name,
        String roadAddress
    ) {
        this(locker, language, name, roadAddress, null);
    }

    public LockerTranslationEntity(
        LockerEntity locker,
        SupportedLanguage language,
        String name,
        String roadAddress,
        String detailInfo
    ) {
        this.locker = requireValue(locker);
        this.language = requireValue(language);
        this.name = requireText(name, "name");
        this.roadAddress = requireText(roadAddress, "roadAddress");
        this.detailInfo = detailInfo;
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
