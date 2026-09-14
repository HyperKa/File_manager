package org.example.file_manager_cloudflare.security;

import org.example.file_manager_cloudflare.common.exception.FileAccessDeniedException;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public UserId currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserId userId)) {
            throw new FileAccessDeniedException();
        }
        return userId;
    }
}
