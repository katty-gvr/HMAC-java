package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HanderSignHMAC extends MyAbstractHttpHandler {

    private final ServiceHMAC service;
    private final PrintWriter log;

    public HanderSignHMAC(ServiceHMAC service, ConfigStorage.ConfigHMAC config, PrintWriter log) {
        super("POST");
        this.service = service;
        this.log = log;
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        try {
            InputStream requestBody = exchange.getRequestBody();
            OutputStream responseBody = exchange.getResponseBody();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            SignRequest request = gson.fromJson(new InputStreamReader(requestBody, StandardCharsets.UTF_8), SignRequest.class);
            byte[] sign = service.sign(request.msg.getBytes(StandardCharsets.UTF_8));
            SignResponse response = new SignResponse();
            response.setSignature(HelperBase64.encode(sign));
            log.println(String.format("signed %s, result %s", request.getMsg(), response.getSignature()));
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

    public class SignRequest {

        private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public class SignResponse {
        String signature;

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }


}
