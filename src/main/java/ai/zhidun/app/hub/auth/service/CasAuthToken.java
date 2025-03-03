package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.service.TokenService.AuthedClaimInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record CasAuthToken(AuthedClaimInfo info) implements Authentication {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public Object getCredentials() {
        return info;
    }

    @Override
    public Object getDetails() {
        return new YsUserDetail(info.name(), info.userId(), info.permit(), info.st());
    }

    @Override
    public Object getPrincipal() {
        return info;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return info.name();
    }
}
