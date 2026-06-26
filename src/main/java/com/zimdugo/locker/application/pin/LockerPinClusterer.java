package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.result.GeoBoundsUtils;
import com.zimdugo.locker.application.result.LockerBoundsResult;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LockerPinClusterer {

    private static final double CLUSTER_ZOOM_THRESHOLD = 15.0;
    private static final int WEB_MERCATOR_TILE_SIZE_PX = 256;
    private static final int CLUSTER_CELL_SIZE_PX = 128;
    private static final double EARTH_RADIUS_METERS = 6_378_137.0;
    private static final double EARTH_CIRCUMFERENCE_METERS = 2 * Math.PI * EARTH_RADIUS_METERS;
    private static final double WEB_MERCATOR_LATITUDE_OFFSET_RADIANS = Math.PI / 4.0;

    public List<LockerPinItemResult> cluster(List<LockerPinItemResult> pins, double zoomLevel) {
        if (pins.isEmpty() || zoomLevel >= CLUSTER_ZOOM_THRESHOLD) {
            return pins;
        }

        int cellSizeMeters = cellSizeMetersFor(zoomLevel);
        Map<CellKey, List<LockerPinItemResult>> pinsByCell = groupByCell(pins, cellSizeMeters);
        List<LockerPinItemResult> clusters = new ArrayList<>(pinsByCell.size());
        for (List<LockerPinItemResult> cellPins : pinsByCell.values()) {
            if (cellPins.size() == 1) {
                clusters.add(cellPins.getFirst());
                continue;
            }
            clusters.add(toCluster(cellPins));
        }
        return clusters;
    }

    int cellSizeMetersFor(double zoomLevel) {
        double scale = Math.pow(2, Math.floor(zoomLevel));
        double metersPerPixel = EARTH_CIRCUMFERENCE_METERS / (WEB_MERCATOR_TILE_SIZE_PX * scale);
        return (int) Math.round(CLUSTER_CELL_SIZE_PX * metersPerPixel);
    }

    private Map<CellKey, List<LockerPinItemResult>> groupByCell(List<LockerPinItemResult> pins, int cellSizeMeters) {
        Map<CellKey, List<LockerPinItemResult>> pinsByCell = new LinkedHashMap<>();
        for (LockerPinItemResult pin : pins) {
            CellKey key = toCellKey(pin.latitude(), pin.longitude(), cellSizeMeters);
            pinsByCell.computeIfAbsent(key, ignored -> new ArrayList<>()).add(pin);
        }
        return pinsByCell;
    }

    private CellKey toCellKey(double latitude, double longitude, int cellSizeMeters) {
        return new CellKey(
            (long) Math.floor(longitudeToX(longitude) / cellSizeMeters),
            (long) Math.floor(latitudeToY(latitude) / cellSizeMeters)
        );
    }

    private double longitudeToX(double longitude) {
        return Math.toRadians(longitude) * EARTH_RADIUS_METERS;
    }

    private double latitudeToY(double latitude) {
        return Math.log(Math.tan(
            WEB_MERCATOR_LATITUDE_OFFSET_RADIANS + Math.toRadians(latitude) / 2
        )) * EARTH_RADIUS_METERS;
    }

    private LockerPinItemResult toCluster(List<LockerPinItemResult> pins) {
        double latitudeSum = 0d;
        double longitudeSum = 0d;
        for (LockerPinItemResult pin : pins) {
            latitudeSum += pin.latitude();
            longitudeSum += pin.longitude();
        }

        LockerBoundsResult bounds = GeoBoundsUtils.from(
            pins,
            LockerPinItemResult::latitude,
            LockerPinItemResult::longitude
        ).orElseThrow();

        return LockerPinItemResult.cluster(
            latitudeSum / pins.size(),
            longitudeSum / pins.size(),
            pins.size(),
            bounds
        );
    }

    private record CellKey(long x, long y) {
    }
}
