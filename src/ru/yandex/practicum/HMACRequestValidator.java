package ru.yandex.practicum;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HMACRequestValidator {

    private final ConfigStorage.ConfigHMAC configStorage;
    private final JsonResponseSender responseSender;

    public HMACRequestValidator(ConfigStorage.ConfigHMAC configStorage, JsonResponseSender responseSender) {
        this.configStorage = configStorage;
        this.responseSender = responseSender;
    }

    public void validateMessage(HttpExchange exchange, Gson gson, String message) throws IOException {
        if (isInvalidLength(message)) {
            responseSender.sendJsonError(exchange, 400, "invalid_msg", gson);
        }
    }

    public void validateSignature(HttpExchange exchange, Gson gson, String signature) throws IOException {
        if (isInvalidLength(signature)) {
            responseSender.sendJsonError(exchange, 400, "invalid_signature_format", gson);
        }
        try {
            HelperBase64.decode(signature);
        } catch (IllegalArgumentException e) {
            responseSender.sendJsonError(exchange, 400, " invalid_signature_format", gson);
        }
    }

    private boolean isInvalidLength(String value) {
        return value == null
                || value.isBlank()
                || value.length() > configStorage.getMaxMsgSizeBytes();
    }

}