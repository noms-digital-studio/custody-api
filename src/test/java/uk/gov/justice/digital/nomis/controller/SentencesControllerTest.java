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
import uk.gov.justice.digital.nomis.api.Sentence;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class SentencesControllerTest {

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
        RestAssured.basePath = "";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void canGetAllSentences() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/sentences")
                .then()
                .statusCode(200)
                .body("page.totalElements", greaterThan(0));
    }

    @Test
    public void canGetOffenderSentences() {
        Sentence[] sentences = given()
                .when().auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001/sentences")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Sentence[].class);

        assertThat(sentences.length).isGreaterThan(0);
    }

    @Test
    public void sentencesAreAuthorized() {
        given()
                .when()
                .get("/sentences")
                .then()
                .statusCode(401);
    }

    @Test
    public void offenderSentencesAreAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/sentences")
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
                .get("/sentences")
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