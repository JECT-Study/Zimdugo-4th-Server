package com.zimdugo.locker.application.result;

import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

public final class GeoBoundsUtils {

    private GeoBoundsUtils() {
    }

    public static <T> Optional<LockerBoundsResult> from(
        List<T> items,
        ToDoubleFunction<T> latitudeExtractor,
        ToDoubleFunction<T> longitudeExtractor
    ) {
        if (items == null || items.isEmpty()) {
            return Optional.empty();
        }

        double swLat = Double.POSITIVE_INFINITY;
        double swLng = Double.POSITIVE_INFINITY;
        double neLat = Double.NEGATIVE_INFINITY;
        double neLng = Double.NEGATIVE_INFINITY;

        for (T item : items) {
            double latitude = latitudeExtractor.applyAsDouble(item);
            double longitude = longitudeExtractor.applyAsDouble(item);
            swLat = Math.min(swLat, latitude);
            swLng = Math.min(swLng, longitude);
            neLat = Math.max(neLat, latitude);
            neLng = Math.max(neLng, longitude);
        }

        return Optional.of(new LockerBoundsResult(swLat, swLng, neLat, neLng));
    }
}
