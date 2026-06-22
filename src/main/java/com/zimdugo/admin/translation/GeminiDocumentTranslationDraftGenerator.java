package com.zimdugo.admin.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.translation.GeminiLockerReportTranslationDraftGenerator.Candidate;
import com.zimdugo.admin.translation.GeminiLockerReportTranslationDraftGenerator.Content;
import com.zimdugo.admin.translation.GeminiLockerReportTranslationDraftGenerator.GenerationConfig;
import com.zimdugo.admin.translation.GeminiLockerReportTranslationDraftGenerator.GeminiGenerateContentRequest;
import com.zimdugo.admin.translation.GeminiLockerReportTranslationDraftGenerator.GeminiGenerateContentResponse;
import com.zimdugo.admin.translation.GeminiLockerReportTranslationDraftGenerator.Part;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.ExternalApiException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiDocumentTranslationDraftGenerator implements DocumentTranslationDraftGenerator {

    private static final String JSON_MIME_TYPE = "application/json";

    private final RestClient geminiRestClient;
    private final GeminiTranslationProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public AdminDocumentTranslationDraftResult generate(AdminDocumentTranslationSource source) {
        return generate(source, SupportedLanguage.translationTargets());
    }

    @Override
    public AdminDocumentTranslationDraftResult generate(
        AdminDocumentTranslationSource source,
        SupportedLanguage language
    ) {
        return generate(source, List.of(language));
    }

    private AdminDocumentTranslationDraftResult generate(
        AdminDocumentTranslationSource source,
        List<SupportedLanguage> languages
    ) {
        if (!properties.hasApiKey()) {
            throw new ExternalApiException("번역 API 키가 설정되지 않았습니다.");
        }

        GeminiGenerateContentRequest request = createRequest(source, languages);
        GeminiGenerateContentResponse response = callGemini(request);
        return parseDraft(response);
    }

    private GeminiGenerateContentRequest createRequest(
        AdminDocumentTranslationSource source,
        List<SupportedLanguage> languages
    ) {
        try {
            return request(source, languages);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException("문서 번역 요청 생성에 실패했습니다.", e);
        }
    }

    private GeminiGenerateContentResponse callGemini(GeminiGenerateContentRequest request) {
        try {
            return geminiRestClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1beta/models/{model}:generateContent")
                    .queryParam("key", properties.apiKey())
                    .build(properties.model()))
                .body(request)
                .retrieve()
                .body(GeminiGenerateContentResponse.class);
        } catch (RestClientResponseException e) {
            String message = geminiErrorMessage(e.getResponseBodyAsString());
            log.warn(
                "문서 번역 초안 생성 실패. status={}, message={}",
                e.getStatusCode(),
                message
            );
            throw new ExternalApiException(
                GeminiTranslationErrorMessage.from(e.getStatusCode()),
                e
            );
        } catch (RestClientException e) {
            String message = "문서 번역 초안 생성 실패: " + e.getClass().getSimpleName();
            throw new ExternalApiException(message, e);
        }
    }

    GeminiGenerateContentRequest request(
        AdminDocumentTranslationSource source,
        List<SupportedLanguage> languages
    )
        throws JsonProcessingException {
        String prompt = """
            You are translating admin documents for a production travel app.
            Translate each title, subtitle, and section content for every requested language.

            Rules:
            - Preserve product names, legal terms, URLs, variables, and numbers exactly when appropriate.
            - Keep legal/policy language precise and formal.
            - Keep notice language clear and concise.
            - Return every source section with the same sectionId.
            - If a source subtitle is empty, return an empty subtitle.

            Requested language tags: %s
            Source document JSON:
            %s
            """.formatted(languageTags(languages), objectMapper.writeValueAsString(source));

        return new GeminiGenerateContentRequest(
            List.of(new Content(List.of(new Part(prompt)))),
            new GenerationConfig(JSON_MIME_TYPE, responseSchema())
        );
    }

    private String languageTags(List<SupportedLanguage> languages) {
        return languages.stream()
            .map(SupportedLanguage::languageTag)
            .collect(Collectors.joining(", "));
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "translations", Map.of(
                    "type", "ARRAY",
                    "items", translationSchema()
                )
            ),
            "required", List.of("translations")
        );
    }

    private Map<String, Object> sectionSchema() {
        return Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "sectionId", Map.of("type", "INTEGER"),
                "subtitle", Map.of("type", "STRING"),
                "content", Map.of("type", "STRING")
            ),
            "required", List.of("sectionId", "subtitle", "content")
        );
    }

    private Map<String, Object> translationSchema() {
        return Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "language", Map.of("type", "STRING"),
                "title", Map.of("type", "STRING"),
                "sections", Map.of(
                    "type", "ARRAY",
                    "items", sectionSchema()
                )
            ),
            "required", List.of("language", "title", "sections")
        );
    }

    private AdminDocumentTranslationDraftResult parseResponse(GeminiGenerateContentResponse response)
        throws JsonProcessingException {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new ExternalApiException("번역 API 응답이 비어 있습니다.");
        }

        Candidate candidate = response.candidates().getFirst();
        if (candidate.content() == null || candidate.content().parts() == null
            || candidate.content().parts().isEmpty()) {
            throw new ExternalApiException("번역 API 응답 본문이 비어 있습니다.");
        }

        return objectMapper.readValue(
            candidate.content().parts().getFirst().text(),
            AdminDocumentTranslationDraftResult.class
        );
    }

    private AdminDocumentTranslationDraftResult parseDraft(GeminiGenerateContentResponse response) {
        try {
            return parseResponse(response);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException("문서 번역 응답을 해석할 수 없습니다.", e);
        }
    }

    private String geminiErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "응답 본문 없음";
        }
        try {
            JsonNode errorMessage = objectMapper.readTree(responseBody).path("error").path("message");
            if (!errorMessage.isMissingNode() && !errorMessage.asText().isBlank()) {
                return errorMessage.asText();
            }
        } catch (JsonProcessingException ignored) {
            return "응답 본문 해석 실패";
        }
        return "오류 메시지 없음";
    }
}
