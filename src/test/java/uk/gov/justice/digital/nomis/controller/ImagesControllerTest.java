package uk.gov.justice.digital.nomis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
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
        final JsonPath offenderImages = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/nomsId/A1234AA/images")
                .then()
                .statusCode(200)
                .extract()
                .body().jsonPath();

        assertThat(offenderImages.getList("$").size()).isEqualTo(5);
        assertThat(offenderImages.get().toString()).contains("offenderImageId");
        assertThat(offenderImages.get().toString()).contains("bookingId");
        assertThat(offenderImages.get().toString()).contains("captureDateTime");
        assertThat(offenderImages.get().toString()).contains("orientationType");
        assertThat(offenderImages.get().toString()).contains("imageObjectType");
        assertThat(offenderImages.get().toString()).contains("imageViewType");
        assertThat(offenderImages.get().toString()).contains("activeFlag");
        assertThat(offenderImages.get().toString()).contains("createDatetime");
        assertThat(offenderImages.get().toString()).contains("modifyDatetime");
    }

    @Test
    public void canGetImagesByNomsIdCompareFullJson() {
        final var offenderImages = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/offenders/nomsId/A1234AA/images")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThatJsonFile(offenderImages, "offender_images.json");

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

    <T> void assertThatJsonFile(final String response, final String jsonFile) {
        final var responseAsJson = getBodyAsJsonContent(response);
        assertThat(responseAsJson).isEqualToJson(jsonFile);
    }

    private <T> JsonContent<T> getBodyAsJsonContent(final String response) {
        return new JsonContent<T>(getClass(), forType(String.class), Objects.requireNonNull(response));
    }

}
