package com.zimdugo.auth.domain;

import java.time.Duration;

public interface RefreshTokenRepository {

    /** RT 화이트리스트 저장 (해시로 저장) */
    void save(Long userId, String sid, String jti, String rawToken, Duration ttl);

    /** 제출된 RT가 저장된 해시와 일치하는지 확인 */
    boolean matches(Long userId, String sid, String rawToken);

    /** 해당 세션 RT 삭제 (로그아웃, Rotation 시 폐기) */
    void delete(Long userId, String sid);

    /** jti가 이미 사용된 적 있는지 확인 (재사용 탐지) */
    boolean isJtiUsed(String jti);

    /** jti를 '사용됨'으로 기록 */
    void markJtiUsed(String jti, Long userId, String sid, Duration ttl);

    /** uv 조회 (없으면 1로 초기화) */
    long getUserVersion(Long userId);

    /** uv 1 증가 (전체 로그아웃 / 보안 이벤트) */
    void incrementUserVersion(Long userId);
}