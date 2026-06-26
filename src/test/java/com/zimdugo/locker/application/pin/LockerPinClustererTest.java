package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerPinClustererTest {

    private final LockerPinClusterer lockerPinClusterer = new LockerPinClusterer();

    @Test
    @DisplayName("지도 화면 픽셀 기준에 맞춰 줌 레벨별 클러스터 셀 크기를 정한다")
    void resolvesCellSizeByZoomLevel() {
        assertThat(lockerPinClusterer.cellSizeMetersFor(14.0)).isEqualTo(1_223);
        assertThat(lockerPinClusterer.cellSizeMetersFor(13.0)).isEqualTo(2_446);
        assertThat(lockerPinClusterer.cellSizeMetersFor(12.0)).isEqualTo(4_892);
        assertThat(lockerPinClusterer.cellSizeMetersFor(11.0)).isEqualTo(9_784);
        assertThat(lockerPinClusterer.cellSizeMetersFor(6.0)).isEqualTo(313_086);
    }

    @Test
    @DisplayName("줌 레벨 15 이상이면 기존 핀을 그대로 반환한다")
    void returnsOriginalPinsWhenZoomIsDetailLevel() {
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.5000, 127.0000, false),
            LockerPinItemResult.place(101L, 37.5010, 127.0010, 2)
        );

        List<LockerPinItemResult> result = lockerPinClusterer.cluster(pins, 15.0);

        assertThat(result).isSameAs(pins);
    }

    @Test
    @DisplayName("줌 레벨 14 이하면 같은 격자 안의 핀을 CLUSTER로 묶고 평균 좌표에 배치한다")
    void clustersPinsInSameCellAtAveragePositionWhenZoomedOut() {
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.500000, 127.000000, false),
            LockerPinItemResult.place(101L, 37.500100, 127.000100, 2)
        );

        List<LockerPinItemResult> result = lockerPinClusterer.cluster(pins, 14.0);

        assertThat(result).hasSize(1);
        LockerPinItemResult cluster = result.getFirst();
        assertThat(cluster.pinType()).isEqualTo(LockerPinType.CLUSTER);
        assertThat(cluster.pinCount()).isEqualTo(2);
        assertThat(cluster.latitude()).isEqualTo(37.500050);
        assertThat(cluster.longitude()).isEqualTo(127.000050);
        assertThat(cluster.bounds().swLat()).isEqualTo(37.500000);
        assertThat(cluster.bounds().swLng()).isEqualTo(127.000000);
        assertThat(cluster.bounds().neLat()).isEqualTo(37.500100);
        assertThat(cluster.bounds().neLng()).isEqualTo(127.000100);
    }

    @Test
    @DisplayName("줌아웃 상태에서도 격자 안의 핀이 1개면 기존 핀을 유지한다")
    void returnsOriginalPinWhenCellHasSinglePin() {
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.500000, 127.000000, false)
        );

        List<LockerPinItemResult> result = lockerPinClusterer.cluster(pins, 14.0);

        assertThat(result).hasSize(1);
        LockerPinItemResult pin = result.getFirst();
        assertThat(pin.pinType()).isEqualTo(LockerPinType.LOCKER);
        assertThat(pin.pinCount()).isNull();
        assertThat(pin.latitude()).isEqualTo(37.500000);
        assertThat(pin.longitude()).isEqualTo(127.000000);
    }

    @Test
    @DisplayName("줌 레벨 6에서도 부산과 제주처럼 떨어진 핀은 같은 격자로 묶지 않는다")
    void keepsBusanAndJejuInDifferentCellsAtCountryLevelZoom() {
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 35.1796, 129.0756, false),
            LockerPinItemResult.locker(2L, 33.4996, 126.5312, false)
        );

        List<LockerPinItemResult> result = lockerPinClusterer.cluster(pins, 6.0);

        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(LockerPinItemResult::pinType)
            .containsExactly(LockerPinType.LOCKER, LockerPinType.LOCKER);
    }
}
