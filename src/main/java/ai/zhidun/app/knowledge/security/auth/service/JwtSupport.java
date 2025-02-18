package ai.zhidun.app.knowledge.security.auth.service;

import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@UtilityClass
public class JwtSupport {

    public static Optional<JwtService.AuthedClaimInfo> claimInfo() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            Object principal = token.getPrincipal();
            if (principal instanceof JwtService.AuthedClaimInfo info) {
                return Optional.of(info);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public static int userId() {
        return claimInfo().orElseThrow().userId();
    }
}
