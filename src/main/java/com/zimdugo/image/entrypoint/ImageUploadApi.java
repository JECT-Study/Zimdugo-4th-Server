package com.zimdugo.image.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.image.entrypoint.dto.request.PresignedUploadRequest;
import com.zimdugo.image.entrypoint.dto.response.PresignedUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Image Upload", description = "이미지 업로드 URL 발급 API")
public interface ImageUploadApi {

    @Operation(
        summary = "이미지 업로드용 presigned URL 발급",
        description = "프로필 이미지 또는 제보 이미지 업로드에 사용할 presigned PUT URL을 발급합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "발급 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @PostMapping("/uploads")
    ResponseEntity<RestResponse<PresignedUploadResponse>> createPresignedUpload(
        @CurrentUser Long userId,
        @Valid @RequestBody PresignedUploadRequest request
    );
}
