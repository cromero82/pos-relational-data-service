package com.infinitesoft.pos_relational_data_service.security.service;

import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;

public interface AuthValidationService {
    AuthUserDto validateToken(String token);
    java.util.UUID fetchUserIdByEmail(String email);
    AuthUserDto fetchUserInfoById(java.util.UUID userId);
}
