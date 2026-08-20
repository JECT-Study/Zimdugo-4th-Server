package com.zimdugo.locker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class LockerEntityTest {

    @Test
    void replacesImagesInSubmittedOrder() {
        LockerEntity locker = new LockerEntity("서울역 보관함", "서울 중구", 37.5, 127.0);

        locker.replaceImages(List.of("https://cdn.example.com/first.jpg", "https://cdn.example.com/second.jpg"));

        assertThat(locker.getImages())
            .extracting(LockerImageEntity::getImageUrl)
            .containsExactly("https://cdn.example.com/first.jpg", "https://cdn.example.com/second.jpg");
        assertThat(locker.getImages())
            .extracting(LockerImageEntity::getListOrder)
            .containsExactly(0, 1);
    }
}
