package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.push.domain.PushDeviceStore;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PushDeviceServiceTest {

    @Test
    @DisplayName("deviceToken 쿠키가 없으면 새 토큰의 해시를 저장하고 쿠키 발급 결과를 반환한다")
    void createsDeviceWhenCookieIsMissing() {
        RecordingStore store = new RecordingStore();
        PushDeviceTokenHasher tokenHasher = new PushDeviceTokenHasher();
        PushDeviceService service = new PushDeviceService(tokenHash -> Optional.empty(), store, tokenHasher);

        PushDeviceBootstrapResult result = service.ensureDevice(null);

        assertThat(result.issued()).isTrue();
        assertThat(result.deviceToken()).isNotBlank();
        assertThat(store.tokenHash).isEqualTo(tokenHasher.hash(result.deviceToken()));
        assertThat(store.tokenHash).isNotEqualTo(result.deviceToken());
    }

    @Test
    @DisplayName("저장된 기기의 deviceToken 쿠키는 다시 발급하지 않는다")
    void reusesExistingDeviceWhenCookieIsKnown() {
        String deviceToken = "known-device-token";
        RecordingStore store = new RecordingStore();
        PushDeviceTokenHasher tokenHasher = new PushDeviceTokenHasher();
        PushDeviceService service = new PushDeviceService(
            tokenHash -> Optional.of(1L),
            store,
            tokenHasher
        );

        PushDeviceBootstrapResult result = service.ensureDevice(deviceToken);

        assertThat(result.issued()).isFalse();
        assertThat(result.deviceToken()).isEqualTo(deviceToken);
        assertThat(store.tokenHash).isNull();
    }

    private static class RecordingStore implements PushDeviceStore {

        private String tokenHash;

        @Override
        public void save(String tokenHash) {
            this.tokenHash = tokenHash;
        }
    }
}
