package com.zimdugo.admin.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.ExternalApiException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiLockerReportTranslationDraftGenerator implements LockerReportTranslationDraftGenerator {

    private static final String JSON_MIME_TYPE = "application/json";

    private final RestClient geminiRestClient;
    private final GeminiTranslationProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public AdminTranslationDraftResult generate(LockerReportTranslationSource source) {
        if (!properties.hasApiKey()) {
            throw new ExternalApiException("번역 API 키가 설정되지 않았습니다.");
        }

        GeminiGenerateContentRequest request = createRequest(source);
        GeminiGenerateContentResponse response = callGemini(request);
        return parseDraft(response);
    }

    private GeminiGenerateContentRequest createRequest(LockerReportTranslationSource source) {
        try {
            return request(source);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException("번역 요청 생성에 실패했습니다.", e);
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
                "제보 번역 초안 생성 실패. status={}, message={}",
                e.getStatusCode(),
                message
            );
            throw new ExternalApiException(
                "번역 초안 생성 실패: HTTP " + e.getStatusCode().value() + " - " + message,
                e
            );
        } catch (RestClientException e) {
            throw new ExternalApiException("번역 초안 생성 실패: " + e.getClass().getSimpleName(), e);
        }
    }

    private GeminiGenerateContentRequest request(LockerReportTranslationSource source)
        throws JsonProcessingException {
        String prompt = """
            You are translating a Korean locker report for a production travel app.
            Return translations for every requested language.

            Rules:
            - Preserve Korean station, building, and place names by natural transliteration when needed.
            - Keep addresses searchable and do not invent missing administrative details.
            - Make name short enough for a mobile UI.
            - Make detailInfo natural as user-facing guidance.
            - aliases are search keywords, 2 to 5 per language.
            - If the source field is empty, return an empty string for that translated field.

            Requested language tags: %s
            Source report JSON:
            %s
            """.formatted(languageTags(), objectMapper.writeValueAsString(source));

        return new GeminiGenerateContentRequest(
            List.of(new Content(List.of(new Part(prompt)))),
            new GenerationConfig(JSON_MIME_TYPE, responseSchema())
        );
    }

    private String languageTags() {
        return SupportedLanguage.all().stream()
            .map(SupportedLanguage::languageTag)
            .reduce((left, right) -> left + ", " + right)
            .orElse("");
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> translationSchema = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "language", Map.of("type", "STRING"),
                "name", Map.of("type", "STRING"),
                "roadAddress", Map.of("type", "STRING"),
                "detailInfo", Map.of("type", "STRING"),
                "aliases", Map.of(
                    "type", "ARRAY",
                    "items", Map.of("type", "STRING")
                )
            ),
            "required", List.of("language", "name", "roadAddress", "detailInfo", "aliases")
        );

        return Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "translations", Map.of(
                    "type", "ARRAY",
                    "items", translationSchema
                )
            ),
            "required", List.of("translations")
        );
    }

    private AdminTranslationDraftResult parseResponse(GeminiGenerateContentResponse response)
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
            AdminTranslationDraftResult.class
        );
    }

    private AdminTranslationDraftResult parseDraft(GeminiGenerateContentResponse response) {
        try {
            return parseResponse(response);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException("번역 응답을 해석할 수 없습니다.", e);
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

    public record GeminiGenerateContentRequest(
        List<Content> contents,
        GenerationConfig generationConfig
    ) {
    }

    public record GenerationConfig(
        String responseMimeType,
        Map<String, Object> responseSchema
    ) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }

    public record GeminiGenerateContentResponse(List<Candidate> candidates) {
    }

    public record Candidate(Content content) {
    }
}
