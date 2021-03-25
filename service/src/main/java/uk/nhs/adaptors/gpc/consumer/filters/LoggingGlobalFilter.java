package uk.nhs.adaptors.gpc.consumer.filters;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    private static final List<String> LOGGABLE_HEADER_KEYS = List.of("Ssp-From", "Ssp-To", "Ssp-TraceID");
    private static final String HEADERS_PREFIX = "Headers: ";
    private static final String EQUAL_SIGN = "=";
    private static final String REQUEST_ID = "RequestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        applyRequestId(exchange.getLogPrefix());
        Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
        String originalUri = uris.isEmpty() ? exchange.getRequest().getURI().toString() : uris.iterator().next().toString();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        URI routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        LOGGER.info("Incoming request {} is routed to id: {}, uri: {} with headers: {}",
            originalUri, route.getId(), routeUri,
            prepareHeaderLog(exchange.getRequest().getHeaders()));
        resetMDCKeys();
        return chain.filter(exchange.mutate().response(prepareErrorHandlingResponseDecorator(exchange)).build());
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    private String prepareHeaderLog(HttpHeaders httpHeaders) {
        StringBuilder headersLogBuilder = new StringBuilder(HEADERS_PREFIX);
        LOGGABLE_HEADER_KEYS.forEach(key -> {
            if (httpHeaders.containsKey(key)) {
                headersLogBuilder.append(key)
                    .append(EQUAL_SIGN)
                    .append(Objects.requireNonNull(httpHeaders.get(key)).toArray()[0])
                    .append(StringUtils.SPACE);
            }
        });

        return headersLogBuilder.toString();
    }

    private ServerHttpResponseDecorator prepareErrorHandlingResponseDecorator(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return DataBufferUtils.join(body)
                    .flatMap(dataBuffer -> handleError(getDelegate(), dataBuffer, exchange.getLogPrefix()));
            }
        };
    }

    private Mono<Void> handleError(ServerHttpResponse response, DataBuffer dataBuffer, String requestId) {
        applyRequestId(requestId);
        if (response != null && dataBuffer != null) {
            if (isErrorResponseCode(response)) {
                LOGGER.error("An error with status occurred: " + response.getStatusCode());
            } else {
                LOGGER.info("Request was successful");
            }
            resetMDCKeys();
            return response.writeWith(Mono.just(dataBuffer));
        }

        return Mono.empty();
    }

    private boolean isErrorResponseCode(ServerHttpResponse response) {
        HttpStatus httpStatus = response.getStatusCode();
        return httpStatus != null && !httpStatus.is2xxSuccessful();
    }

    private static void applyRequestId(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }

    private static void resetMDCKeys() {
        MDC.clear();
    }
}
