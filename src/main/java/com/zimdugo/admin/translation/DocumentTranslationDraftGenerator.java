package com.zimdugo.admin.translation;

import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;

public interface DocumentTranslationDraftGenerator {

    AdminDocumentTranslationDraftResult generate(AdminDocumentTranslationSource source);

    AdminDocumentTranslationDraftResult generate(
        AdminDocumentTranslationSource source,
        SupportedLanguage language
    );
}
