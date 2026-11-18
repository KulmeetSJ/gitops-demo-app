package dev.demo.accounts.fault;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fails a configurable fraction of /api/** requests with HTTP 500 (env var FAULT_RATE).
 * Exists only to demonstrate Argo Rollouts aborting and rolling back a bad canary.
 */
@Component
public class FaultInjectionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FaultInjectionFilter.class);

    private final double faultRate;
    private final DoubleSupplier random;
    private final Counter injectedFaults;

    @Autowired
    public FaultInjectionFilter(@Value("${app.fault-rate:0}") double faultRate, MeterRegistry registry) {
        this(faultRate, () -> ThreadLocalRandom.current().nextDouble(), registry);
    }

    FaultInjectionFilter(double faultRate, DoubleSupplier random, MeterRegistry registry) {
        if (Double.isNaN(faultRate) || faultRate < 0.0 || faultRate > 1.0) {
            throw new IllegalArgumentException("app.fault-rate must be between 0.0 and 1.0, was " + faultRate);
        }
        this.faultRate = faultRate;
        this.random = random;
        this.injectedFaults = Counter.builder("app.injected.faults")
                .description("Requests deliberately failed by FAULT_RATE")
                .register(registry);
        if (faultRate > 0) {
            log.warn("Fault injection ENABLED: {}% of /api requests will fail with HTTP 500", faultRate * 100);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return faultRate == 0.0 || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (random.getAsDouble() < faultRate) {
            injectedFaults.increment();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write(
                    "{\"title\":\"Internal Server Error\",\"status\":500,\"detail\":\"Injected fault (FAULT_RATE)\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
