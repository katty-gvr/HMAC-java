package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.request.VerifyRequest;
import ru.yandex.practicum.response.VerifyResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HanderVerifyHMAC extends MyAbstractHttpHandler {

    private final ServiceHMAC service;
    private final PrintWriter log;
    private final HMACRequestValidator requestValidator;

    public HanderVerifyHMAC(ServiceHMAC service, ConfigStorage.ConfigHMAC config, PrintWriter log,
                            HMACRequestValidator requestValidator, JsonResponseSender jsonResponseSender) {
        super("POST", jsonResponseSender, config);
        this.service = service;
        this.log = log;
        this.requestValidator = requestValidator;
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try (InputStream requestBody = exchange.getRequestBody();
             InputStreamReader reader = new InputStreamReader(requestBody, StandardCharsets.UTF_8)) {
            VerifyRequest request = gson.fromJson(reader, VerifyRequest.class);
            requestValidator.validateSignature(exchange, gson, request.getSignature());
            boolean ok = service.verify(
                    request.getMsg().getBytes(StandardCharsets.UTF_8),
                    HelperBase64.decode(request.getSignature()));
            VerifyResponse response = new VerifyResponse();
            response.setOk(ok);
            log.println(String.format(
                    "verified message with length %s signed length = %s, result %b",
                    request.getMsg().length(),
                    request.getSignature().length(),
                    ok));
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream responseBody = exchange.getResponseBody();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(responseBody, StandardCharsets.UTF_8))) {
                gson.toJson(response, writer);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
            try (PrintStream ps = new PrintStream(exchange.getResponseBody())) {
                e.printStackTrace(ps);
            }
        }
    }

}