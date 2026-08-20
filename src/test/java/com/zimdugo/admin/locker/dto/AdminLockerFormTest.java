package com.zimdugo.admin.locker.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminLockerFormTest {

    @Test
    void exposesExistingImagesForEditing() {
        AdminLockerDetailResult result = new AdminLockerDetailResult(
            1L, "서울역 보관함", "서울", 37.5, 127.0, null, null, PublicationStatus.DRAFT,
            LockerType.SUBWAY_STATION, IndoorOutdoorType.INDOOR, null, null, null, null, Set.of(),
            null, null, null, "https://cdn.example.com/first.jpg",
            List.of("https://cdn.example.com/first.jpg", "https://cdn.example.com/second.jpg"), 0, 0
        );

        AdminLockerForm form = AdminLockerForm.from(result);

        assertThat(form.getImageUrls()).containsExactly(
            "https://cdn.example.com/first.jpg",
            "https://cdn.example.com/second.jpg"
        );
    }
}
