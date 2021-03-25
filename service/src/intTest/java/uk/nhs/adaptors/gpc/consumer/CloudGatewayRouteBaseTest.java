package uk.nhs.adaptors.gpc.consumer;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.WireMockServer;

import lombok.Getter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CloudGatewayRouteBaseTest {
    private static final int WIREMOCK_PORT = 8210;
    private static final int MAX_TIMEOUT = 10;
    private static final String LOCALHOST_URI = "http://localhost:";
    protected static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(WIREMOCK_PORT);
    protected static final String GPC_URL_ENVIRONMENT_VARIABLE_NAME = "GPC_CONSUMER_GPC_GET_URL";
    protected static final String GPC_CONSUMER_SSP_FQDN_VARIABLE = "GPC_CONSUMER_SSP_FQDN";
    protected static final String ENDPOINT = "/Endpoint";
    protected static final String SSP_FROM_HEADER = "Ssp-From";
    protected static final String SSP_TO_HEADER = "Ssp-To";
    protected static final String SSP_INTERACTION_ID_HEADER = "Ssp-InteractionID";
    protected static final String SSP_TRACE_ID_HEADER = "Ssp-TraceID";
    protected static final String ANY_STRING = "any";
    protected static final String SDS_LOOKUP_URI = "/ABC/5/gpcdocuments/";
    protected static final String EXAMPLE_SDS_BODY = "{\"resourceType\":\"Bundle\","
        + "\"id\":\"47DBB1CA-256D-410E-B00B-C19C1F13E9F6\","
        + "\"entry\":[{\"resource\":{\"resourceType\":\"Endpoint\",\"id\":\"307B4278-DFED-4A27-8B51-1539DB1B2C62\","
        + "\"address\":\"%s/ABC/5/gpcdocuments/\"}}]}";
    protected static final String EXPECTED_NOT_FOUND_BODY = "{\"resourceType\": \"OperationOutcome\",\"meta\": {\"profile\": "
        + "[\"https://fhir.nhs.uk/StructureDefinition/gpconnect-operationoutcome-1\" ]},\"issue\": [{\"severity\": \"error\","
        + "\"code\": \"invalid\",\"details\": {\"coding\":[{\"system\": \"https://fhir.nhs.uk/ValueSet/gpconnect-error-or-warning-code-1\","
        + "\"code\": \"NO_RECORD_FOUND\",\"display\": \"No Record Found\"}]},\"diagnostics\": \"No record found\"}]}";

    @LocalServerPort
    private int port = 0;
    @Getter
    private WebTestClient webTestClient;
    private String baseUri;

    @BeforeAll
    public static void initialize() {
        WIRE_MOCK_SERVER.start();
    }

    @AfterAll
    public static void deinitialize() {
        WIRE_MOCK_SERVER.stop();
    }

    @BeforeEach
    public void setUp() {
        baseUri = LOCALHOST_URI + port;
        webTestClient = WebTestClient.bindToServer()
            .responseTimeout(Duration.ofSeconds(MAX_TIMEOUT))
            .baseUrl(baseUri)
            .build();
        System.setProperty(GPC_CONSUMER_SSP_FQDN_VARIABLE, WIRE_MOCK_SERVER.baseUrl());
    }

    @AfterEach
    public void tearDown() {
        WIRE_MOCK_SERVER.resetAll();
        System.clearProperty(GPC_URL_ENVIRONMENT_VARIABLE_NAME);
        System.clearProperty(GPC_CONSUMER_SSP_FQDN_VARIABLE);
    }
}
