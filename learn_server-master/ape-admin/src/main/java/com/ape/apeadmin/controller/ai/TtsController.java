package com.ape.apeadmin.controller.ai;

import com.ape.apeadmin.service.ai.XunfeiTtsService;
import com.ape.apeadmin.service.ai.XunfeiTtsStreamService;
import com.ape.apecommon.domain.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tts")
public class TtsController {

    private final XunfeiTtsService xunfeiTtsService;
    private final XunfeiTtsStreamService xunfeiTtsStreamService;

    public TtsController(XunfeiTtsService xunfeiTtsService, XunfeiTtsStreamService xunfeiTtsStreamService) {
        this.xunfeiTtsService = xunfeiTtsService;
        this.xunfeiTtsStreamService = xunfeiTtsStreamService;
    }

    @PostMapping("/synthesize")
    public Result synthesize(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return Result.fail("合成文本不能为空");
            }

            byte[] audioData = xunfeiTtsService.synthesize(text);
            if (audioData == null || audioData.length == 0) {
                return Result.fail("语音合成失败");
            }

            String audioBase64 = java.util.Base64.getEncoder().encodeToString(audioData);
            Map<String, Object> data = new HashMap<>();
            data.put("audio", audioBase64);
            data.put("sampleRate", 16000);
            data.put("channels", 1);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("语音合成失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter synthesizeStream(@RequestParam("text") String text) {
        return xunfeiTtsStreamService.synthesizeStream(text);
    }
}
