package com.zimdugo.admin.translation;

import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;

public interface LockerReportTranslationDraftGenerator {

    AdminTranslationDraftResult generate(LockerReportTranslationSource source);
}
