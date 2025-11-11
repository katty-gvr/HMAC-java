package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceHMACTest {

    static ConfigStorage.ConfigHMAC config;
    static ServiceHMAC service;
    @BeforeAll
    static void setup() throws NoSuchAlgorithmException, InvalidKeyException {
        config = new ConfigStorage.ConfigHMAC();
        config.setHmacAlg("HmacSHA256");
        config.setSecret("IkRvd24gdGhlIHJhYmJpdCBob2xlLiI=");
        service = new ServiceHMAC(config, new PrintWriter(System.out, true));
    }

    @Test
    void sign() {
        byte[] sign = service.sign("Hello, Practicum!".getBytes());
        assertNotNull(sign);
        String signStr = HelperBase64.encode(sign);
        assertEquals("PuYy8fFTam6qb22PvLM6fEH2popsTH77/Hbuq145Sgg=", signStr);
    }

    @Test
    void verify() {
        assertTrue(service.verify("Hello, Practicum!".getBytes(), HelperBase64.decode("PuYy8fFTam6qb22PvLM6fEH2popsTH77/Hbuq145Sgg=")));
    }
}
