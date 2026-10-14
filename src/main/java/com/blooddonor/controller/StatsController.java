package com.blooddonor.controller;

import com.blooddonor.dto.StatsResponseDto;
import com.blooddonor.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsResponseDto> getStats() {
        StatsResponseDto stats = statsService.getStats();
        return ResponseEntity.ok(stats);
    }
}
