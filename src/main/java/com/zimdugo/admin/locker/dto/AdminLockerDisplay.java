package com.zimdugo.admin.locker.dto;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;

public final class AdminLockerDisplay {

    private AdminLockerDisplay() {
    }

    public static String lockerType(LockerType lockerType) {
        if (lockerType == null) {
            return "-";
        }
        return switch (lockerType) {
            case MUSEUM -> "박물관";
            case SUBWAY_STATION -> "지하철역";
            case DEPARTMENT_STORE -> "백화점";
            case CONVENIENCE_STORE -> "편의점";
            case PUBLIC_OFFICE -> "공공기관";
            case PRIVATE_LOCKER -> "사설 보관함";
            case TRAIN_STATION -> "기차역";
            case ETC -> "기타";
        };
    }

    public static String indoorOutdoor(IndoorOutdoorType indoorOutdoorType) {
        if (indoorOutdoorType == null) {
            return "-";
        }
        return indoorOutdoorType == IndoorOutdoorType.INDOOR ? "실내" : "실외";
    }

    public static String groundLevel(GroundLevelType groundLevelType) {
        if (groundLevelType == null) {
            return "-";
        }
        return groundLevelType == GroundLevelType.ABOVE_GROUND ? "지상" : "지하";
    }

    public static String lockerSize(LockerSizeType lockerSizeType) {
        if (lockerSizeType == null) {
            return "-";
        }
        return switch (lockerSizeType) {
            case SMALL -> "소형";
            case MEDIUM -> "중형";
            case LARGE -> "대형";
        };
    }

    public static String price(Integer minPrice, Integer maxPrice) {
        if (Integer.valueOf(0).equals(minPrice) && Integer.valueOf(0).equals(maxPrice)) {
            return "무료";
        }
        if (minPrice == null && maxPrice == null) {
            return "-";
        }
        if (minPrice == null) {
            return "최대 %,d원".formatted(maxPrice);
        }
        if (maxPrice == null) {
            return "%,d원부터".formatted(minPrice);
        }
        return "%,d원 ~ %,d원".formatted(minPrice, maxPrice);
    }
}
