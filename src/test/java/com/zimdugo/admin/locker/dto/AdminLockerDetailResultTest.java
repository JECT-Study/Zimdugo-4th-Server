package com.zimdugo.admin.locker.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailUpdateValues;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminLockerDetailResultTest {

    @Test
    void doesNotExposeTheLegacyRepresentativeImageField() {
        assertThat(Arrays.stream(AdminLockerDetailResult.class.getRecordComponents())
            .map(component -> component.getName()))
            .doesNotContain("imageUrl");
    }

    @Test
    void copiesLockerSizesBeforeThePersistenceContextCloses() {
        LockerEntity locker = new LockerEntity("서울역 보관함", "서울 중구", 37.5, 127.0);
        LockerDetailEntity detail = new LockerDetailEntity(
            locker,
            new LockerDetailUpdateValues(
                LockerType.ETC,
                IndoorOutdoorType.INDOOR,
                null,
                null,
                null,
                null,
                Set.of(LockerSizeType.SMALL),
                null,
                null,
                null
            )
        );

        AdminLockerDetailResult result = AdminLockerDetailResult.from(locker, detail);
        detail.getLockerSize().clear();

        assertThat(result.lockerSizes()).containsExactly(LockerSizeType.SMALL);
    }
}
