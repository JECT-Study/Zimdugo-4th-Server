package com.zimdugo.admin.translation;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

final class GeminiTranslationErrorMessage {

    private static final String SERVICE_UNAVAILABLE_MESSAGE =
        "번역 모델에 요청이 몰려 일시적으로 이용할 수 없습니다. 잠시 후 다시 시도해 주세요.";
    private static final String RATE_LIMIT_MESSAGE =
        "번역 API 사용 한도를 초과했습니다. 사용량과 요금제를 확인한 후 다시 시도해 주세요.";
    private static final String DEFAULT_MESSAGE =
        "번역 초안 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.";

    private GeminiTranslationErrorMessage() {
    }

    static String from(HttpStatusCode statusCode) {
        if (statusCode.value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return SERVICE_UNAVAILABLE_MESSAGE;
        }
        if (statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return RATE_LIMIT_MESSAGE;
        }
        return DEFAULT_MESSAGE;
    }
}
