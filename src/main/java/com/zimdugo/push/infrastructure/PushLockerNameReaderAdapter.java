package com.zimdugo.push.infrastructure;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.push.domain.PushLockerNameReader;
import com.zimdugo.push.domain.PushLocale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushLockerNameReaderAdapter implements PushLockerNameReader {

    private final LockerRepository lockerRepository;
    private final LockerTranslationRepository lockerTranslationRepository;

    @Override
    public String findName(Long lockerId, PushLocale locale) {
        return lockerTranslationRepository.findByLockerIdAndLanguage(lockerId, languageOf(locale))
            .map(translation -> translation.getName())
            .orElseGet(() -> lockerRepository.findById(lockerId)
                .map(locker -> locker.getName())
                .orElse("보관함"));
    }

    private SupportedLanguage languageOf(PushLocale locale) {
        return switch (locale) {
            case KO -> SupportedLanguage.KOREAN;
            case EN -> SupportedLanguage.ENGLISH;
            case JA -> SupportedLanguage.JAPANESE;
            case ZH_HANS -> SupportedLanguage.SIMPLIFIED_CHINESE;
            case ZH_HANT -> SupportedLanguage.TRADITIONAL_CHINESE;
        };
    }
}
