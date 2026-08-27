package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.dto.ClientDocumentResult;
import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class ClientDocumentController {

    private final AdminDocumentService adminDocumentService;
    private final CurrentRequestLanguage currentRequestLanguage;

    @GetMapping
    public ResponseEntity<RestResponse<List<ClientDocumentResult>>> getActiveDocuments(
        @RequestParam(name = "type") String type
    ) {
        List<ClientDocumentResult> responses = adminDocumentService.getLocalizedActiveDocumentsByType(
            type,
            currentRequestLanguage.resolve()
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, responses));
    }
}
