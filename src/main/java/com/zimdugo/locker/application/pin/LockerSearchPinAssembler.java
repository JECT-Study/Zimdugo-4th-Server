package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.search.LockerSearchLockerResult;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LockerSearchPinAssembler {

    public List<LockerPinItemResult> assemble(List<LockerSearchItemResult> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<LockerPinItemResult> pins = new ArrayList<>(items.size());
        for (LockerSearchItemResult item : items) {
            if (item.type() == LockerItemType.LOCKER) {
                pins.add(LockerPinItemResult.locker(
                    item.lockerId(),
                    item.latitude(),
                    item.longitude(),
                    Boolean.TRUE.equals(item.isFavorite())
                ));
                continue;
            }

            if (item.lockers().size() == 1) {
                LockerSearchLockerResult locker = item.lockers().getFirst();
                pins.add(LockerPinItemResult.locker(
                    locker.lockerId(),
                    locker.latitude(),
                    locker.longitude(),
                    locker.isFavorite()
                ));
                continue;
            }

            pins.add(createPlacePin(item));
        }
        return pins;
    }

    private LockerPinItemResult createPlacePin(LockerSearchItemResult item) {
        double latitudeSum = 0d;
        double longitudeSum = 0d;
        for (LockerSearchLockerResult locker : item.lockers()) {
            latitudeSum += locker.latitude();
            longitudeSum += locker.longitude();
        }

        return LockerPinItemResult.place(
            item.placeId(),
            latitudeSum / item.lockers().size(),
            longitudeSum / item.lockers().size(),
            item.lockers().size()
        );
    }
}
