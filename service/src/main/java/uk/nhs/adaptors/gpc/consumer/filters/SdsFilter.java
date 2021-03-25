package uk.nhs.adaptors.gpc.consumer.filters;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import uk.nhs.adaptors.gpc.consumer.filters.uri.SdsUrlMapper;
import uk.nhs.adaptors.gpc.consumer.sds.SdsClient;
import uk.nhs.adaptors.gpc.consumer.sds.exception.SdsException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SdsFilter implements GlobalFilter, Ordered {
    public static final int SDS_FILTER_ORDER = RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;

    private static final String GPC_URL_ENVIRONMENT_VARIABLE = "GPC_CONSUMER_GPC_GET_URL";
    private static final String INTERACTION_ID_PREFIX = "urn:nhs:names:services:gpconnect:";
    private static final String STRUCTURED_ID = INTERACTION_ID_PREFIX + "fhir:operation:gpc.getstructuredrecord-1";
    private static final String PATIENT_SEARCH_ID = INTERACTION_ID_PREFIX + "documents:fhir:rest:search:patient-1";
    private static final String DOCUMENT_SEARCH_ID = INTERACTION_ID_PREFIX + "documents:fhir:rest:search:documentreference-1";
    private static final String BINARY_READ_ID = INTERACTION_ID_PREFIX + "documents:fhir:rest:read:binary-1";
    private static final String SSP_INTERACTION_ID = "Ssp-InteractionID";

    private final SdsClient sdsClient;
    private final SdsUrlMapper sdsUrlMapper;

    private Map<String, Function<String, Optional<SdsClient.SdsResponseData>>> sdsRequestFunctions;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (StringUtils.isBlank(System.getenv(GPC_URL_ENVIRONMENT_VARIABLE))) {
            LOGGER.info("Using SDS API to perform service discovery of GPC Provider endpoint");
//            ServerHttpRequest serverHttpRequest = exchange.getRequest();
//            String interactionId = extractInteractionId(serverHttpRequest.getHeaders());
//            proceedSdsLookup(serverHttpRequest, exchange, interactionId);
        } else {
            LOGGER.info("Using GP Provider endpoint specified by {}", GPC_URL_ENVIRONMENT_VARIABLE);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return SDS_FILTER_ORDER;
    }

    @PostConstruct
    @SuppressWarnings("unused")
    public void initializeSdsRequestFunctions() {
        sdsRequestFunctions = Map.of(
            STRUCTURED_ID, sdsClient::callForGetStructuredRecord,
            PATIENT_SEARCH_ID, sdsClient::callForPatientSearchAccessDocument,
            DOCUMENT_SEARCH_ID, sdsClient::callForSearchForDocumentRecord,
            BINARY_READ_ID, sdsClient::callForRetrieveDocumentRecord);
    }

    private void proceedSdsLookup(ServerHttpRequest serverHttpRequest,
        ServerWebExchange exchange,
        String integrationId) {
        String organisation = extractOrganisation(serverHttpRequest.getPath());
        SdsClient.SdsResponseData response = performRequestAccordingToInteractionId(integrationId, organisation)
            .orElseThrow(() -> new SdsException(
                String.format("No endpoint found in SDS for GP Connect endpoint InteractionId=%s OdsCode=%s",
                    integrationId,
                    organisation))
            );
        prepareLookupUri(response.getAddress(), serverHttpRequest)
            .ifPresent(uri -> exchange.getAttributes()
                .put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, uri));
    }

    private Optional<SdsClient.SdsResponseData> performRequestAccordingToInteractionId(String interactionId,
        String organisation) {
        if (sdsRequestFunctions.containsKey(interactionId)) {
            LOGGER.info("Performing request with organisation \"{}\" and NHS service endpoint id \"{}\"",
                organisation, interactionId);
            return sdsRequestFunctions.get(interactionId)
                .apply(organisation);
        }
        throw new IllegalArgumentException(String.format("Not recognised InteractionId %s", interactionId));
    }

    private String extractOrganisation(RequestPath requestPath) {
        return requestPath.elements()
            .stream()
            .skip(1)
            .findFirst()
            .map(PathContainer.Element::value)
            .orElseThrow(() -> new IllegalArgumentException("URL does not contain ODS code in its second element"));
    }

    private String extractInteractionId(HttpHeaders httpHeaders) {
        if (httpHeaders.containsKey(SSP_INTERACTION_ID)) {
            List<String> interactionIds = httpHeaders.get(SSP_INTERACTION_ID);

            if (!CollectionUtils.isEmpty(interactionIds)) {
                return interactionIds.get(0);
            }
        }
        throw new IllegalArgumentException("Request is missing required header: " + SSP_INTERACTION_ID);
    }

    private Optional<URI> prepareLookupUri(String address, ServerHttpRequest serverHttpRequest) {
        String url = sdsUrlMapper.map(address, serverHttpRequest.getPath());
        URI constructedUri = UriComponentsBuilder.fromUriString(url)
            .queryParams(serverHttpRequest.getQueryParams())
            .build()
            .toUri();
        return Optional.of(constructedUri);
    }
}
