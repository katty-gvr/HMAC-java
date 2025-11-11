package ru.yandex.practicum;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.PrintWriter;

public class HanderVerifyHMAC implements HttpHandler {

    private final PrintWriter log;

    public HanderVerifyHMAC(ServiceHMAC service, ConfigStorage.ConfigHMAC config, PrintWriter log) {
        this.log = log;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
