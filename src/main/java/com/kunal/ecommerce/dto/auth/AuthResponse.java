package com.kunal.ecommerce.dto.auth;

import com.kunal.ecommerce.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private final String token;
    private final Long userId;
    private final String name;
    private final String email;
    private final Role role;
}
