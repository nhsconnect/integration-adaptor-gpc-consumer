package uk.nhs.adaptors.gpc.consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class GetStructuredDocumentRouteTest extends CloudGatewayRouteBaseTest {
    private static final String SDS_STRUCTURED_URI_ENCODED = SDS_LOOKUP_URI + "Patient/$gpc.getstructuredrecord";

    private static final String EXAMPLE_STRUCTURED_BODY = "{\"resourceType\":\"Bundle\","
        + "\"meta\":{\"profile\":[\"https://fhir.nhs"
        + ".uk/STU3/StructureDefinition/GPConnect-StructuredRecord-Bundle-1\"]},"
        + "\"type\":\"collection\",\"entry\":[]}";
    private static final String STRUCTURED_INTERACTION_ID =
        "urn:nhs:names:services:gpconnect:fhir:operation:gpc.getstructuredrecord-1";
    private static final String STRUCTURED_URI = "/GP0001/STU3/1/gpconnect/fhir/Patient/$gpc.getstructuredrecord";

    @Test
    public void When_MakingRequestForStructuredDocument_Expect_OkResponse() {
        WIRE_MOCK_SERVER.stubFor(post(urlPathEqualTo(SDS_STRUCTURED_URI_ENCODED))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(EXAMPLE_STRUCTURED_BODY)));
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo(ENDPOINT))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(String.format(EXAMPLE_SDS_BODY, WIRE_MOCK_SERVER.baseUrl()))));

        getWebTestClient().post()
            .uri(STRUCTURED_URI)
            .header(SSP_FROM_HEADER, ANY_STRING)
            .header(SSP_TO_HEADER, ANY_STRING)
            .header(SSP_INTERACTION_ID_HEADER, STRUCTURED_INTERACTION_ID)
            .header(SSP_TRACE_ID_HEADER, ANY_STRING)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .json(EXAMPLE_STRUCTURED_BODY);
    }

    @Test
    public void When_MakingRequestForNonExistingStructuredDocument_Expect_NotFoundResponse() {
        WIRE_MOCK_SERVER.stubFor(post(urlPathEqualTo(SDS_STRUCTURED_URI_ENCODED))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_NOT_FOUND)
                .withBody(EXPECTED_NOT_FOUND_BODY)));
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo(ENDPOINT))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(String.format(EXAMPLE_SDS_BODY, WIRE_MOCK_SERVER.baseUrl()))));

        getWebTestClient().post()
            .uri(STRUCTURED_URI)
            .header(SSP_FROM_HEADER, ANY_STRING)
            .header(SSP_TO_HEADER, ANY_STRING)
            .header(SSP_INTERACTION_ID_HEADER, STRUCTURED_INTERACTION_ID)
            .header(SSP_TRACE_ID_HEADER, ANY_STRING)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .json(EXPECTED_NOT_FOUND_BODY);
    }

    @Test
    public void When_MakingRequestForStructuredDocument_Given_GpcUrlEnvVariable_Expect_OkResponse() {
        System.setProperty(GPC_URL_ENVIRONMENT_VARIABLE_NAME, WIRE_MOCK_SERVER.baseUrl());

        WIRE_MOCK_SERVER.stubFor(post(urlPathEqualTo( STRUCTURED_URI))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(EXAMPLE_STRUCTURED_BODY)));

        getWebTestClient().post()
            .uri(STRUCTURED_URI)
            .header(SSP_FROM_HEADER, ANY_STRING)
            .header(SSP_TO_HEADER, ANY_STRING)
            .header(SSP_INTERACTION_ID_HEADER, STRUCTURED_INTERACTION_ID)
            .header(SSP_TRACE_ID_HEADER, ANY_STRING)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .json(EXAMPLE_STRUCTURED_BODY);
    }
}
