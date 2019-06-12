package uk.gov.justice.digital.nomis.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static uk.gov.justice.digital.nomis.utils.MdcUtility.REQUEST_DURATION;
import static uk.gov.justice.digital.nomis.utils.MdcUtility.REQUEST_ID;
import static uk.gov.justice.digital.nomis.utils.MdcUtility.RESPONSE_STATUS;
import static uk.gov.justice.digital.nomis.utils.MdcUtility.SKIP_LOGGING;
import static uk.gov.justice.digital.nomis.utils.MdcUtility.USER_ID;
import static uk.gov.justice.digital.nomis.utils.MdcUtility.isLoggingAllowed;


@Component
@Slf4j
@Order(2)
public class RequestLogFilter implements Filter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    private final MdcUtility mdcUtility;

    private final Pattern excludeUriRegex;

    @Autowired
    public RequestLogFilter(MdcUtility mdcUtility, @Value("${logging.uris.exclude.regex}") String excludeUris) {
        this.mdcUtility = mdcUtility;
        excludeUriRegex = Pattern.compile(excludeUris);
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (excludeUriRegex.matcher(req.getRequestURI()).matches()) {
            MDC.put(SKIP_LOGGING, "true");
        }

        try {
            LocalDateTime start = LocalDateTime.now();
            MDC.put(REQUEST_ID, mdcUtility.generateUUID());
            MDC.put(USER_ID, getUser(req));
            if (log.isTraceEnabled() && isLoggingAllowed()) {
                log.trace("Request: {} {}", req.getMethod(), req.getRequestURI());
            }

            chain.doFilter(request, response);

            long duration = Duration.between(start, LocalDateTime.now()).toMillis();
            MDC.put(REQUEST_DURATION, String.valueOf(duration));
            int status = res.getStatus();
            MDC.put(RESPONSE_STATUS, String.valueOf(status));
            if (log.isTraceEnabled() && isLoggingAllowed()) {
                log.trace("Response: {} {} - Status {} - Start {}, User {}, Duration {} ms", req.getMethod(), req.getRequestURI(), status, start.format(formatter), getUser(req), duration);
            }
        } finally {
            MDC.remove(REQUEST_DURATION);
            MDC.remove(RESPONSE_STATUS);
            MDC.remove(REQUEST_ID);
            MDC.remove(USER_ID);
            MDC.remove(SKIP_LOGGING);
        }
    }

    private String getUser(HttpServletRequest req) {
        return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous";
    }

    @Override
    public void destroy() {

    }
}
