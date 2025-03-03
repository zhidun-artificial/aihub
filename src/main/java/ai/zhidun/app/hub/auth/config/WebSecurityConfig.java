package ai.zhidun.app.hub.auth.config;

import ai.zhidun.app.hub.auth.filter.JwtAuthenticationFilter;
import ai.zhidun.app.hub.auth.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenService service,
                                                   JwtProperties properties) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();
        jwtAuthenticationFilter.setService(service);
        jwtAuthenticationFilter.setProperties(properties);
        var objectPostProcessor = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                if (object instanceof AuthorizationFilter filter) {
                    AuthorizationManager<HttpServletRequest> authorizationManager = filter.getAuthorizationManager();
                    jwtAuthenticationFilter.setManager(authorizationManager);
                }
                return object;
            }
        };
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                // login
                                "/api/auth/**",
                                // swagger and openapi 3.0
                                "/swagger-ui*/**",
                                "/v3/**",
                                "/manager/api/**",
                                "/test/**",
                                "/api/v1/models/show"
                        )
                        .permitAll()
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .anonymous()
                        .withObjectPostProcessor(objectPostProcessor)
                )
                .exceptionHandling(c -> c
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().write("{\"code\":403,\"msg\":\"" + accessDeniedException.getMessage() + "\"}");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().write("{\"code\":401,\"msg\":\"" + authException.getMessage() + "\"}");
                        })
                )
                .sessionManagement(c -> c
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterAfter(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .build();

    }
}