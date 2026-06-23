package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.seo.LockerSeo;
import com.zimdugo.locker.domain.seo.LockerSeoReader;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.LockerSeoQueryProjection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerSeoReaderAdapter implements LockerSeoReader {

    private final LockerRepository lockerRepository;

    @Override
    public List<LockerSeo> readAllForSeo() {
        List<LockerSeoQueryProjection> projections = lockerRepository.findAllForSeo();

        Map<Long, List<LockerSeoQueryProjection>> grouped = projections.stream()
            .collect(Collectors.groupingBy(
                LockerSeoQueryProjection::getLockerId,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<LockerSeo> result = new ArrayList<>();
        for (Map.Entry<Long, List<LockerSeoQueryProjection>> entry : grouped.entrySet()) {
            Long lockerId = entry.getKey();
            List<LockerSeoQueryProjection> list = entry.getValue();

            LockerSeoQueryProjection first = list.get(0);
            String defaultName = first.getLockerName();

            Map<String, String> names = new HashMap<>();
            names.put("ko", defaultName);

            for (LockerSeoQueryProjection p : list) {
                String langCode = p.getLanguageCode();
                String transName = p.getTranslatedName();
                if (langCode != null && transName != null) {
                    names.put(formatLanguageTag(langCode), transName);
                }
            }

            result.add(new LockerSeo(lockerId, names));
        }

        return result;
    }

    private String formatLanguageTag(String langCode) {
        if ("zh-Hans".equalsIgnoreCase(langCode)) {
            return "zh";
        }
        if ("zh-Hant".equalsIgnoreCase(langCode)) {
            return "zh-tw";
        }
        return langCode.toLowerCase();
    }
}
