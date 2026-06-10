package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminMember;
import com.zimdugo.admin.domain.AdminMemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomAdminDetailsService implements UserDetailsService {

    private final AdminMemberRepository adminMemberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminMember adminMember = adminMemberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 어드민 계정입니다: " + username));

        return new User(
            adminMember.getUsername(),
            adminMember.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(adminMember.getRole().name()))
        );
    }
}
