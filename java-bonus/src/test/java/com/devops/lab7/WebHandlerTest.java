package com.devops.lab7;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/** Integration tests against the Java HTTP server on an ephemeral port. */
class WebHandlerTest {
    private static HttpServer server;
    private static String baseUrl;
    private static HttpClient client;

    @BeforeAll
    static void start() throws Exception {
        server = Main.createServer(0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stop() {
        server.stop(0);
    }

    private static HttpResponse<String> get(String path) throws Exception {
        return client.send(
            HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    @Test
    void healthReturns200() throws Exception {
        HttpResponse<String> res = get("/health");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"status\":\"OK\""));
    }

    @Test
    void homeReturnsJavaInfo() throws Exception {
        HttpResponse<String> res = get("/");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Java CI/CD"));
    }

    @Test
    void calculatorReturnsResult() throws Exception {
        HttpResponse<String> res = get("/api/calculate?op=add&a=10&b=5");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"result\":15.0"));
    }

    @Test
    void invalidParametersReturn400() throws Exception {
        assertEquals(400, get("/api/calculate?op=divide&a=10&b=0").statusCode());
        assertEquals(400, get("/api/calculate?op=add&a=abc&b=5").statusCode());
    }

    @Test
    void unknownEndpointReturns404() throws Exception {
        assertEquals(404, get("/missing").statusCode());
    }
}
