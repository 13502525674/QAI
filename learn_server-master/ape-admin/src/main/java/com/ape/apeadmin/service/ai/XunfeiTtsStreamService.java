package com.ape.apeadmin.service.ai;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class XunfeiTtsStreamService {

    private static final Logger log = LoggerFactory.getLogger(XunfeiTtsStreamService.class);
    private static final String HOST_URL = "https://cbm01.cn-huabei-1.xf-yun.com/v1/private/mcd9m97e6";
    private static final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Value("${xunfei.tts.appid:}")
    private String appId;

    @Value("${xunfei.tts.apiKey:}")
    private String apiKey;

    @Value("${xunfei.tts.apiSecret:}")
    private String apiSecret;

    @Value("${xunfei.tts.vcn:x5_lingfeiyi_flow}")
    private String vcn;

    public SseEmitter synthesizeStream(String text) {
        SseEmitter emitter = new SseEmitter(60000L);

        if (appId == null || appId.isEmpty() || apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
            executorService.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data("TTS配置缺失"));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            return emitter;
        }

        if (text == null || text.trim().isEmpty()) {
            executorService.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data("合成文本为空"));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            return emitter;
        }

        executorService.execute(() -> {
            try {
                doSynthesizeStream(text.trim(), emitter);
            } catch (Exception e) {
                log.error("流式TTS合成失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("合成失败: " + e.getMessage()));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(e);
                }
            }
        });

        emitter.onCompletion(() -> {
            log.info("SSE连接已关闭");
        });

        return emitter;
    }

    private void doSynthesizeStream(String text, SseEmitter emitter) throws Exception {
        String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String wsUrl = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(wsUrl).build();
        
        final boolean[] completed = {false};

        WebSocketListener listener = new WebSocketListener() {
            private WebSocket webSocketRef = null;

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                this.webSocketRef = webSocket;
                log.info("TTS Stream WebSocket连接已建立");
                sendTtsRequest(webSocket, text);
            }

            @Override
            public void onMessage(WebSocket webSocket, String message) {
                if (completed[0]) {
                    webSocket.close(1000, "已取消");
                    return;
                }
                
                try {
                    JsonParse jsonParse = gson.fromJson(message, JsonParse.class);
                    if (jsonParse.header.code != 0) {
                        log.error("TTS合成错误 code={}, sid={}", jsonParse.header.code, jsonParse.header.sid);
                        if (!completed[0]) {
                            completed[0] = true;
                            emitter.send(SseEmitter.event().name("error").data("合成错误: " + jsonParse.header.code));
                            emitter.complete();
                        }
                        return;
                    }
                    if (jsonParse.payload != null && jsonParse.payload.audio != null && jsonParse.payload.audio.audio != null) {
                        if (!completed[0]) {
                            emitter.send(SseEmitter.event()
                                    .name("audio")
                                    .data(jsonParse.payload.audio.audio));
                        }
                    }
                    if (jsonParse.header.status == 2) {
                        log.info("TTS流式合成完成 sid={}", jsonParse.header.sid);
                        if (!completed[0]) {
                            completed[0] = true;
                            emitter.send(SseEmitter.event().name("done").data("completed"));
                            emitter.complete();
                        }
                        webSocket.close(1000, "合成完成");
                    }
                } catch (IllegalStateException e) {
                    if (e.getMessage() != null && e.getMessage().contains("already completed")) {
                        completed[0] = true;
                        if (webSocketRef != null) {
                            webSocketRef.cancel();
                        }
                    } else {
                        log.error("处理TTS响应失败", e);
                    }
                } catch (Exception e) {
                    log.error("处理TTS响应失败", e);
                    if (!completed[0]) {
                        completed[0] = true;
                        emitter.completeWithError(e);
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("TTS Stream WebSocket连接失败", t);
                if (!completed[0]) {
                    completed[0] = true;
                    try {
                        emitter.send(SseEmitter.event().name("error").data("连接失败: " + t.getMessage()));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(t);
                    }
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                log.info("TTS Stream WebSocket正在关闭: {} - {}", code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                log.info("TTS Stream WebSocket已关闭: {} - {}", code, reason);
            }
        };

        client.newWebSocket(request, listener);
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
            log.info("TTS Stream请求已发送, 文本长度: {}", text.length());
        } catch (Exception e) {
            log.error("发送TTS Stream请求失败", e);
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
