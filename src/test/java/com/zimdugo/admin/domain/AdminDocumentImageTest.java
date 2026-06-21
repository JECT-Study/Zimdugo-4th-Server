package com.zimdugo.admin.domain;

import com.zimdugo.core.exception.BusinessException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminDocumentImageTest {

    @Test
    void replaceImagesKeepsOrderAndFirstImageCompatibility() {
        AdminDocument document = notice();

        document.replaceImages(List.of("https://cdn.example.com/second.png", "https://cdn.example.com/first.png"));

        assertThat(document.getImageUrls()).containsExactly(
            "https://cdn.example.com/second.png",
            "https://cdn.example.com/first.png"
        );
        assertThat(document.getImageUrl()).isEqualTo("https://cdn.example.com/second.png");
        assertThat(document.getImages())
            .extracting(AdminDocumentImage::getListOrder)
            .containsExactly(0, 1);
    }

    @Test
    void replaceImagesRejectsMoreThanTenImages() {
        AdminDocument document = notice();
        List<String> urls = IntStream.range(0, 11)
            .mapToObj(index -> "https://cdn.example.com/" + index + ".png")
            .toList();

        assertThatThrownBy(() -> document.replaceImages(urls))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void replaceImagesAllowsEmptyImages() {
        AdminDocument document = notice();
        document.replaceImages(List.of("https://cdn.example.com/notice.png"));

        document.replaceImages(List.of());

        assertThat(document.getImages()).isEmpty();
        assertThat(document.getImageUrls()).isEmpty();
        assertThat(document.getImageUrl()).isNull();
    }

    private AdminDocument notice() {
        return AdminDocument.builder()
            .type(DocumentType.NOTICE)
            .title("이미지 공지")
            .build();
    }
}
