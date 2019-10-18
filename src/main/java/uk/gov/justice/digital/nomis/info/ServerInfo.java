package uk.gov.justice.digital.nomis.info;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ServerInfo implements InfoContributor {

    @Override
    public void contribute(final Info.Builder builder) {
        final Map<String, Object> serverDetails = new HashMap<>();

        final InetAddress localHost;
        final InetAddress loopbackAddress;
        try {
            localHost = InetAddress.getLocalHost();
            loopbackAddress = InetAddress.getLoopbackAddress();
        } catch (final UnknownHostException e) {
            log.error(e.getMessage());
            return;
        }


        serverDetails.put("localHostAddress", localHost.getHostAddress());
        serverDetails.put("localHostName", localHost.getHostName());
        serverDetails.put("localHostCanonicalHostName", localHost.getCanonicalHostName());
        serverDetails.put("loopbackAddress", loopbackAddress.getHostAddress());
        serverDetails.put("loopbackHostName", loopbackAddress.getHostName());
        serverDetails.put("loopbackCanonicalHostName", loopbackAddress.getCanonicalHostName());
        serverDetails.put("serverTimeMillis", System.currentTimeMillis());
        serverDetails.put("serverDateTime", LocalDateTime.now().toString());

        final var mem = ImmutableMap.of("memoryUsedByJVM", Runtime.getRuntime().totalMemory(),
                "remainingMemoryAvailableToJVM", Runtime.getRuntime().freeMemory());

        serverDetails.put("memory", mem);
        builder.withDetail("server", serverDetails);
    }
}
