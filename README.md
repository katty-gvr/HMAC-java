# HMAC Server

HTTP‑сервис для подписи и проверки сообщений с использованием HMAC.
Сервис поднимает встроенный HTTP‑сервер и предоставляет два endpoint’а:

* `POST /sign` — генерация HMAC‑подписи для сообщения
* `POST /verify` — проверка подписи для сообщения

---

## 1. Требования к окружению и установка

### Требования

* Java 11+
* ОС: Linux / macOS / Windows

## 2. Генерация секрета для config.json (HMAC key)

HMAC использует **симметричный секретный ключ**. Он должен храниться в защищённом виде и не передаваться клиентам.

### Пример (Linux / macOS)

```bash
openssl rand -base64 32
```
### Пример (Java)

```java
KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
keyGen.init(256);
SecretKey key = keyGen.generateKey();
String base64 = Base64.getEncoder().encodeToString(key.getEncoded());
```

---

## 3. Формат `config.json`

Пример конфигурационного файла:

```json
{
  "hmacAlg": "HmacSHA256",
  "secret": "IkRvd24gdGhlIHJhYmJpdCBob2xlLiI=",
  "listenPort": 8080,
  "maxMsgSizeBytes": 1048576
}

```

## 4. Запуск сервера

### Локальный запуск

Запустить метод `main()` класса `ServerHMAC`.
Сервер будет доступен по адресу:

```text
http://localhost:<listenPort>
```

---

## 5. Примеры `curl`

### Подписать сообщение

```bash
curl -X POST http://localhost:8080/sign \
  -H "Content-Type: application/json" \
  -d '{
    "msg": "Hello"
  }'
```

Пример ответа:

```json
{
  "signature": "c2lnbmF0dXJlX2Jhc2U2NA=="
}
```

---

### Проверить подпись

```bash
curl -X POST http://localhost:8080/verify \
  -H "Content-Type: application/json" \
  -d '{
    "msg": "Hello",
    "signature": "c2lnbmF0dXJlX2Jhc2U2NA=="
  }'
```

Пример ответа:

```json
{
  "ok": true
}
```

---

## 6. Ограничения учебной реализации

1. HMAC — не заменяет асимметричную электронную подпись.
2. Один ключ, нет многоключевой валидации.
3. Смена ключа требует перезапуска сервера.