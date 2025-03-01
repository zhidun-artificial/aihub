package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.model.UserVo;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final UserService service;

    public CasUserDetailsService(UserService service) {
        this.service = service;
    }

    @Override
    public YsUserDetail loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        String name = token.getName();

        // todo get user info from database
        // 0: 普通用户, 1: 部门管理员 2: 系统管理员 -1: 特殊用户
        UserVo vo = service.getByName(name);

        return new YsUserDetail(name, vo.id(), vo.permit(), "st");
    }
}
