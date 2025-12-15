package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigInteger;

public abstract class MyAbstractHttpHandler implements HttpHandler {

    private final String method;
    private final JsonResponseSender responseSender;
    private final ConfigStorage.ConfigHMAC configStorage;

    public MyAbstractHttpHandler(String method, JsonResponseSender responseSender, ConfigStorage.ConfigHMAC configStorage) {
        this.method = method;
        this.responseSender = responseSender;
        this.configStorage = configStorage;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (!exchange.getRequestMethod().equals(method)) {
                System.out.println("unknown method: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1);
            }
            String contentTypeHeader = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentTypeHeader == null || !contentTypeHeader.equals("application/json")) {
                responseSender.sendJsonError(exchange, 415, "unsupported_media_type", gson);
                return;
            }
            String contentLengthHeader = exchange.getRequestHeaders().getFirst("Content-Length");
            BigInteger contentLength = new BigInteger(contentLengthHeader);
            BigInteger maxSize = BigInteger.valueOf(configStorage.getMaxMsgSizeBytes());
            if (contentLength.compareTo(maxSize) > 0) {
                responseSender.sendJsonError(exchange, 413, "payload_too_large", gson);
                return;
            }
            switch (exchange.getRequestMethod()) {
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    throw new RuntimeException("unknown method: " + exchange.getRequestMethod());
            }
        } catch (Exception e) {
            responseSender.sendJsonError(exchange, 500, "internal_server_error", gson);
        } finally {
            exchange.getResponseBody().close();
        }
    }

    protected abstract void handlePost(HttpExchange exchange) throws IOException;
}
