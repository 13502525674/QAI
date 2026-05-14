package com.ape.apeadmin.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
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
import java.util.concurrent.TimeUnit;

@Service
public class XunfeiSpeechService {

    private static final Logger log = LoggerFactory.getLogger(XunfeiSpeechService.class);
    private static final String HOST_URL = "https://iat.xf-yun.com/v1";
    private static final int STATUS_FIRST_FRAME = 0;
    private static final int STATUS_CONTINUE_FRAME = 1;
    private static final int STATUS_LAST_FRAME = 2;
    private static final Gson gson = new Gson();

    @Value("${xunfei.iat.appid:}")
    private String appId;

    @Value("${xunfei.iat.apiKey:}")
    private String apiKey;

    @Value("${xunfei.iat.apiSecret:}")
    private String apiSecret;

    public String recognize(byte[] audioData) {
        if (appId == null || appId.isEmpty() || apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
            log.error("讯飞语音识别配置缺失，请检查 xunfei.iat.appid, xunfei.iat.apiKey, xunfei.iat.apiSecret");
            return "语音识别服务未配置";
        }

        try {
            byte[] pcmData = extractPcmFromWav(audioData);
            return doRecognize(pcmData);
        } catch (Exception e) {
            log.error("语音识别失败", e);
            return "语音识别失败: " + e.getMessage();
        }
    }

    private byte[] extractPcmFromWav(byte[] wavData) {
        if (wavData == null || wavData.length < 44) {
            return wavData;
        }
        
        if (wavData[0] == 'R' && wavData[1] == 'I' && wavData[2] == 'F' && wavData[3] == 'F') {
            int dataOffset = 12;
            while (dataOffset < wavData.length - 8) {
                String chunkId = new String(wavData, dataOffset, 4);
                int chunkSize = bytesToIntLittleEndian(wavData, dataOffset + 4);
                
                if ("data".equals(chunkId)) {
                    int pcmStart = dataOffset + 8;
                    int pcmLength = chunkSize;
                    byte[] pcmData = new byte[pcmLength];
                    System.arraycopy(wavData, pcmStart, pcmData, 0, pcmLength);
                    return pcmData;
                }
                dataOffset += 8 + chunkSize;
            }
        }
        
        return wavData;
    }

