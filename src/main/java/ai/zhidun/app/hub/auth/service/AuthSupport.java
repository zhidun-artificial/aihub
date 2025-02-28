package ai.zhidun.app.hub.auth.service;

import lombok.experimental.UtilityClass;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@UtilityClass
public class AuthSupport {

    public static Optional<YsUserDetail> userDetail() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof CasAuthenticationToken token) {
            Object principal = token.getPrincipal();
            if (principal instanceof YsUserDetail detail) {
                return Optional.of(detail);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public static String userId() {
        return userDetail().orElseThrow().userId();
    }
}
