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
import static org.springframework.core.ResolvableType.forType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
@DirtiesContext
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
    public void canGetOffenderSentences() {
        final var sentences = given()
                .when().auth().oauth2(validOauthToken)
                .get("/offenders/offenderId/-1001/sentences")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThatJsonFile(sentences, "sentences.json");
    }

    @Test
    public void offenderSentencesAreAuthorized() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/sentences")
                .then()
                .statusCode(401);
    }


    <T> void assertThatJsonFile(final String response, final String jsonFile) {
        final var responseAsJson = getBodyAsJsonContent(response);
        assertThat(responseAsJson).isEqualToJson(jsonFile);
    }

    private <T> JsonContent<T> getBodyAsJsonContent(final String response) {
        return new JsonContent<T>(getClass(), forType(String.class), Objects.requireNonNull(response));
    }
}
