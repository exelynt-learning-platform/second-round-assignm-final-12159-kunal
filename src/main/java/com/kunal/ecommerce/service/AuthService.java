package com.kunal.ecommerce.service;

import com.kunal.ecommerce.dto.auth.AuthResponse;
import com.kunal.ecommerce.dto.auth.LoginRequest;
import com.kunal.ecommerce.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
