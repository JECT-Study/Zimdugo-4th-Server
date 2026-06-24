package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinType;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class LockerPinAssemblerTest {

    private final LockerPinAssembler lockerPinAssembler = new LockerPinAssembler();

    @Test
    @DisplayName("장소 내 보관함이 1개이고 즐겨찾기 된 보관함이면 LOCKER 핀을 만들고 즐겨찾기 여부를 true로 표시한다")
    void makesLockerPinWithFavoriteTrue() {
        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(
            List.of(locker(1L, 101L, 37.55, 126.93)),
            Set.of(1L)
        );

        assertThat(pins).hasSize(1);
        assertThat(pins.getFirst().pinType()).isEqualTo(LockerPinType.LOCKER);
        assertThat(pins.getFirst().lockerId()).isEqualTo(1L);
        assertThat(pins.getFirst().placeId()).isNull();
        assertThat(pins.getFirst().isFavorite()).isTrue();
        assertThat(pins.getFirst().lockerCount()).isNull();
    }

    @Test
    @DisplayName("장소 내 보관함이 1개이고 즐겨찾기 되지 않은 보관함이면 LOCKER 핀을 만들고 즐겨찾기 여부를 false로 표시한다")
    void makesLockerPinWithFavoriteFalse() {
        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(
            List.of(locker(1L, 101L, 37.55, 126.93)),
            Set.of(2L)
        );

        assertThat(pins).hasSize(1);
        assertThat(pins.getFirst().pinType()).isEqualTo(LockerPinType.LOCKER);
        assertThat(pins.getFirst().lockerId()).isEqualTo(1L);
        assertThat(pins.getFirst().placeId()).isNull();
        assertThat(pins.getFirst().isFavorite()).isFalse();
        assertThat(pins.getFirst().lockerCount()).isNull();
    }

    @Test
    @DisplayName("장소 내 보관함이 2개 이상이면 PLACE 핀을 만들고 하위 보관함 개수를 표시한다")
    void makesPlacePinWithLockerCount() {
        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(
            List.of(
                locker(1L, 101L, 37.5500, 126.9300),
                locker(2L, 101L, 37.5520, 126.9340)
            ),
            Set.of()
        );

        assertThat(pins).hasSize(1);
        assertThat(pins.getFirst().pinType()).isEqualTo(LockerPinType.PLACE);
        assertThat(pins.getFirst().placeId()).isEqualTo(101L);
        assertThat(pins.getFirst().lockerId()).isNull();
        assertThat(pins.getFirst().latitude()).isCloseTo(37.5510, offset(0.000001));
        assertThat(pins.getFirst().longitude()).isCloseTo(126.9320, offset(0.000001));
        assertThat(pins.getFirst().isFavorite()).isNull();
        assertThat(pins.getFirst().lockerCount()).isEqualTo(2);
    }

    private NearbyLocker locker(Long id, Long placeId, double latitude, double longitude) {
        return new NearbyLocker(
            id,
            latitude,
            longitude,
            placeId
        );
    }
}
