package com.ape.apeadmin.service.ai;

import com.google.gson.Gson;
import okhttp3.*;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class XunfeiTtsService {

    private static final Logger log = LoggerFactory.getLogger(XunfeiTtsService.class);
    private static final String HOST_URL = "https://cbm01.cn-huabei-1.xf-yun.com/v1/private/mcd9m97e6";
    private static final Gson gson = new Gson();

    @Value("${xunfei.tts.appid:}")
    private String appId;

    @Value("${xunfei.tts.apiKey:}")
    private String apiKey;

    @Value("${xunfei.tts.apiSecret:}")
    private String apiSecret;

    @Value("${xunfei.tts.vcn:x5_lingfeiyi_flow}")
    private String vcn;

    public byte[] synthesize(String text) {
        if (appId == null || appId.isEmpty() || apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
            log.error("讯飞TTS配置缺失，请检查 xunfei.tts.appid, xunfei.tts.apiKey, xunfei.tts.apiSecret");
            return null;
        }

        if (text == null || text.trim().isEmpty()) {
            log.error("合成文本为空");
            return null;
        }

        try {
            return doSynthesize(text.trim());
        } catch (Exception e) {
            log.error("语音合成失败", e);
            return null;
        }
    }

    private byte[] doSynthesize(String text) throws Exception {
        String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String wsUrl = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(wsUrl).build();

        List<byte[]> audioDataList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] hasError = {false};

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.info("TTS WebSocket连接已建立");
                sendTtsRequest(webSocket, text);
            }

            @Override
            public void onMessage(WebSocket webSocket, String message) {
                try {
                    JsonParse jsonParse = gson.fromJson(message, JsonParse.class);
                    if (jsonParse.header.code != 0) {
                        log.error("TTS合成错误 code={}, sid={}", jsonParse.header.code, jsonParse.header.sid);
                        hasError[0] = true;
                        latch.countDown();
                        return;
                    }
                    if (jsonParse.payload != null && jsonParse.payload.audio != null && jsonParse.payload.audio.audio != null) {
                        byte[] audioData = Base64.getDecoder().decode(jsonParse.payload.audio.audio);
                        audioDataList.add(audioData);
                    }
                    if (jsonParse.header.status == 2) {
                        log.info("TTS合成完成 sid={}", jsonParse.header.sid);
                        webSocket.close(1000, "合成完成");
                        latch.countDown();
                    }
                } catch (Exception e) {
                    log.error("解析TTS响应失败", e);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("TTS WebSocket连接失败", t);
                hasError[0] = true;
                latch.countDown();
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                log.info("TTS WebSocket正在关闭: {} - {}", code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                log.info("TTS WebSocket已关闭: {} - {}", code, reason);
            }
        };

        client.newWebSocket(request, listener);

        boolean completed = latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
        if (!completed || hasError[0]) {
            log.error("TTS合成超时或失败");
            return null;
        }

        int totalLength = audioDataList.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] data : audioDataList) {
            System.arraycopy(data, 0, result, offset, data.length);
            offset += data.length;
        }

        return result;
    }

    private void sendTtsRequest(WebSocket webSocket, String text) {
        try {
            String textBase64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

            String json = "{\n" +
                    "  \"header\": {\n" +
                    "    \"app_id\": \"" + appId + "\",\n" +
                    "    \"status\": 2\n" +
                    "  },\n" +
                    "  \"parameter\": {\n" +
                    "    \"tts\": {\n" +
                    "      \"vcn\": \"" + vcn + "\",\n" +
                    "      \"speed\": 50,\n" +
                    "      \"volume\": 50,\n" +
                    "      \"pitch\": 50,\n" +
                    "      \"bgs\": 0,\n" +
                    "      \"reg\": 0,\n" +
                    "      \"rdn\": 0,\n" +
                    "      \"audio\": {\n" +
                    "        \"encoding\": \"raw\",\n" +
                    "        \"sample_rate\": 16000,\n" +
                    "        \"channels\": 1,\n" +
                    "        \"bit_depth\": 16\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"payload\": {\n" +
                    "    \"text\": {\n" +
                    "      \"encoding\": \"utf8\",\n" +
                    "      \"compress\": \"raw\",\n" +
                    "      \"format\": \"json\",\n" +
                    "      \"status\": 2,\n" +
                    "      \"seq\": 0,\n" +
                    "      \"text\": \"" + textBase64 + "\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            webSocket.send(json);
            log.info("TTS请求已发送, 文本长度: {}", text.length());
        } catch (Exception e) {
            log.error("发送TTS请求失败", e);
        }
    }

    private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n")
                .append("date: ").append(date).append("\n")
                .append("GET ").append(url.getPath()).append(" HTTP/1.1");

        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        String authorization = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", sha);

        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();

        return httpUrl.toString();
    }

    static class JsonParse {
        Header header;
        Payload payload;
    }

    static class Header {
        int code;
        String sid;
        int status;
    }

    static class Payload {
        Audio audio;
    }

    static class Audio {
        String audio;
        int seq;
    }
}
