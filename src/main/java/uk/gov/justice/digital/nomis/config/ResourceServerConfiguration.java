package uk.gov.justice.digital.nomis.config;


import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${jwt.public.key}")
    private String jwtPublicKey;

    @Value("${api.base.path:api}")
    private String apiBasePath;

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .csrf().disable().antMatcher("/**")
                .cors().disable().antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/h2-console/**", "/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
                        "/swagger-ui.html", "/webjars/**", "/swagger-resources/configuration/ui", "/swagger-ui.html",
                        "/swagger-resources/configuration/security", "/health", "/health/ping", "/info", "/ping").permitAll()
                .antMatchers("/{apiBasePath}/**").hasAnyRole("REPORTING", "SYSTEM_READ_ONLY")
                .anyRequest()
                .permitAll();

        http.headers().frameOptions().disable();

    }

    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenServices());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        final var converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(new String(Base64.decodeBase64(jwtPublicKey)));
        return converter;
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        final var defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

}
