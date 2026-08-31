package com.zimdugo.push.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushReminderJobRepository extends JpaRepository<PushReminderJobEntity, Long> {

    List<PushReminderJobEntity> findTop100ByProcessedAtIsNullAndNextAttemptAtLessThanEqualOrderByFireAtAsc(Instant now);
}
