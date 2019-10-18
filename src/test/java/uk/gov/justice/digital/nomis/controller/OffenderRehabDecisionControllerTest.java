package uk.gov.justice.digital.nomis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.nomis.api.RehabDecision;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class OffenderRehabDecisionControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    @Qualifier("globalObjectMapper")
    private ObjectMapper objectMapper;

    @Value("${sample.token}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void canGetOffenderRehabDecisionsOrderedAppropriately() {
        final var rehabDecisions = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001/rehabDecisions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(RehabDecision[].class);

        assertThat(rehabDecisions.length).isGreaterThan(0);

        assertThat(rehabDecisions).extracting("offenderRehabDecisionId").isEqualTo(ImmutableList.of(2L, 3L, 1L));

        assertThat(rehabDecisions[0].getProviders()).extracting("offenderRehabProviderId").isEqualTo(ImmutableList.of(2L, 3L, 1L));
    }

    @Test
    public void offenderRehabDecisionsAreAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/rehabDecisions")
                .then()
                .statusCode(401);
    }

}
