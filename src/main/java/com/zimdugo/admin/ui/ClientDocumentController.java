package com.zimdugo.admin.ui;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.ui.dto.ClientDocumentResponse;
import com.zimdugo.common.i18n.AcceptLanguageResolver;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class ClientDocumentController {

    private final AdminDocumentService adminDocumentService;
    private final AcceptLanguageResolver acceptLanguageResolver;

    @GetMapping
    public ResponseEntity<RestResponse<List<ClientDocumentResponse>>> getActiveDocuments(
        @RequestParam(name = "type") DocumentType type,
        @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage
    ) {
        List<ClientDocumentResponse> responses = adminDocumentService.getLocalizedActiveDocumentsByType(
            type,
            acceptLanguageResolver.resolve(acceptLanguage)
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, responses));
    }
}
