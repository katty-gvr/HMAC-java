package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.request.SignRequest;
import ru.yandex.practicum.response.SignResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HanderSignHMAC extends MyAbstractHttpHandler {

    private final ServiceHMAC service;
    private final PrintWriter log;
    private final HMACRequestValidator requestValidator;

    public HanderSignHMAC(ServiceHMAC service, ConfigStorage.ConfigHMAC config, PrintWriter log,
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
            SignRequest request = gson.fromJson(reader, SignRequest.class);
            requestValidator.validateMessage(exchange, gson, request.getMsg());
            byte[] sign = service.sign(request.getMsg().getBytes(StandardCharsets.UTF_8));
            SignResponse response = new SignResponse();
            response.setSignature(HelperBase64.encode(sign));
            log.println(String.format(
                    "signed message with length %s, result length = %s",
                    request.getMsg().length(),
                    response.getSignature().length()
            ));
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