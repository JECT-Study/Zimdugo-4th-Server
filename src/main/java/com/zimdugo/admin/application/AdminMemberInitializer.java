package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminMember;
import com.zimdugo.admin.domain.AdminMemberRepository;
import com.zimdugo.admin.domain.AdminRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminMemberInitializer implements ApplicationRunner {

    private final AdminMemberRepository adminMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (adminMemberRepository.findByUsername("zimdugo").isEmpty()) {
            AdminMember admin = AdminMember.builder()
                .username("zimdugo")
                .password(passwordEncoder.encode("dugozim11!!"))
                .name("시스템 관리자")
                .role(AdminRole.ROLE_ADMIN)
                .build();
            adminMemberRepository.save(admin);
            log.info("Default admin member created. ID: zimdugo / PW: dugozim11!!");
        }
    }
}
