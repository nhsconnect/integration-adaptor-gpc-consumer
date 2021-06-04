package uk.nhs.adaptors.gpc.consumer.filters;

import java.net.URI;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import uk.nhs.adaptors.gpc.consumer.utils.LoggingUtil;

@Slf4j
@Component
public class UrlsInResponseBodyRewriteFunction implements RewriteFunction<String, String> {
    @Value("${gpc-consumer.gpc.gpcConsumerUrl}")
    private String gpcConsumerUrl;
    @Value("${gpc-consumer.gpc.overrideGpcProviderUrl}")
    private String overrideGpcProviderUrl;

    public static String replaceUrl(String gpcConsumerUrl, String overrideGpcProviderUrl, String responseBody) {
        LOGGER.info(String.format("Replace host: %s, to: %s", overrideGpcProviderUrl, gpcConsumerUrl));
        return responseBody.replace(overrideGpcProviderUrl, gpcConsumerUrl);
    }

    @Override
    public Publisher<String> apply(ServerWebExchange exchange, String responseBody) {
        return Mono.just(responseBody)
            .map(originalResponseBody -> {
                // TODO: Steps for NIAD-1283 to enable multiple GPC providers
                // 1) Determine the correct prefix of *this* service - GPC_URL env var
                var gpcConsumerUrlPrefix = gpcConsumerUrl;
                LoggingUtil.info(LOGGER, exchange, "The URL prefix for this GPC Consumer service is {}", gpcConsumerUrlPrefix);
                // 2) Calculate a pattern for the URL we need to replace (based on previous SDS lookup, put onto event context)
                URI proxyTargetUri = (URI) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
                var gpcProducerUrlPrefix = proxyTargetUri.toString();
                LoggingUtil.info(LOGGER, exchange, "The URL prefix of the GPC Producer endpoint is {}", gpcProducerUrlPrefix);
                // 3) Perform the replace operation using calculated pattern
                return replaceUrl(gpcConsumerUrl, overrideGpcProviderUrl, originalResponseBody);
            });
    }
}