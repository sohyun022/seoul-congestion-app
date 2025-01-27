package com.example.seoulcongestion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @GetMapping("/kakao-key")
    public String getKakaoApiKey() {
        return kakaoApiKey;
    }
}
