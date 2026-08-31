package com.zimdugo.push.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PushDeviceRepositoryTest {

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Test
    void findsOnlyDeviceIdByTokenHash() {
        Long deviceId = pushDeviceRepository.save(new PushDeviceEntity("device-token-hash")).getId();

        assertThat(pushDeviceRepository.findIdByTokenHash("device-token-hash"))
            .contains(deviceId);
    }
}
