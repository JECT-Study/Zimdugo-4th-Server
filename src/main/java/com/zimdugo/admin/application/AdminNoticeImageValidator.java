package com.zimdugo.admin.application;

import com.zimdugo.common.storage.S3ImageDimensionReader;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminNoticeImageValidator {

    private final AdminNoticeImageProperties properties;
    private final S3ImageDimensionReader imageDimensionReader;

    public void validate(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        int imageWidth = imageDimensionReader.readWidth(imageUrl);
        int requiredWidth = properties.requiredWidthPixels();
        if (imageWidth != requiredWidth) {
            throw new BusinessException(
                ErrorCode.INVALID_IMAGE_WIDTH,
                "공지 이미지 가로 크기는 " + requiredWidth + "px이어야 합니다."
            );
        }
    }
}
