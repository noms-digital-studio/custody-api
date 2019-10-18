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
import uk.gov.justice.digital.nomis.api.Alert;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class AlertsControllerTest {

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
    public void canGetOffenderAlerts() {
        final var alerts = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001/alerts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Alert[].class);

        assertThat(alerts.length).isGreaterThan(0);
    }


    @Test
    public void canGetOffenderAlertsForBooking() {
        final var alerts = given()
                .when()
                .auth().oauth2(validOauthToken)
                .param("bookingId", -1)
                .get("/offenders/offenderId/-1001/alerts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Alert[].class);

        assertThat(alerts.length).isGreaterThan(0);
    }

    @Test
    public void noSuchOffenderForAlertsGives404() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .param("bookingId", -1)
                .get("/offenders/offenderId/-6666/alerts")
                .then()
                .statusCode(404);
    }

    @Test
    public void noSuchBookingForAlertsGives404() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .param("bookingId", -155555)
                .get("/offenders/offenderId/-1001/alerts")
                .then()
                .statusCode(404);
    }

    @Test
    public void offenderAlertsAreAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/alerts")
                .then()
                .statusCode(401);
    }

    @Test
    public void embeddedHateoasLinksWork() {
        final var response = given()
                .when()
                .auth().oauth2(validOauthToken)
                .queryParam("page", 1)
                .queryParam("size", 1)
                .get("/alerts")
                .then()
                .statusCode(200)
                .extract().asString();

        final JSONArray hrefs = JsonPath.parse(response).read("_links.*.href");

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
