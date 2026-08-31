package com.zimdugo.push.application;

import com.zimdugo.push.config.PushVapidProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushVapidKeyService {

    private final PushVapidProperties pushVapidProperties;

    public PushVapidKeyResult getPublicKey() {
        return new PushVapidKeyResult(pushVapidProperties.getPublicKey());
    }
}
