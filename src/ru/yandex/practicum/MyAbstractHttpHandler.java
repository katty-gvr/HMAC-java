package ru.yandex.practicum;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public abstract class MyAbstractHttpHandler implements HttpHandler {

    private final String method;

    public MyAbstractHttpHandler(String method) {
        this.method = method;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals(method)) {
            System.out.println("unknown method: " + exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
        }
        switch (exchange.getRequestMethod()) {
            case "POST":
                handlePost(exchange);
                break;
            default:
                throw new RuntimeException("unknown method: " + exchange.getRequestMethod());
        }
    }

    protected abstract void handlePost(HttpExchange exchange) throws IOException;
}
