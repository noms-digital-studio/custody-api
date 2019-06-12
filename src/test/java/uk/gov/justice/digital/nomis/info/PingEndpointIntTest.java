package uk.gov.justice.digital.nomis.info;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
public class PingEndpointIntTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    public void canGetPingResponse() {
        String pingResponse = given()
                .when()
                .get("/ping")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .asString();

        assertThat(pingResponse).isEqualTo("pong");
    }
}