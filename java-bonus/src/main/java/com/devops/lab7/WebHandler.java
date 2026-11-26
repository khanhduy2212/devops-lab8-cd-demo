package com.devops.lab7;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Minimal JSON API that exposes the original Lab 7 Calculator. */
public final class WebHandler implements HttpHandler {
    private final Calculator calculator;

    public WebHandler(Calculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        switch (path) {
            case "/health":
                send(exchange, 200, "{\"status\":\"OK\",\"service\":\"lab8-java-cicd\"}");
                break;
            case "/":
                send(exchange, 200, "{\"message\":\"DevOps Lab 8 Java CI/CD\",\"version\":\"1.0.0\",\"language\":\"Java 21\"}");
                break;
            case "/api/calculate":
                calculate(exchange);
                break;
            default:
                send(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void calculate(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> params = queryParams(exchange.getRequestURI().getRawQuery());
            String operation = params.get("op");
            int a = Integer.parseInt(params.get("a"));
            int b = Integer.parseInt(params.get("b"));
            if (operation == null) {
                send(exchange, 400, "{\"error\":\"Missing operation\"}");
                return;
            }
            double result;
            switch (operation) {
                case "add": result = calculator.add(a, b); break;
                case "subtract": result = calculator.subtract(a, b); break;
                case "multiply": result = calculator.multiply(a, b); break;
                case "divide": result = calculator.divide(a, b); break;
                default:
                    send(exchange, 400, "{\"error\":\"Unsupported operation\"}");
                    return;
            }
            send(exchange, 200, "{\"operation\":\"" + operation + "\",\"result\":" + result + "}");
        } catch (NullPointerException | IllegalArgumentException e) {
            send(exchange, 400, "{\"error\":\"Invalid input or division by zero\"}");
        }
    }

    private static Map<String, String> queryParams(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return params;
        for (String kv : rawQuery.split("&")) {
            String[] parts = kv.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static void send(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream stream = exchange.getResponseBody()) {
            stream.write(bytes);
        }
    }
}
