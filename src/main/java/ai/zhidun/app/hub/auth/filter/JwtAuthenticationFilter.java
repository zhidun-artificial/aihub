package ai.zhidun.app.hub.auth.filter;

import ai.zhidun.app.hub.auth.config.JwtProperties;
import ai.zhidun.app.hub.auth.service.CasAuthToken;
import ai.zhidun.app.hub.auth.service.TokenService;
import ai.zhidun.app.hub.common.BizError;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Setter
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean {

    private TokenService service;

    private JwtProperties properties;

    private AuthorizationManager<HttpServletRequest> manager;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String jwt = request.getHeader(properties.head());
        // 这里如果没有jwt，继续往后走，因为后面还有鉴权管理器等去判断是否拥有身份凭证，所以是可以放行的
        // 没有jwt相当于匿名访问，若有一些接口是需要权限的，则不能访问这些接口
        if (!StringUtils.hasText(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("auth head:{}", jwt);
        }

        // 已经完成鉴权，比如匿名访问，则忽略token校验
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null) {
            if (manager.authorize(context::getAuthentication, request) instanceof AuthorizationResult result) {
                if (result.isGranted()) {
                    chain.doFilter(request, response);
                    return;

                }
            }
        }

        try {
            TokenService.AuthedClaimInfo claimInfo = service.decode(jwt);
            context.setAuthentication(new CasAuthToken(claimInfo));
            chain.doFilter(request, response);
        } catch (InvalidJwtException e) {
            if (e.hasExpired()) {
                log.error("token超时", e);
                write(response, "token超时");
            } else {
                write(response, "token异常");
                log.error("token异常", e);
            }
        }

    }

    private final static JsonMapper mapper = new JsonMapper();

    public static void write(HttpServletResponse response, String msg) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            ServletOutputStream output = response.getOutputStream();
            BizError error = BizError.error(msg);
            output.write(mapper.writeValueAsString(error).getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            log.warn("write error failed", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!((request instanceof HttpServletRequest httpRequest) && (response instanceof HttpServletResponse httpResponse))) {
            throw new ServletException("OncePerRequestFilter only supports HTTP requests");
        }

        doFilterInternal(httpRequest, httpResponse, chain);
    }
}