    private int bytesToIntLittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) |
               ((bytes[offset + 1] & 0xFF) << 8) |
               ((bytes[offset + 2] & 0xFF) << 16) |
               ((bytes[offset + 3] & 0xFF) << 24);
    }

    private String doRecognize(byte[] audioData) throws Exception {
        String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();

        String wsUrl = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(wsUrl).build();

        StringBuilder resultBuilder = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.info("WebSocket连接已建立");
                sendAudioData(webSocket, audioData);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonParse jsonParse = gson.fromJson(text, JsonParse.class);
                    if (jsonParse != null) {
                        if (jsonParse.header.code != 0) {
                            log.error("识别错误 code={}, message={}, sid={}", 
                                jsonParse.header.code, jsonParse.header.message, jsonParse.header.sid);
                            resultBuilder.append("识别错误: ").append(jsonParse.header.message);
                            latch.countDown();
                            return;
                        }
                        if (jsonParse.payload != null && jsonParse.payload.result.text != null) {
                            byte[] decodedBytes = Base64.getDecoder().decode(jsonParse.payload.result.text);
                            String decodeRes = new String(decodedBytes, StandardCharsets.UTF_8);
                            JsonParseText jsonParseText = gson.fromJson(decodeRes, JsonParseText.class);
                            
                            if (jsonParseText.ws != null) {
                                for (Ws ws : jsonParseText.ws) {
                                    if (ws.cw != null) {
                                        for (Cw cw : ws.cw) {
                                            resultBuilder.append(cw.w);
                                        }
                                    }
                                }
                            }
                            
                            if (jsonParse.payload.result.status == 2) {
                                log.info("识别完成 sid={}, result={}", jsonParse.header.sid, resultBuilder.toString());
                                webSocket.close(1000, "识别完成");
                                latch.countDown();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析识别结果失败", e);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("WebSocket连接失败", t);
                latch.countDown();
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                log.info("WebSocket正在关闭: {} - {}", code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                log.info("WebSocket已关闭: {} - {}", code, reason);
            }
        };

        client.newWebSocket(request, listener);

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("语音识别超时");
            return "语音识别超时";
        }

        return resultBuilder.toString();
    }

    private void sendAudioData(WebSocket webSocket, byte[] audioData) {
        new Thread(() -> {
            try {
                int frameSize = 1280;
                int interval = 40;
                int status = STATUS_FIRST_FRAME;
                int seq = 0;
                int offset = 0;

                while (true) {
                    seq++;
                    
                    if (offset >= audioData.length) {
                        String json = buildLastFrame(seq);
                        log.info("发送最后一帧音频");
                        webSocket.send(json);
                        break;
                    }
                    
                    int len = Math.min(frameSize, audioData.length - offset);
                    byte[] frameData = Arrays.copyOfRange(audioData, offset, offset + len);
                    String audioBase64 = Base64.getEncoder().encodeToString(frameData);

                    String json;
                    if (status == STATUS_FIRST_FRAME) {
                        json = buildFirstFrame(seq, audioBase64);
                        status = STATUS_CONTINUE_FRAME;
                        log.info("发送第一帧音频");
                    } else {
                        json = buildContinueFrame(seq, audioBase64);
                    }

                    webSocket.send(json);
                    offset += len;
                    Thread.sleep(interval);
                }
                log.info("音频数据发送完成");
            } catch (Exception e) {
                log.error("发送音频数据失败", e);
            }
        }).start();
    }

    private String buildFirstFrame(int seq, String audioBase64) {
        return "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appId + "\",\n" +
                "    \"status\": " + STATUS_FIRST_FRAME + "\n" +
                "  },\n" +
                "  \"parameter\": {\n" +
                "    \"iat\": {\n" +
                "      \"domain\": \"slm\",\n" +
                "      \"language\": \"zh_cn\",\n" +
                "      \"accent\": \"mandarin\",\n" +
                "      \"eos\": 6000,\n" +
                "      \"result\": {\n" +
                "        \"encoding\": \"utf8\",\n" +
                "        \"compress\": \"raw\",\n" +
                "        \"format\": \"json\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"audio\": {\n" +
                "      \"encoding\": \"raw\",\n" +
                "      \"sample_rate\": 16000,\n" +
                "      \"channels\": 1,\n" +
                "      \"bit_depth\": 16,\n" +
                "      \"seq\": " + seq + ",\n" +
                "      \"status\": 0,\n" +
                "      \"audio\": \"" + audioBase64 + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    private String buildContinueFrame(int seq, String audioBase64) {
        return "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appId + "\",\n" +
                "    \"status\": 1\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"audio\": {\n" +
                "      \"encoding\": \"raw\",\n" +
                "      \"sample_rate\": 16000,\n" +
                "      \"channels\": 1,\n" +
                "      \"bit_depth\": 16,\n" +
                "      \"seq\": " + seq + ",\n" +
                "      \"status\": 1,\n" +
                "      \"audio\": \"" + audioBase64 + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    private String buildLastFrame(int seq) {
        return "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appId + "\",\n" +
                "    \"status\": 2\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"audio\": {\n" +
                "      \"encoding\": \"raw\",\n" +
                "      \"sample_rate\": 16000,\n" +
                "      \"channels\": 1,\n" +
                "      \"bit_depth\": 16,\n" +
                "      \"seq\": " + seq + ",\n" +
                "      \"status\": 2,\n" +
                "      \"audio\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
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

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
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
        String message;
        String sid;
        int status;
    }

    static class Payload {
        Result result;
    }

    static class Result {
        String text;
        int status;
        String pgs;
    }

    static class JsonParseText {
        List<Ws> ws;
        String pgs;
        List<Integer> rg;
    }

    static class Ws {
        List<Cw> cw;
    }

    static class Cw {
        String w;
    }
}
