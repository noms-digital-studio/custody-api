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
import uk.gov.justice.digital.nomis.api.SentenceCalculation;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class SentenceCalculationsControllerTest {

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
    public void canGetAllSentenceCalculations() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/sentenceCalculations")
                .then()
                .statusCode(200)
                .body("page.totalElements", greaterThan(0));
    }

    @Test
    public void canGetOffenderSentenceCalculations() {
        SentenceCalculation[] sentenceCalculations = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001/sentenceCalculations")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentenceCalculation[].class);

        assertThat(sentenceCalculations.length).isGreaterThan(0);
    }

    @Test
    public void canGetBookingIdSentenceCalculations() {
        List<SentenceCalculation> sentenceCalculations = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/sentenceCalculations?bookingId=-6&bookingId=-5")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .path("_embedded.sentenceCalculationList");

        assertThat(sentenceCalculations).asList().extracting("sentenceCalculationId", "bookingId", "calcReasonCode", "releaseDate").
                contains(Tuple.tuple(-6, -5, "NEW", "2023-05-07"),
                        Tuple.tuple(-7, -6, "NEW", "2018-05-15"));
    }

    @Test
    public void sentenceCalculationsAreAuthorized() {
        given()
                .when()
                .get("/sentenceCalculations")
                .then()
                .statusCode(401);
    }

    @Test
    public void offenderSentenceCalculationsAreAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/sentenceCalculations")
                .then()
                .statusCode(401);
    }

    @Test
    public void embeddedHateoasLinksWork() {
        final String response = given()
                .when()
                .auth().oauth2(validOauthToken)
                .queryParam("page", 1)
                .queryParam("size", 1)
                .get("/sentenceCalculations")
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