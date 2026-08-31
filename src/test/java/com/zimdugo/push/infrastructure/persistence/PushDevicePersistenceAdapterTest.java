package com.zimdugo.push.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushDevicePersistenceAdapterTest {

    @Mock
    private PushDeviceRepository pushDeviceRepository;

    @Test
    @DisplayName("토큰 해시로 기기 존재 여부를 조회한다")
    void findsDeviceIdByTokenHash() {
        given(pushDeviceRepository.findIdByTokenHash("token-hash"))
            .willReturn(Optional.of(7L));
        PushDevicePersistenceAdapter adapter = new PushDevicePersistenceAdapter(pushDeviceRepository);

        Optional<Long> result = adapter.findIdByTokenHash("token-hash");

        assertThat(result).contains(7L);
    }

    @Test
    @DisplayName("원문이 아닌 토큰 해시만 새 기기로 저장한다")
    void savesTokenHash() {
        PushDevicePersistenceAdapter adapter = new PushDevicePersistenceAdapter(pushDeviceRepository);

        adapter.save("token-hash");

        ArgumentCaptor<PushDeviceEntity> captor = ArgumentCaptor.forClass(PushDeviceEntity.class);
        verify(pushDeviceRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("token-hash");
    }
}
