package com.kunal.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kunal.ecommerce.dto.auth.AuthResponse;
import com.kunal.ecommerce.dto.auth.LoginRequest;
import com.kunal.ecommerce.dto.auth.RegisterRequest;
import com.kunal.ecommerce.entity.Role;
import com.kunal.ecommerce.repository.CartRepository;
import com.kunal.ecommerce.repository.UserRepository;
import com.kunal.ecommerce.security.CustomUserDetailsService;
import com.kunal.ecommerce.security.JwtService;
import com.kunal.ecommerce.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldCreateUserAndCart() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Kunal");
        request.setEmail("kunal@example.com");
        request.setPassword("secret");

        com.kunal.ecommerce.entity.User savedUser = com.kunal.ecommerce.entity.User.builder()
                .id(1L)
                .name("Kunal")
                .email("kunal@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("kunal@example.com", "encoded", java.util.List.of());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(com.kunal.ecommerce.entity.User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername(request.getEmail())).thenReturn(springUser);
        when(jwtService.generateToken(springUser)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(Role.USER, response.getRole());
        verify(cartRepository).save(any());
    }

    @Test
    void loginShouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("kunal@example.com");
        request.setPassword("secret");

        com.kunal.ecommerce.entity.User savedUser = com.kunal.ecommerce.entity.User.builder()
                .id(1L)
                .name("Kunal")
                .email("kunal@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("kunal@example.com", "encoded", java.util.List.of());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.of(savedUser));
        when(userDetailsService.loadUserByUsername(request.getEmail())).thenReturn(springUser);
        when(jwtService.generateToken(springUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        assertEquals("jwt-token", response.getToken());
    }
}
