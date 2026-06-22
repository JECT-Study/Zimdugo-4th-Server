package com.zimdugo.admin.translation;

import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;

public interface LockerReportTranslationDraftGenerator {

    AdminTranslationDraftResult generate(LockerReportTranslationSource source);

    AdminTranslationDraftResult generate(
        LockerReportTranslationSource source,
        SupportedLanguage language
    );
}
