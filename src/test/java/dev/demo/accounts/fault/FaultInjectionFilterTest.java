package dev.demo.accounts.fault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class FaultInjectionFilterTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Test
    void injectsFaultWhenRandomFallsBelowRate() throws Exception {
        var filter = new FaultInjectionFilter(0.2, () -> 0.1, registry);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/accounts"), response, chain);

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString()).contains("Injected fault");
        assertThat(chain.getRequest()).as("request must not reach the controller").isNull();
        assertThat(registry.get("app.injected.faults").counter().count()).isEqualTo(1.0);
    }

    @Test
    void passesThroughWhenRandomIsAboveRate() throws Exception {
        var filter = new FaultInjectionFilter(0.2, () -> 0.5, registry);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/accounts"), response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void neverTouchesActuatorEndpoints() throws Exception {
        var filter = new FaultInjectionFilter(1.0, () -> 0.0, registry);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void zeroRateIsANoOp() throws Exception {
        var filter = new FaultInjectionFilter(0.0, () -> 0.0, registry);
        var chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/accounts"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.5, Double.NaN})
    void rejectsInvalidRates(double rate) {
        assertThatThrownBy(() -> new FaultInjectionFilter(rate, () -> 0.0, registry))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
