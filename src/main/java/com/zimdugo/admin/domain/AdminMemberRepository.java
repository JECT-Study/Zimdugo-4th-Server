package com.zimdugo.admin.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMemberRepository extends JpaRepository<AdminMember, Long> {
    Optional<AdminMember> findByUsername(String username);
}
