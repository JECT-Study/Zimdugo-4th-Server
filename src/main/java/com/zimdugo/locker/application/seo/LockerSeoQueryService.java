package com.zimdugo.locker.application.seo;

import com.zimdugo.locker.domain.seo.LockerSeoReader;
import com.zimdugo.locker.application.result.seo.LockerSeoResult;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerSeoQueryService {

    private final LockerSeoReader lockerSeoReader;

    public List<LockerSeoResult> getSeoList() {
        return lockerSeoReader.readAllForSeo().stream()
            .map(LockerSeoResult::from)
            .collect(Collectors.toList());
    }
}
