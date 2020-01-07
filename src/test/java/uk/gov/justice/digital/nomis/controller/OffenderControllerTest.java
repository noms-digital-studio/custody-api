package uk.gov.justice.digital.nomis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.core.ResolvableType.forType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
@DirtiesContext
public class OffenderControllerTest {

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
    public void canGetOffenderByOffenderId() {
        final var offender = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThatJsonFile(offender, "offender.json");
    }

    @Test
    public void getOffenderByOffenderIdReturns404WhenOffenderNotFound() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-666666")
                .then()
                .statusCode(404);
    }

    @Test
    public void offendersAreAuthorized() {
        given()
                .when()
                .get("/offenders")
                .then()
                .statusCode(401);
    }

    @Test
    public void offenderIsAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001")
                .then()
                .statusCode(401);
    }

    @Test
    public void canGetOffenderByNomsId() {
        final var offender = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/nomsId/A1234AA")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThatJsonFile(offender, "offender.json");
    }

    @Test
    public void getOffenderByNomsIdReturns404WhenOffenderNotFound() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/nomsId/Z666666ZZ")
                .then()
                .statusCode(404);
    }

    @Test
    public void offenderByNomsIdIsAuthorized() {
        given()
                .when()
                .get("/offenders/nomsId/A1234AA")
                .then()
                .statusCode(401);
    }

    @Test
    public void activeOffendersByPrisonIsAuthorized() {
        given()
                .when()
                .get("/offenders/prison/MDI")
                .then()
                .statusCode(401);
    }

    @Test
    public void canGetActiveOffendersByPrison() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/prison/MDI")
                .then()
                .statusCode(200)
                .body("totalElements", greaterThan(0))
                .body("content[0].surname", equalTo("TRESCOTHICK"))
                .body("content.activeBooking.activeFlag", not(contains(is(false))));

    }

    @Test
    public void getActiveOffendersByPrisonFirstPage() {
        final var offenders = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/prison/SYI?page=0&size=2")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThatJsonFile(offenders, "offenders_page0.json");
    }

    @Test
    public void getActiveOffendersByPrisonSecondPage() {
        final var offenders = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/prison/SYI?page=1&size=2")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThatJsonFile(offenders, "offenders_page1.json");
    }

    @Test
    public void getActiveOffendersByPrisonReturns404WhenPrisonNotFound() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/prison/ZZZZZZZ")
                .then()
                .statusCode(404);
    }

    <T> void assertThatJsonFile(final String response, final String jsonFile) {
        final var responseAsJson = getBodyAsJsonContent(response);
        assertThat(responseAsJson).isEqualToJson(jsonFile);
    }

    private <T> JsonContent<T> getBodyAsJsonContent(final String response) {
        return new JsonContent<T>(getClass(), forType(String.class), Objects.requireNonNull(response));
    }
}
