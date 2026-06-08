package com.zimdugo.image.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.image.application.PresignedImageUploadService;
import com.zimdugo.image.application.PresignedUploadResult;
import com.zimdugo.image.entrypoint.dto.request.PresignedUploadRequest;
import com.zimdugo.image.entrypoint.dto.response.PresignedUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ImageUploadController implements ImageUploadApi {

    private final PresignedImageUploadService presignedImageUploadService;

    @Override
    public ResponseEntity<RestResponse<PresignedUploadResponse>> createPresignedUpload(
        @CurrentUser Long userId,
        PresignedUploadRequest request
    ) {
        PresignedUploadResult result = presignedImageUploadService.createPresignedUpload(
            request.category(),
            request.fileName(),
            request.contentType(),
            userId
        );

        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, PresignedUploadResponse.from(result)));
    }
}
