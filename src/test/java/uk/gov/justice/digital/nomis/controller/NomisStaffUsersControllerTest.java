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
import uk.gov.justice.digital.nomis.api.NomisStaffUser;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class NomisStaffUsersControllerTest {

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
    public void canGetUserDetails() {
        final var nomisStaffUser = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/nomis-staff-users/ITAG_USER")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(NomisStaffUser.class);

        assertThat(nomisStaffUser.getUsername()).isEqualTo("ITAG_USER");
        assertThat(nomisStaffUser.getStaffId()).isEqualTo(1);
        assertThat(nomisStaffUser.getFirstName()).isEqualTo("ITAG");
        assertThat(nomisStaffUser.getLastName()).isEqualTo("USER");
        assertThat(nomisStaffUser.getStatus()).isEqualTo("ACTIVE");
        assertThat(nomisStaffUser.getNomisCaseloads().values().size()).isEqualTo(2);
        final var activeCaseload = nomisStaffUser.getActiveNomisCaseload();
        assertThat(activeCaseload).isEqualTo("MDI");
        final var mdiNomisCaseload = nomisStaffUser.getNomisCaseloads().get(activeCaseload);
        assertThat(mdiNomisCaseload.getAgencies().size()).isEqualTo(1);
        assertThat(mdiNomisCaseload.isCurrentActive()).isTrue();
        assertThat(mdiNomisCaseload.getAgencies().get(0).getAgencyLocationId()).isEqualTo("MDI");
    }

    @Test
    public void canGetUserDetailsMultipleAgencies() {
        final var nomisStaffUser = given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/nomis-staff-users/ITAG_USER_ADM")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(NomisStaffUser.class);

        assertThat(nomisStaffUser.getUsername()).isEqualTo("ITAG_USER_ADM");
        assertThat(nomisStaffUser.getStaffId()).isEqualTo(1);
        assertThat(nomisStaffUser.getFirstName()).isEqualTo("ITAG");
        assertThat(nomisStaffUser.getLastName()).isEqualTo("USER");
        assertThat(nomisStaffUser.getStatus()).isEqualTo("ACTIVE");
        assertThat(nomisStaffUser.getNomisCaseloads().values().size()).isEqualTo(2);
        final var activeCaseload = nomisStaffUser.getActiveNomisCaseload();
        assertThat(activeCaseload).isNull();
        final var mdiNomisCaseload = nomisStaffUser.getNomisCaseloads().get("CADM");
        assertThat(mdiNomisCaseload.getAgencies().size()).isEqualTo(4);
    }

    @Test
    public void canGetAllStaffUsers() {
        given()
                .when()
                .auth().oauth2(validOauthToken)
                .get("/nomis-staff-users")
                .then()
                .statusCode(200)
                .body("page.totalElements", greaterThan(0));
    }

    @Test
    public void userDetailsAreAuthorized() {
        given()
                .when()
                .get("/nomis-staff-users/ITAG_USER")
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
                .get("/nomis-staff-users")
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
