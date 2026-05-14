package com.ape.apeadmin.controller.ai;

import com.ape.apeadmin.service.ai.XunfeiSpeechService;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    private final XunfeiSpeechService xunfeiSpeechService;

    public SpeechController(XunfeiSpeechService xunfeiSpeechService) {
        this.xunfeiSpeechService = xunfeiSpeechService;
    }

    @Log(name = "语音识别", type = BusinessType.OTHER)
    @PostMapping("/recognize")
    public Result recognize(@RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile.isEmpty()) {
                return Result.fail("音频文件为空");
            }

            byte[] audioData = audioFile.getBytes();
            String result = xunfeiSpeechService.recognize(audioData);

            Map<String, Object> data = new HashMap<>();
            data.put("text", result);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("语音识别失败: " + e.getMessage());
        }
    }
}
