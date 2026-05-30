package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.AuthenticationDto;
import ru.angelika.boutique.exception.PasswordInvalidException;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.repository.AuthenticationRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationRepository authenticationRepository;


    public void add(Authentication authentication) {
        authentication.setPassword(passwordEncoder.encode(authentication.getPassword()));
        authenticationRepository.save(authentication);
        log.info("Add new profile authentication: number={}, role={}",
                authentication.getNumber(), authentication.getRole());
    }

    public Authentication findByNumber(String number) {
        Authentication authentication = authenticationRepository.findByNumber(number);
        if (authentication ==  null) {
            log.error("Authentication not found by number={}", number);
            throw  new ResourceNotFoundException(Authentication.class, number);
        }
        return authentication;
    }

    public void update(AuthenticationDto dto, Authentication authentication) {
        if (passwordEncoder.matches(dto.getOldPassword(), authentication.getPassword())) {
            if (!dto.getNewPassword().isBlank()) {
                authentication.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            }
            if (!authentication.getNumber().equals(dto.getNumber())) {
                if (authenticationRepository.findByNumber(dto.getNumber()) != null) {
                    log.error("Authentication with number={} already exists", dto.getNumber());
                    throw new ResourceExistsException(Authentication.class, dto.getNumber());
                }
                authentication.setNumber(dto.getNumber());
            }
            authenticationRepository.save(authentication);
            log.info("Update data authentication: id={}, number={}, role={}",
                    authentication.getId(), authentication.getNumber(), authentication.getRole());
        } else {
            log.error("Password is incorrect, profile with id={}", authentication.getId());
            throw new PasswordInvalidException("Your password is incorrect");
        }
    }

    public void delete(String number) {
        Authentication authentication = findByNumber(number);
        authenticationRepository.deleteById(authentication.getId());
        log.info("Delete authentication by id={}, number={}", authentication.getId(), number);
    }

    @Override
    public UserDetails loadUserByUsername(String number) throws UsernameNotFoundException {
        Authentication authentication = findByNumber(number);
        log.debug("Authorized profile: number={}, role={}", authentication.getNumber(), authentication.getRole());
        return new User(authentication.getNumber(), authentication.getPassword(), extractRoles(authentication));
    }

    private Collection<? extends GrantedAuthority> extractRoles(Authentication authentication) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + authentication.getRole()));
    }
}
