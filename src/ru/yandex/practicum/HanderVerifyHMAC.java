package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

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
        try {
            InputStream requestBody = exchange.getRequestBody();
            OutputStream responseBody = exchange.getResponseBody();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            VerifyRequest request = gson.fromJson(new InputStreamReader(requestBody, StandardCharsets.UTF_8), VerifyRequest.class);
            requestValidator.validateSignature(exchange, gson, request.signature);
            boolean ok = service.verify(request.getMsg().getBytes(StandardCharsets.UTF_8), HelperBase64.decode(request.getSignature()));
            VerifyResponse response = new VerifyResponse();
            response.setOk(ok);
            log.println(String.format("verified message with length %s signed length = %s, result %b", request.getMsg().length(), request.getSignature().length(), ok));
            exchange.sendResponseHeaders(200, 0);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(responseBody, StandardCharsets.UTF_8))) {
                gson.toJson(response, writer);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } finally {
            exchange.getResponseBody().close();
        }
    }

    class VerifyRequest {
        String msg;
        String signature;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }

    class VerifyResponse {
        private boolean ok;

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }
    }
}
