package uk.gov.justice.digital.nomis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import net.minidev.json.JSONArray;
import org.assertj.core.groups.Tuple;
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
import uk.gov.justice.digital.nomis.api.Charge;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class ChargesControllerTest {

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
    public void canGetAllCharges() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/charges")
                .then()
                .statusCode(200)
                .body("page.totalElements", greaterThan(0));
    }

    @Test
    public void canGetOffenderCharges() {
        Charge[] charges = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001/charges")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Charge[].class);

        assertThat(charges.length).isGreaterThan(0);
    }

    @Test
    public void canGetBookingIdCharges() {
        List<Charge> charges = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/charges?bookingId=-2&bookingId=-3")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .path("_embedded.chargeList");

        assertThat(charges).asList().extracting("chargeId", "bookingId", "chargeStatus", "mostSeriousCharge").
                contains(Tuple.tuple(-3, -2, "A", true),
                        Tuple.tuple(-4, -3, "A", false));
    }

    @Test
    public void chargesAreAuthorized() {
        given()
                .when()
                .get("/charges")
                .then()
                .statusCode(401);
    }

    @Test
    public void offenderChargesAreAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/charges")
                .then()
                .statusCode(401);
    }

    @Test
    public void embeddedHateoasLinksWorks() {
        final String response = given()
                .when()
                .auth().oauth2(validOauthToken)
                .queryParam("page", 1)
                .queryParam("size", 1)
                .get("/charges")
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