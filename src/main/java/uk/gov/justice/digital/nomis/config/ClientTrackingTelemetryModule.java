package uk.gov.justice.digital.nomis.config;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.extensibility.TelemetryModule;
import com.microsoft.applicationinsights.web.extensibility.modules.WebTelemetryModule;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
@Configuration
public class ClientTrackingTelemetryModule implements WebTelemetryModule, TelemetryModule {
    private final String jwtPublicKey;

    @Autowired
    public ClientTrackingTelemetryModule(
            @Value("${jwt.public.key}") final String jwtPublicKey) {
        this.jwtPublicKey = jwtPublicKey;
    }

    @Override
    public void onBeginRequest(final ServletRequest req, final ServletResponse res) {

        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        final var token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final var bearer = "Bearer ";
        if (StringUtils.startsWithIgnoreCase(token, bearer)) {

            try {
                final var jwtBody = getClaimsFromJWT(token);

                final var properties = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

                if(!(jwtBody.get("user_name") == null)) {
                    properties.put("username", String.valueOf(jwtBody.get("user_name")));
                }
                properties.put("clientId", String.valueOf(jwtBody.get("client_id")));

            } catch (ExpiredJwtException e) {
                // Expired token which spring security will handle
            } catch (GeneralSecurityException e) {
                log.warn("problem decoding jwt public key for application insights", e);
            }
        }
    }

    private Claims getClaimsFromJWT(final String token) throws ExpiredJwtException, GeneralSecurityException {

        return Jwts.parser()
                .setSigningKey(getPublicKeyFromString(jwtPublicKey))
                .parseClaimsJws(token.substring(7))
                .getBody();
    }

    RSAPublicKey getPublicKeyFromString(final String key) throws GeneralSecurityException {
        final var publicKey = new String(Base64.decodeBase64(key))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\R", "");
        final var encoded = Base64.decodeBase64(publicKey);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
    }

    @Override
    public void onEndRequest(final ServletRequest req, final ServletResponse res) {
    }

    @Override
    public void initialize(final TelemetryConfiguration configuration) {

    }
}
