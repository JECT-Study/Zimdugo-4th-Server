package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushDeviceLockReader;
import com.zimdugo.push.domain.PushDeviceStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushDevicePersistenceAdapter implements PushDeviceReader, PushDeviceLockReader, PushDeviceStore {

    private final PushDeviceRepository pushDeviceRepository;

    @Override
    public Optional<Long> findIdByTokenHash(String tokenHash) {
        return pushDeviceRepository.findIdByTokenHash(tokenHash);
    }

    @Override
    public Optional<Long> findIdByTokenHashForUpdate(String tokenHash) {
        return pushDeviceRepository.findByTokenHashForUpdate(tokenHash)
            .map(PushDeviceEntity::getId);
    }

    @Override
    public void save(String tokenHash) {
        pushDeviceRepository.save(new PushDeviceEntity(tokenHash));
    }
}
