package com.zimdugo.push.application;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushSubscriptionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushSubscriptionDeleteService {

    private final PushDeviceReader pushDeviceReader;
    private final PushSubscriptionStore pushSubscriptionStore;

    // 권한 취소·구독 소실 감지가 재전송돼도 이미 없는 구독은 성공 처리한다.
    public void delete(String deviceTokenHash) {
        // 권한 철회 후 중복 호출될 수 있으므로 기기나 구독이 없어도 성공으로 끝낸다.
        pushDeviceReader.findIdByTokenHash(deviceTokenHash)
            .ifPresent(pushSubscriptionStore::deleteByDeviceId);
    }
}
