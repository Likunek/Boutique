package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.repository.AuthenticationRepository;

import java.util.Collection;
import java.util.List;
@Slf4j
@Service
public class AuthenticationService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationRepository authenticationRepository;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder, AuthenticationRepository authenticationRepository) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationRepository = authenticationRepository;
    }

    public void addAuthentication(Authentication authentication) {
        authentication.setPassword(passwordEncoder.encode(authentication.getPassword()));
        authenticationRepository.save(authentication);
        log.info("Add new profile authentication: number={}, role={}",
                authentication.getNumber(),authentication.getRole());
    }

    public Authentication findByNumber(String number) {
        Authentication authentication = authenticationRepository.findByNumber(number);
        if (authentication == null) {
            log.error("Authentication not found by number ={}", number);
            throw  new ResourceNotFoundException(Authentication.class, number);
        }
        return authentication;
    }
    @Override
    public UserDetails loadUserByUsername(String number) throws UsernameNotFoundException {
        Authentication authentication = authenticationRepository.findByNumber(number);
        log.debug("Authorized profile: number={}, role={}", authentication.getNumber(),authentication.getRole());
        return new User(authentication.getNumber(), authentication.getPassword(), extractRoles(authentication));
    }
    private Collection<? extends GrantedAuthority> extractRoles(Authentication authentication) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + authentication.getRole()));
    }
}
