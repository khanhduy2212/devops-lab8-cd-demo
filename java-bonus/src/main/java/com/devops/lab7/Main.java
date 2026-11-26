package com.devops.lab7;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Reuses the Lab 7 Calculator and exposes it through HTTP for Lab 8 CD.
 * Only standard Java/JDK APIs are needed at runtime (no Spring Boot).
 */
public final class Main {
    private Main() { }

    public static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", new WebHandler(new Calculator()));
        return server;
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = createServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();
        System.out.println("Lab 8 Java CI/CD app listening on port " + port);
    }
}
