package uk.gov.justice.digital.nomis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import net.minidev.json.JSONArray;
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
import uk.gov.justice.digital.nomis.api.Offender;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
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
    public void canGetAllOffenders() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders")
                .then()
                .statusCode(200)
                .body("page.totalElements", greaterThan(0));
    }

    @Test
    public void canGetOffenderByOffenderId() {
        Offender offender = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Offender.class);

        assertThat(offender).isNotNull();
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
        Offender offender = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/nomsId/A1234AA")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Offender.class);

        assertThat(offender).extracting("offenderId").containsOnly(-1001L);
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
                .body("page.totalElements", greaterThan(0))
                .body("_embedded.offenders[0].surname", equalTo("TRESCOTHICK"))
                .body("_embedded.offenders.activeBooking.activeFlag", not(contains(is(false))));
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

    @Test
    public void embeddedHateoasLinksWorkForOffenders() {
        final String response = given()
                .when()
                .auth().oauth2(validOauthToken)
                .queryParam("page", 1)
                .queryParam("size", 1)
                .get("/offenders")
                .then()
                .statusCode(200)
                .extract().asString();

        JSONArray hrefs = JsonPath.parse(response).read("_links.*.href");

        hrefs.forEach(href -> given()
                .when()
                .auth().oauth2(validOauthToken)
                .log()
                .all()
                .get(href.toString())
                .then()
                .statusCode(200));

    }

    @Test
    public void embeddedHateoasLinksWorkForOffenderActiveBookings() {
        final String response = given()
                .when()
                .auth().oauth2(validOauthToken)
                .queryParam("page", 1)
                .queryParam("size", 1)
                .get("/offenders/prison/MDI")
                .then()
                .statusCode(200)
                .extract().asString();

        JSONArray hrefs = JsonPath.parse(response).read("_links.*.href");

        hrefs.forEach(href -> given()
                .when()
                .auth().oauth2(validOauthToken)
                .log()
                .all()
                .get(href.toString())
                .then()
                .statusCode(200));

    }


}