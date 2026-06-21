package com.zimdugo.image.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum UploadCategory {
    PROFILE,
    LOCKER_REPORT;

    @JsonCreator
    public static UploadCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalizedValue = value.trim()
            .replace("-", "_")
            .replace(" ", "_")
            .toUpperCase(Locale.ROOT);
        for (UploadCategory category : values()) {
            if (category.name().equals(normalizedValue)
                || category.name().replace("_", "").equals(normalizedValue.replace("_", ""))) {
                return category;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 업로드 카테고리입니다: " + value);
    }

    @JsonValue
    public String value() {
        return name();
    }
}
