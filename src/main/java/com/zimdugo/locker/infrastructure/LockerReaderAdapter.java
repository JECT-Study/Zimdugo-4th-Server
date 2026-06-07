package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerReaderAdapter implements LockerReader {

    private final LockerRepository lockerRepository;

    @Override
    public boolean existsById(Long lockerId) {
        return lockerRepository.existsById(lockerId);
    }
}
