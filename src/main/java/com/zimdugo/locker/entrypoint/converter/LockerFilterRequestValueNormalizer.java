package com.zimdugo.locker.entrypoint.converter;

final class LockerFilterRequestValueNormalizer {

    private LockerFilterRequestValueNormalizer() {
    }

    static String normalize(String source) {
        if (source == null) {
            return null;
        }
        return source.replaceAll("[\\[\\]\"]", "").trim();
    }
}
