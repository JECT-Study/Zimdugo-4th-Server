package com.zimdugo.image.entrypoint;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.image.application.PresignedImageUploadService;
import com.zimdugo.image.application.PresignedUploadResult;
import com.zimdugo.image.entrypoint.dto.request.PresignedUploadRequest;
import com.zimdugo.image.entrypoint.dto.response.PresignedUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ImageUploadController implements ImageUploadApi {

    private final PresignedImageUploadService presignedImageUploadService;

    @Override
    public ResponseEntity<RestResponse<PresignedUploadResponse>> createPresignedUpload(
        Authentication authentication,
        PresignedUploadRequest request
    ) {
        Long userId = extractUserId(authentication);
        PresignedUploadResult result = presignedImageUploadService.createPresignedUpload(
            request.category(),
            request.fileName(),
            request.contentType(),
            userId
        );

        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, PresignedUploadResponse.from(result)));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }
    }
}
