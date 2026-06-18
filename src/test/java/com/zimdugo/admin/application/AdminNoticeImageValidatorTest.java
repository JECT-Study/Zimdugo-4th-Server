package com.zimdugo.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zimdugo.common.storage.S3ImageDimensionReader;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminNoticeImageValidatorTest {

    private static final String IMAGE_URL = "https://cdn.example.com/admin/notice-images/notice.jpg";

    @Mock
    private S3ImageDimensionReader imageDimensionReader;

    @Test
    void validateAllowsRequiredWidth() {
        given(imageDimensionReader.readWidth(IMAGE_URL)).willReturn(1080);
        AdminNoticeImageValidator validator = validator();

        validator.validate(IMAGE_URL);

        verify(imageDimensionReader).readWidth(IMAGE_URL);
    }

    @Test
    void validateRejectsUnexpectedWidth() {
        given(imageDimensionReader.readWidth(IMAGE_URL)).willReturn(1200);
        AdminNoticeImageValidator validator = validator();

        assertThatThrownBy(() -> validator.validate(IMAGE_URL))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_IMAGE_WIDTH)
            );
    }

    @Test
    void validateSkipsBlankImageUrl() {
        AdminNoticeImageValidator validator = validator();

        validator.validate(" ");

        verify(imageDimensionReader, never()).readWidth(anyString());
    }

    private AdminNoticeImageValidator validator() {
        return new AdminNoticeImageValidator(
            new AdminNoticeImageProperties(1080),
            imageDimensionReader
        );
    }
}
