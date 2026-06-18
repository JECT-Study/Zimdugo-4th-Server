package com.zimdugo.admin.ui;

import com.zimdugo.admin.application.AdminPresignedImageUploadService;
import com.zimdugo.admin.ui.dto.AdminPresignedUploadRequest;
import com.zimdugo.admin.ui.dto.AdminPresignedUploadResponse;
import com.zimdugo.common.storage.PresignedUpload;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/uploads")
@RequiredArgsConstructor
public class AdminImageUploadController {

    private final AdminPresignedImageUploadService adminPresignedImageUploadService;

    @PostMapping
    public ResponseEntity<RestResponse<AdminPresignedUploadResponse>> createPresignedUpload(
        @Valid @RequestBody AdminPresignedUploadRequest request
    ) {
        PresignedUpload upload = adminPresignedImageUploadService.createPresignedUpload(
            request.category(),
            request.fileName(),
            request.contentType(),
            request.contentLength()
        );

        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, AdminPresignedUploadResponse.from(upload)));
    }
}
