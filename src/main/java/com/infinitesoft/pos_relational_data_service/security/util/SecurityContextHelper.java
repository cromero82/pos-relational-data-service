package com.infinitesoft.pos_relational_data_service.security.util;

import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityContextHelper {

    private SecurityContextHelper() {}

    public static AuthUserDto getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUserDto) {
            return (AuthUserDto) authentication.getPrincipal();
        }
        return null;
    }

    public static UUID getUserId() {
        AuthUserDto user = getUser();
        return user != null ? user.getId() : null;
    }
}
