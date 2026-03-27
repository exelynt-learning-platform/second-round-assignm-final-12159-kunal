package com.kunal.ecommerce.service.impl;

import com.kunal.ecommerce.dto.auth.AuthResponse;
import com.kunal.ecommerce.dto.auth.LoginRequest;
import com.kunal.ecommerce.dto.auth.RegisterRequest;
import com.kunal.ecommerce.entity.Cart;
import com.kunal.ecommerce.entity.Role;
import com.kunal.ecommerce.entity.User;
import com.kunal.ecommerce.exception.ValidationException;
import com.kunal.ecommerce.repository.CartRepository;
import com.kunal.ecommerce.repository.UserRepository;
import com.kunal.ecommerce.security.CustomUserDetailsService;
import com.kunal.ecommerce.security.JwtService;
import com.kunal.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);

        Cart cart = Cart.builder().user(savedUser).build();
        cartRepository.save(cart);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        return buildAuthResponse(savedUser, jwtService.generateToken(userDetails));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(user, jwtService.generateToken(userDetails));
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
