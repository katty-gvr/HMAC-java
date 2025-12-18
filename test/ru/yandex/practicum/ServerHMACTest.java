package ru.yandex.practicum;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerHMACTest {

    private static final int PORT = 8099;
    private HttpServer httpServer;

    @BeforeAll
    void startServer() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        PrintWriter log = new PrintWriter(System.out, true);

        ConfigStorage.ConfigHMAC config = new ConfigStorage.ConfigHMAC();
        config.setListenPort(PORT);
        config.setHmacAlg("HmacSHA256");
        config.setSecret("IkRvd24gdGhlIHJhYmJpdCBob2xlLiI=");
        config.setMaxMsgSizeBytes(1024);

        JsonResponseSender jsonSender = new JsonResponseSender();
        HMACRequestValidator validator = new HMACRequestValidator(config, jsonSender);

        ServiceHMAC service = new ServiceHMAC(config, log);
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/sign", new HanderSignHMAC(service, config, log, validator, jsonSender));
        httpServer.createContext("/verify", new HanderVerifyHMAC(service, config, log, validator, jsonSender));
        httpServer.start();
    }

    @AfterAll
    void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void invalidBase64Signature() throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("msg", "hello");
        payload.put("signature", "@@@");

        String json = new Gson().toJson(payload);
        HttpURLConnection con = post("/verify", json);

        assertEquals(400, con.getResponseCode());
        String body = readResponse(con);
        assertTrue(body.contains("invalid_signature_format"));
    }

    @Test
    void emptyMessage() throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("msg", "");

        String json = new Gson().toJson(payload);
        HttpURLConnection con = post("/sign", json);

        assertEquals(400, con.getResponseCode());
        String body = readResponse(con);
        assertTrue(body.contains("invalid_msg"));
    }

    @Test
    void messageTooLarge() throws IOException {
        String bigMsg = "a".repeat(2000);

        Map<String, String> payload = new HashMap<>();
        payload.put("msg", bigMsg);

        String json = new Gson().toJson(payload);

        HttpURLConnection con = post("/sign", json);
        assertEquals(413, con.getResponseCode());
        String body = readResponse(con);
        assertTrue(body.contains("payload_too_large"));
    }

    private HttpURLConnection post(String path, String json) throws IOException {
        URL url = new URL("http://localhost:" + PORT + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = con.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return con;
    }

    private String readResponse(HttpURLConnection con) throws IOException {
        InputStream stream = con.getResponseCode() >= 400 ? con.getErrorStream() : con.getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

}
