package com.example.flightinformationtracker.controller;

import com.example.flightinformationtracker.service.OpenSkyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统状态接口。
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final OpenSkyService openSkyService;

    public SystemController(OpenSkyService openSkyService) {
        this.openSkyService = openSkyService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(openSkyService.getSystemStatus());
    }
}
