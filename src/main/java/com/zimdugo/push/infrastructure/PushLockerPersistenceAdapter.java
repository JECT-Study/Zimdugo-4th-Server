package com.zimdugo.push.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.push.domain.PushLockerReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushLockerPersistenceAdapter implements PushLockerReader {

    private final LockerRepository lockerRepository;

    @Override
    public boolean existsById(Long lockerId) {
        return lockerRepository.existsById(lockerId);
    }
}
