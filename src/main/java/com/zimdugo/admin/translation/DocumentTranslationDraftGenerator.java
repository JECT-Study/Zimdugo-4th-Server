package com.zimdugo.admin.translation;

import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;

public interface DocumentTranslationDraftGenerator {

    AdminDocumentTranslationDraftResult generate(AdminDocumentTranslationSource source);
}
