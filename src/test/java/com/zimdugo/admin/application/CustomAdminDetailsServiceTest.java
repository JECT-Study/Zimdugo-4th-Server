package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminMember;
import com.zimdugo.admin.domain.AdminMemberRepository;
import com.zimdugo.admin.domain.AdminRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(CustomAdminDetailsService.class)
@Transactional
class CustomAdminDetailsServiceTest {

    @Autowired
    private CustomAdminDetailsService customAdminDetailsService;

    @Autowired
    private AdminMemberRepository adminMemberRepository;

    @Test
    @DisplayName("DB에 등록된 어드민 계정의 username으로 UserDetails 정보를 성공적으로 로드한다")
    void loadUserByUsernameSuccess() {
        // given
        AdminMember admin = adminMemberRepository.save(AdminMember.builder()
            .username("testadmin")
            .password("hashedpw")
            .name("테스트관리자")
            .role(AdminRole.ROLE_ADMIN)
            .build());

        // when
        UserDetails userDetails = customAdminDetailsService.loadUserByUsername("testadmin");

        // then
        assertThat(userDetails.getUsername()).isEqualTo("testadmin");
        assertThat(userDetails.getPassword()).isEqualTo("hashedpw");
        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("존재하지 않는 어드민 username으로 조회할 경우 UsernameNotFoundException 예외가 발생한다")
    void loadUserByUsernameNotFound() {
        // when & then
        assertThatThrownBy(() -> customAdminDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
