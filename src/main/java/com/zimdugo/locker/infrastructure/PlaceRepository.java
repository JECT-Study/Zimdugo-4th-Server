package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {
}
