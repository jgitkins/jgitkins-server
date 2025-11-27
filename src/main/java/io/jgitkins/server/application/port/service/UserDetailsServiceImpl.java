// TODO: Have To Customization

//package io.jgitkins.server.application.port.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import io.jgitkins.server.application.port.out.LoadUserPort;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    private final LoadUserPort loadUserPort;
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        log.info("Finding User: [{}]", username);
//        Object userEntity = loadUserPort.getUser(username)
//                .orElseThrow(() -> {
//
//                    log.warn("User Could Not Found");
//                    return new UsernameNotFoundException("User Not Found: " + username);
//                });
//
//        return User.withUsername(username)
////                .password(userEntity.getPswd())
//                .password("tmp")
//                .build();
//    }
//}
