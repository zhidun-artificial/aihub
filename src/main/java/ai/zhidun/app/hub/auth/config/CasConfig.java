package ai.zhidun.app.hub.auth.config;

import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;

@Configuration
public class CasConfig {

	@Autowired
	SpringCasAutoconfig autoconfig;

	private static boolean casEnabled = true;

    /**public CasConfig() {
	}*/

    @Bean
    SpringCasAutoconfig getSpringCasAutoconfig() {
		return new SpringCasAutoconfig();
	}

    /**
     * 用于实现单点登出功能
     */
    @Bean
    ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener() {
		ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listener = new ServletListenerRegistrationBean<SingleSignOutHttpSessionListener>();
		listener.setEnabled(casEnabled);
		listener.setListener(new SingleSignOutHttpSessionListener());
		listener.setOrder(1);
		return listener;
	}

    /**
     * 该过滤器用于实现单点登出功能，单点�?出配置，�?��要放在其他filter之前
     */
    @Bean
    FilterRegistrationBean<Filter> logOutFilter() {
		FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
		LogoutFilter logoutFilter = new LogoutFilter(autoconfig
				.getCasServerUrlPrefix()
				+ "/logout?service=" + autoconfig.getServerName(),
				new SecurityContextLogoutHandler());
		filterRegistration.setFilter(logoutFilter);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getSignOutFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getSignOutFilters());
		else
			filterRegistration.addUrlPatterns("/logout");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig
				.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig
				.getServerName());
		filterRegistration.setOrder(2);
		return filterRegistration;
	}

    /**
     * 该过滤器用于实现单点登出功能，单点�?出配置，�?��要放在其他filter之前
     */
    @Bean
    FilterRegistrationBean<Filter> singleSignOutFilter() {
    	FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(new SingleSignOutFilter());
		filterRegistration.setOrder(1);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getSignOutFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getSignOutFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig
				.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig
				.getServerName());
		filterRegistration.setOrder(3);
		return filterRegistration;
	}

	/**
	 * 该过滤器负责用户的认证工�?
	 */
    @Bean
    FilterRegistrationBean<Filter> authenticationFilter() {
		FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(new AuthenticationFilter());
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getAuthFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getAuthFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		// casServerLoginUrl:cas服务的登陆url
		filterRegistration.addInitParameter("casServerLoginUrl", autoconfig
				.getCasServerLoginUrl());
		// 本项目登录ip+port
		filterRegistration.addInitParameter("serverName", autoconfig
				.getServerName());
		filterRegistration.addInitParameter("useSession", autoconfig
				.isUseSession() ? "true" : "false");
		filterRegistration.addInitParameter("redirectAfterValidation",
				autoconfig.isRedirectAfterValidation() ? "true" : "false");
		filterRegistration.setOrder(4);
		return filterRegistration;
	}

    /**
     * 该过滤器负责对Ticket的校验工�?
     */
    @Bean
    FilterRegistrationBean<Filter> cas20ProxyReceivingTicketValidationFilter() {
		FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
		Cas20ProxyReceivingTicketValidationFilter cas20ProxyReceivingTicketValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
//		Cas20ProxyReceivingTicketValidationFilter cas20ProxyReceivingTicketValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
		cas20ProxyReceivingTicketValidationFilter.setServerName(autoconfig
				.getServerName());
		Filter filter = (Filter) cas20ProxyReceivingTicketValidationFilter;
		filterRegistration.setFilter(filter);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getValidateFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getValidateFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig
				.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig
				.getServerName());
		filterRegistration.setOrder(5);
		return filterRegistration;
	}

    /**
     * 该过滤器对HttpServletRequest请求包装�?
     * 可�?过HttpServletRequest的getRemoteUser()方法获得登录用户的登录名
     * 
     */
    @Bean
    FilterRegistrationBean<Filter> httpServletRequestWrapperFilter() {
		FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(new HttpServletRequestWrapperFilter());
		filterRegistration.setEnabled(true);
		if (autoconfig.getRequestWrapperFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig
					.getRequestWrapperFilters());
		else
			filterRegistration.addUrlPatterns("/login");
		filterRegistration.setOrder(6);
		return filterRegistration;
	}

    /**
     * 该过滤器使得可以通过org.jasig.cas.client.util.AssertionHolder来获取用户的登录名�?
     * 比如AssertionHolder.getAssertion().getPrincipal().getName()�?
     * 这个类把Assertion信息放在ThreadLocal变量中，这样应用程序不在web层也能够获取到当前登录信�?
     */
    @Bean
    FilterRegistrationBean<Filter> assertionThreadLocalFilter() {
		FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(new AssertionThreadLocalFilter());
		filterRegistration.setEnabled(true);
		if (autoconfig.getAssertionFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getAssertionFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.setOrder(7);
		return filterRegistration;
	}

}
