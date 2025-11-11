package ru.yandex.practicum;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ServerHMAC {

    private final ConfigStorage.ConfigHMAC config;
    private final PrintWriter log;

    public ServerHMAC(ConfigStorage.ConfigHMAC config, PrintWriter log) {
        this.config = config;
        this.log = log;
    }

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("log.txt"); Writer writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            PrintWriter log = new PrintWriter(writer, true);
            log = new PrintWriter(System.out, true); //TODO comment on commit
            try {
                ConfigStorage.ConfigHMAC config = new ConfigStorage(log).load();
                new ServerHMAC(config, log).run();
            } catch (Exception e) {
                e.printStackTrace(log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        HttpServer httpServer = HttpServer.create();
        ServiceHMAC service = new ServiceHMAC(config, log);
        httpServer.bind(new InetSocketAddress(config.getListenPort()), 0);
        httpServer.createContext("/api/sign", new HanderSignHMAC(service, config, log));
        httpServer.createContext("/api/verify", new HanderVerifyHMAC(service, config, log));
        httpServer.start();
        log.println("HMAC Server Started");
    }
}
