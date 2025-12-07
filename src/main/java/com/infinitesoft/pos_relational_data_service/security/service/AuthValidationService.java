package com.infinitesoft.pos_relational_data_service.security.service;

import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;

public interface AuthValidationService {
    AuthUserDto validateToken(String token);
}
