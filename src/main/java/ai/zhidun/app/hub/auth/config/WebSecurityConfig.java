package ai.zhidun.app.hub.auth.config;

import ai.zhidun.app.hub.auth.service.CasUserDetailsService;
import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CasUserDetailsService detailsService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                // index
                                "/index",
                                // login
                                "/api/v1/auth/show_me",
                                // swagger and openapi 3.0
                                "/swagger-ui*/**",
                                "/v3/**",
                                "/manager/api/**",
                                "/test/**"
                        )
                        .permitAll()
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .anonymous()
                )
                .exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(casAuthenticationEntryPoint()))
                .addFilter(casAuthenticationFilter(detailsService))
                .addFilterBefore(new SingleSignOutFilter(), CasAuthenticationFilter.class)
                .build();

    }

    public CasAuthenticationFilter casAuthenticationFilter(CasUserDetailsService detailsService) {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        CasAuthenticationProvider casAuthenticationProvider = casAuthenticationProvider(detailsService);
        filter.setAuthenticationManager(new ProviderManager(casAuthenticationProvider));
        return filter;
    }

    public CasAuthenticationProvider casAuthenticationProvider(CasUserDetailsService userDetailsService) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setAuthenticationUserDetailsService(userDetailsService);
        provider.setServiceProperties(serviceProperties());
        provider.setTicketValidator(cas30ServiceTicketValidator());
        provider.setKey("key");
        return provider;
    }


    @Value("${cas.base.url}")
    private String casBaseUrl;

    @Value("${cas.login.url}")
    private String casLoginUrl;

    private TicketValidator cas30ServiceTicketValidator() {
        return new Cas30ServiceTicketValidator(this.casBaseUrl);
    }

    @Value("${server.port}")
    private Integer serverPort;

    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(this.casLoginUrl);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("http://localhost:" + serverPort + "/login/cas");
        return serviceProperties;
    }
}