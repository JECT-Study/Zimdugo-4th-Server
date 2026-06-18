package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.LockerReader;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
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
