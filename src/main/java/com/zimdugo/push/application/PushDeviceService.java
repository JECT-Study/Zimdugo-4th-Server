package com.zimdugo.push.application;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushDeviceStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushDeviceService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PushDeviceReader pushDeviceReader;
    private final PushDeviceStore pushDeviceStore;

    public PushDeviceBootstrapResult ensureDevice(String deviceToken) {
        if (deviceToken != null && !deviceToken.isBlank()) {
            String tokenHash = hash(deviceToken);
            // 원문 토큰은 쿠키에서만 유지하고, DB 조회 및 저장은 항상 단방향 해시로 수행
            if (pushDeviceReader.findIdByTokenHash(tokenHash).isPresent()) {
                return new PushDeviceBootstrapResult(deviceToken, false);
            }
        }

        // 쿠키가 없거나 서버에 없는 토큰이면 이전 익명 기기와 연결하지 않고 새 기기를 발급한다.
        String issuedToken = createToken();
        pushDeviceStore.save(hash(issuedToken));
        return new PushDeviceBootstrapResult(issuedToken, true);
    }

    private String createToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 해시 알고리즘을 찾을 수 없습니다.", exception);
        }
    }
}
