package com.zimdugo.push.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscriptionEntity, Long> {

    Optional<PushSubscriptionEntity> findByEndpoint(String endpoint);

    Optional<PushSubscriptionEntity> findByDeviceId(Long deviceId);

    void deleteByDeviceId(Long deviceId);
}
