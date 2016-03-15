package io.dropwizard.revolver.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.revolver.BaseRevolverTest;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.http.config.RevolverHttpApiConfig;
import io.dropwizard.revolver.http.model.RevolverHttpRequest;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author phaneesh
 */
public class RevolverSimpleHttpCommandTest extends BaseRevolverTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    @Test
    public void testSimpleGetHttpCommand() {
        stubFor(get(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.GET)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleGetHttpCommandWithWrongPath() {
        stubFor(get(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.GET)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimpleGetHttpCommandWithMultiplePathSegment() {
        stubFor(get(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.GET)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleGetHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(get(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.GET)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimplePostHttpCommand() {
        stubFor(post(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.POST)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimplePostHttpCommandWithWithWrongPath() {
        stubFor(post(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.POST)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimplePostHttpCommandWithMultiplePathSegment() {
        stubFor(post(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.POST)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimplePostHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(post(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.POST)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimplePutHttpCommand() {
        stubFor(put(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PUT)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimplePutHttpCommandWithWrongPath() {
        stubFor(put(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PUT)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimplePutHttpCommandWithMultiplePathSegment() {
        stubFor(put(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PUT)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimplePutHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(put(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PUT)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }


    @Test
    public void testSimpleDeleteHttpCommand() {
        stubFor(delete(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.DELETE)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleDeleteHttpCommandWithWrongPath() {
        stubFor(delete(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.DELETE)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimpleDeleteHttpCommandWithMultiplePathSegment() {
        stubFor(delete(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.DELETE)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleDeleteHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(delete(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.DELETE)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimpleHeadHttpCommand() {
        stubFor(head(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.HEAD)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleHeadHttpCommandWithWrongPath() {
        stubFor(head(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.HEAD)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimpleHeadHttpCommandWithMultiplePathSegment() {
        stubFor(head(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.HEAD)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleHeadHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(head(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.HEAD)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimplePatchHttpCommand() {
        stubFor(patch(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PATCH)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimplePatchHttpCommandWithWrongPath() {
        stubFor(patch(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PATCH)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimplePatchHttpCommandWithMultiplePathSegment() {
        stubFor(patch(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PATCH)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimplePatchHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(patch(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.PATCH)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimpleOptionsHttpCommand() {
        stubFor(options(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.OPTIONS)
                .path("v1/test")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleOptionsHttpCommandWithWrongPath() {
        stubFor(options(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.OPTIONS)
                .path("v1/test_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void testSimpleOptionHttpCommandWithMultiplePathSegment() {
        stubFor(options(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.OPTIONS)
                .path("v1/test/multi")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testSimpleOptionsHttpCommandWithMultiplePathSegmentWithWrongPath() {
        stubFor(patch(urlEqualTo("/v1/test/multi"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand("test");
        val request = RevolverHttpRequest.builder()
                .service("test")
                .api("test")
                .method(RevolverHttpApiConfig.RequestMethod.OPTIONS)
                .path("v1/test/multi_invalid")
                .build();
        val response = httpCommand.execute(request);
        assertEquals(response.getStatusCode(), 404);
    }

}
