package uk.nhs.adaptors.gpc.consumer.filters;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import uk.nhs.adaptors.gpc.consumer.gpc.GpcConfiguration;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DirectGpcUrlFilter implements GlobalFilter, Ordered {
    static final int DIRECT_FILTER_ORDER = SdsFilter.SDS_FILTER_ORDER - 1;

    private final GpcConfiguration gpcConfiguration;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
       if(isDirectGpcUrlEnabled()) {
            URI requestUri = exchange.getRequest().getURI();
            URI resolvedUri = requestUri.resolve(
                requestUri
                .toString()
                .replace(gpcConfiguration.getGpcConsumerUrl(), getDirectGpcUrl())
            );

            exchange.getAttributes()
                .put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, resolvedUri);
        }

        return chain.filter(exchange);
    }

    private String getDirectGpcUrl() {
        return gpcConfiguration.getGpcUrl();
    }

    private boolean isDirectGpcUrlEnabled() {
        return StringUtils.isNotBlank(gpcConfiguration.getGpcUrl());
    }

    @Override
    public int getOrder() {
        return DIRECT_FILTER_ORDER;
    }
}
