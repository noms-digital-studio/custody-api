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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.nomis.api.OffenderImage;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class ImagesControllerTest {

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
    public void canGetImagesByOffenderId() {
        final var offenderImages = given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/offenderId/-1001/images")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderImage[].class);

        assertThat(offenderImages.length).isGreaterThan(0);
    }

    @Test
    public void getImagesByOffenderIdReturns404WhenOffenderNotFound() {
        given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/offenderId/-666666/images")
            .then()
            .statusCode(404);
    }

    @Test
    public void returnsUnauthorisedWhenOAuth2CredentialsAreMissing() {
        given()
                .when()
                .get("/offenders/offenderId/-1001/images")
                .then()
                .statusCode(401);
    }

    @Test
    public void canGetImagesByNomsId() {
        final var offenderImages = given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/nomsId/A1234AA/images")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderImage[].class);

        assertThat(offenderImages.length).isGreaterThan(0);
    }

    @Test
    public void getImagesByNomsIdReturns404WhenOffenderNotFound() {
        given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/nomsId/Z666666ZZ/images")
            .then()
            .statusCode(404);
    }

    @Test
    public void getImagesByNomsIdReturnsUnauthorisedWhenOAuth2CredentialsAreMissing() {
        given()
            .when()
            .get("/offenders/nomsId/A66666ZZ/images")
            .then()
            .statusCode(401);
    }

    @Test
    public void canGetThumbnailByOffenderIdAndImageId() {
        given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/offenderId/-1001/images/-1/thumbnail")
            .then()
            .statusCode(200);
    }

    @Test
    public void canGetThumbnailByNomsIdAndImageId() {
        given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/nomsId/A1234AA/images/-1/thumbnail")
            .then()
            .statusCode(200);
    }

    @Test
    public void getThumbnailByOffenderIdAndImageIdReturns404WhenOffenderNotFound() {
        given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/offenderId/-666666/images/-1/thumbnail")
            .then()
            .statusCode(404);
    }

    @Test
    public void getThumbnailByOffenderIdAndImageIdReturns404WhenImageNotFound() {
        given()
            .when()
            .auth().oauth2(validOauthToken)
            .get("/offenders/offenderId/-1001/images/-66/thumbnail")
            .then()
            .statusCode(404);
    }

}
