package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.LoginRequest;
import com.proyecto.servicios.onboarding.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest req);
}
