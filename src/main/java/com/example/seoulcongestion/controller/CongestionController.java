package com.example.seoulcongestion.controller;

import com.example.seoulcongestion.domain.CongestionData;
import com.example.seoulcongestion.service.SeoulApiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/congestion")
@RequiredArgsConstructor
public class CongestionController {

    private static final Logger logger = LoggerFactory.getLogger(CongestionController.class);
    private final SeoulApiService seoulApiService;

    @GetMapping("/{areaName}")
    public CongestionData getCongestion(@PathVariable String areaName) {
        logger.info("Received GET request for congestion info with location: {}", areaName);
        return seoulApiService.getCongestionInfo(areaName);

    }
}
