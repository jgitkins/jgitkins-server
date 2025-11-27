// TODO: Have To Customization
//package io.jgitkins.server.infrastructure.config.security;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import io.jgitkins.server.application.port.out.LoadUserPort;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class CustermAuthenticationProvider implements AuthenticationProvider {
//
//    private final LoadUserPort loadUserPort;
//
//    /***
//     * Basic Authentication 인증센터
//     * 1. Clone 요청시 Security 설정에 의해 authenticate 동작
//     */
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//
//        String username = authentication.getName();
//        String raw = (String) authentication.getCredentials();
//        log.debug("username: [{}] || raw: [{}]", username, raw);
//
//        Object userEntity = loadUserPort.getUser(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));
//
//        String decryptedPassword = "";
//
//        if (StringUtils.equals(decryptedPassword, raw))
//            throw new BadCredentialsException("bad credentials");
//
//
//        // build user's permission(role)
//        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
//
//        //
//        return new UsernamePasswordAuthenticationToken(username, "N/A", authorities);
//
//    }
//
//    @Override
//    public boolean supports(Class<?> c) {
//        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(c);
//    }
//
//}
